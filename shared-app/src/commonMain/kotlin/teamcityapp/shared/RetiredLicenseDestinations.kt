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

package teamcityapp.shared

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.libraries.navigation.Navigator

// Compatibility keys only: the custom catalog and text reader have been removed.
@Serializable internal data object NativeLicensesDestination : NavKey

@Serializable internal data class NativeLicenseDestination(val id: String) : NavKey

internal fun Navigator.closeRetiredLicenseScreens(originEntryId: String) {
    if (current.id != originEntryId) return
    fun isRetired() = current.destination is NativeLicensesDestination || current.destination is NativeLicenseDestination
    while (isRetired() && snapshot.entries.size > 1) pop()
    if (isRetired()) replace(AboutDestination)
}
