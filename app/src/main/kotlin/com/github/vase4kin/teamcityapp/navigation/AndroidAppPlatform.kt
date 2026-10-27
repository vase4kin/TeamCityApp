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

package com.github.vase4kin.teamcityapp.navigation

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import teamcityapp.features.artifacts.api.DownloadedArtifact
import teamcityapp.libraries.analytics.AnalyticsTracker
import teamcityapp.libraries.app_rating.AppRating
import teamcityapp.libraries.app_theme.ThemePreferencesRepository
import teamcityapp.libraries.shared_data.ArtifactFileStore
import teamcityapp.libraries.shared_data.ArtifactTarget
import teamcityapp.libraries.shared_data.PendingArtifact
import teamcityapp.shared.*

/** UI-owned OS launchers only. Feature rules and request execution live in the common root. */
class AndroidAppPlatform(
    private val activity: Activity,
    private val lifecycle: Lifecycle,
    override val theme: ThemePreferencesRepository,
    private val rating: AppRating,
    override val analytics: AnalyticsTracker
) : NativePlatform {
    override val responseCache: teamcityapp.libraries.network.ResponseCacheStore = AndroidResponseCacheStore(activity.applicationContext)
    override val powerSaving = kotlinx.coroutines.flow.callbackFlow {
        val context = activity.applicationContext
        val power = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        trySend(power.isPowerSaveMode)
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(power.isPowerSaveMode)
            }
        }
        androidx.core.content.ContextCompat.registerReceiver(context, receiver, android.content.IntentFilter(android.os.PowerManager.ACTION_POWER_SAVE_MODE_CHANGED), androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED)
        awaitClose { context.unregisterReceiver(receiver) }
    }
    override fun openSourceLibraries() {
        com.google.android.gms.oss.licenses.OssLicensesMenuActivity.setActivityTitle(activity.getString(teamcityapp.features.about.impl.R.string.about_app_text_libraries))
        activity.startActivity(Intent(activity, com.google.android.gms.oss.licenses.OssLicensesMenuActivity::class.java))
    }
    override val files: ArtifactFileStore = AndroidArtifactFileStore(activity.applicationContext)
    override val failure = MutableStateFlow<NativePlatformFailure?>(null)
    override fun clearFailure() {
        failure.value = null
    }
    override fun rejected(action: NativePlatformFailure) {
        failure.value = action
    }
    override fun onBackground(cancel: () -> Unit): () -> Unit {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) cancel() }
        lifecycle.addObserver(observer)
        return { lifecycle.removeObserver(observer) }
    }
    private fun perform(action: NativePlatformFailure, block: () -> Unit) {
        try {
            block()
        } catch (_: Exception) {
            rejected(action)
        }
    }
    override fun browser(url: String) = perform(NativePlatformFailure.Browser) {
        val uri = Uri.parse(url)
        require(uri.scheme in listOf("http", "https", "mailto") && uri.userInfo == null)
        if (uri.scheme == "mailto") {
            activity.startActivity(Intent(Intent.ACTION_SENDTO, uri))
        } else {
            CustomTabsIntent.Builder().build().launchUrl(activity, uri)
        }
    }
    override fun share(text: String) = perform(NativePlatformFailure.Share) {
        activity.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null))
    }
    override fun copy(text: String) = perform(NativePlatformFailure.Clipboard) {
        (activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("TeamCityApp", text))
    }
    override fun openFile(path: String): Boolean = runCatching {
        val file = File(path).canonicalFile
        val roots = listOfNotNull(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), activity.filesDir, File(activity.cacheDir, "TeamCityArtifacts"))
        require(file.isFile && roots.any { file.path.startsWith(it.canonicalPath + File.separator) })
        val uri = FileProvider.getUriForFile(activity, activity.packageName + ".provider", file)
        val mime = activity.contentResolver.getType(uri) ?: "application/octet-stream"
        activity.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType(mime).putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), null))
        true
    }.getOrDefault(false)
    override fun rate() = perform(NativePlatformFailure.Rating) { rating.open(activity) }
}

/** The common transfer target owns naming; this adapter only writes and atomically moves bytes. */
internal class AndroidArtifactFileStore(context: Context) : ArtifactFileStore {
    private val context = context.applicationContext
    override fun create(target: ArtifactTarget): PendingArtifact {
        val folder = File(context.cacheDir, "TeamCityArtifacts/${target.transferId}")
        check(!folder.exists() && folder.mkdirs()) { "Cannot create download directory" }
        val pending = File(folder, target.pendingName)
        val output = File(folder, target.name)
        val stream = FileOutputStream(pending)
        return object : PendingArtifact {
            private var open = true
            private var complete = false
            override fun write(bytes: ByteArray, count: Int) {
                check(open)
                stream.write(bytes, 0, count)
            }
            override fun finish(): DownloadedArtifact {
                check(open)
                stream.fd.sync()
                stream.close()
                open = false
                check(pending.renameTo(output)) { "Cannot finish artifact download" }
                complete = true
                return DownloadedArtifact(output.absolutePath)
            }
            override fun close() {
                if (open) {
                    stream.close()
                    open = false
                }
                if (!complete) folder.deleteRecursively()
            }
        }
    }
}
