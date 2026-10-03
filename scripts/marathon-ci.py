#!/usr/bin/env python3
"""Plan paid UI runs, select exact tests, and reject empty Marathon results."""

import argparse
import fnmatch
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import xml.etree.ElementTree as ET


ROOT = Path(__file__).resolve().parents[1]
MOCK = {
    "label": "instrumentation",
    "artifact": "mock-test-apks",
    "application": "app/build/outputs/apk/mock/debug/app-mock-debug.apk",
    "test-application": "app/build/outputs/apk/androidTest/mock/debug/app-mock-debug-androidTest.apk",
    "coverage": True,
}
R8 = {
    "label": "r8",
    "artifact": "r8-test-apks",
    "application": "app/build/outputs/apk/prod/r8Verification/app-prod-r8Verification.apk",
    "test-application": "app/build/outputs/apk/androidTest/prod/r8Verification/app-prod-r8Verification-androidTest.apk",
    "coverage": False,
}


def policy(event_name, ref, event):
    default_branch = event.get("repository", {}).get("default_branch", "dev")
    if event_name == "push" and ref == f"refs/heads/{default_branch}":
        return {"run": True, "suite": "full", "reason": "Full validation for application or Android build changes pushed to the default branch"}
    if event_name == "workflow_dispatch":
        inputs = event.get("inputs", {})
        enabled = inputs.get("run_ui_tests", False) in (True, "true")
        suite = inputs.get("suite", "full") if enabled else "changed"
        if enabled and suite not in ("full", "smoke", "single", "random"):
            raise ValueError(f"Unknown UI suite: {suite}")
        return {"run": True, "suite": suite, "reason": "Manual suite override enabled" if enabled else "Default changed-feature and smoke validation"}
    if event_name == "pull_request":
        pr = event.get("pull_request", {})
        head_repo = pr.get("head", {}).get("repo", {}).get("full_name")
        repository = event.get("repository", {}).get("full_name")
        if head_repo != repository:
            return {"run": False, "suite": "none", "reason": "Fork PR has no Marathon API secret"}
        return {"run": True, "suite": "changed", "reason": "Changed-feature and smoke validation for application or Android build changes"}
    return {"run": False, "suite": "none", "reason": "No UI policy for this event"}


def source_test_index(root):
    """Validate selectors against the repository's ordinary Kotlin JUnit4 tests.

    This is a preflight for our current test sources, not APK test discovery.
    Downloaded JUnit results independently verify that every selected test ran.
    """
    index = {}
    annotations = r"((?:@[\w.]+(?:\([^\n]*?\))?\s*)+)"
    for path in (root / "app/src/androidTest").rglob("*.kt"):
        # Preserve string boundaries: URLs in @Ignore reasons are not comments.
        tokens = r'"""[\s\S]*?"""|"(?:\\.|[^"\\])*"|/\*[\s\S]*?\*/|//[^\n]*'
        text = re.sub(tokens, lambda match: ('""' if match[0].startswith('"') else '') + '\n' * match[0].count('\n'), path.read_text())
        package = re.search(r"(?m)^package\s+([^\s;]+)", text)
        clazz = re.search(r"(?m)^class\s+(\w+)", text)
        if not package or not clazz:
            continue
        tests = index.setdefault(path.relative_to(root).as_posix(), set())
        class_annotations = re.search(annotations + r"class\s+" + clazz[1], text)
        if class_annotations and re.search(r"@(?:org\.junit\.)?Ignore\b", class_annotations[1]):
            continue
        for match in re.finditer(annotations + r"(?:public\s+)?fun\s+(\w+)\s*\(", text):
            decorators, method = match.groups()
            if re.search(r"@(?:org\.junit\.)?Test\b", decorators) and not re.search(r"@(?:org\.junit\.)?Ignore\b", decorators):
                tests.add(f"{package[1].replace('`', '')}.{clazz[1]}#{method}")
    return index


def source_tests(root):
    return set().union(*source_test_index(root).values())


def changed_paths(root, event_name, event):
    """Compare the complete PR branch or push, retaining both rename paths."""
    if event_name == "pull_request":
        pr = event["pull_request"]
        base, head = pr["base"]["sha"], pr["head"]["sha"]
        comparison = f"{base}...{head}"
    elif event_name == "push":
        # Compare the old and new branch tips, including every commit in a push.
        # A merge-base diff would miss removals when a branch is force-pushed.
        comparison = f"{event['before']}..{event['after']}"
    else:
        base = "origin/" + event.get("repository", {}).get("default_branch", "dev")
        comparison = f"{base}...HEAD"
    result = subprocess.run(
        ["git", "diff", "--name-only", "--no-renames", "-z", comparison],
        cwd=root, capture_output=True, check=True,
    )
    return [path for path in result.stdout.decode().split("\0") if path]


def ui_test_input(path):
    # CI scripts and workflows are checked without launching Android devices.
    return not path.endswith(".md") and (
        path.startswith(("app/", "features/", "libraries/", "build-logic/", "gradle/"))
        or path in ("build.gradle.kts", "settings.gradle.kts", "gradle.properties", "gradlew", "gradlew.bat", "mock-mockDebug-google-services.json", "mock-prodDebug-google-services.json")
    )


def changed_selection(paths, catalog, available, index, mapping):
    # Only application, Android test, and build inputs need device validation.
    inputs = [path for path in paths if ui_test_input(path)]
    if not inputs:
        return {"suite": "changed", "tests": [], "seed": "", "changed_files": paths, "features": [], "fallback_paths": []}
    smoke = selection("smoke", catalog, available)["tests"]
    selected = set(smoke)
    fallback = []
    owners = set()

    def feature_tests(owner):
        normalized = owner.replace("-", "_")
        prefixes = (f"com.github.vase4kin.teamcityapp.{normalized}.", f"teamcityapp.features.{normalized}.")
        found = {test for test in available if test.replace("-", "_").startswith(prefixes)}
        for pattern in mapping.get(owner, {}).get("tests", []):
            found.update(test for test in available if fnmatch.fnmatchcase(test, pattern))
        return found

    for path in inputs:
        if path in index and index[path]:
            selected.update(index[path])
            continue
        if path in index and "/helper/" not in path:
            # Ignored classes have no runnable tests; smoke still runs.
            continue
        matches = {owner for owner, rule in mapping.items() if any(fnmatch.fnmatchcase(path, pattern) for pattern in rule.get("paths", []))}
        legacy = re.match(r"app/src/[^/]+/(?:java|kotlin)/com/github/vase4kin/teamcityapp/([^/]+)/", path)
        module = re.match(r"features/([^/]+)/", path)
        if legacy:
            if legacy[1] in ("api", "base", "dagger", "storage", "utils", "navigation", "app_navigation", "bottomsheet_dialog", "filter_bottom_sheet_dialog"):
                fallback.append(path)
                continue
            matches.add(legacy[1])
        if module:
            matches.add(module[1])
        resolved = set().union(*(feature_tests(owner) for owner in matches))
        if resolved:
            selected.update(resolved)
            owners.update(matches)
        else:
            # Shared/unmapped application inputs can affect any instrumentation test.
            fallback.append(path)
    if fallback:
        selected.update(available)
    return {"suite": "changed", "tests": sorted(selected), "seed": "", "changed_files": paths, "features": sorted(owners), "fallback_paths": fallback}


def selection(suite, catalog, available, test_name="", seed=""):
    if suite == "full":
        return {"suite": suite, "tests": [], "seed": ""}
    if suite == "single":
        selected = [test_name.strip()]
    else:
        pool = catalog[suite]
        if not pool or len(pool) != len(set(pool)):
            raise ValueError(f"The {suite} catalog must be nonempty and contain no duplicates")
        missing = set(pool) - available
        if missing:
            raise ValueError(f"Unknown or ignored catalog tests: {sorted(missing)}")
        if suite == "random":
            if not seed:
                raise ValueError("Random selection needs a seed")
            index = int(hashlib.sha256(seed.encode()).hexdigest(), 16) % len(pool)
            selected = [sorted(pool)[index]]
        else:
            selected = pool
    missing = set(selected) - available
    if missing:
        raise ValueError(f"Unknown or ignored tests: {sorted(missing)}; use package.TestClass#testMethod")
    return {"suite": suite, "tests": selected, "seed": seed if suite == "random" else ""}


def write_selection(directory, selected):
    directory.mkdir(parents=True, exist_ok=True)
    (directory / "selection.json").write_text(json.dumps(selected, indent=2) + "\n")
    if selected["tests"]:
        # JSON strings are also valid YAML scalars; no selectors become shell code.
        values = "".join(f"        - {json.dumps(test)}\n" for test in selected["tests"])
        (directory / "filter.yaml").write_text(
            'filteringConfiguration:\n  allowlist:\n    - type: "fully-qualified-test-name"\n      values:\n' + values
        )


def verify_results(directory, selected, label):
    passed = set()
    failed = set()
    for path in directory.rglob("*.xml"):
        # Marathon also downloads XML attachments which are not JUnit reports.
        try:
            document = ET.parse(path).getroot()
        except ET.ParseError:
            continue
        if document.tag not in ("testsuite", "testsuites"):
            continue
        for case in document.iter("testcase"):
            name = f"{case.get('classname', '')}#{case.get('name', '')}"
            if case.find("failure") is not None or case.find("error") is not None:
                failed.add(name)
            elif case.find("skipped") is None:
                passed.add(name)
    if failed:
        raise ValueError(f"Failed Marathon tests: {sorted(failed)}")
    minimum = 5 if label == "r8" else 1
    if len(passed) < minimum:
        raise ValueError(f"Expected at least {minimum} successful {label} tests, found {len(passed)}")
    if label == "instrumentation" and selected["tests"]:
        expected = set(selected["tests"])
        if passed != expected:
            raise ValueError(f"Test selection mismatch: missing={sorted(expected - passed)}, unexpected={sorted(passed - expected)}")
    return len(passed)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    plan = commands.add_parser("policy")
    plan.add_argument("--selection-dir", type=Path, required=True)
    verify = commands.add_parser("verify")
    verify.add_argument("--results", type=Path, required=True)
    verify.add_argument("--selection", type=Path, required=True)
    verify.add_argument("--label", choices=("instrumentation", "r8"), required=True)
    args = parser.parse_args()
    if args.command == "verify":
        count = verify_results(args.results, json.loads(args.selection.read_text()), args.label)
        print(f"Verified {count} successful {args.label} tests")
        return
    event = json.loads(Path(os.environ["GITHUB_EVENT_PATH"]).read_text())
    decision = policy(os.environ["GITHUB_EVENT_NAME"], os.environ["GITHUB_REF"], event)
    selected = None
    if decision["run"]:
        inputs = event.get("inputs", {})
        seed = inputs.get("random_seed", "").strip() or ":".join(
            os.environ.get(key, "") for key in ("GITHUB_SHA", "GITHUB_RUN_ID", "GITHUB_RUN_ATTEMPT")
        )
        catalog = json.loads((ROOT / "scripts/marathon-tests.json").read_text())
        index = source_test_index(ROOT)
        available = set().union(*index.values())
        if decision["suite"] == "changed" or os.environ["GITHUB_EVENT_NAME"] == "push":
            mapping = json.loads((ROOT / "scripts/marathon-changes.json").read_text())
            try:
                paths = changed_paths(ROOT, os.environ["GITHUB_EVENT_NAME"], event)
                if decision["suite"] == "full":
                    selected = {**selection("full", catalog, available), "changed_files": paths}
                else:
                    selected = changed_selection(paths, catalog, available, index, mapping)
                if not any(ui_test_input(path) for path in paths):
                    decision = {"run": False, "suite": "none", "reason": "No application, Android test, or build inputs changed"}
            except (subprocess.CalledProcessError, KeyError):
                # Missing comparison history must not silently omit affected tests.
                if decision["suite"] == "full":
                    selected = {**selection("full", catalog, available), "fallback_reason": "Could not compare the push before and after commits; running both full UI suites"}
                else:
                    selected = {"suite": "changed", "tests": sorted(available), "seed": "", "fallback_reason": "Could not compare the base and head; selecting all mock instrumentation tests"}
        else:
            selected = selection(decision["suite"], catalog, available, inputs.get("test_name", ""), seed)
        if decision["run"]:
            write_selection(args.selection_dir, selected)
    collect_coverage = decision["run"] and decision["suite"] in ("full", "single", "random")
    mock = dict(MOCK, coverage=collect_coverage)
    matrix = {"include": ([mock, R8] if decision["suite"] == "full" else [mock]) if decision["run"] else []}
    summary = f"UI tests: {decision['suite']} — {decision['reason']}\n"
    if selected is not None:
        summary += "\n" + json.dumps(selected, indent=2) + "\n"
    print(summary)
    with Path(os.environ["GITHUB_OUTPUT"]).open("a") as output:
        output.write(f"run_ui_tests={str(decision['run']).lower()}\nsuite={decision['suite']}\ncollect_coverage={str(collect_coverage).lower()}\nmatrix={json.dumps(matrix)}\n")
    with Path(os.environ["GITHUB_STEP_SUMMARY"]).open("a") as output:
        output.write("### UI test selection\n\n```text\n" + summary.replace("```", "") + "```\n")


if __name__ == "__main__":
    main()
