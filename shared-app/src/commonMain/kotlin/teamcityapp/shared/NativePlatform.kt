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

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import teamcityapp.libraries.analytics.AnalyticsTracker
import teamcityapp.libraries.app_theme.*
import teamcityapp.libraries.network.activeServerWebUrl
import teamcityapp.libraries.shared_data.ArtifactFileStore
import teamcityapp.shared.resources.*

interface NativePlatform {
    val responseCache: teamcityapp.libraries.network.ResponseCacheStore? get() = null
    val powerSaving: kotlinx.coroutines.flow.Flow<Boolean> get() = kotlinx.coroutines.flow.flowOf(false)
    val analytics: AnalyticsTracker
    val files: ArtifactFileStore
    val theme: ThemePreferencesRepository
    val failure: StateFlow<NativePlatformFailure?>

    /** Returns an unsubscribe function; the destination owns this registration. */
    fun onBackground(cancel: () -> Unit): () -> Unit
    fun browser(url: String)
    fun share(text: String)
    fun copy(text: String)

    /** True means the OS accepted presentation, not that the user exported the file. */
    fun openFile(path: String): Boolean
    fun rate()
    fun openSourceLibraries() = Unit
    fun rejected(action: NativePlatformFailure)
    fun clearFailure()
}
enum class NativePlatformFailure { Browser, Share, Clipboard, File, Rating }
internal val LocalNativePlatform = staticCompositionLocalOf<NativePlatform> { error("Native platform services are required") }
internal val LocalNativeAppLifecycle = staticCompositionLocalOf<Lifecycle> { error("Native app lifecycle is required") }

internal fun NativePlatformFailure.message(): org.jetbrains.compose.resources.StringResource = when (this) {
    NativePlatformFailure.Browser -> teamcityapp.shared.resources.Res.string.platform_browser_failed
    NativePlatformFailure.Share -> teamcityapp.shared.resources.Res.string.platform_share_failed
    NativePlatformFailure.Clipboard -> teamcityapp.shared.resources.Res.string.platform_copy_failed
    NativePlatformFailure.File -> teamcityapp.shared.resources.Res.string.platform_file_failed
    NativePlatformFailure.Rating -> teamcityapp.shared.resources.Res.string.platform_rating_failed
}

internal fun NativePlatform.serverBrowser(server: String, href: String) {
    runCatching { activeServerWebUrl(server, href) }.onSuccess(::browser).onFailure { rejected(NativePlatformFailure.Browser) }
}
