#!/usr/bin/env python3
"""Plan paid UI runs, select exact tests, and reject empty Marathon results."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
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
        return {"run": True, "suite": "full", "reason": "Full validation after a default-branch push"}
    if event_name == "workflow_dispatch":
        inputs = event.get("inputs", {})
        enabled = inputs.get("run_ui_tests", False) in (True, "true")
        suite = inputs.get("suite", "full") if enabled else "none"
        if enabled and suite not in ("full", "smoke", "single", "random"):
            raise ValueError(f"Unknown UI suite: {suite}")
        return {"run": enabled, "suite": suite, "reason": "Manual UI checkbox enabled" if enabled else "Manual UI checkbox unchecked"}
    title = event.get("pull_request", {}).get("title", "")
    chore = re.match(r"^chore(?:\([^\r\n()]+\))?!?:\s", title) is not None
    return {"run": False, "suite": "none", "reason": "Chore PR: UI tests require manual dispatch" if chore else "PR UI tests require manual dispatch"}


def source_tests(root):
    """Validate selectors against the repository's ordinary Kotlin JUnit4 tests.

    This is a preflight for our current test sources, not APK test discovery.
    Downloaded JUnit results independently verify that every selected test ran.
    """
    tests = set()
    annotations = r"((?:@[\w.]+(?:\([^\n]*?\))?\s*)+)"
    for path in (root / "app/src/androidTest").rglob("*.kt"):
        # Preserve string boundaries: URLs in @Ignore reasons are not comments.
        tokens = r'"""[\s\S]*?"""|"(?:\\.|[^"\\])*"|/\*[\s\S]*?\*/|//[^\n]*'
        text = re.sub(tokens, lambda match: ('""' if match[0].startswith('"') else '') + '\n' * match[0].count('\n'), path.read_text())
        package = re.search(r"(?m)^package\s+([^\s;]+)", text)
        clazz = re.search(r"(?m)^class\s+(\w+)", text)
        if not package or not clazz:
            continue
        class_annotations = re.search(annotations + r"class\s+" + clazz[1], text)
        if class_annotations and re.search(r"@(?:org\.junit\.)?Ignore\b", class_annotations[1]):
            continue
        for match in re.finditer(annotations + r"(?:public\s+)?fun\s+(\w+)\s*\(", text):
            decorators, method = match.groups()
            if re.search(r"@(?:org\.junit\.)?Test\b", decorators) and not re.search(r"@(?:org\.junit\.)?Ignore\b", decorators):
                tests.add(f"{package[1].replace('`', '')}.{clazz[1]}#{method}")
    return tests


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
    matrix = {"include": [MOCK, R8] if decision["suite"] == "full" else [MOCK]}
    summary = f"UI tests: {decision['suite']} — {decision['reason']}\n"
    if decision["run"]:
        inputs = event.get("inputs", {})
        seed = inputs.get("random_seed", "").strip() or ":".join(
            os.environ.get(key, "") for key in ("GITHUB_SHA", "GITHUB_RUN_ID", "GITHUB_RUN_ATTEMPT")
        )
        selected = selection(decision["suite"], json.loads((ROOT / "scripts/marathon-tests.json").read_text()), source_tests(ROOT), inputs.get("test_name", ""), seed)
        write_selection(args.selection_dir, selected)
        summary += "\n" + json.dumps(selected, indent=2) + "\n"
    print(summary)
    with Path(os.environ["GITHUB_OUTPUT"]).open("a") as output:
        output.write(f"run_ui_tests={str(decision['run']).lower()}\nsuite={decision['suite']}\nmatrix={json.dumps(matrix)}\n")
    with Path(os.environ["GITHUB_STEP_SUMMARY"]).open("a") as output:
        output.write("### UI test selection\n\n```text\n" + summary.replace("```", "") + "```\n")


if __name__ == "__main__":
    main()
