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

package teamcityapp.features.properties.impl

import android.app.Application
import android.os.Bundle
import android.os.Parcel
import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.properties.api.Property
import teamcityapp.features.properties.impl.navigation.PropertiesNavigationImpl

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PropertiesNavigationTest {
    @Test fun fragmentArgumentsSurviveParcelingAndRecreation() {
        val properties = listOf(Property("sdk", "24"), Property("sdk", ""))
        val original = PropertiesNavigationImpl().create(properties)
        val parcel = Parcel.obtain()
        val restored: Bundle
        try {
            parcel.writeBundle(original.arguments)
            parcel.setDataPosition(0)
            restored = parcel.readBundle(javaClass.classLoader)!!
        } finally {
            parcel.recycle()
        }
        val recreated = PropertiesFragment().apply { arguments = restored }
        val model = PropertiesViewModel(SavedStateHandle(mapOf(
            PropertiesViewModel.ARG_NAMES to recreated.requireArguments().getStringArrayList(PropertiesViewModel.ARG_NAMES),
            PropertiesViewModel.ARG_VALUES to recreated.requireArguments().getStringArrayList(PropertiesViewModel.ARG_VALUES))))
        assertEquals(PropertiesUiState.Content(properties), model.state.value)
    }
}
