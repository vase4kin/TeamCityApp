import importlib.util
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / "scripts/marathon-ci.py"
spec = importlib.util.spec_from_file_location("marathon_ci", SCRIPT)
ci = importlib.util.module_from_spec(spec)
spec.loader.exec_module(ci)


class MarathonPolicyTest(unittest.TestCase):
    def test_prs_never_launch_paid_tests_automatically(self):
        for title in ("chore: update docs", "chore(ci): tune CI", "fix: correct login", "feat: add screen"):
            for action in ("opened", "synchronize", "reopened"):
                with self.subTest(title=title, action=action):
                    result = ci.policy("pull_request", "refs/pull/389/merge", {"action": action, "pull_request": {"title": title}})
                    self.assertFalse(result["run"])
                    self.assertEqual(result["suite"], "none")

    def test_only_semantic_chore_type_is_identified_as_chore(self):
        for title, chore in (("chore(ci): update checks", True), ("chore: cleanup", True), ("chore!: change", True), ("choreography: change", False)):
            result = ci.policy("pull_request", "refs/pull/1/merge", {"pull_request": {"title": title}})
            self.assertEqual(result["reason"].startswith("Chore PR"), chore)

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

    def test_dispatch_is_opt_in(self):
        for inputs in ({}, {"run_ui_tests": False}, {"run_ui_tests": "false", "suite": "full"}):
            self.assertFalse(ci.policy("workflow_dispatch", "refs/heads/dev", {"inputs": inputs})["run"])
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
            self.assertEqual([job["label"] for job in json.loads(outputs["matrix"])["include"]], ["instrumentation"])
            selected = json.loads((root / "selection/selection.json").read_text())
            self.assertEqual(selected["seed"], "repeat-me")
            self.assertIn(json.dumps(selected["tests"][0]), (root / "selection/filter.yaml").read_text())
            self.assertIn(selected["tests"][0], (root / "summary").read_text())


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
