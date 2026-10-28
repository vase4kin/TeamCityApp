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

"""Coverage transport and complete test partition regression checks."""
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
import zipfile


spec = importlib.util.spec_from_file_location("unit_test_ci", Path(__file__).resolve().parents[1] / "unit-test-ci.py")
ci = importlib.util.module_from_spec(spec)
spec.loader.exec_module(ci)


class UnitTestCiTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name) / "producer"
        self.root.mkdir()
        self.names = [":app", ":libraries:shared"] + [f":features:screen-{i}:impl" for i in range(6)]
        self.write(self.root, "settings.gradle.kts", "\n".join(f'include("{name}")' for name in self.names))
        for name in self.names:
            module = name[1:].replace(":", "/")
            kind = "application" if name == ":app" else "library"
            self.write(self.root, f"{module}/build.gradle.kts", f'plugins {{ id("teamcityapp.android.{kind}") }}\nid("teamcityapp.android.screenshot")')
            self.write(self.root, f"{module}/src/main/kotlin/Screen.kt", "class Screen")
            if name != ":libraries:shared":
                self.write(self.root, f"{module}/src/test/kotlin/ScreenTest.kt", "@RunWith(RobolectricTestRunner::class) class ScreenTest")
            variant = "mockDebug" if name == ":app" else "debug"
            self.write(self.root, f"{module}/build/intermediates/built_in_kotlinc/{variant}/compile{variant[0].upper() + variant[1:]}Kotlin/classes/Screen.class", name)
            self.write(self.root, f"{module}/build/jacoco/test.exec", name + " coverage")
        self.inputs = Path(self.temporary.name) / "inputs"
        self.inputs.mkdir()

    @staticmethod
    def write(root, name, text):
        path = root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text)
        return path

    def consumer(self):
        root = Path(self.temporary.name) / "consumer"
        root.mkdir(exist_ok=True)
        self.write(root, "settings.gradle.kts", (self.root / "settings.gradle.kts").read_text())
        for path in self.root.rglob("*"):
            if path.is_file() and "build" not in path.relative_to(self.root).parts:
                self.write(root, path.relative_to(self.root), path.read_text())
        return root

    def pack_all(self):
        for shard in range(ci.SHARD_COUNT):
            ci.pack(self.root, shard, self.inputs / f"shard-{shard}.zip")

    def rewrite_archive(self, shard, change):
        path = self.inputs / f"shard-{shard}.zip"
        with zipfile.ZipFile(path) as archive:
            contents = {name: archive.read(name) for name in archive.namelist()}
        change(contents)
        with zipfile.ZipFile(path, "w") as archive:
            for name, data in contents.items():
                archive.writestr(name, data)

    def test_every_module_and_screenshot_task_runs_exactly_once(self):
        shards = ci.plan(self.root)["include"]
        selected = [module for shard in shards for module in shard["modules"]]
        tasks = [task for shard in shards for task in shard["tasks"].split()]
        self.assertCountEqual(selected, self.names + [":build-logic"])
        self.assertEqual(len(tasks), len(set(tasks)))
        self.assertIn(":app:testMockDebugUnitTest", tasks)
        self.assertIn(":app:verifyRoborazziMockDebug", tasks)
        self.assertIn(":features:screen-0:impl:verifyRoborazziDebug", tasks)
        expected = [":build-logic:test"]
        for module in self.names:
            variant = "MockDebug" if module == ":app" else "Debug"
            expected.extend([f"{module}:test{variant}UnitTest", f"{module}:verifyRoborazzi{variant}"])
        self.assertCountEqual(tasks, expected)

    def test_new_modules_are_automatically_included(self):
        with (self.root / "settings.gradle.kts").open("a") as settings:
            settings.write('\ninclude(":libraries:new")')
        self.write(self.root, "libraries/new/build.gradle.kts", 'id("teamcityapp.android.library.java")')
        tasks = " ".join(s["tasks"] for s in ci.plan(self.root)["include"])
        self.assertIn(":libraries:new:testDebugUnitTest", tasks)
        self.assertNotIn(":libraries:new:verifyRoborazziDebug", tasks)

    def test_unknown_conventions_or_variants_fail_instead_of_skipping(self):
        for text in ('id("java-library")', 'id("teamcityapp.android.library")\nproductFlavors {}'):
            self.write(self.root, "libraries/shared/build.gradle.kts", text)
            with self.assertRaisesRegex(ValueError, "Unrecognized test"):
                ci.plan(self.root)

    def test_kmp_common_tests_are_scheduled_and_coverage_round_trips(self):
        with (self.root / "settings.gradle.kts").open("a") as settings:
            settings.write('\ninclude(":features:about:impl")')
        module = "features/about/impl"
        self.write(self.root, f"{module}/build.gradle.kts", 'id("teamcityapp.kmp.compose")')
        self.write(self.root, f"{module}/src/commonTest/kotlin/AboutTest.kt", "class AboutTest")
        self.write(self.root, f"{module}/build/classes/kotlin/android/main/About.class", "shared")
        self.write(self.root, f"{module}/build/jacoco/testAndroidHostTest.exec", "shared coverage")
        tasks = " ".join(s["tasks"] for s in ci.plan(self.root)["include"])
        self.assertIn(":features:about:impl:testAndroidHostTest", tasks)
        self.assertNotIn(":features:about:impl:testDebugUnitTest", tasks)
        self.pack_all()
        consumer = self.consumer()
        ci.restore(consumer, self.inputs)
        self.assertEqual([p.read_text() for p in ci.coverage_paths(consumer, ":features:about:impl")], ["shared"])

    def test_kmp_screenshot_modules_schedule_the_host_golden_task(self):
        self.write(self.root, "libraries/shared/build.gradle.kts",
                   'id("teamcityapp.kmp.compose")\nid("teamcityapp.android.screenshot")')
        self.write(self.root, "libraries/shared/src/androidHostTest/kotlin/ScreenTest.kt", "class ScreenTest")
        tasks = " ".join(s["tasks"] for s in ci.plan(self.root)["include"])
        self.assertIn(":libraries:shared:testAndroidHostTest", tasks)
        self.assertIn(":libraries:shared:verifyRoborazziAndroidHostTest", tasks)
        self.assertNotIn(":libraries:shared:verifyRoborazziDebug", tasks)

    def test_kmp_tests_require_execution_data_in_coverage_transport(self):
        self.write(self.root, "libraries/shared/build.gradle.kts", 'id("teamcityapp.kmp.library")')
        self.write(self.root, "libraries/shared/src/commonTest/kotlin/SharedTest.kt", "class SharedTest")
        (self.root / "libraries/shared/build/jacoco/test.exec").unlink()
        shard = next(s["shard"] for s in ci.plan(self.root)["include"] if ":libraries:shared" in s["modules"])
        with self.assertRaisesRegex(ValueError, "Missing unit coverage execution"):
            ci.pack(self.root, shard, self.inputs / "shared.zip")

    def test_coverage_round_trip_preserves_classes_and_execution_data(self):
        self.pack_all()
        consumer = self.consumer()
        ci.restore(consumer, self.inputs)
        for name in self.names:
            self.assertEqual([p.read_bytes() for p in ci.coverage_paths(consumer, name)], [p.read_bytes() for p in ci.coverage_paths(self.root, name)])
            self.assertEqual([p.read_bytes() for p in ci.execution_paths(consumer, name)], [p.read_bytes() for p in ci.execution_paths(self.root, name)])

    def harness_only_module(self, convention, test_source_set):
        module = "libraries/shared"
        self.write(self.root, f"{module}/build.gradle.kts", f'id("{convention}")')
        (self.root / f"{module}/src/main/kotlin/Screen.kt").unlink()
        (self.root / f"{module}/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes/Screen.class").unlink()
        self.write(self.root, f"{module}/src/{test_source_set}/kotlin/ScreenTest.kt", "class ScreenTest")

    def assert_harness_only_round_trip(self):
        self.pack_all()
        consumer = self.consumer()
        ci.restore(consumer, self.inputs)
        self.assertEqual([], list(ci.coverage_paths(consumer, ":libraries:shared")))
        self.assertEqual([b":libraries:shared coverage"], [p.read_bytes() for p in ci.execution_paths(consumer, ":libraries:shared")])

    def test_android_harness_only_module_keeps_tests_and_execution_without_production_classes(self):
        self.harness_only_module("teamcityapp.android.library", "test")
        self.assert_harness_only_round_trip()

    def test_kmp_harness_only_module_keeps_tests_and_execution_without_production_classes(self):
        self.harness_only_module("teamcityapp.kmp.library", "androidHostTest")
        self.assert_harness_only_round_trip()

    def test_harness_only_module_still_requires_execution_data(self):
        self.harness_only_module("teamcityapp.android.library", "test")
        (self.root / "libraries/shared/build/jacoco/test.exec").unlink()
        shard = next(s["shard"] for s in ci.plan(self.root)["include"] if ":libraries:shared" in s["modules"])
        with self.assertRaisesRegex(ValueError, "Missing unit coverage execution"):
            ci.pack(self.root, shard, self.inputs / "shared.zip")

    def test_kmp_production_sources_still_require_compiled_classes(self):
        self.harness_only_module("teamcityapp.kmp.library", "androidHostTest")
        shard = next(s["shard"] for s in ci.plan(self.root)["include"] if ":libraries:shared" in s["modules"])
        for source in ("commonMain/kotlin/Shared.kt", "androidMain/java/Shared.java"):
            with self.subTest(source=source):
                path = self.write(self.root, f"libraries/shared/src/{source}", "class Shared {}")
                with self.assertRaisesRegex(ValueError, "Missing compiled coverage"):
                    ci.pack(self.root, shard, self.inputs / "shared.zip")
                path.unlink()

    def test_compiled_dependencies_are_included_without_unowned_test_data(self):
        ci.pack(self.root, 0, self.inputs / "shard-0.zip")
        shard = ci.plan(self.root)["include"][0]
        with zipfile.ZipFile(self.inputs / "shard-0.zip") as archive:
            self.assertIn("libraries/shared/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes/Screen.class", archive.namelist())
            for name in self.names:
                execution = name[1:].replace(":", "/") + "/build/jacoco/test.exec"
                self.assertEqual(execution in archive.namelist(), name in shard["modules"])

    def test_transformed_classes_take_precedence_and_prod_data_is_excluded(self):
        self.write(self.root, "app/build/intermediates/classes/mockDebug/transformMockDebugClassesWithAsm/dirs/Screen.class", "transformed")
        self.write(self.root, "app/build/intermediates/built_in_kotlinc/prodDebug/compileProdDebugKotlin/classes/Screen.class", "prod")
        self.write(self.root, "app/build/outputs/unit_test_code_coverage/prodDebugUnitTest/test.exec", "prod")
        self.write(self.root, "app/build/outputs/unit_test_code_coverage/mockDebugUnitTest/test.exec", "mock")
        self.assertEqual([p.read_text() for p in ci.coverage_paths(self.root, ":app")], ["transformed"])
        self.assertEqual(set(p.read_text() for p in ci.execution_paths(self.root, ":app")), {":app coverage", "mock"})

    def test_missing_execution_data_or_classes_fails_before_upload(self):
        shard = next(s["shard"] for s in ci.plan(self.root)["include"] if ":app" in s["modules"])
        (self.root / "app/build/jacoco/test.exec").unlink()
        with self.assertRaisesRegex(ValueError, "Missing unit coverage execution"):
            ci.pack(self.root, shard, self.inputs / "shard.zip")
        self.write(self.root, "app/build/jacoco/test.exec", "coverage")
        (self.root / "app/build/intermediates/built_in_kotlinc/mockDebug/compileMockDebugKotlin/classes/Screen.class").unlink()
        with self.assertRaisesRegex(ValueError, "Missing compiled coverage"):
            ci.pack(self.root, shard, self.inputs / "shard.zip")

    def test_missing_shard_fails_instead_of_uploading_partial_coverage(self):
        self.pack_all()
        (self.inputs / "shard-3.zip").unlink()
        with self.assertRaisesRegex(ValueError, "Expected 4 coverage archives"):
            ci.restore(self.consumer(), self.inputs)

    def test_duplicate_shard_and_changed_plan_are_rejected(self):
        self.pack_all()
        original = (self.inputs / "shard-3.zip").read_bytes()
        (self.inputs / "shard-3.zip").write_bytes((self.inputs / "shard-0.zip").read_bytes())
        with self.assertRaisesRegex(ValueError, "Duplicate or unexpected"):
            ci.restore(self.consumer(), self.inputs)
        (self.inputs / "shard-3.zip").write_bytes(original)
        consumer = self.consumer()
        self.write(consumer, "app/build.gradle.kts", 'id("teamcityapp.android.application")')
        with self.assertRaisesRegex(ValueError, "Duplicate or unexpected"):
            ci.restore(consumer, self.inputs)

    def test_conflicting_class_bytes_are_rejected(self):
        self.pack_all()
        self.rewrite_archive(1, lambda contents: contents.update({"app/build/intermediates/built_in_kotlinc/mockDebug/compileMockDebugKotlin/classes/Screen.class": b"different revision"}))
        with self.assertRaisesRegex(ValueError, "Conflicting coverage input"):
            ci.restore(self.consumer(), self.inputs)

    def test_unsafe_archive_paths_are_rejected(self):
        self.pack_all()
        self.rewrite_archive(0, lambda contents: contents.update({"../build/Bad.class": b"bad"}))
        with self.assertRaisesRegex(ValueError, "Unexpected coverage input"):
            ci.restore(self.consumer(), self.inputs)

    def test_missing_execution_data_in_downloaded_archive_is_rejected(self):
        self.pack_all()
        shard = next(s["shard"] for s in ci.plan(self.root)["include"] if ":app" in s["modules"])
        self.rewrite_archive(shard, lambda contents: contents.pop("app/build/jacoco/test.exec"))
        with self.assertRaisesRegex(ValueError, "Missing unit coverage execution"):
            ci.restore(self.consumer(), self.inputs)

    def test_reordering_settings_preserves_shards(self):
        original = ci.plan(self.root)
        self.write(self.root, "settings.gradle.kts", "\n".join(f'include("{name}")' for name in reversed(self.names)))
        self.assertEqual(ci.plan(self.root), original)


if __name__ == "__main__":
    unittest.main()
