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

package teamcityapp.features.about.impl.router

import android.app.Activity
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*
import teamcityapp.features.about.impl.R
import teamcityapp.features.about.impl.AboutAction
import teamcityapp.libraries.app_rating.AppRating
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs

class AboutRouterTest {
    private val activity = mock(Activity::class.java)
    private val urls = mutableListOf<String>()
    private val tabs = object : ChromeCustomTabs {
        var bound = false
        override fun initCustomsTabs() { bound = true }
        override fun unbindCustomsTabs() { bound = false }
        override fun launchUrl(url: String) { urls += url }
    }
    private val ratedActivities = mutableListOf<Activity>()
    private val rating = object : AppRating {
        override fun open(activity: Activity) { ratedActivities += activity }
    }
    private val router = AboutRouterImpl(activity, tabs, rating)

    @Test fun ratingUsesInjectedLauncherAndCurrentActivity() {
        router.open(AboutAction.Rate)
        assertEquals(listOf(activity), ratedActivities)
        assertTrue(urls.isEmpty())
    }

    @Test fun websiteUsesFeatureDestination() {
        `when`(activity.getString(R.string.about_app_url_web)).thenReturn("https://teamcity.app")
        router.open(AboutAction.Website)
        assertEquals(listOf("https://teamcity.app"), urls)
        assertTrue(ratedActivities.isEmpty())
    }

    @Test fun browserBindingMatchesVisibleLifecycle() {
        router.start()
        assertTrue(tabs.bound)
        router.stop()
        assertFalse(tabs.bound)
    }
}
