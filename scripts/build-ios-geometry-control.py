#!/usr/bin/env python3
# Copyright 2026 Andrey Tolpeev
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software distributed
# under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
# CONDITIONS OF ANY KIND, either express or implied. See the License for the
# specific language governing permissions and limitations under the License.
"""Build a storage/network-free simulator-only window-geometry control."""
import plistlib
import re
import subprocess
from pathlib import Path

def main():
    root = Path(__file__).resolve().parents[1]
    config = (root / "build-logic/src/main/kotlin/Config.kt").read_text()
    minimum_match = re.search(r'minIos\s*=\s*"([0-9.]+)"', config)
    if minimum_match is None:
        raise RuntimeError("Cannot read the configured iOS minimum")
    minimum_os = minimum_match.group(1)
    app = root / "build/kmp-phase-6/GeometryControl.app"
    app.mkdir(parents=True, exist_ok=True)
    sdk_version = subprocess.check_output(["xcrun", "--sdk", "iphonesimulator", "--show-sdk-version"], text=True).strip()
    sdk_build = subprocess.check_output(["xcrun", "--sdk", "iphonesimulator", "--show-sdk-build-version"], text=True).strip()
    orientations = ["UIInterfaceOrientationPortrait", "UIInterfaceOrientationLandscapeLeft", "UIInterfaceOrientationLandscapeRight"]
    info = {
        "CFBundleSupportedPlatforms": ["iPhoneSimulator"],
        "DTPlatformName": "iphonesimulator",
        "DTPlatformVersion": sdk_version,
        "DTSDKName": "iphonesimulator" + sdk_version,
        "DTSDKBuild": sdk_build,
        "CFBundleIdentifier": "app.teamcity.geometry-control",
        "CFBundleExecutable": "GeometryControl",
        "CFBundleName": "GeometryControl",
        "CFBundleDisplayName": "Geometry Control",
        "CFBundlePackageType": "APPL",
        "CFBundleVersion": "1",
        "CFBundleShortVersionString": "1.0",
        "MinimumOSVersion": minimum_os,
        "UIDeviceFamily": [1, 2],
        "UILaunchScreen": {},
        "UISupportedInterfaceOrientations": orientations,
        "UISupportedInterfaceOrientations~ipad": orientations,
        "UIApplicationSceneManifest": {
            "UIApplicationSupportsMultipleScenes": False,
            "UISceneConfigurations": {"UIWindowSceneSessionRoleApplication": [{
                "UISceneConfigurationName": "Control",
                "UISceneClassName": "UIWindowScene",
                "UISceneDelegateClassName": "GeometryControl.GeometryControlScene",
            }]},
        },
    }
    (app / "Info.plist").write_bytes(plistlib.dumps(info))
    sdk = subprocess.check_output(["xcrun", "--sdk", "iphonesimulator", "--show-sdk-path"], text=True).strip()
    subprocess.run(["xcrun", "swiftc", "-parse-as-library", "-module-name", "GeometryControl", "-sdk", sdk,
                    "-target", f"arm64-apple-ios{minimum_os}-simulator", "-framework", "UIKit", "-framework", "SwiftUI",
                    "-Xlinker", "-platform_version", "-Xlinker", "ios-simulator", "-Xlinker", minimum_os, "-Xlinker", sdk_version,
                    str(root / "scripts/ios-geometry-control.swift"), "-o", str(app / "GeometryControl")], check=True)
    subprocess.run(["codesign", "--force", "--sign", "-", str(app)], check=True)
    print(app)


if __name__ == "__main__":
    main()
