# Copyright 2026 Andrey Tolpeev
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy at http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software distributed
# under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
# CONDITIONS OF ANY KIND, either express or implied. See the License for the
# specific language governing permissions and limitations under the License.

"""Release checks must reject stale metadata and incomplete packaged resources."""
import importlib.util
import plistlib
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location("ios_release", Path(__file__).resolve().parents[1] / "ios-release-audit.py")
audit = importlib.util.module_from_spec(spec)
spec.loader.exec_module(audit)


class IosReleaseAuditTest(unittest.TestCase):
    def manifest(self):
        return {"NSPrivacyTracking": False, "NSPrivacyTrackingDomains": [], "NSPrivacyCollectedDataTypes": [],
                "NSPrivacyAccessedAPITypes": [{"NSPrivacyAccessedAPIType": k, "NSPrivacyAccessedAPITypeReasons": [v]}
                                             for k, v in audit.REASONS.items()]}

    def test_app_scoped_reasons_are_accepted(self):
        self.assertEqual(audit.privacy_errors(self.manifest()), [])

    def test_tracking_or_collection_changes_require_review(self):
        for key, value in [("NSPrivacyTracking", True), ("NSPrivacyTrackingDomains", ["example.test"]),
                           ("NSPrivacyCollectedDataTypes", [{"NSPrivacyCollectedDataType": "Other"}])]:
            manifest = self.manifest()
            manifest[key] = value
            self.assertTrue(audit.privacy_errors(manifest))

    def test_missing_userdefaults_reason_is_rejected(self):
        manifest = self.manifest()
        manifest["NSPrivacyAccessedAPITypes"] = []
        self.assertEqual(len(audit.privacy_errors(manifest)), 3)

    def test_built_app_missing_privacy_and_icons_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            app = Path(directory)
            (app / "Info.plist").write_bytes(plistlib.dumps({"CFBundleExecutable": "app", "CFBundleShortVersionString": "1.0", "MinimumOSVersion": "15.0"}))
            (app / "app").write_bytes(b"binary")
            result = audit.app_audit(app, "2.0.0", "17.0")
            self.assertEqual(len(result["errors"]), 5)
            self.assertFalse(result["embeddedProvisioning"])

    def test_every_xcode_configuration_must_match_the_shared_minimum(self):
        self.assertEqual(audit.deployment_errors("17.0", "IPHONEOS_DEPLOYMENT_TARGET = 17.0;\n" * 6), [])
        for project in ["", "IPHONEOS_DEPLOYMENT_TARGET = 16.0;", "IPHONEOS_DEPLOYMENT_TARGET = 17.0;\nIPHONEOS_DEPLOYMENT_TARGET = 16.0;"]:
            with self.subTest(project=project):
                self.assertTrue(audit.deployment_errors("17.0", project))

    def test_built_app_must_match_the_selected_minimum(self):
        with tempfile.TemporaryDirectory() as directory:
            app = Path(directory)
            (app / "app").write_bytes(b"binary")
            for minimum in ["16.0", "17.0", "18.0"]:
                with self.subTest(minimum=minimum):
                    (app / "Info.plist").write_bytes(plistlib.dumps({"CFBundleExecutable": "app", "CFBundleShortVersionString": "2.0.0", "MinimumOSVersion": minimum}))
                    errors = audit.app_audit(app, "2.0.0", "17.0")["errors"]
                    self.assertEqual("Unexpected iOS deployment minimum" in errors, minimum != "17.0")

    def test_missing_offline_license_inventory_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            self.assertTrue(audit.license_errors(Path(directory)))

    def test_packaging_notices_detect_missing_and_tampered_license_texts(self):
        source = Path(__file__).resolve().parents[2] / "iosApp/TeamCityApp/ThirdPartyNotices.plist"
        self.assertEqual(audit.license_errors(source), [])
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "ThirdPartyNotices.plist"
            data = plistlib.loads(source.read_bytes())
            data["licenses"][0]["text"] += "changed"
            path.write_bytes(plistlib.dumps(data))
            self.assertTrue(audit.license_errors(path))
            data["licenses"] = []
            path.write_bytes(plistlib.dumps(data))
            self.assertTrue(audit.license_errors(path))

    def test_compiled_binary_plist_matches_the_xml_notice_source(self):
        source = Path(__file__).resolve().parents[2] / "iosApp/TeamCityApp/ThirdPartyNotices.plist"
        with tempfile.TemporaryDirectory() as directory:
            compiled = Path(directory) / "ThirdPartyNotices.plist"
            compiled.write_bytes(plistlib.dumps(plistlib.loads(source.read_bytes()), fmt=plistlib.FMT_BINARY))
            self.assertEqual(audit.notice_digest(source), audit.notice_digest(compiled))

    def test_empty_native_inventory_cannot_pass(self):
        with tempfile.TemporaryDirectory() as directory:
            tsv = Path(directory) / "dependencies.tsv"
            tsv.write_text("")
            with self.assertRaisesRegex(ValueError, "Empty native"):
                audit.dependencies(tsv, Path(directory))

    def test_unknown_dependency_without_license_metadata_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            folder = Path(directory)
            artifact = folder / "library.klib"
            artifact.write_bytes(b"library")
            tsv = folder / "dependencies.tsv"
            tsv.write_text(f"example:library:1.0\t{artifact}\n")
            with self.assertRaisesRegex(ValueError, "Expected one cached POM"):
                audit.dependencies(tsv, folder)


if __name__ == "__main__":
    unittest.main()
