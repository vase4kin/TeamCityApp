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

package teamcityapp.libraries.app_rating

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class AppRatingTest {
    private val activity = mock(Activity::class.java).also {
        `when`(it.packageName).thenReturn("teamcityapp.test")
    }

    @Test fun opensCurrentAppInMarket() {
        AppRatingImpl().open(activity)
        val intents = ArgumentCaptor.forClass(Intent::class.java)
        verify(activity).startActivity(intents.capture())
        assertEquals("market://details?id=teamcityapp.test", intents.value.data.toString())
        assertEquals(Intent.ACTION_VIEW, intents.value.action)
        assertTrue(intents.value.flags and Intent.FLAG_ACTIVITY_NEW_DOCUMENT != 0)
    }

    @Test fun fallsBackToBrowserWhenMarketIsUnavailable() {
        doThrow(ActivityNotFoundException()).doNothing().`when`(activity).startActivity(any(Intent::class.java))
        AppRatingImpl().open(activity)
        val intents = ArgumentCaptor.forClass(Intent::class.java)
        verify(activity, times(2)).startActivity(intents.capture())
        assertEquals("https://play.google.com/store/apps/details?id=teamcityapp.test", intents.allValues[1].data.toString())
        assertEquals(Intent.ACTION_VIEW, intents.allValues[1].action)
    }
}
