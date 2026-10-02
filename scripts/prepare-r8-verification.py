#!/usr/bin/env python3
"""Prepare a mock Firebase configuration for the isolated minified verification APK."""
import json
from pathlib import Path

root = Path(__file__).resolve().parent.parent
config = json.loads((root / "mock-prodDebug-google-services.json").read_text())
for client in config["client"]:
    android = client["client_info"]["android_client_info"]
    android["package_name"] = "com.github.vase4kin.teamcityapp.r8"
output = root / "app/src/prod/r8Verification/google-services.json"
output.parent.mkdir(parents=True, exist_ok=True)
output.write_text(json.dumps(config, indent=2) + "\n")
