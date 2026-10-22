#!/usr/bin/env python3
#
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
#
"""Read-only local HTTP/TLS fixtures for opt-in KMP platform-engine verification.

No real credentials or TeamCity writes are used. Stop with Ctrl-C.
"""
import argparse
import base64
import json
from pathlib import Path
import ssl
import subprocess
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from threading import Thread
from urllib.parse import urlsplit, parse_qs


class Handler(BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        pass  # Never print Authorization or request credentials.

    def do_GET(self):
        if "httpAuth/" in self.path:
            raw = self.headers.get("Authorization", "")
            known = { "Basic " + base64.b64encode((name + ":fixture-only-password").encode()).decode(): name
                      for name in ("alice", "bob") }
            name = known.get(raw)
            if name is None:
                self.send_response(401)
                self.send_header("WWW-Authenticate", 'Basic realm="fixture"')
                self.end_headers()
                return
        else:
            name = "guest"
        if "redirect=true" in self.path:
            self.send_response(302)
            self.send_header("Location", "/context/guestAuth/app/rest/server?trap=true")
            self.end_headers()
            return
        data = {"version": "fixture-" + name, "webUrl": "http://127.0.0.1:18081/context/"}
        parsed = urlsplit(self.path)
        path = parsed.path
        query = parse_qs(parsed.query)
        rest = path.split("/app/rest/", 1)[-1]
        def build(number="1", state="finished"):
            return {"id": number, "href": "/context/app/rest/builds/id:" + number,
                    "buildTypeId": "Demo_Build", "number": number, "state": state,
                    "status": "FAILURE" if number == "1" else "SUCCESS", "statusText": "One failed test" if number == "1" else "Success",
                    "buildType": {"id": "Demo_Build", "name": "Verify", "projectId": "Demo", "projectName": "Demonstration"},
                    "startDate": "20261021T102030+0000", "finishDate": "20261021T103030+0000",
                    "branchName": "main", "agent": {"id": "1", "name": "macOS agent"},
                    "changes": {"href": "/context/app/rest/changes?locator=build:(id:"+number+")", "count": 1},
                    "testOccurrences": {"href": "/context/app/rest/testOccurrences?locator=build:(id:"+number+")", "passed": 1, "failed": 1, "ignored": 0, "count": 2},
                    "properties": {"property": [{"name": "env.PLATFORM", "value": "Apple Silicon"}, {"name": "build.description", "value": "A deterministic read-only build used for shared UI verification. " * 8}]},
                    "snapshot-dependencies": {"href": "/context/app/rest/builds?locator=snapshotDependency:(to:(id:"+number+"))", "count": 1}}
        configuration = {"id": "Demo_Build", "name": "Verify", "projectId": "Demo", "projectName": "Demonstration"}
        if rest.startswith("projects/"):
            data = {"projects": {"project": []}, "buildTypes": {"buildType": [configuration]}}
        elif rest.startswith("buildTypes/") and rest.endswith("/builds"):
            data = {"count": 2, "build": [build("1"), build("2")]}
        elif rest.startswith("buildTypes/"):
            data = configuration
        elif rest.startswith("builds/id:"):
            data = build(rest.rsplit(":", 1)[-1])
        elif rest == "builds":
            locator = query.get("locator", [""])[0]
            data = {"count": 1, "build": [build("3", "running") if "running:true" in locator else build("2")]}
        elif rest == "buildQueue":
            data = {"count": 1, "build": [build("4", "queued")]}
        elif rest == "agents":
            data = {"count": 3, "agent": [{"id": str(i), "name": f"Build agent {i}"} for i in range(1, 4)]}
        elif rest == "changes":
            data = {"count": 1, "change": [{"id": "5", "href": "/context/app/rest/changes/id:5"}]}
        elif rest.startswith("changes/id:"):
            data = {"id": "5", "comment": "Share Home and build browsing between Android and iOS", "username": "Developer", "date": "20261021T102030+0000", "version": "abc123def456", "webUrl": "https://example.invalid/change/5", "files": {"file": [{"file": "shared-app/NativeBrowsing.kt", "changeType": "added"}, {"file": "features/home/HomeScreen.kt", "changeType": "changed"}]}}
        elif rest == "testOccurrences":
            status = "SUCCESS" if "status:SUCCESS" in query.get("locator", [""])[0] else "FAILURE"
            if "status:UNKNOWN" in query.get("locator", [""])[0]:
                data = {"count": 0, "testOccurrence": []}
            elif query.get("fields") == ["count,passed,failed,ignored"]:
                data = {"count": 2, "passed": 1, "failed": 1, "ignored": 0}
            else:
                data = {"count": 1, "testOccurrence": [{"id": "failure" if status == "FAILURE" else "passed", "href": "/context/app/rest/testOccurrences/id:failure", "name": "sharedHomeRetainsSelectedActivity" if status == "FAILURE" else "favoritesKeepSavedIds", "status": status}]}
        elif rest.startswith("testOccurrences/id:"):
            data = {"details": "AssertionError: expected <Queue> but was <Running>\n    at HomeStateTest.retainsActivity(HomeStateTest.kt:42)\n" * 12}
        if "/loading/" in path and rest != "server":
            time.sleep(6)
        if ("/error/" in path and rest in ("builds", "buildQueue", "agents")) or ("/optional/" in path and rest in ("changes", "testOccurrences") and "fields" in query):
            self.send_response(503)
            self.end_headers()
            return
        prefix = path.split("/guestAuth/app/rest/", 1)[0].split("/httpAuth/app/rest/", 1)[0]
        body = json.dumps(data).replace("/context/app/rest/", prefix + "/app/rest/").encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        try:
            self.wfile.write(body)
        except (BrokenPipeError, ConnectionResetError):
            pass


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", default="build/kmp-phase-2/platform-fixture")
    args = parser.parse_args()
    output = Path(args.output)
    output.mkdir(parents=True, exist_ok=True)
    cert, key = output / "certificate.pem", output / "private-key.pem"
    if not cert.exists():
        subprocess.run(["openssl", "req", "-x509", "-newkey", "rsa:2048", "-nodes", "-days", "2",
                        "-subj", "/CN=localhost", "-addext", "subjectAltName=DNS:localhost,IP:127.0.0.1",
                        "-keyout", str(key), "-out", str(cert)], check=True,
                       stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    http = ThreadingHTTPServer(("127.0.0.1", 18081), Handler)
    https = ThreadingHTTPServer(("127.0.0.1", 18444), Handler)
    context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    context.load_cert_chain(cert, key)
    https.socket = context.wrap_socket(https.socket, server_side=True)
    worker = Thread(target=http.serve_forever, daemon=True)
    worker.start()
    print("Read-only fixtures: HTTP 127.0.0.1:18081; self-signed TLS 127.0.0.1:18444", flush=True)
    try:
        https.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        http.shutdown()
        https.server_close()
        http.server_close()


if __name__ == "__main__":
    main()
