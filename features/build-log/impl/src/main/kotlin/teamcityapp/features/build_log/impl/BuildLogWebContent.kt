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

package teamcityapp.features.build_log.impl

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner

internal const val LOG_SCRIPT = "$('topWrapper').style.display='none';\n" +
    "document.getElementsByClassName('tabsTable')[0].style.display='none';\n" +
    "document.getElementById('mainNavigation').style.display='none';\n" +
    "document.getElementsByClassName('footerMainContainer')[0].style.display='none';\n" +
    "document.getElementsByClassName('subTabsRight')[0].style.display='none';\n" +
    "document.body.className = '';"

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun BuildLogWebContent(url: String, attempt: Int, pageDelay: Long, onStarted: () -> Unit, onFinished: () -> Unit, onError: () -> Unit, modifier: Modifier = Modifier) {
    val started by rememberUpdatedState(onStarted)
    val finished by rememberUpdatedState(onFinished)
    val error by rememberUpdatedState(onError)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var webView by remember { mutableStateOf<WebView?>(null) }
    val handler = remember { Handler(Looper.getMainLooper()) }
    var loadedAttempt by remember { mutableIntStateOf(-1) }
    var loadedUrl by remember { mutableStateOf("") }
    var failed by remember { mutableStateOf(false) }
    val client = remember {
        object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                handler.removeCallbacksAndMessages(null)
                failed = false
                started()
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                if (failed) return
                view?.evaluateJavascript(LOG_SCRIPT, null)
                handler.postDelayed({ if (!failed) finished() }, pageDelay)
            }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, problem: WebResourceError?) {
                if (request?.isForMainFrame == true) {
                    failed = true
                    handler.removeCallbacksAndMessages(null)
                    error()
                }
            }
            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, response: WebResourceResponse?) {
                if (request?.isForMainFrame == true) {
                    failed = true
                    handler.removeCallbacksAndMessages(null)
                    error()
                }
            }
        }
    }
    DisposableEffect(lifecycle, webView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> webView?.onResume()
                Lifecycle.Event.ON_PAUSE -> webView?.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) webView?.onResume() else webView?.onPause()
        onDispose { lifecycle.removeObserver(observer) }
    }
    DisposableEffect(Unit) { onDispose { handler.removeCallbacksAndMessages(null) } }
    AndroidView(modifier = modifier, factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            webViewClient = client
            webView = this
        }
    }, onRelease = { view ->
        handler.removeCallbacksAndMessages(null)
        view.stopLoading()
        view.webViewClient = WebViewClient()
        view.destroy()
        webView = null
    }, update = { view ->
        if (loadedAttempt != attempt || loadedUrl != url) {
            loadedAttempt = attempt
            loadedUrl = url
            failed = false
            view.loadUrl(url)
        }
    })
}
