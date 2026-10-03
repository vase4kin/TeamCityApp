import importlib.util
import io
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch


ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / "scripts/marathon-ci.py"
spec = importlib.util.spec_from_file_location("marathon_ci", SCRIPT)
ci = importlib.util.module_from_spec(spec)
spec.loader.exec_module(ci)


class MarathonPolicyTest(unittest.TestCase):
    def test_same_repository_pr_is_eligible_for_changed_selection_regardless_of_title(self):
        for title in ("chore: update docs", "chore(ci): tune CI", "fix: correct login", "feat: add screen"):
            for action in ("opened", "synchronize", "reopened"):
                with self.subTest(title=title, action=action):
                    result = ci.policy("pull_request", "refs/pull/389/merge", {"action": action, "pull_request": {"title": title}})
                    self.assertTrue(result["run"])
                    self.assertEqual(result["suite"], "changed")

    def test_fork_prs_skip_paid_tests_because_the_secret_is_unavailable(self):
        event = {"repository": {"full_name": "owner/app"}, "pull_request": {"head": {"repo": {"full_name": "fork/app"}}}}
        self.assertFalse(ci.policy("pull_request", "refs/pull/1/merge", event)["run"])

    def test_default_branch_push_always_runs_both_full_suites(self):
        event = {"repository": {"default_branch": "dev"}, "head_commit": {"message": "chore(ci): update checks"}}
        self.assertEqual(ci.policy("push", "refs/heads/dev", event)["suite"], "full")
        self.assertFalse(ci.policy("push", "refs/heads/feature", event)["run"])

    def test_dispatch_checkbox_overrides_chore_title_for_every_suite(self):
        for checked in (True, "true"):
            for suite in ("full", "smoke", "single", "random"):
                event = {"inputs": {"run_ui_tests": checked, "suite": suite}, "pull_request": {"title": "chore(ci): update checks"}}
                result = ci.policy("workflow_dispatch", "refs/heads/codex/optimize-ci", event)
                self.assertTrue(result["run"])
                self.assertEqual(result["suite"], suite)

    def test_dispatch_without_override_runs_changed_and_smoke(self):
        for inputs in ({}, {"run_ui_tests": False}, {"run_ui_tests": "false", "suite": "full"}):
            result = ci.policy("workflow_dispatch", "refs/heads/dev", {"inputs": inputs})
            self.assertTrue(result["run"])
            self.assertEqual(result["suite"], "changed")
        with self.assertRaisesRegex(ValueError, "Unknown UI suite"):
            ci.policy("workflow_dispatch", "refs/heads/dev", {"inputs": {"run_ui_tests": True, "suite": "typo"}})


class MarathonSelectionTest(unittest.TestCase):
    def setUp(self):
        self.catalog = json.loads((ROOT / "scripts/marathon-tests.json").read_text())
        self.available = ci.source_tests(ROOT)

    def test_catalog_contains_only_current_nonignored_tests(self):
        for suite, tests in self.catalog.items():
            with self.subTest(suite=suite):
                self.assertTrue(tests)
                self.assertEqual(len(tests), len(set(tests)))
                self.assertFalse(set(tests) - self.available)
        self.assertTrue(set(self.catalog["smoke"]) <= set(self.catalog["random"]))

    def test_random_selects_one_test_and_is_repeatable(self):
        result = ci.selection("random", self.catalog, self.available, seed="repeat-me")
        self.assertEqual(len(result["tests"]), 1)
        self.assertIn(result["tests"][0], self.catalog["random"])
        self.assertEqual(result, ci.selection("random", self.catalog, self.available, seed="repeat-me"))
        choices = {ci.selection("random", self.catalog, self.available, seed=str(seed))["tests"][0] for seed in range(100)}
        self.assertEqual(choices, set(self.catalog["random"]))

    def test_smoke_is_explicit_and_full_is_unfiltered(self):
        self.assertEqual(ci.selection("smoke", self.catalog, self.available)["tests"], self.catalog["smoke"])
        self.assertEqual(ci.selection("full", self.catalog, self.available)["tests"], [])

    def test_single_can_select_outside_the_smoke_pool(self):
        test = "com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivityTest#testUserCanOpenFilterBuilds"
        self.assertNotIn(test, self.catalog["smoke"])
        self.assertEqual(ci.selection("single", self.catalog, self.available, test)["tests"], [test])

    def test_invalid_or_ignored_selection_fails_before_cloud_launch(self):
        for test in ("", "$(touch /tmp/no)", "example.Missing#test", "teamcityapp.features.splash.view.SplashActivityTest#testUserNavigatesToLoginActivityIgnored", "com.github.vase4kin.teamcityapp.agents.view.AgentListFragmentTest#testUserCanSeeUpdatedToolbar"):
            with self.subTest(test=test), self.assertRaisesRegex(ValueError, "Unknown or ignored"):
                ci.selection("single", self.catalog, self.available, test)
        with self.assertRaisesRegex(ValueError, "catalog"):
            ci.selection("smoke", {"smoke": []}, self.available)

    def test_source_preflight_preserves_ignore_urls_and_throws_annotations(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "app/src/androidTest"
            source.mkdir(parents=True)
            (source / "Ignored.kt").write_text('package example\n@Ignore("https://example.com/issue")\n@RunWith(AndroidJUnit4::class)\nclass Ignored {\n@Test fun skipped() {}\n}')
            (source / "Active.kt").write_text('package example\nclass Active {\n@Test\n@Throws(Exception::class)\nfun active() {}\n@Ignore("https://example.com/issue")\n@Test fun skipped() {}\n}')
            self.assertEqual(ci.source_tests(root), {"example.Active#active"})

    def test_policy_command_records_filter_seed_and_matrix(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            event = root / "event.json"
            event.write_text(json.dumps({"inputs": {"run_ui_tests": "true", "suite": "random", "random_seed": "repeat-me"}}))
            env = dict(os.environ, GITHUB_EVENT_PATH=str(event), GITHUB_EVENT_NAME="workflow_dispatch", GITHUB_REF="refs/heads/example", GITHUB_OUTPUT=str(root / "outputs"), GITHUB_STEP_SUMMARY=str(root / "summary"))
            subprocess.run([sys.executable, str(SCRIPT), "policy", "--selection-dir", str(root / "selection")], env=env, check=True, capture_output=True)
            outputs = dict(line.split("=", 1) for line in (root / "outputs").read_text().splitlines())
            self.assertEqual(outputs["run_ui_tests"], "true")
            self.assertEqual(outputs["collect_coverage"], "true")
            self.assertEqual([job["label"] for job in json.loads(outputs["matrix"])["include"]], ["instrumentation"])
            selected = json.loads((root / "selection/selection.json").read_text())
            self.assertEqual(selected["seed"], "repeat-me")
            self.assertIn(json.dumps(selected["tests"][0]), (root / "selection/filter.yaml").read_text())
            self.assertIn(selected["tests"][0], (root / "summary").read_text())


class ChangedSelectionTest(unittest.TestCase):
    def setUp(self):
        self.catalog = json.loads((ROOT / "scripts/marathon-tests.json").read_text())
        self.mapping = json.loads((ROOT / "scripts/marathon-changes.json").read_text())
        self.index = ci.source_test_index(ROOT)
        self.available = set().union(*self.index.values())
        self.smoke = set(self.catalog["smoke"])

    def select(self, *paths):
        return ci.changed_selection(list(paths), self.catalog, self.available, self.index, self.mapping)

    def test_ci_only_changes_skip_ui_tests_even_with_documentation(self):
        result = self.select("README.md", "build-logic/README.md", ".github/workflows/build.yml", "scripts/marathon-ci.py", "scripts/tests/test_marathon_ci.py", "scripts/marathon-tests.json", "codecov.yml")
        self.assertEqual(result["tests"], [])
        self.assertEqual(result["fallback_paths"], [])

    def test_repository_housekeeping_selects_no_ui_tests(self):
        self.assertEqual(self.select(".gitignore", "LICENSE", "res/screenshots/feature-graphic.png")["tests"], [])

    def test_documentation_only_and_empty_diffs_select_no_tests(self):
        for paths in ((), ("README.md",), ("README.md", "build-logic/README.md")):
            with self.subTest(paths=paths):
                result = self.select(*paths)
                self.assertEqual(result["tests"], [])
                self.assertEqual(result["changed_files"], list(paths))
                self.assertEqual(result["fallback_paths"], [])

    def test_ci_and_documentation_do_not_suppress_tests_for_code_changes(self):
        path = "app/src/main/java/com/github/vase4kin/teamcityapp/buildlog/view/BuildLogFragment.kt"
        self.assertEqual(self.select("README.md", ".github/workflows/build.yml", "scripts/marathon-ci.py", path)["tests"], self.select(path)["tests"])

    def test_changed_policy_outputs_skip_ci_documentation_and_empty_diffs(self):
        for name in ("pull_request", "workflow_dispatch"):
            for paths in ([], ["README.md"], ["README.md", "build-logic/README.md"], [".github/workflows/build.yml"], ["build-logic/README.md", "scripts/marathon-ci.py", "scripts/tests/test_marathon_ci.py"]):
                with self.subTest(name=name, paths=paths), tempfile.TemporaryDirectory() as directory:
                    root = Path(directory)
                    event = root / "event.json"
                    event.write_text(json.dumps({"repository": {"full_name": "owner/app"}, "pull_request": {"head": {"repo": {"full_name": "owner/app"}}}}))
                    env = dict(os.environ, GITHUB_EVENT_PATH=str(event), GITHUB_EVENT_NAME=name, GITHUB_REF="refs/pull/1/merge", GITHUB_OUTPUT=str(root / "output"), GITHUB_STEP_SUMMARY=str(root / "summary"))
                    with patch.dict(os.environ, env), patch.object(sys, "argv", [str(SCRIPT), "policy", "--selection-dir", str(root / "selection")]), patch.object(ci, "changed_paths", return_value=paths), patch("sys.stdout", new_callable=io.StringIO):
                        ci.main()
                    outputs = dict(line.split("=", 1) for line in (root / "output").read_text().splitlines())
                    self.assertEqual(outputs["run_ui_tests"], "false")
                    self.assertEqual(outputs["suite"], "none")
                    self.assertEqual(outputs["collect_coverage"], "false")
                    self.assertEqual(json.loads(outputs["matrix"]), {"include": []})
                    self.assertFalse((root / "selection").exists())
                    self.assertIn("No application, Android test, or build inputs changed", (root / "summary").read_text())
                    for path in paths:
                        self.assertIn(json.dumps(path), (root / "summary").read_text())

    def test_changed_policy_keeps_smoke_for_code_changes(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            event = root / "event.json"
            event.write_text(json.dumps({"pull_request": {}}))
            env = dict(os.environ, GITHUB_EVENT_PATH=str(event), GITHUB_EVENT_NAME="pull_request", GITHUB_REF="refs/pull/1/merge", GITHUB_OUTPUT=str(root / "output"), GITHUB_STEP_SUMMARY=str(root / "summary"))
            paths = ["README.md", "app/src/main/java/com/github/vase4kin/teamcityapp/buildlog/view/BuildLogFragment.kt"]
            with patch.dict(os.environ, env), patch.object(sys, "argv", [str(SCRIPT), "policy", "--selection-dir", str(root / "selection")]), patch.object(ci, "changed_paths", return_value=paths), patch("sys.stdout", new_callable=io.StringIO):
                ci.main()
            outputs = dict(line.split("=", 1) for line in (root / "output").read_text().splitlines())
            self.assertEqual(outputs["run_ui_tests"], "true")
            self.assertEqual(outputs["suite"], "changed")
            self.assertEqual(outputs["collect_coverage"], "false")
            self.assertEqual([job["label"] for job in json.loads(outputs["matrix"])["include"]], ["instrumentation"])
            selected = json.loads((root / "selection/selection.json").read_text())
            self.assertEqual(selected["tests"], self.select(*paths)["tests"])

    def test_build_log_sources_resources_and_mock_data_select_screen_and_lifecycle(self):
        expected = self.smoke | {
            "com.github.vase4kin.teamcityapp.buildlog.view.BuildLogFragmentTest#testUserCanSeeBuildLog",
            "com.github.vase4kin.teamcityapp.hilt.BuildLogViewLifecycleTest#sameFragmentCanCreateAndDestroyItsViewRepeatedly",
        }
        for path in ("app/src/main/java/com/github/vase4kin/teamcityapp/buildlog/view/BuildLogFragment.kt", "app/src/main/res/layout/fragment_build_log.xml", "app/src/mock/assets/fake_build_log.html"):
            with self.subTest(path=path):
                result = self.select(path)
                self.assertEqual(set(result["tests"]), expected)
                self.assertEqual(result["fallback_paths"], [])

    def test_changed_test_file_selects_every_active_method_in_that_class(self):
        path = "app/src/androidTest/java/com/github/vase4kin/teamcityapp/buildlist/view/BuildListActivityTest.kt"
        self.assertEqual(set(self.select(path)["tests"]), self.smoke | self.index[path])

    def test_new_methods_enter_changed_selection_without_catalog_updates(self):
        path = "app/src/androidTest/java/example/NewFeatureTest.kt"
        methods = {"example.NewFeatureTest#first", "example.NewFeatureTest#second"}
        result = ci.changed_selection([path], self.catalog, self.available | methods, {**self.index, path: methods}, self.mapping)
        self.assertEqual(set(result["tests"]), self.smoke | methods)

    def test_migrated_module_includes_cross_package_injection_tests(self):
        result = self.select("features/about/feature/src/main/java/teamcityapp/features/about/AboutActivity.kt")
        self.assertIn("com.github.vase4kin.teamcityapp.hilt.HiltMigrationSmokeTest#aboutInjectsAndRecreates", result["tests"])
        self.assertTrue(any("AboutActivityTest#" in test for test in result["tests"]))
        self.assertEqual(result["fallback_paths"], [])

    def test_shared_unmapped_and_helper_changes_broaden_mock_selection(self):
        for path in ("libraries/storage/src/main/java/Store.kt", "app/src/main/java/com/github/vase4kin/teamcityapp/storage/SharedUserStorage.kt", "app/src/main/res/values/strings.xml", "app/src/main/java/com/github/vase4kin/teamcityapp/new_feature/View.kt", "app/src/androidTest/java/com/github/vase4kin/teamcityapp/helper/HiltApiTestRule.kt", "build-logic/src/main/kotlin/AndroidBaseConventionPlugin.kt", "gradle/libs.versions.toml", "gradle/wrapper/gradle-wrapper.properties", "build.gradle.kts", "settings.gradle.kts", "gradle.properties", "gradlew", "gradlew.bat", "mock-mockDebug-google-services.json", "mock-prodDebug-google-services.json"):
            with self.subTest(path=path):
                result = self.select(path)
                self.assertEqual(set(result["tests"]), self.available)
                self.assertEqual(result["suite"], "changed")
                self.assertEqual(result["fallback_paths"], [path])

    def test_combining_changes_deduplicates_smoke_and_feature_tests(self):
        paths = ("app/src/main/java/com/github/vase4kin/teamcityapp/home/view/HomeActivity.kt", "app/src/main/res/layout/activity_home.xml")
        result = self.select(*paths)
        self.assertEqual(len(result["tests"]), len(set(result["tests"])))
        self.assertTrue(self.smoke <= set(result["tests"]))
        self.assertIn("com.github.vase4kin.teamcityapp.hilt.PresenterReplacementTest#homeDisposesOldPresenterBeforeAccountReload", result["tests"])

    def test_mapping_exceptions_reference_current_tests(self):
        for owner, rule in self.mapping.items():
            for pattern in rule.get("tests", []):
                with self.subTest(owner=owner, pattern=pattern):
                    self.assertTrue(any(ci.fnmatch.fnmatchcase(test, pattern) for test in self.available))

    def test_pr_diff_uses_complete_branch_and_keeps_both_rename_paths(self):
        event = {"pull_request": {"base": {"sha": "base"}, "head": {"sha": "head"}}}
        completed = subprocess.CompletedProcess([], 0, stdout=b"old/View.kt\0new/View.kt\0")
        with patch.object(ci.subprocess, "run", return_value=completed) as run:
            self.assertEqual(ci.changed_paths(ROOT, "pull_request", event), ["old/View.kt", "new/View.kt"])
        self.assertIn("base...head", run.call_args.args[0])
        self.assertIn("--no-renames", run.call_args.args[0])

    def test_explicit_smoke_command_collects_no_device_coverage(self):
        events = [
            ("workflow_dispatch", "refs/heads/example", {"inputs": {"run_ui_tests": "true", "suite": "smoke"}}),
        ]
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for i, (name, ref, payload) in enumerate(events):
                event = root / f"event-{i}.json"
                event.write_text(json.dumps(payload))
                env = dict(os.environ, GITHUB_EVENT_PATH=str(event), GITHUB_EVENT_NAME=name, GITHUB_REF=ref, GITHUB_OUTPUT=str(root / f"output-{i}"), GITHUB_STEP_SUMMARY=str(root / f"summary-{i}"))
                subprocess.run([sys.executable, str(SCRIPT), "policy", "--selection-dir", str(root / f"selection-{i}")], env=env, check=True, capture_output=True)
                outputs = dict(line.split("=", 1) for line in (root / f"output-{i}").read_text().splitlines())
                matrix = json.loads(outputs["matrix"])["include"]
                self.assertEqual(outputs["collect_coverage"], "false")
                self.assertEqual(len(matrix), 1)
                self.assertFalse(matrix[0]["coverage"])
                selected = json.loads((root / f"selection-{i}/selection.json").read_text())
                self.assertEqual(set(selected["tests"]), self.smoke)

    def test_missing_diff_baseline_broadens_selection_without_enabling_coverage(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            event = root / "event.json"
            event.write_text(json.dumps({"pull_request": {"base": {"sha": "missing-base"}, "head": {"sha": "HEAD"}}}))
            env = dict(os.environ, GITHUB_EVENT_PATH=str(event), GITHUB_EVENT_NAME="pull_request", GITHUB_REF="refs/pull/1/merge", GITHUB_OUTPUT=str(root / "output"), GITHUB_STEP_SUMMARY=str(root / "summary"))
            subprocess.run([sys.executable, str(SCRIPT), "policy", "--selection-dir", str(root / "selection")], env=env, check=True, capture_output=True)
            selected = json.loads((root / "selection/selection.json").read_text())
            self.assertEqual(set(selected["tests"]), self.available)
            self.assertIn("fallback_reason", selected)
            self.assertIn("collect_coverage=false", (root / "output").read_text())


class MarathonResultTest(unittest.TestCase):
    def verify(self, xml, tests=(), label="instrumentation"):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "junit.xml").write_text(xml)
            return ci.verify_results(root, {"tests": list(tests)}, label)

    def test_zero_tests_skipped_tests_and_failures_do_not_pass(self):
        for xml in ('<testsuite/>', '<testsuite><testcase classname="Test" name="one"><skipped/></testcase></testsuite>', '<testsuite><testcase classname="Test" name="one"><failure/></testcase></testsuite>'):
            with self.subTest(xml=xml), self.assertRaises(ValueError):
                self.verify(xml)

    def test_every_selected_test_must_run_and_no_extra_test_may_run(self):
        xml = '<testsuite><testcase classname="Test" name="one"/></testsuite>'
        self.assertEqual(self.verify(xml, ["Test#one"]), 1)
        for tests in (["Test#two"], ["Test#one", "Test#two"]):
            with self.assertRaisesRegex(ValueError, "selection mismatch"):
                self.verify(xml, tests)

    def test_r8_requires_all_five_smoke_tests(self):
        cases = ''.join(f'<testcase classname="R8" name="test{i}"/>' for i in range(5))
        self.assertEqual(self.verify('<testsuite>' + cases + '</testsuite>', label="r8"), 5)
        with self.assertRaises(ValueError):
            self.verify('<testsuite><testcase classname="R8" name="one"/></testsuite>', label="r8")


if __name__ == "__main__":
    unittest.main()
