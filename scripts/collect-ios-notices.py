#!/usr/bin/env python3
# Copyright 2026 Andrey Tolpeev
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy at http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software distributed
# under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
# CONDITIONS OF ANY KIND, either express or implied. See the License for the
# specific language governing permissions and limitations under the License.

"""Bundle pinned native notices and the resolved device dependency inventory offline.

Run exportIosReleaseDependencies, ios-release-audit.py --dependencies, then this
script with that audit and the official Skia m150-a3f509bf52 iOS Release archive.
No upstream source is executed. Native source license notices retain their owners.
"""
import argparse
import base64
from concurrent.futures import ThreadPoolExecutor
import hashlib
import json
from pathlib import Path
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]
SKIA = "m150-a3f509bf52"
SOURCES = [
    ("Skia", f"https://raw.githubusercontent.com/JetBrains/skia/{SKIA}/LICENSE", False),
    ("Skiko NOTICE", "https://raw.githubusercontent.com/JetBrains/skiko/v0.150.2/NOTICE", False),
    ("ICU", "https://chromium.googlesource.com/chromium/deps/icu/+/364118a1d9da24bb5b770ac3d762ac144d6da5a4/LICENSE?format=TEXT", True),
    ("Expat", "https://chromium.googlesource.com/external/github.com/libexpat/libexpat/+/6154446fccefbf3ca644894f598969113b0c7bcd/expat/COPYING?format=TEXT", True),
    ("libjpeg-turbo", "https://chromium.googlesource.com/chromium/deps/libjpeg_turbo/+/e14cbfaa85529d47f9f55b0f104a579c1061f9ad/LICENSE.md?format=TEXT", True),
    ("Independent JPEG Group", "https://chromium.googlesource.com/chromium/deps/libjpeg_turbo/+/e14cbfaa85529d47f9f55b0f104a579c1061f9ad/README.ijg?format=TEXT", True),
    ("piex", "https://android.googlesource.com/platform/external/piex/+/bb217acdca1cc0c16b704669dd6f91a1b509c406/LICENSE?format=TEXT", True),
]
for name in ["LICENSE", "LICENSE.source_code", "LICENSE.technology", "NOTICE", "PATENTS"]:
    SOURCES.append(("DNG SDK " + name, "https://android.googlesource.com/platform/external/dng_sdk/+/dbe0a676450d9b8c71bf00688bb306409b779e90/" + name + "?format=TEXT", True))


def fetch(source):
    name, url, encoded = source
    data = subprocess.check_output(["curl", "--fail", "--silent", "--show-error", "--location", "--max-time", "30", "--retry", "1", url])
    if encoded:
        data = base64.b64decode(data, validate=False)
    text = data.decode("utf-8")
    if not text.strip() or text.lstrip().startswith("<!DOCTYPE"):
        raise ValueError(f"Invalid upstream notice: {name}")
    return name, url, data


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--audit", type=Path, required=True)
    parser.add_argument("--skia-archive", type=Path, required=True)
    parser.add_argument("--kotlin-native", type=Path, required=True)
    args = parser.parse_args()
    audit = json.loads(args.audit.read_text())
    inventory = audit["dependencies"]
    if any("apache" not in license["name"].lower() for entry in inventory for license in entry["licenses"]):
        raise ValueError("New non-Apache dependency: review and bundle its license first")
    with ThreadPoolExecutor(max_workers=3) as executor:
        notices = list(executor.map(fetch, SOURCES))
    for relative in ["NOTICE.txt", "third_party/gwt_license.txt", "third_party/guava_license.txt", "third_party/threetenbp_license.txt", "third_party/libbacktrace_LICENSE.txt", "third_party/libffi_license.txt", "third_party/mpmc_queue_LICENSE.txt", "third_party/unicode_LICENSE.txt"]:
        path = args.kotlin_native / "licenses" / relative
        notices.append(("Kotlin runtime " + relative, "Kotlin/Native 2.4.20 distribution/licenses/" + relative, path.read_bytes()))
    archive_hash = hashlib.sha256(args.skia_archive.read_bytes()).hexdigest()
    archive_url = f"https://github.com/JetBrains/skia/releases/download/{SKIA}/Skia-{SKIA}-ios-Release-arm64.zip"
    with zipfile.ZipFile(args.skia_archive) as archive:
        for path in sorted(archive.namelist()):
            if any(word in path.split('/')[-1].lower() for word in ["license", "copying", "notice", "patents", "ftl.txt"]):
                notices.append((path, archive_url + "#" + path, archive.read(path)))
    folder = ROOT / "shared-app/src/commonMain/composeResources/files/licenses"
    manifest = {"copyright": "Copyright 2026 Andrey Tolpeev; upstream notices retain their original owners", "license": "Apache-2.0 (inventory); individual notices specify their own terms",
                "skikoVersion": "0.150.2", "skiaTag": SKIA, "skiaArchiveSha256": archive_hash,
                "artifacts": inventory, "notices": []}
    sections = []
    for name, url, data in notices:
        manifest["notices"].append({"name": name, "source": url, "sha256": hashlib.sha256(data).hexdigest()})
        sections.append("\n\n" + "=" * 72 + "\n" + name + "\nSource: " + url + "\n\n" + data.decode("utf-8"))
    # Keep the existing first-party and Skia texts at the beginning of their readers.
    apache = (ROOT / "LICENSE.txt").read_text()
    components = "\n\nResolved iOS device runtime artifacts (Apache 2.0):\n" + "\n".join(entry["coordinate"] for entry in inventory)
    (folder / "teamcityapp.txt").write_text(apache + components + "\n\nThese artifacts use the Apache license above. Skia and native dependency\nlicenses and notices are bundled in the Skia reader.\n")
    skia_text = notices[0][2].decode("utf-8")
    bundled_skia = skia_text + "\n\nNative library redistribution notices\n" + "".join(sections[1:]) + "\n"
    # Git stores text with LF; hash the reproducible bundle, keeping upstream hashes raw.
    (folder / "skia.txt").write_text(bundled_skia.replace("\r\n", "\n"))
    (folder / "native-inventory.json").write_text(json.dumps(manifest, indent=2) + "\n")
    provenance = json.loads((folder / "provenance.json").read_text())
    for entry in provenance["entries"]:
        if entry["id"] in ["teamcityapp", "skia"]:
            entry["sha256"] = hashlib.sha256((folder / (entry["id"] + ".txt")).read_bytes()).hexdigest()
            entry["text_source"] = "Bundled native-inventory.json; standard Apache text plus resolved artifact inventory" if entry["id"] == "teamcityapp" else "Pinned Skia m150-a3f509bf52 and native dependencies; see native-inventory.json"
    (folder / "provenance.json").write_text(json.dumps(provenance, indent=2) + "\n")
    print(json.dumps({"externalArtifacts": len(inventory), "nativeNotices": len(notices), "skiaArchiveSha256": archive_hash}))


if __name__ == "__main__":
    main()
