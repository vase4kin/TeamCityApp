#!/usr/bin/env python3
"""Convert one Marathon run's execution files to a separate JaCoCo XML report."""
import argparse
from pathlib import Path
import shutil
import subprocess
import tarfile
import tempfile
import zipfile


def execution_files(directory: Path, unpacked: Path) -> list[Path]:
    """Read loose files and coverage inside downloaded device archives safely."""
    files = []

    def copy_coverage(name, stream):
        if Path(name).suffix not in {".ec", ".exec"}:
            return
        destination = unpacked / f"coverage-{len(files)}.exec"
        with destination.open("wb") as output:
            shutil.copyfileobj(stream, output)
        files.append(destination)

    for path in sorted(directory.rglob("*")):
        if not path.is_file():
            continue
        if path.suffix in {".ec", ".exec"}:
            files.append(path)
        elif path.suffix == ".zip":
            with zipfile.ZipFile(path) as archive:
                for member in archive.infolist():
                    if not member.is_dir() and Path(member.filename).suffix in {".ec", ".exec"}:
                        with archive.open(member) as stream:
                            copy_coverage(member.filename, stream)
        elif path.name.endswith((".tar", ".tar.gz", ".tgz")):
            with tarfile.open(path) as archive:
                for member in archive:
                    if member.isfile() and Path(member.name).suffix in {".ec", ".exec"}:
                        with archive.extractfile(member) as stream:
                            copy_coverage(member.name, stream)
    return files


def generate_report(inputs: Path, execution_data: Path, output: Path) -> None:
    cli = inputs / "tools/jacococli.jar"
    classes = inputs / "classes"
    if not cli.is_file() or not any(classes.rglob("*.class")):
        raise ValueError(f"Missing JaCoCo CLI or compiled classes in {inputs}")
    output.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as temporary:
        files = execution_files(execution_data, Path(temporary))
        if not files:
            raise ValueError(f"No instrumentation coverage data found in {execution_data}")
        result = subprocess.run(
            [
                "java", "-jar", str(cli), "report", *map(str, files),
                "--classfiles", str(classes), "--sourcefiles", str(inputs / "sources"),
                "--xml", str(output), "--name", "Instrumentation tests",
            ],
            capture_output=True, text=True, check=True,
        )
    log = result.stdout + result.stderr
    print(log, end="")
    if "does not match" in log or "do not match" in log:
        raise ValueError("Instrumentation coverage does not match the exported classes")
    if not output.is_file() or output.stat().st_size == 0:
        raise ValueError(f"JaCoCo did not generate a report at {output}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--inputs", type=Path, required=True)
    parser.add_argument("--execution-data", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    try:
        generate_report(args.inputs, args.execution_data, args.output)
    except (ValueError, OSError, tarfile.TarError, zipfile.BadZipFile, subprocess.CalledProcessError) as error:
        if isinstance(error, subprocess.CalledProcessError):
            print(error.stdout or "", end="")
            print(error.stderr or "", end="")
        parser.exit(1, f"{error}\n")


if __name__ == "__main__":
    main()
