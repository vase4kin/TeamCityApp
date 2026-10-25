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
"""Local HTTP/TLS fixtures for opt-in KMP platform-engine verification.

Synthetic mutations require --enable-mutations and a /context/manage URL.
No real credentials or TeamCity writes are used. Stop with Ctrl-C.
"""
import argparse
import base64
import ipaddress
import json
from pathlib import Path
import ssl
import subprocess
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from threading import Thread, Lock
from urllib.parse import urlsplit, parse_qs


class Handler(BaseHTTPRequestHandler):
    http_origin = "http://127.0.0.1:18081"
    mutation_enabled = False
    mutation_lock = Lock()
    mutation_state = {}
    mutation_attempts = {}
    mutation_log = []
    artifact_attempts = {}

    def do_POST(self):
        # Drain bounded request bytes before rejecting a POST, so closing the socket
        # cannot reset a valid 401/405 response with unread client data still pending.
        try:
            length = int(self.headers.get("Content-Length", "0"))
            if not 0 <= length < 100000: raise ValueError("body size")
            raw_body = self.rfile.read(length)
        except ValueError:
            self.send_response(400); self.end_headers(); return
        # Synthetic writes exist only in explicitly enabled, local /manage contexts.
        parsed = urlsplit(self.path)
        prefix = parsed.path.split("/app/rest/", 1)[0].removesuffix("/guestAuth").removesuffix("/httpAuth")
        if not self.mutation_enabled or not prefix.startswith("/context/manage") or "/app/rest/" not in parsed.path:
            self.send_response(405); self.end_headers(); return
        name = "guest"
        if "/httpAuth/" in parsed.path:
            known = {"Basic " + base64.b64encode((n + ":fixture-only-password").encode()).decode(): n for n in ("alice", "bob")}
            name = known.get(self.headers.get("Authorization", ""))
            if name is None:
                self.send_response(401); self.send_header("WWW-Authenticate", 'Basic realm="fixture"'); self.end_headers(); return
        try:
            if not length: raise ValueError("body size")
            body = json.loads(raw_body)
            if not isinstance(body, dict): raise ValueError("body")
        except (ValueError, json.JSONDecodeError):
            self.send_response(400); self.end_headers(); return
        rest = parsed.path.split("/app/rest/", 1)[1]
        owner = (prefix, name)
        with self.mutation_lock:
            attempt_key = (owner, rest)
            attempt = self.mutation_attempts.get(attempt_key, 0) + 1
            self.mutation_attempts[attempt_key] = attempt
        if "forbidden" in prefix:
            self.send_response(403); self.end_headers(); return
        if "retry" in prefix and attempt == 1:
            self.send_response(503); self.end_headers(); return
        if "slow" in prefix: time.sleep(4)
        with self.mutation_lock:
            state = self.mutation_state.setdefault(owner, {})
            if rest == "buildQueue":
                number = str(40 + sum(1 for item in self.mutation_log if item["owner"] == list(owner) and item["action"] == "run") + 1)
                value = {"state": "queued", "branchName": body.get("branchName", "main"), "properties": body.get("properties", {"property": []}), "triggered": {"type": "user", "user": {"username": name}}}
                action = "run"
            elif rest.startswith(("builds/id:", "buildQueue/id:")):
                number = rest.rsplit(":", 1)[-1]
                value = {"state": "finished", "statusText": "Canceled by fixture user", "canceledInfo": {"user": {"username": name}, "timestamp": "20261022T100000+0000"}}
                action = "cancel"
                if body.get("readdIntoQueue"):
                    state["99"] = {"state": "queued", "statusText": "Re-added by fixture"}
            else:
                self.send_response(404); self.end_headers(); return
            state[number] = value
            self.mutation_log.append({"owner": list(owner), "action": action, "id": number, "body": body})
        data = json.dumps({"id": number, "href": prefix + "/app/rest/builds/id:" + number}).encode()
        self.send_response(200); self.send_header("Content-Type", "application/json"); self.send_header("Content-Length", str(len(data))); self.end_headers()
        try: self.wfile.write(data)
        except (BrokenPipeError, ConnectionResetError): pass

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
        prefix = path.split("/app/rest/", 1)[0].removesuffix("/guestAuth").removesuffix("/httpAuth")
        with self.mutation_lock:
            managed = dict(self.mutation_state.get((prefix, name), {}))
        def build(number="1", state="finished"):
            value = {"id": number, "href": "/context/app/rest/builds/id:" + number,
                    "buildTypeId": "Demo_Build", "number": number, "state": state,
                    "webUrl": "http://127.0.0.1:18081/context/viewLog.html?buildId=" + number,
                    "artifacts": {"href": "/context/app/rest/builds/id:" + number + "/artifacts/children"},
                    "status": "FAILURE" if number == "1" else "SUCCESS", "statusText": "One failed test" if number == "1" else "Success",
                    "buildType": {"id": "Demo_Build", "name": "Verify", "projectId": "Demo", "projectName": "Demonstration"},
                    "startDate": "20261021T102030+0000", "finishDate": "20261021T103030+0000",
                    "branchName": "main", "agent": {"id": "1", "name": "macOS agent"},
                    "changes": {"href": "/context/app/rest/changes?locator=build:(id:"+number+")", "count": 1},
                    "testOccurrences": {"href": "/context/app/rest/testOccurrences?locator=build:(id:"+number+")", "passed": 1, "failed": 1, "ignored": 0, "count": 2},
                    "properties": {"property": [{"name": "env.PLATFORM", "value": "Apple Silicon"}, {"name": "build.description", "value": "A deterministic read-only build used for shared UI verification. " * 8}]},
                    "snapshot-dependencies": {"href": "/context/app/rest/builds?locator=snapshotDependency:(to:(id:"+number+"))", "count": 1}}
            if prefix.startswith("/context/manage"):
                if number == "3": value["state"] = "running"
                if number == "4": value["state"] = "queued"
                value.update(managed.get(number, {}))
            return value
        configuration = {"id": "Demo_Build", "name": "Verify", "projectId": "Demo", "projectName": "Demonstration"}
        if "/artifacts/" in rest:
            artifact_root = "/context/app/rest/builds/id:1/artifacts/"
            if "/artifacts/content/" in rest:
                key = (prefix, name, rest)
                with self.mutation_lock:
                    attempt = self.artifact_attempts.get(key, 0) + 1
                    self.artifact_attempts[key] = attempt
                if "/artifact-retry/" in path and attempt == 1:
                    self.send_response(503); self.end_headers(); return
                body = ("TeamCityApp artifact fixture\n" * 32768).encode()
                self.send_response(200)
                self.send_header("Content-Type", "text/plain")
                self.send_header("Content-Length", str(len(body)))
                self.end_headers()
                try:
                    for start in range(0, len(body), 8192):
                        self.wfile.write(body[start:start + 8192]); self.wfile.flush()
                        if "/artifact-slow/" in path: time.sleep(0.15)
                        if "/artifact-long/" in path: time.sleep(0.3)
                        if "/artifact-truncated/" in path: self.close_connection = True; break
                except (BrokenPipeError, ConnectionResetError): pass
                return
            elif rest.endswith("/children/logs") or rest.endswith("/children/bundle.zip!"):
                data = {"file": [{"name": "report.txt", "href": artifact_root + "metadata/report.txt", "size": 950272, "content": {"href": artifact_root + "content/report.txt"}}]}
            else:
                data = {"file": [
                    {"name": "logs", "href": artifact_root + "metadata/logs", "children": {"href": artifact_root + "children/logs"}},
                    {"name": "report.txt", "href": artifact_root + "metadata/report.txt", "size": 950272, "content": {"href": artifact_root + "content/report.txt"}},
                    {"name": "bundle.zip", "href": artifact_root + "metadata/bundle.zip", "size": 1024, "content": {"href": artifact_root + "content/bundle.zip"}, "children": {"href": artifact_root + "children/bundle.zip!"}},
                    {"name": "index.html", "href": artifact_root + "metadata/index.html", "size": 1024, "content": {"href": artifact_root + "content/index.html"}}]}
        elif rest.startswith("projects/"):
            data = {"projects": {"project": []}, "buildTypes": {"buildType": [configuration]}}
        elif rest.startswith("buildTypes/") and rest.endswith("/builds"):
            data = {"count": 2, "build": [build("1"), build("2")]}
        elif rest.startswith("buildTypes/") and rest.endswith("/branches"):
            data = {"branch": [{"name": "main"}, {"name": "release"}]}
        elif rest.startswith("buildTypes/"):
            data = configuration
        elif rest.startswith("builds/id:"):
            data = build(rest.rsplit(":", 1)[-1])
        elif rest == "builds":
            locator = query.get("locator", [""])[0]
            data = {"count": 1, "build": [build("3", "running") if "running:true" in locator else build("2")]}
        elif rest == "buildQueue":
            values = [build("4", "queued")] + [build(number, value.get("state", "queued")) for number, value in managed.items() if number != "4"]
            values = [value for value in values if value["state"] == "queued"]
            data = {"count": len(values), "build": values}
        elif rest == "fixtureMutations":
            with self.mutation_lock:
                data = {"mutations": [item for item in self.mutation_log if item["owner"] == [prefix, name]]}
        elif rest == "agents":
            data = {"count": 3, "agent": [{"id": str(i), "name": f"Build agent {i}"} for i in range(1, 4)]}
        elif rest == "changes":
            data = {"count": 1, "change": [{"id": "5", "href": "/context/app/rest/changes/id:5"}]}
        elif rest.startswith("changes/id:"):
            data = {"id": "5", "comment": "Share Home and build browsing between Android and iOS", "username": "Developer", "date": "20261021T102030+0000", "version": "abc123def456", "webUrl": "http://127.0.0.1:18081/context/viewModification.html?modId=5", "files": {"file": [{"file": "shared-app/NativeBrowsing.kt", "changeType": "added"}, {"file": "features/home/HomeScreen.kt", "changeType": "changed"}]}}
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
        prefix = path.split("/app/rest/", 1)[0].removesuffix("/guestAuth").removesuffix("/httpAuth")
        body = json.dumps(data).replace("/context/app/rest/", prefix + "/app/rest/").replace("http://127.0.0.1:18081/context/", self.http_origin + prefix + "/").encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        try:
            self.wfile.write(body)
        except (BrokenPipeError, ConnectionResetError):
            pass


def fixture_bind_address(value):
    try:
        address = ipaddress.IPv4Address(value)
    except ipaddress.AddressValueError as error:
        raise argparse.ArgumentTypeError("Use a local/private IPv4 interface address") from error
    allowed = address.is_loopback or any(address in ipaddress.IPv4Network(network) for network in ["10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16"])
    if not allowed:
        raise argparse.ArgumentTypeError("Use a local/private IPv4 interface address")
    return str(address)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", default="build/kmp-phase-2/platform-fixture")
    parser.add_argument("--bind-address", type=fixture_bind_address, default="127.0.0.1", help="Explicit private interface for read-only physical-device tests; default is loopback")
    parser.add_argument("--enable-mutations", action="store_true", help="Enable synthetic POSTs only in local /context/manage contexts")
    args = parser.parse_args()
    if args.enable_mutations and not ipaddress.IPv4Address(args.bind_address).is_loopback:
        parser.error("Synthetic mutations require a loopback bind address")
    Handler.mutation_enabled = args.enable_mutations
    Handler.http_origin = "http://" + args.bind_address + ":18081"
    output = Path(args.output)
    output.mkdir(parents=True, exist_ok=True)
    cert, key = output / "certificate.pem", output / "private-key.pem"
    if not cert.exists():
        subprocess.run(["openssl", "req", "-x509", "-newkey", "rsa:2048", "-nodes", "-days", "2",
                        "-subj", "/CN=localhost", "-addext", "subjectAltName=DNS:localhost,IP:127.0.0.1",
                        "-keyout", str(key), "-out", str(cert)], check=True,
                       stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    http = ThreadingHTTPServer((args.bind_address, 18081), Handler)
    https = ThreadingHTTPServer((args.bind_address, 18444), Handler)
    context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    context.load_cert_chain(cert, key)
    https.socket = context.wrap_socket(https.socket, server_side=True)
    worker = Thread(target=http.serve_forever, daemon=True)
    worker.start()
    print("Local fixtures (synthetic mutations=" + str(args.enable_mutations) + "): HTTP " + args.bind_address + ":18081; self-signed TLS " + args.bind_address + ":18444", flush=True)
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
