#!/usr/bin/env python3
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

"""Verify that the platform fixture's synthetic writes stay local and opt-in."""
import importlib.util
import json
from pathlib import Path
from http.client import HTTPConnection
from http.server import ThreadingHTTPServer
from threading import Thread
import unittest

spec = importlib.util.spec_from_file_location("fixture", Path(__file__).parents[1] / "kmp-platform-fixture.py")
fixture = importlib.util.module_from_spec(spec)
spec.loader.exec_module(fixture)


class PlatformFixtureTest(unittest.TestCase):
    def setUp(self):
        class Handler(fixture.Handler):
            mutation_enabled = True
            mutation_state = {}
            mutation_attempts = {}
            mutation_log = []
            artifact_attempts = {}
        self.handler = Handler
        self.server = ThreadingHTTPServer(("127.0.0.1", 0), Handler)
        self.thread = Thread(target=self.server.serve_forever, daemon=True)
        self.thread.start()

    def tearDown(self):
        self.server.shutdown()
        self.server.server_close()
        self.thread.join()

    def request(self, method, path, body=None):
        client = HTTPConnection("127.0.0.1", self.server.server_port, timeout=5)
        client.request(method, path, body=json.dumps(body) if body is not None else None,
                       headers={"Content-Type": "application/json"})
        response = client.getresponse()
        data = response.read()
        client.close()
        return response.status, json.loads(data) if data else None

    def test_web_links_keep_the_active_fixture_context(self):
        status, build = self.request("GET", "/context/platform/unique/guestAuth/app/rest/builds/id:1")
        self.assertEqual(200, status)
        self.assertTrue(build["webUrl"].startswith("http://127.0.0.1:18081/context/platform/unique/"))
        _, change = self.request("GET", "/context/platform/unique/guestAuth/app/rest/changes/id:5")
        self.assertTrue(change["webUrl"].startswith("http://127.0.0.1:18081/context/platform/unique/"))

    def test_artifacts_expose_folder_archive_and_content_links_without_mutations(self):
        prefix = "/context/platform/guestAuth/app/rest/"
        status, listing = self.request("GET", prefix + "builds/id:1/artifacts/children?locator=browseArchives:true")
        self.assertEqual(200, status)
        self.assertEqual(["logs", "report.txt", "bundle.zip", "index.html"], [item["name"] for item in listing["file"]])
        archive = listing["file"][2]
        self.assertTrue(archive["children"]["href"].endswith("/bundle.zip!"))
        _, child = self.request("GET", archive["children"]["href"])
        self.assertEqual("report.txt", child["file"][0]["name"])
        self.assertFalse(self.handler.mutation_log)

    def test_artifact_retry_fails_once_then_returns_the_complete_stream(self):
        path = "/context/artifact-retry/test/guestAuth/app/rest/builds/id:1/artifacts/content/report.txt"
        for expected in [503, 200]:
            client = HTTPConnection("127.0.0.1", self.server.server_port, timeout=5)
            client.request("GET", path)
            response = client.getresponse()
            body = response.read()
            self.assertEqual(expected, response.status)
            if expected == 200: self.assertEqual(950272, len(body))
            client.close()
        self.assertFalse(self.handler.mutation_log)

    def test_mutations_require_opt_in_and_management_context(self):
        status, _ = self.request("POST", "/context/guestAuth/app/rest/buildQueue", {})
        self.assertEqual(405, status)
        self.handler.mutation_enabled = False
        status, _ = self.request("POST", "/context/manage/guestAuth/app/rest/buildQueue", {})
        self.assertEqual(405, status)
        self.assertFalse(self.handler.mutation_log)

    def test_queued_reference_resolves_complete_snapshot_and_remove_isolated(self):
        prefix = "/context/manage/guestAuth/app/rest/"
        status, result = self.request("POST", prefix + "buildQueue", {"branchName": "release"})
        self.assertEqual(200, status)
        self.assertEqual("41", result["id"])
        _, build = self.request("GET", result["href"])
        self.assertEqual("queued", build["state"])
        self.assertEqual("Verify", build["buildType"]["name"])
        self.assertEqual("release", build["branchName"])
        self.request("POST", prefix + "builds/id:41", {"readdIntoQueue": False})
        _, removed = self.request("GET", result["href"])
        self.assertEqual("finished", removed["state"])
        _, other = self.request("GET", "/context/manage-other/guestAuth/app/rest/builds/id:41")
        self.assertEqual("finished", other["state"])
        self.assertNotIn("canceledInfo", other)

    def test_forbidden_and_first_failure_do_not_record_accepted_writes(self):
        status, _ = self.request("POST", "/context/manage-forbidden/guestAuth/app/rest/buildQueue", {})
        self.assertEqual(403, status)
        path = "/context/manage-retry/guestAuth/app/rest/buildQueue"
        self.assertEqual(503, self.request("POST", path, {})[0])
        self.assertFalse(self.handler.mutation_log)
        self.assertEqual(200, self.request("POST", path, {})[0])
        self.assertEqual(1, len(self.handler.mutation_log))

    def test_readd_creates_a_queued_snapshot_and_running_stop_is_reflected(self):
        path = "/context/manage/guestAuth/app/rest/"
        self.request("POST", path + "builds/id:3", {"readdIntoQueue": True})
        _, stopped = self.request("GET", path + "builds/id:3")
        self.assertEqual("finished", stopped["state"])
        _, queue = self.request("GET", path + "buildQueue")
        self.assertIn("99", [build["id"] for build in queue["build"]])


if __name__ == "__main__":
    unittest.main()
