#!/usr/bin/env python3
# Copyright 2026 Andrey Tolpeev
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Enforce feature api/impl ownership and portable KMP implementation sources."""
import argparse
from pathlib import Path
import re


# Splash preserves installed Android launch compatibility; it already has api/impl.
ANDROID_COMPATIBILITY_FEATURES = {"splash"}


def validate(root):
    errors = []
    settings = (root / "settings.gradle.kts").read_text()
    includes = {module for call in re.findall(r'^include\((.*?)\)', settings, re.MULTILINE | re.DOTALL)
                for module in re.findall(r'"(:features:[\w-]+:[\w-]+)"', call)}
    features = root / "features"
    for feature in sorted(features.iterdir()):
        if not feature.is_dir():
            continue
        modules = {p.parent.name for p in feature.glob("*/build.gradle.kts")}
        if modules != {"api", "impl"}:
            errors.append(f"{feature.name}: expected only api/impl modules, found {sorted(modules)}")
        for module in modules:
            if f":features:{feature.name}:{module}" not in includes:
                errors.append(f"{feature.name}/{module}: missing Gradle include")
            build = (feature / module / "build.gradle.kts").read_text()
            if re.search(r'projects\.features\.[\w]+\.(?:impl|presentation)\b', build):
                errors.append(f"{feature.name}/{module}: feature dependencies must use APIs")
        namespace = {"build-artifacts": "artifacts"}.get(feature.name, feature.name.replace("-", "_"))
        for source in feature.glob("*/src/**/*.kt"):
            text = source.read_text()
            for owner, member in re.findall(r"^import teamcityapp\.features\.(\w+)\.impl(?:\.([\w.*]+))?", text, re.MULTILINE):
                if source.is_relative_to(feature / "api"):
                    errors.append(f"{source.relative_to(root)}: feature APIs must not expose implementation types")
                elif owner != namespace and not ("androidAppTest" in source.parts and (member == "R" or member.startswith("R."))):
                    errors.append(f"{source.relative_to(root)}: feature source and tests must not import another feature implementation")
            if any(part == "main" or part.endswith("Main") for part in source.parts) and "teamcityapp.libraries.network.testing" in text:
                errors.append(f"{source.relative_to(root)}: network fixtures are test-only dependencies")
        impl = feature / "impl"
        if not (impl / "build.gradle.kts").exists():
            continue
        build = (impl / "build.gradle.kts").read_text()
        if feature.name not in ANDROID_COMPATIBILITY_FEATURES and 'id("teamcityapp.kmp.compose")' not in build:
            errors.append(f"{feature.name}/impl: shared feature implementations must use KMP Compose")
        if 'id("teamcityapp.kmp.compose")' in build:
            for source_set in ("main", "test"):
                for language in ("kotlin", "java"):
                    if any((impl / f"src/{source_set}/{language}").rglob("*.*")):
                        errors.append(f"{feature.name}/impl: move src/{source_set}/{language} into KMP source sets")
            for source in (impl / "src/androidMain").rglob("*.kt"):
                if re.search(r"@(?:dagger\.hilt\.android\.)?(?:AndroidEntryPoint|HiltAndroidApp)\b", source.read_text()):
                    errors.append(f"{source.relative_to(root)}: Android entry points belong in the Android host")
            for source in (impl / "src/commonMain").rglob("*.kt"):
                if re.search(r"^import (?:android\.|dagger\.|javax\.inject\.|retrofit2\.|io\.reactivex\.)", source.read_text(), re.MULTILINE):
                    errors.append(f"{source.relative_to(root)}: platform dependency in commonMain")
    # Libraries provide reusable functionality; feature adapters depend on them.
    for build in (root / "libraries").glob("*/build.gradle.kts"):
        if re.search(r"projects\.features\.", build.read_text()):
            errors.append(f"{build.relative_to(root)}: libraries must not depend on feature modules")
    for module in ("accounts", "network", "shared-data"):
        build = root / "libraries" / module / "build.gradle.kts"
        if build.exists() and re.search(r'projects\.libraries\.(?:authenticationShared|authentication|designSystem|theme|buildUi|buildPresentation|listUi|listPresentation)\b|teamcityapp\.kmp\.compose', build.read_text()):
            errors.append(f"{build.relative_to(root)}: account and transport libraries must not depend on UI modules")
    for source in (root / "libraries").glob("*/src/*Main/**/*.kt"):
        if re.search(r"^import teamcityapp\.features\.", source.read_text(), re.MULTILINE):
            errors.append(f"{source.relative_to(root)}: feature adapters belong in their owning feature")
    # Event decisions belong to each feature; the root only composes implementations.
    for source in (root / "shared-app/src/commonMain").rglob("*.kt"):
        text = source.read_text()
        if re.search(r"(?:class|object)\s+\w*Tracker\w*\b|object\s*:\s*\w*Tracker\w*\b", text):
            errors.append(f"{source.relative_to(root)}: tracker implementations belong to their owning feature")
        if re.search(r'AnalyticsEvent\s*\(|\.track\s*\(\s*"', text):
            errors.append(f"{source.relative_to(root)}: analytics event mappings belong to their owning feature")
        if re.search(r"^import teamcityapp\.features\.[\w.]+\.resources(?:\.|$)", text, re.MULTILINE):
            errors.append(f"{source.relative_to(root)}: feature resources belong in feature routes")
        if re.search(r"\b(?:[A-Z]\w*UiState|LoadState|PagingListSnapshot|collectAsLazyPagingItems)\b", text):
            errors.append(f"{source.relative_to(root)}: feature presentation state and paging belong in feature routes")
        if re.search(r"\b(?:vm|model)\.(?:viewed|onScreenViewed|onResumed|onPaused)\s*\(", text):
            errors.append(f"{source.relative_to(root)}: feature screen lifecycle belongs in feature routes")
    for source in features.glob("*/impl/src/*Main/**/*.kt"):
        if re.search(r"^import com\.google\.firebase\.analytics\.", source.read_text(), re.MULTILINE):
            errors.append(f"{source.relative_to(root)}: analytics SDKs belong in libraries/analytics platform source sets")
    for source in (root / "app/src/main").rglob("*"):
        if source.suffix in {".kt", ".java"} and re.search(r"^import com\.google\.firebase\.analytics\.", source.read_text(), re.MULTILINE):
            errors.append(f"{source.relative_to(root)}: analytics SDKs belong in libraries/analytics platform source sets")
    # Android is a composition/platform host, with the same feature ownership boundary.
    app_main = root / "app/src/main"
    for source in app_main.rglob("*"):
        if source.suffix not in {".kt", ".java"}:
            continue
        text = source.read_text()
        if re.search(r"\bteamcityapp\.features\.[\w.]+\.(?:R|resources)\b", text, re.MULTILINE):
            errors.append(f"{source.relative_to(root)}: feature resources belong in feature platform adapters")
        if re.search(r"\b(?:[A-Z]\w*UiState|PagingListSnapshot|collectAsLazyPagingItems)\b", text):
            errors.append(f"{source.relative_to(root)}: feature presentation belongs in feature modules")
        if re.search(r"class\s+\w+(?:\([^{}\n]*\))?\s*:\s*(?:[\w.]+\.)?(?:ArtifactFileStore|ResponseCacheStore)\b", text):
            errors.append(f"{source.relative_to(root)}: platform persistence belongs beside its library contract")
        if re.search(r'\bAccountPreferences\s*\(|"(?:home\.Drawer|history\.Run|details\.Stop)"', text):
            errors.append(f"{source.relative_to(root)}: legacy preference migration belongs in libraries/session")
    for source in (app_main / "res/values").glob("*.xml"):
        for name in re.findall(r'<(?:string|plurals|array|string-array)\s+name="([^"]+)"', source.read_text()):
            if name != "app_name":
                errors.append(f"{source.relative_to(root)}: feature copy {name} belongs in its owning module")
    for include in includes:
        if not (root / include[1:].replace(":", "/") / "build.gradle.kts").exists():
            errors.append(f"{include}: includes a missing feature module")
    for source in [root / "shared-app/build.gradle.kts", *features.glob("*/*/build.gradle.kts")]:
        if re.search(r'projects\.features\.\w+\.presentation\b', source.read_text()):
            errors.append(f"{source.relative_to(root)}: obsolete presentation module reference")
    return errors


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    errors = validate(parser.parse_args().root)
    if errors:
        raise SystemExit("\n".join(errors))
    print("Feature api/impl boundaries and KMP source ownership verified.")


if __name__ == "__main__":
    main()
