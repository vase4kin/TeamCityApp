#!/usr/bin/env python3
# Copyright 2026 Andrey Tolpeev
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy at http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software distributed
# under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
# CONDITIONS OF ANY KIND, either express or implied. See the License for the
# specific language governing permissions and limitations under the License.

"""Audit iOS release metadata and an optional built app without signing or uploading."""
import argparse
import hashlib
import json
from pathlib import Path
import plistlib
import re
import struct
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
REASONS = {
    "NSPrivacyAccessedAPICategoryUserDefaults": "CA92.1",
    "NSPrivacyAccessedAPICategoryFileTimestamp": "0A2A.1",
    "NSPrivacyAccessedAPICategorySystemBootTime": "35F9.1",
}


def privacy_errors(data):
    errors = []
    if data.get("NSPrivacyTracking") is not False or data.get("NSPrivacyTrackingDomains") != []:
        errors.append("Unexpected tracking declaration")
    if data.get("NSPrivacyCollectedDataTypes") != []:
        errors.append("Collection changed: review the release privacy disclosure")
    declarations = {item.get("NSPrivacyAccessedAPIType"): item.get("NSPrivacyAccessedAPITypeReasons", [])
                    for item in data.get("NSPrivacyAccessedAPITypes", [])}
    for api, reason in REASONS.items():
        if reason not in declarations.get(api, []):
            errors.append(f"Missing required API reason: {api} / {reason}")
    return errors


def license_errors(folder):
    errors = []
    try:
        provenance = json.loads((folder / "provenance.json").read_text())
        for entry in provenance["entries"]:
            path = folder / (entry["id"] + ".txt")
            if not path.exists() or hashlib.sha256(path.read_bytes()).hexdigest() != entry["sha256"]:
                errors.append("Missing or stale bundled license: " + entry["id"])
        inventory = json.loads((folder / "native-inventory.json").read_text())
        if not inventory.get("artifacts") or not inventory.get("notices"):
            errors.append("Empty native redistribution inventory")
    except (OSError, ValueError, KeyError):
        errors.append("Missing or invalid native redistribution inventory")
    return errors


def deployment_errors(minimum, project):
    targets = re.findall(r"IPHONEOS_DEPLOYMENT_TARGET\s*=\s*([^;]+);", project)
    if not targets or set(targets) != {minimum}:
        return ["Xcode and shared iOS deployment minimums differ"]
    return []


def source_audit(root):
    config = (root / "build-logic/src/main/kotlin/Config.kt").read_text()
    minimum = re.search(r'const val minIos = "([^"]+)"', config).group(1)
    version = re.search(r'const val versionName = "([^"]+)"', config).group(1)
    xcconfig = (root / "iosApp/Version.xcconfig").read_text()
    ios_version = re.search(r"MARKETING_VERSION\s*=\s*([^\s/]+)", xcconfig).group(1)
    errors = []
    if ios_version != version.split("-")[0]:
        errors.append("Android/shared and iOS marketing versions differ")
    manifest = plistlib.loads((root / "iosApp/TeamCityApp/PrivacyInfo.xcprivacy").read_bytes())
    errors.extend(privacy_errors(manifest))
    project = (root / "iosApp/TeamCityApp.xcodeproj/project.pbxproj").read_text()
    errors.extend(deployment_errors(minimum, project))
    for resource in ["PrivacyInfo.xcprivacy in Resources", "Assets.xcassets in Resources"]:
        if project.count(resource) < 2:
            errors.append(f"Resource missing from build phase: {resource}")
    icon = root / "iosApp/TeamCityApp/Assets.xcassets/AppIcon.appiconset/AppIcon.png"
    if not icon.is_file():
        errors.append("Missing iOS app icon")
    else:
        image = icon.read_bytes()
        if image[:8] != b"\x89PNG\r\n\x1a\n" or len(image) < 26 or struct.unpack(">II", image[16:24]) != (1024, 1024) or image[25] in [4, 6]:
            errors.append("iOS icon must be an opaque 1024px PNG")
    errors.extend(license_errors(root / "shared-app/src/commonMain/composeResources/files/licenses"))
    return {"version": version, "iosMarketingVersion": ios_version, "minimumOS": minimum, "errors": errors}


def dependencies(tsv, cache):
    entries = []
    for line in tsv.read_text().splitlines():
        coordinate, filename = line.split("\t")
        group, name, version = coordinate.split(":")
        if group.startswith("TeamCityApp"):
            continue
        candidates = list((cache / group / name / version).rglob("*.pom"))
        if len(candidates) != 1:
            raise ValueError(f"Expected one cached POM for {coordinate}")
        pom = candidates[0]
        ns = {"m": "http://maven.apache.org/POM/4.0.0"}
        xml = ET.parse(pom).getroot()
        licenses = [{"name": x.findtext("m:name", namespaces=ns), "url": x.findtext("m:url", namespaces=ns)}
                    for x in xml.findall("m:licenses/m:license", ns)]
        if not licenses:
            raise ValueError(f"Missing license metadata: {coordinate}")
        artifact = Path(filename)
        entries.append({"coordinate": coordinate, "licenses": licenses,
                        "artifactSha256": hashlib.sha256(artifact.read_bytes()).hexdigest(),
                        "pomSha256": hashlib.sha256(pom.read_bytes()).hexdigest()})
    if not entries:
        raise ValueError("Empty native dependency inventory")
    return sorted(entries, key=lambda x: x["coordinate"])


def app_audit(app, version, minimum):
    errors = []
    info = plistlib.loads((app / "Info.plist").read_bytes())
    if info.get("CFBundleShortVersionString") != version:
        errors.append("Built app version differs from source")
    manifest = app / "PrivacyInfo.xcprivacy"
    if manifest.exists():
        errors.extend(privacy_errors(plistlib.loads(manifest.read_bytes())))
    else:
        errors.append("Built app has no privacy manifest")
    if not info.get("CFBundleIcons"):
        errors.append("Built app has no compiled iPhone icon")
    if info.get("MinimumOSVersion") != minimum:
        errors.append("Unexpected iOS deployment minimum")
    licenses = list(app.rglob("files/licenses/native-inventory.json"))
    if len(licenses) != 1:
        errors.append("Built app has no unique bundled native license inventory")
    else:
        errors.extend(license_errors(licenses[0].parent))
    binary = app / info["CFBundleExecutable"]
    return {"bundleId": info.get("CFBundleIdentifier"), "version": info.get("CFBundleShortVersionString"),
            "buildNumber": info.get("CFBundleVersion"), "minimumOS": info.get("MinimumOSVersion"),
            "binaryBytes": binary.stat().st_size, "binarySha256": hashlib.sha256(binary.read_bytes()).hexdigest(),
            "embeddedProvisioning": (app / "embedded.mobileprovision").exists(),
            "licenseInventorySha256": hashlib.sha256(licenses[0].read_bytes()).hexdigest() if len(licenses) == 1 else None,
            "errors": errors}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=ROOT)
    parser.add_argument("--app", type=Path)
    parser.add_argument("--dependencies", type=Path)
    parser.add_argument("--cache", type=Path, default=Path.home() / ".gradle/caches/modules-2/files-2.1")
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    result = {"source": source_audit(args.root)}
    if args.dependencies:
        result["dependencies"] = dependencies(args.dependencies, args.cache)
        bundled = json.loads((args.root / "shared-app/src/commonMain/composeResources/files/licenses/native-inventory.json").read_text())
        signature = lambda entries: sorted((x["coordinate"], x["artifactSha256"], x["pomSha256"]) for x in entries)
        if signature(result["dependencies"]) != signature(bundled["artifacts"]):
            result["source"]["errors"].append("Resolved native dependencies differ from bundled notices; regenerate and review")
    if args.app:
        result["app"] = app_audit(args.app, result["source"]["iosMarketingVersion"], result["source"]["minimumOS"])
        inventory = args.root / "shared-app/src/commonMain/composeResources/files/licenses/native-inventory.json"
        if result["app"]["licenseInventorySha256"] != hashlib.sha256(inventory.read_bytes()).hexdigest():
            result["app"]["errors"].append("Built app license inventory differs from current source")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2) + "\n")
    errors = result["source"]["errors"] + result.get("app", {}).get("errors", [])
    print(json.dumps({"errors": errors, "output": str(args.output)}))
    raise SystemExit(bool(errors))


if __name__ == "__main__":
    main()
