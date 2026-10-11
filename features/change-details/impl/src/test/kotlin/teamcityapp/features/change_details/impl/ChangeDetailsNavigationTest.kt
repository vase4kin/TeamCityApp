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
import android.os.Parcel
import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.change_details.api.navigation.ChangeDetailsDestination
import teamcityapp.features.change_details.impl.navigation.ChangeDetailsNavigationImpl
import teamcityapp.features.change_details.impl.tracker.ChangeDetailsTracker
import teamcityapp.libraries.navigation.DestinationLauncher

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ChangeDetailsNavigationTest {
    @Test fun navigationKeepsTheCompletePlainSnapshotInTheRootDestination() {
        val owner = mock(Activity::class.java)
        val launcher = mock(DestinationLauncher::class.java)
        ChangeDetailsNavigationImpl(launcher).open(owner, fixture)
        verify(launcher).open(owner, ChangeDetailsDestination(fixture), 0)
        verifyNoInteractions(owner)
    }

    @Test fun entryArgumentsRemainRestorableThroughAndroidSavedState() {
        val parcel = Parcel.obtain()
        try {
            parcel.writeBundle(ChangeDetailsArguments.bundle(fixture))
            parcel.setDataPosition(0)
            val restored = parcel.readBundle(javaClass.classLoader)!!
            val arguments = restored.keySet().associateWith { restored.get(it) }
            assertEquals(ChangeDetailsUiState.Content(fixture), ChangeDetailsViewModel(SavedStateHandle(arguments), mock(ChangeDetailsTracker::class.java)).state.value)
        } finally {
            parcel.recycle()
        }
    }
}
