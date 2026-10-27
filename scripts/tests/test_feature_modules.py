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

"""Exercise the feature module boundary checks against representative regressions."""
import importlib.util
from pathlib import Path
import tempfile
import unittest


SPEC = importlib.util.spec_from_file_location("feature_modules", Path(__file__).resolve().parents[1] / "check-feature-modules.py")
guard = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(guard)


class FeatureModulesTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.write("settings.gradle.kts", 'include(":features:about:api")\ninclude(":features:about:impl")')
        self.write("features/about/api/build.gradle.kts", 'plugins { id("teamcityapp.kmp.library") }')
        self.write("features/about/impl/build.gradle.kts", 'plugins { id("teamcityapp.kmp.compose") }')
        self.write("shared-app/build.gradle.kts", "implementation(projects.features.about.impl)")
        self.write("features/about/impl/src/commonMain/kotlin/About.kt", "class About")

    def write(self, path, content):
        destination = self.root / path
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_text(content)

    def test_shared_feature_and_composition_root_are_allowed(self):
        self.write("features/about/impl/src/androidMain/kotlin/Port.kt", "import android.app.Activity")
        self.write("features/about/impl/src/test/screenshots/debug/state.png", "retained fixture")
        self.assertEqual([], guard.validate(self.root))

    def test_feature_owned_analytics_and_root_wiring_are_allowed(self):
        self.write("features/about/impl/src/commonMain/kotlin/tracker/AboutTrackerImpl.kt",
                   'class AboutTrackerImpl(private val analytics: AnalyticsTracker) : AboutTracker { fun viewed() = analytics.track("screen_about") }')
        self.write("shared-app/src/commonMain/kotlin/Root.kt", 'val tracker = AboutTrackerImpl(platform.analytics)')
        self.assertEqual([], guard.validate(self.root))

    def test_tracker_implementations_and_event_mappings_cannot_return_to_the_root(self):
        self.write("shared-app/src/commonMain/kotlin/Trackers.kt",
                   'class AboutTrackerImpl : AboutTracker { fun viewed() = analytics.track("screen_about") }')
        errors = guard.validate(self.root)
        self.assertTrue(any("tracker implementations belong" in e for e in errors))
        self.assertTrue(any("event mappings belong" in e for e in errors))

    def test_feature_android_sources_cannot_depend_on_the_analytics_sdk(self):
        self.write("features/about/impl/src/androidMain/kotlin/Tracker.kt", 'import com.google.firebase.analytics.FirebaseAnalytics')
        self.assertTrue(any("analytics SDKs belong" in e for e in guard.validate(self.root)))

    def test_app_navigation_cannot_own_an_analytics_sdk_backend(self):
        self.write("app/src/main/kotlin/navigation/AndroidTracker.kt", 'import com.google.firebase.analytics.FirebaseAnalytics')
        self.assertTrue(any("analytics SDKs belong" in e for e in guard.validate(self.root)))

    def test_third_feature_module_and_stale_include_are_rejected(self):
        self.write("features/about/presentation/build.gradle.kts", "")
        self.assertTrue(any("expected only api/impl" in e for e in guard.validate(self.root)))
        self.write("settings.gradle.kts", 'include(":features:about:presentation")')
        (self.root / "features/about/presentation/build.gradle.kts").unlink()
        self.assertTrue(any("missing feature module" in e for e in guard.validate(self.root)))

    def test_multiple_includes_cannot_hide_a_stale_module(self):
        self.write("settings.gradle.kts", 'include(":features:about:api", ":features:about:impl", ":features:about:presentation")')
        self.assertTrue(any("missing feature module" in e for e in guard.validate(self.root)))

    def test_cross_feature_implementation_dependency_is_rejected(self):
        self.write("features/about/impl/build.gradle.kts", 'id("teamcityapp.kmp.compose")\nimplementation(projects.features.login.impl)')
        self.assertTrue(any("dependencies must use APIs" in e for e in guard.validate(self.root)))

    def test_android_leak_and_unmoved_sources_are_rejected(self):
        self.write("features/about/impl/src/commonMain/kotlin/About.kt", "import android.app.Activity")
        self.write("features/about/impl/src/main/kotlin/Old.kt", "class Old")
        self.write("features/about/impl/src/test/kotlin/OldTest.kt", "class OldTest")
        errors = guard.validate(self.root)
        self.assertTrue(any("platform dependency" in e for e in errors))
        self.assertEqual(2, sum("move src/" in e for e in errors))

    def test_hilt_framework_entry_points_stay_in_the_host(self):
        self.write("features/about/impl/src/androidMain/kotlin/Port.kt", "@AndroidEntryPoint class AboutActivity")
        self.assertTrue(any("entry points belong" in e for e in guard.validate(self.root)))

    def test_new_features_cannot_silently_become_android_only(self):
        self.write("features/about/impl/build.gradle.kts", 'id("teamcityapp.android.library")')
        self.assertTrue(any("must use KMP Compose" in e for e in guard.validate(self.root)))

    def test_root_cannot_own_feature_resources_or_paging(self):
        for body in ("import teamcityapp.features.login.impl.resources.Res", "val rows = collectAsLazyPagingItems()", "val state = LoginUiState()"):
            with self.subTest(body=body):
                self.write("shared-app/src/commonMain/kotlin/FeatureLeak.kt", body)
                self.assertTrue(any("feature routes" in e for e in guard.validate(self.root)))

    def test_root_cannot_own_screen_lifecycle_callbacks(self):
        self.write("shared-app/src/commonMain/kotlin/FeatureLeak.kt", "LifecycleResumeEffect(vm) { vm.onScreenViewed() }")
        self.assertTrue(any("screen lifecycle" in e for e in guard.validate(self.root)))

    def test_root_can_compose_routes_and_deliver_navigation_results(self):
        self.write("shared-app/src/commonMain/kotlin/Entry.kt", "val vm = viewModel { LoginViewModel(repository, tracker) }; SharedLoginRoute(vm); vm.applyFilter(result.filter)")
        self.assertEqual([], guard.validate(self.root))

    def test_android_host_rejects_feature_resources_and_presentation(self):
        for body in ("import teamcityapp.features.about.impl.R", "val title = teamcityapp.features.about.impl.R.string.title", "val state = LoginUiState()"):
            with self.subTest(body=body):
                self.write("app/src/main/kotlin/Leak.kt", body)
                self.assertTrue(any("feature" in e for e in guard.validate(self.root)))

    def test_android_host_rejects_persistence_and_migration_implementations(self):
        for body in ("class AndroidFiles(context: Context) : ArtifactFileStore", "class AndroidCache : ResponseCacheStore", "val preferences = AccountPreferences()"):
            with self.subTest(body=body):
                self.write("app/src/main/kotlin/Leak.kt", body)
                self.assertTrue(any("persistence" in e or "preference migration" in e for e in guard.validate(self.root)))

    def test_android_host_rejects_feature_copy_but_keeps_app_identity(self):
        self.write("app/src/main/res/values/strings.xml", '<resources><string name="app_name">TeamCityApp</string></resources>')
        self.assertEqual([], guard.validate(self.root))
        self.write("app/src/main/res/values/strings.xml", '<resources><string name="build_title">Build</string></resources>')
        self.assertTrue(any("feature copy" in e for e in guard.validate(self.root)))

    def test_android_host_can_wire_library_and_feature_adapters(self):
        self.write("app/src/main/kotlin/Host.kt", "class HostModel @Inject constructor(cache: ResponseCacheStore) : SharedAppViewModel(); val cache = AndroidResponseCacheStore(context); openSourceLibraries(activity)")
        self.assertEqual([], guard.validate(self.root))

    def test_obsolete_root_dependency_is_rejected(self):
        self.write("shared-app/build.gradle.kts", "implementation(projects.features.about.presentation)")
        self.assertTrue(any("obsolete presentation" in e for e in guard.validate(self.root)))


if __name__ == "__main__":
    unittest.main()
