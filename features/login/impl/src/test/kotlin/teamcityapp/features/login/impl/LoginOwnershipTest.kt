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

package teamcityapp.features.login.impl

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.login.api.router.LoginAppRouter
import teamcityapp.features.login.impl.navigation.LoginNavigationImpl
import teamcityapp.features.login.impl.router.LoginRouterImpl
import teamcityapp.features.login.impl.tracker.LoginTrackerImpl
import teamcityapp.libraries.authentication.AuthenticationAnalytics
import teamcityapp.libraries.resources.R as SharedR

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class LoginOwnershipTest {
    @Test fun navigationPreservesLaunchBehavior() {
        val owner = spy(Robolectric.buildActivity(Activity::class.java).setup().get())
        doNothing().`when`(owner).startActivity(any(Intent::class.java))
        LoginNavigationImpl().open(owner)
        val intent = ArgumentCaptor.forClass(Intent::class.java)
        verify(owner).startActivity(intent.capture())
        assertEquals(LoginActivity::class.java.name, intent.value.component!!.className)
        assertEquals(0, intent.value.flags)
    }

    @Test fun accountSwitchClearsTheTask() {
        val owner = spy(Robolectric.buildActivity(Activity::class.java).setup().get())
        doNothing().`when`(owner).startActivity(any(Intent::class.java))
        LoginNavigationImpl().openWithClearStack(owner)
        val intent = ArgumentCaptor.forClass(Intent::class.java)
        verify(owner).startActivity(intent.capture())
        assertEquals(LoginActivity::class.java.name, intent.value.component!!.className)
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK, intent.value.flags)
    }

    @Test fun successfulNavigationPreservesCloseOrder() {
        val owner = mock(Activity::class.java)
        val destination = mock(LoginAppRouter::class.java)
        LoginRouterImpl(owner, destination).openProjects()
        val order = inOrder(owner, destination)
        order.verify(destination).openProjects(owner)
        order.verify(owner).finish()

        order.verifyNoMoreInteractions()
    }

    @Test fun analyticsPreserveScreenAndActionEvents() {
        val analytics = mock(FirebaseAnalytics::class.java)
        val tracker = LoginTrackerImpl(analytics, AuthenticationAnalytics(analytics))
        tracker.trackView()
        tracker.trackUserClicksOnTryItOut()
        tracker.trackUserTriesTryItOut()
        tracker.trackUserDeclinesTryingTryItOut()
        val order = inOrder(analytics)
        order.verify(analytics).logEvent("screen_first_login", null)
        order.verify(analytics).logEvent("event_user_clicks_on_try_it_out", null)
        order.verify(analytics).logEvent("event_user_tries_try_it_out", null)
        order.verify(analytics).logEvent("event_user_declines_try_it_out", null)
        order.verifyNoMoreInteractions()
    }

    @Test fun analyticsPreserveAuthenticationPayloads() {
        val analytics = mock(FirebaseAnalytics::class.java)
        val tracker = LoginTrackerImpl(analytics, AuthenticationAnalytics(analytics))
        tracker.trackUserLoginSuccess(true)
        tracker.trackGuestUserLoginSuccess(false)
        tracker.trackUserLoginFailed("user failure")
        tracker.trackGuestUserLoginFailed("guest failure")
        tracker.trackUserDataSaveFailed()
        val names = ArgumentCaptor.forClass(String::class.java)
        val bundles = ArgumentCaptor.forClass(Bundle::class.java)
        verify(analytics, times(5)).logEvent(names.capture(), bundles.capture())
        assertEquals(listOf("login_user_success", "login_guest_user_success", "login_user_failed", "login_guest_user_failed", "login_user_failed"), names.allValues)
        assertEquals(true, bundles.allValues[0].getBoolean("sslEnabled"))
        assertEquals(false, bundles.allValues[1].getBoolean("sslEnabled"))
        assertEquals("user failure", bundles.allValues[2].getString("errorMessage"))
        assertEquals("guest failure", bundles.allValues[3].getString("errorMessage"))
        assertEquals("Failed to save user data!", bundles.allValues[4].getString("errorMessage"))
        bundles.allValues.forEach { assertEquals(1, it.size()) }
    }
}
