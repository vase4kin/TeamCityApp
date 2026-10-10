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

package teamcityapp.features.properties.impl.router

import android.os.Build
import teamcityapp.libraries.clipboard.ClipboardWriter

class PropertiesRouterImpl(private val clipboard: ClipboardWriter) : PropertiesRouter {
    override fun copyValue(name: String, value: String): Boolean {
        if (value.isEmpty()) return false
        clipboard.copy(name, value)
        // Android 13 and newer display their own clipboard confirmation.
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    }
}
