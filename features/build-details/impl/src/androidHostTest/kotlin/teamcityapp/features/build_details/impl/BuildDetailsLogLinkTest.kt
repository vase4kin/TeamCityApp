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

package teamcityapp.features.build_details.impl

import android.app.Application
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.build_details.impl.router.BuildDetailsLogLink
import teamcityapp.libraries.storage.models.UserAccount

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class BuildDetailsLogLinkTest {
    @Test fun activeAccountControlsServerGuestFlagAndEncodedBuildIdWithoutCredentials() {
        val storage = mock(SharedUserStorage::class.java)
        val guest = mock(UserAccount::class.java)
        `when`(guest.teamcityUrl).thenReturn("https://server.example/teamcity/")
        `when`(guest.isGuestUser).thenReturn(true)
        `when`(storage.activeUser).thenReturn(guest)
        val link = BuildDetailsLogLink(storage)
        assertEquals("https://server.example/teamcity/viewLog.html?buildId=42%26tab%3Dother&tab=buildLog&guest=1", link.url("42&tab=other"))
        val user = mock(UserAccount::class.java)
        `when`(user.teamcityUrl).thenReturn("https://other.example")
        `when`(user.isGuestUser).thenReturn(false)
        `when`(storage.activeUser).thenReturn(user)
        assertEquals("https://other.example/viewLog.html?buildId=42&tab=buildLog", link.url("42"))
        verify(user, never()).getPasswordAsString()
    }
}
