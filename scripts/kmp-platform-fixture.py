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
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from threading import Thread


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
        body = json.dumps(data).encode()
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
