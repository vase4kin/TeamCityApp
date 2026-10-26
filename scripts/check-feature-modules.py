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
