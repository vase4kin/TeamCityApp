#!/usr/bin/env python3
# Copyright 2019, 2026 Andrey Tolpeev
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy at http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software distributed
# under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
# CONDITIONS OF ANY KIND, either express or implied. See the License for the
# specific language governing permissions and limitations under the License.

"""Render the repository's existing Android vector as an opaque iOS icon on macOS."""
import json
from pathlib import Path
import re
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
A = "{http://schemas.android.com/apk/res/android}"


def path_commands(data):
    tokens = re.findall(r"[mlczMLCZ]|[-+]?(?:\d*\.\d+|\d+)(?:[eE][-+]?\d+)?", data)
    result = []
    i = 0
    command = None
    x = y = start_x = start_y = 0.0
    while i < len(tokens):
        if tokens[i].isalpha():
            command = tokens[i]
            i += 1
        if command.lower() == "z":
            result.append("path.closeSubpath()")
            x, y = start_x, start_y
            command = None
            continue
        size = {"m": 2, "l": 2, "c": 6}[command.lower()]
        values = list(map(float, tokens[i:i + size]))
        if len(values) != size:
            raise ValueError("Incomplete vector command")
        i += size
        if command.islower():
            values = [v + (x if n % 2 == 0 else y) for n, v in enumerate(values)]
        if command.lower() == "m":
            x, y = start_x, start_y = values
            result.append(f"path.move(to: CGPoint(x: {x}, y: {y}))")
            command = "l" if command.islower() else "L"
        elif command.lower() == "l":
            x, y = values
            result.append(f"path.addLine(to: CGPoint(x: {x}, y: {y}))")
        else:
            x1, y1, x2, y2, x, y = values
            result.append(f"path.addCurve(to: CGPoint(x: {x}, y: {y}), control1: CGPoint(x: {x1}, y: {y1}), control2: CGPoint(x: {x2}, y: {y2}))")
    return result


def main():
    vector = ET.parse(ROOT / "app/src/main/res/drawable/ic_launcher_foreground.xml").getroot()
    group = vector.find("group")
    folder = ROOT / "iosApp/TeamCityApp/Assets.xcassets/AppIcon.appiconset"
    folder.mkdir(parents=True, exist_ok=True)
    build = ROOT / "build/ios-icon"
    build.mkdir(parents=True, exist_ok=True)
    source = ["import AppKit", "import CoreGraphics",
              "let context = CGContext(data: nil, width: 1024, height: 1024, bitsPerComponent: 8, bytesPerRow: 0, space: CGColorSpaceCreateDeviceRGB(), bitmapInfo: CGImageAlphaInfo.noneSkipLast.rawValue)!",
              "context.setFillColor(CGColor(red: 70.0/255, green: 137.0/255, blue: 1, alpha: 1))",
              "context.fill(CGRect(x: 0, y: 0, width: 1024, height: 1024))",
              "context.translateBy(x: 0, y: 1024)", "context.scaleBy(x: 1024/236.36363, y: -1024/236.36363)",
              f"context.translateBy(x: {group.attrib[A + 'translateX']}, y: {group.attrib[A + 'translateY']})",
              "context.setFillColor(CGColor(gray: 1, alpha: 1))"]
    for item in group.findall("path"):
        source.append("do { let path = CGMutablePath()")
        source.extend(path_commands(item.attrib[A + "pathData"]))
        source.append("context.addPath(path); context.fillPath() }")
    source += ["let image = NSBitmapImageRep(cgImage: context.makeImage()!)",
               f"try image.representation(using: .png, properties: [:])!.write(to: URL(fileURLWithPath: {json.dumps(str(folder / 'AppIcon.png'))}))"]
    script = build / "RenderIcon.swift"
    script.write_text("\n".join(source) + "\n")
    subprocess.run(["swift", "-module-cache-path", str(build / "ModuleCache"), str(script)], check=True)
    (folder / "Contents.json").write_text(json.dumps({"images": [{"filename": "AppIcon.png", "idiom": "universal", "platform": "ios", "size": "1024x1024"}], "info": {"author": "Andrey Tolpeev", "version": 1}}, indent=2) + "\n")
    (folder.parent / "Contents.json").write_text(json.dumps({"info": {"author": "Andrey Tolpeev", "version": 1}}, indent=2) + "\n")


if __name__ == "__main__":
    main()
