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

import org.junit.Assert.*
import org.junit.Test
import teamcityapp.libraries.storage.models.UserAccount

class HomeBuildAccountSnapshotTest {
    @Test fun sameUrlDifferentUsersHaveDifferentQueryKeys() {
        assertNotEquals(account("https://ci", "alice").homeBuildAccountKey(), account("https://ci", "bob").homeBuildAccountKey())
    }

    @Test fun jsonEncodingPreventsDelimiterCollisions() {
        assertNotEquals(account("https://ci|alice", "bob").homeBuildAccountKey(), account("https://ci", "alice|bob").homeBuildAccountKey())
    }

    @Test fun guestAndSslSettingsDifferentiateSessionSnapshotsWithoutExposingCredentials() {
        val user = account("https://ci", "alice")
        val original = user.homeBuildAccountKey()
        user.isSslDisabled = true
        assertNotEquals(original, user.homeBuildAccountKey())
        val guest = UserAccount("https://ci", "alice", "secret-password".toByteArray(), true, true)
        assertNotEquals(original, guest.homeBuildAccountKey())
        assertFalse(original.contains("secret-password"))
        assertFalse(guest.homeBuildAccountKey().contains("secret-password"))
    }
    private fun account(url: String, name: String) = UserAccount(url, name, "secret-password".toByteArray(), false, true)
}
