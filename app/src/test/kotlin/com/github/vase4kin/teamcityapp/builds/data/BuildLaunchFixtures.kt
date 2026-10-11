/*
 * Copyright 2026 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.builds.data

import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.google.gson.Gson
import teamcityapp.libraries.builds.*

internal fun legacyBuild(json: String): Build = Gson().fromJson(json, Build::class.java)

internal val completeBuildJson = """
    {
      "id":"123", "href":"/app/rest/builds/id:123", "webUrl":"https://ci.example/build/123",
      "number":"045", "state":"future-state", "status":"future-status", "statusText":"Pending custom status",
      "branchName":"feature/raw", "buildTypeId":"top-level-id",
      "buildType":{"id":"nested-id", "href":"/config", "name":"Android", "projectId":"Mobile", "projectName":"Mobile project"},
      "queuedDate":"20201010T010000+0700", "startDate":"20201010T010100+0700", "finishDate":"20201010T010200+0700",
      "waitReason":"Waiting for agent", "startEstimate":"20201010T010300+0700",
      "triggered":{"type":"restarted", "date":"20201009T010000+0700", "details":"original build",
        "user":{"id":"user-1", "href":"/users/1", "username":"alice", "name":"Alice"},
        "buildType":{"id":"trigger-id", "href":"/trigger-config", "name":"Upstream", "projectId":"Core", "projectName":"Core project"}},
      "agent":{"id":"agent-1", "href":"/agents/1", "name":"linux"},
      "canceledInfo":{"timestamp":"20201010T010400+0700", "user":{"id":"user-2", "href":"/users/2", "username":"bob", "name":"Bob"}},
      "changes":{"href":"/changes", "count":5, "nextHref":"/changes?next=5"},
      "artifacts":{"href":"/artifacts"},
      "testOccurrences":{"href":"/tests", "passed":10, "failed":2, "ignored":1, "count":13, "nextHref":"/tests?next=13"},
      "properties":{"id":"properties-id", "href":"/properties", "property":[
        {"id":"property-1", "href":"/properties/1", "name":"env", "value":"test", "own":true},
        {"id":"property-2", "href":"/properties/2", "name":null, "value":null, "own":false}]},
      "snapshot-dependencies":{"href":"/snapshots", "count":2, "nextHref":"/snapshots?next=2"},
      "personal":true, "pinned":true, "cleanSources":true, "queueAtTop":true
    }
""".trimIndent()

internal fun completeLaunch() = BuildLaunchData(
    id = "123", href = "/app/rest/builds/id:123", webUrl = "https://ci.example/build/123",
    number = "045", state = "future-state", status = "future-status", statusText = "Pending custom status",
    branchName = "feature/raw", buildTypeId = "top-level-id",
    configuration = BuildConfigurationData("nested-id", "Android", "Mobile", "Mobile project", "/config"),
    queuedDate = "20201010T010000+0700", startDate = "20201010T010100+0700", finishDate = "20201010T010200+0700",
    waitReason = "Waiting for agent", startEstimate = "20201010T010300+0700",
    triggered = BuildTrigger(
        "restarted",
        "20201009T010000+0700",
        "original build",
        BuildUser("alice", "Alice", "user-1", "/users/1"),
        BuildConfigurationData("trigger-id", "Upstream", "Core", "Core project", "/trigger-config")
    ),
    agent = BuildAgent("agent-1", "linux", "/agents/1"),
    canceledInfo = BuildCancellation("20201010T010400+0700", BuildUser("bob", "Bob", "user-2", "/users/2")),
    changes = BuildCollectionLink("/changes", 5, "/changes?next=5"), artifacts = BuildCollectionLink("/artifacts"),
    tests = BuildTests("/tests", 10, 2, 1, 13, "/tests?next=13"),
    properties = BuildProperties(
        listOf(
            BuildProperty("env", "test", true, "property-1", "/properties/1"),
            BuildProperty(null, null, false, "property-2", "/properties/2")
        ),
        "properties-id",
        "/properties"
    ),
    snapshotDependencies = BuildCollectionLink("/snapshots", 2, "/snapshots?next=2"),
    personal = true, pinned = true, cleanSources = true, queueAtTop = true
)
