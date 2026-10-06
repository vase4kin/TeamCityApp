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

package teamcityapp.features.change_details.impl

import android.app.Activity
import android.app.Application
import android.net.Uri
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.change_details.impl.router.ChangeDetailsRouterImpl
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.storage.Storage
import teamcityapp.libraries.storage.models.UserAccount

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ChangeDetailsRouterTest {
    private val activity = mock(Activity::class.java)
    private val tabs = mock(ChromeCustomTabs::class.java)
    private val storage = mock(Storage::class.java)
    private val router = ChangeDetailsRouterImpl(activity, tabs, storage)

    @Test fun lifecycleOwnsBrowserConnection() {
        router.start()
        router.stop()
        router.start()
        router.stop()
        verify(tabs, times(2)).initCustomsTabs()
        verify(tabs, times(2)).unbindCustomsTabs()
    }

    @Test fun moreDetailsOpensOriginalUrl() {
        router.openUrl(fixture.webUrl)
        verify(tabs).launchUrl(fixture.webUrl)
    }

    @Test fun diffUsesCurrentAccountAndEncodesFileNameAndChangeId() {
        val account = mock(UserAccount::class.java)
        `when`(account.teamcityUrl).thenReturn("https://teamcity.example/context")
        `when`(storage.activeUser).thenReturn(account)
        val file = "src/a + b&c#?.kt"
        router.openDiff("123 & 456", file)
        val expected = Uri.parse("https://teamcity.example/context").buildUpon().appendPath("diffView.html")
            .appendQueryParameter("id", "123 & 456").appendQueryParameter("vcsFileName", file).build()
        verify(tabs).launchUrl(expected.toString())
        assertEquals(file, expected.getQueryParameter("vcsFileName"))
    }

    @Test fun switchingAccountChangesDiffDestination() {
        val first = mock(UserAccount::class.java)
        val next = mock(UserAccount::class.java)
        `when`(first.teamcityUrl).thenReturn("https://first.example")
        `when`(next.teamcityUrl).thenReturn("https://next.example")
        `when`(storage.activeUser).thenReturn(first, next)
        router.openDiff("123", "a.kt")
        router.openDiff("123", "a.kt")
        verify(tabs).launchUrl("https://first.example/diffView.html?id=123&vcsFileName=a.kt")
        verify(tabs).launchUrl("https://next.example/diffView.html?id=123&vcsFileName=a.kt")
    }

    @Test fun closeFinishesActivity() {
        router.close()
        verify(activity).finish()
    }
}
