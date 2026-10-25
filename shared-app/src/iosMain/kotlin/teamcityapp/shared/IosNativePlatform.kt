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

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package teamcityapp.shared

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.Foundation.*
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.*
import platform.posix.*
import teamcityapp.features.artifacts.api.DownloadedArtifact
import teamcityapp.libraries.app_theme.*
import teamcityapp.libraries.shared_data.*

/** Files are app-private; iOS grants per-file access through its system export sheet. */
internal class IosArtifactFileStore(private val directory: String) : ArtifactFileStore {
    override fun create(name: String): PendingArtifact {
        require(name.isNotBlank() && name.none { it == '/' || it == '\\' || it.code < 32 } && name !in setOf(".", ".."))
        val folder = "$directory/${NSUUID().UUIDString}"
        check(NSFileManager.defaultManager.createDirectoryAtPath(folder, true, null, null)) { "Cannot create download directory" }
        val pending = "$folder/.download.part"
        val output = "$folder/$name"
        val stream = checkNotNull(fopen(pending, "wb")) { "Cannot create download" }
        return object : PendingArtifact {
            var open = true
            var complete = false
            override fun write(bytes: ByteArray, count: Int) {
                check(open && !complete)
                bytes.usePinned { check(fwrite(it.addressOf(0), 1u, count.toULong(), stream) == count.toULong()) { "Cannot write download" } }
            }
            override fun finish(): DownloadedArtifact {
                check(open && !complete)
                val flushed = fflush(stream) == 0
                val closed = fclose(stream) == 0
                open = false
                check(flushed && closed) { "Cannot finish download" }
                check(rename(pending, output) == 0) { "Cannot commit download" }
                complete = true
                return DownloadedArtifact(output)
            }
            override fun close() {
                if (open) {
                    fclose(stream)
                    open = false
                }
                if (!complete) NSFileManager.defaultManager.removeItemAtPath(folder, null)
            }
        }
    }
}

internal class IosThemePreferences(private val key: String) : ThemePreferencesRepository {
    private val defaults = NSUserDefaults.standardUserDefaults
    override val theme = MutableStateFlow(ThemeMode.fromStoredValue(defaults.stringForKey(key)) ?: ThemeMode.System)
    override suspend fun setTheme(mode: ThemeMode) {
        withContext(Dispatchers.Default) { defaults.setObject(mode.storedValue, key) }
        theme.value = mode
    }
}

/** UI objects belong to this controller; no retained ViewModel holds UIKit references. */
internal class IosNativePlatform(private val host: () -> UIViewController, namespace: String?) : NativePlatform {
    override val responseCache = IosResponseCacheStore()
    override val powerSaving = kotlinx.coroutines.flow.callbackFlow {
        val process = NSProcessInfo.processInfo
        trySend(process.lowPowerModeEnabled)
        val center = NSNotificationCenter.defaultCenter
        val observer = center.addObserverForName(NSProcessInfoPowerStateDidChangeNotification, null, NSOperationQueue.mainQueue) { trySend(process.lowPowerModeEnabled) }
        awaitClose { center.removeObserver(observer) }
    }
    override val diagnostics: NativeDiagnostics = IosNativeDiagnostics
    override val files: ArtifactFileStore = IosArtifactFileStore(NSHomeDirectory() + "/Library/Caches/TeamCityArtifacts")
    override val theme = IosThemePreferences(namespace?.let { "theme.uitest.$it" } ?: "theme.mode.v1")
    override val failure = MutableStateFlow<NativePlatformFailure?>(null)
    override fun clearFailure() {
        failure.value = null
    }
    override fun rejected(action: NativePlatformFailure) = failed(action)
    private fun failed(action: NativePlatformFailure) {
        failure.value = action
        event("platform_failure_${action.name.lowercase()}")
    }
    private fun present(controller: UIViewController, action: NativePlatformFailure) {
        try {
            var owner = host()
            while (owner.presentedViewController != null) owner = checkNotNull(owner.presentedViewController)
            check(owner.view.window != null)
            controller.popoverPresentationController?.let { popover ->
                popover.sourceView = owner.view
                popover.sourceRect = CGRectMake(0.0, 0.0, 1.0, 1.0)
            }
            owner.presentViewController(controller, true, null)
            event("platform_${action.name.lowercase()}")
        } catch (_: Exception) {
            failed(action)
        }
    }
    override fun onBackground(cancel: () -> Unit): () -> Unit {
        val center = NSNotificationCenter.defaultCenter
        val scene = runCatching { host().view.window?.windowScene }.getOrNull()
        val observers = listOf(
            center.addObserverForName(UIApplicationDidEnterBackgroundNotification, null, NSOperationQueue.mainQueue) {
                event("platform_background")
                cancel()
            },
            // SwiftUI uses scene lifecycle callbacks; application callbacks alone miss this transition.
            center.addObserverForName(UISceneDidEnterBackgroundNotification, scene, NSOperationQueue.mainQueue) {
                event("platform_background")
                cancel()
            }
        )
        return { observers.forEach(center::removeObserver) }
    }
    override fun browser(url: String) {
        try {
            val parsed = checkNotNull(NSURL.URLWithString(url))
            require(parsed.scheme in listOf("https", "http", "mailto") && parsed.user == null && parsed.password == null)
            UIApplication.sharedApplication.openURL(parsed, emptyMap<Any?, Any?>()) { accepted -> if (!accepted) failed(NativePlatformFailure.Browser) }
            event("platform_browser")
        } catch (_: Exception) {
            failed(NativePlatformFailure.Browser)
        }
    }
    override fun share(text: String) {
        present(UIActivityViewController(listOf(text), null), NativePlatformFailure.Share)
    }
    override fun copy(text: String) {
        try {
            UIPasteboard.generalPasteboard.string = text
            event("platform_clipboard")
        } catch (_: Exception) {
            failed(NativePlatformFailure.Clipboard)
        }
    }
    override fun openFile(path: String) {
        val root = NSHomeDirectory() + "/Library/Caches/TeamCityArtifacts/"
        if (!path.startsWith(root) || path.split('/').any { it == ".." } || !NSFileManager.defaultManager.fileExistsAtPath(path)) {
            failed(NativePlatformFailure.File)
            return
        }
        present(UIActivityViewController(listOf(NSURL.fileURLWithPath(path)), null), NativePlatformFailure.File)
    }
    override fun rate() {
        try {
            val scene = host().view.window?.windowScene
            if (scene == null) {
                failed(NativePlatformFailure.Rating)
            } else {
                SKStoreReviewController.requestReviewInScene(scene)
                event("platform_rating_requested")
            }
        } catch (_: Exception) {
            failed(NativePlatformFailure.Rating)
        }
    }
}

private object IosNativeDiagnostics : NativeDiagnostics {
    override fun event(name: String) {
        // Only enumerated screen/action labels enter OS diagnostics. Never log user/server/request data.
        require(name.length <= 80 && name.all { it.isLowerCase() || it == '_' })
        NSLog("TeamCityApp: $name")
    }
}

/** Foundation owns atomic file bytes; account partitioning and cache lifetime are common. */
internal class IosResponseCacheStore : teamcityapp.libraries.network.ResponseCacheStore {
    private val path = NSHomeDirectory() + "/Library/Caches/teamcity_rest_v1.json"
    override suspend fun read(): String? = withContext(Dispatchers.Default) {
        lock.withLock {
            if (!NSFileManager.defaultManager.fileExistsAtPath(path)) {
                null
            } else {
                checkNotNull(NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null))
            }
        }
    }
    override suspend fun write(value: String) = withContext(Dispatchers.Default) {
        lock.withLock {
            val bytes = value.encodeToByteArray()
            bytes.usePinned {
                val data = NSData.dataWithBytes(if (bytes.isEmpty()) null else it.addressOf(0), bytes.size.toULong())
                check(data.writeToFile(path, true)) { "Cannot persist response cache" }
            }
        }
    }
    companion object {
        private val lock = Mutex()
    }
}
