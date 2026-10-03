import importlib.util
import io
from pathlib import Path
import tarfile
import tempfile
import unittest
import zipfile


spec = importlib.util.spec_from_file_location(
    "instrumentation_coverage",
    Path(__file__).resolve().parents[1] / "generate-instrumentation-coverage.py",
)
coverage = importlib.util.module_from_spec(spec)
spec.loader.exec_module(coverage)


class InstrumentationCoverageTest(unittest.TestCase):
    def test_loose_and_archived_device_data_preserve_duplicate_names(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            run = root / "marathon"
            run.mkdir()
            unpacked = root / "unpacked"
            unpacked.mkdir()
            (run / "coverage.ec").write_bytes(b"device one")
            (root / "unit.exec").write_bytes(b"unrelated unit data")
            with zipfile.ZipFile(run / "device.zip", "w") as archive:
                archive.writestr("../../coverage.ec", b"device two")
                archive.writestr("junit.xml", b"test results")
            with tarfile.open(run / "device-files.tar", "w") as archive:
                member = tarfile.TarInfo("/device/coverage.ec")
                member.size = len(b"device three")
                archive.addfile(member, io.BytesIO(b"device three"))
                link = tarfile.TarInfo("external.exec")
                link.type = tarfile.SYMTYPE
                link.linkname = str(root / "unit.exec")
                archive.addfile(link)

            files = coverage.execution_files(run, unpacked)
            self.assertEqual(
                {path.read_bytes() for path in files},
                {b"device one", b"device two", b"device three"},
            )
            self.assertTrue(all(path.parent in {run, unpacked} for path in files))
            self.assertFalse((root / "coverage.ec").exists())

    def test_missing_device_data_fails_instead_of_uploading_an_empty_report(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            (root / "tools").mkdir()
            (root / "tools/jacococli.jar").write_bytes(b"fixture")
            (root / "classes").mkdir()
            (root / "classes/App.class").write_bytes(b"fixture")
            with self.assertRaisesRegex(ValueError, "No instrumentation coverage data"):
                coverage.generate_report(root, root / "missing", root / "report.xml")


if __name__ == "__main__":
    unittest.main()
