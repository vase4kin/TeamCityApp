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

"""Partition all local tests and transport matching aggregate coverage inputs."""
import argparse
import json
import os
from pathlib import Path
import re
import zipfile


SHARD_COUNT = 4


def has_production_sources(directory):
    return any(path.suffix in {".kt", ".java"}
               for source_set in ("main", "commonMain", "androidMain", "mock", "prod", "debug")
               for path in (directory / f"src/{source_set}").rglob("*"))


def modules(root):
    """Follow explicit includes and schedule Android and KMP host checks."""
    included = re.findall(r'^include\("(:[\w:-]+)"\)', (root / "settings.gradle.kts").read_text(), re.MULTILINE)
    result = {}
    for module in included:
        directory = root / module[1:].replace(":", "/")
        build = (directory / "build.gradle.kts").read_text()
        if re.search(r'id\("teamcityapp\.kmp\.(?:library|compose)"\)', build):
            sources = [p for source_set in ("commonTest", "androidHostTest")
                       for p in (directory / f"src/{source_set}").rglob("*.kt")]
            tasks = [f"{module}:testAndroidHostTest"]
            if 'id("teamcityapp.android.screenshot")' in build:
                tasks.append(f"{module}:verifyRoborazziAndroidHostTest")
            startups = sum(len(re.findall(r"@RunWith\([^\n]*Robolectric", p.read_text())) for p in sources)
            images = sum(1 for _ in (directory / "src/test/screenshots").rglob("*.png"))
            result[module] = {"tasks": tasks,
                              "weight": max(1, 20 * min(startups, 2) + images * .15 + len(sources) * .1), "has_tests": bool(sources),
                              "has_production_sources": has_production_sources(directory)}
            continue
        if not re.search(r'id\("teamcityapp\.android\.(?:application|library(?:\.java)?)"\)', build):
            raise ValueError(f"Unrecognized test convention for {module}; update unit-test-ci.py")
        variant = "MockDebug" if module == ":app" else "Debug"
        if module != ":app" and "productFlavors" in build:
            raise ValueError(f"Unrecognized test variants for {module}; update unit-test-ci.py")
        tasks = [f"{module}:test{variant}UnitTest"]
        if 'id("teamcityapp.android.screenshot")' in build:
            tasks.append(f"{module}:verifyRoborazzi{variant}")
        sources = [p for p in (directory / "src/test").rglob("*") if p.suffix in {".kt", ".java"}]
        # Most modules share one or two Robolectric sandboxes between test classes.
        # PNG counts estimate render work; the cap avoids overweighting the app.
        startups = sum(len(re.findall(r"@RunWith\([^\n]*Robolectric", p.read_text())) for p in sources)
        images = sum(1 for _ in (directory / "src/test/screenshots").rglob("*.png"))
        result[module] = {"tasks": tasks, "weight": max(1, 20 * min(startups, 2) + images * .15 + len(sources) * .1), "has_tests": bool(sources),
                          "has_production_sources": has_production_sources(directory)}
    if not result:
        raise ValueError("No Android modules discovered")
    return result


def plan(root):
    entries = modules(root)
    entries[":build-logic"] = {"tasks": [":build-logic:test"], "weight": 100}
    shards = [{"shard": index, "modules": [], "tasks": [], "weight": 0} for index in range(SHARD_COUNT)]
    for module, entry in sorted(entries.items(), key=lambda item: (-item[1]["weight"], item[0])):
        shard = min(shards, key=lambda item: (item["weight"], item["shard"]))
        shard["modules"].append(module)
        shard["tasks"].extend(entry["tasks"])
        shard["weight"] += entry["weight"]
    return {"include": [{"shard": s["shard"], "tasks": " ".join(s["tasks"]), "modules": s["modules"]} for s in shards]}


def coverage_paths(root, module):
    directory = root / module[1:].replace(":", "/") / "build"
    yield from sorted((directory / "classes/kotlin/android/main").rglob("*.class"))
    for variant in ("debug", "mockDebug"):
        capitalized = variant[0].upper() + variant[1:]
        transformed = directory / f"intermediates/classes/{variant}/transform{capitalized}ClassesWithAsm/dirs"
        paths = [transformed] if transformed.is_dir() else [directory / f"intermediates/javac/{variant}", directory / f"intermediates/built_in_kotlinc/{variant}/compile{capitalized}Kotlin/classes"]
        for path in paths:
            yield from sorted(path.rglob("*.class"))


def execution_paths(root, module):
    directory = root / module[1:].replace(":", "/") / "build"
    for pattern in ("jacoco/*.exec", "outputs/unit_test_code_coverage/debugUnitTest/*.exec", "outputs/unit_test_code_coverage/mockDebugUnitTest/*.exec"):
        yield from sorted(directory.glob(pattern))


def validate_execution(root, shard, entries):
    for module in shard["modules"]:
        if module == ":build-logic":
            continue
        if entries[module]["has_tests"]:
            if not any(p.stat().st_size for p in execution_paths(root, module)):
                raise ValueError(f"Missing unit coverage execution data for {module}")
            # A resource/re-export module may retain tests without owning any
            # production bytecode. Its execution data still covers dependencies.
            if entries[module]["has_production_sources"] and not any(coverage_paths(root, module)):
                raise ValueError(f"Missing compiled coverage classes for {module}")


def pack(root, shard_id, output):
    shard = plan(root)["include"][shard_id]
    entries = modules(root)
    validate_execution(root, shard, entries)
    paths = set()
    # Include compiled dependencies too, preserving the previous report denominator.
    for module in entries:
        paths.update(coverage_paths(root, module))
    for module in shard["modules"]:
        if module == ":build-logic":
            continue  # The included build was never part of app aggregate coverage.
        execution = list(execution_paths(root, module))
        paths.update(execution)
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("shard.json", json.dumps(shard))
        for path in sorted(paths):
            archive.write(path, path.relative_to(root).as_posix())


def restore(root, inputs):
    expected = plan(root)["include"]
    entries = modules(root)
    archives = sorted(inputs.glob("*.zip"))
    if len(archives) != SHARD_COUNT:
        raise ValueError(f"Expected {SHARD_COUNT} coverage archives, found {len(archives)}")
    seen = set()
    for path in archives:
        with zipfile.ZipFile(path) as archive:
            manifest = json.loads(archive.read("shard.json"))
            shard = manifest["shard"]
            if not isinstance(shard, int) or not 0 <= shard < SHARD_COUNT or shard in seen or manifest != expected[shard]:
                raise ValueError(f"Duplicate or unexpected coverage shard: {shard}")
            seen.add(shard)
            for member in archive.infolist():
                if member.filename == "shard.json":
                    continue
                relative = Path(member.filename)
                if relative.is_absolute() or ".." in relative.parts or "build" not in relative.parts or relative.suffix not in {".class", ".exec"}:
                    raise ValueError(f"Unexpected coverage input: {relative}")
                destination = root / relative
                data = archive.read(member)
                if destination.exists() and destination.read_bytes() != data:
                    raise ValueError(f"Conflicting coverage input: {relative}")
                destination.parent.mkdir(parents=True, exist_ok=True)
                destination.write_bytes(data)
            validate_execution(root, manifest, entries)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=("plan", "pack", "restore"))
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--shard", type=int, choices=range(SHARD_COUNT))
    parser.add_argument("--output", type=Path)
    parser.add_argument("--inputs", type=Path)
    args = parser.parse_args()
    if args.command == "plan":
        matrix = json.dumps(plan(args.root), separators=(",", ":"))
        print(matrix)
        if os.environ.get("GITHUB_OUTPUT"):
            with open(os.environ["GITHUB_OUTPUT"], "a") as output:
                output.write(f"unit_test_matrix={matrix}\n")
    elif args.command == "pack":
        if args.shard is None or args.output is None:
            parser.error("pack requires --shard and --output")
        pack(args.root, args.shard, args.output)
    else:
        if args.inputs is None:
            parser.error("restore requires --inputs")
        restore(args.root, args.inputs)


if __name__ == "__main__":
    main()
