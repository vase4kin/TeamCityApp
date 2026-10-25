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

import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIViewController
import teamcityapp.libraries.accounts.IosKeychainVault
import teamcityapp.libraries.accounts.VaultAccountStorage

/** UIKit supplies platform storage; common code owns the account journey and presentation. */
@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
fun MainViewController(): UIViewController {
    val environment = NSProcessInfo.processInfo.environment
    val fixture = when (environment["PHASE1_STATE"] as? String) {
        "loading" -> AboutFixture.Loading
        "unavailable" -> AboutFixture.Unavailable
        else -> AboutFixture.Available
    }
    lateinit var controller: UIViewController
    val testNamespace = if (kotlin.native.Platform.isDebugBinary) environment["TEAMCITY_UI_TEST_VAULT"] as? String else null
    val platform = IosNativePlatform({ controller }, testNamespace)
    controller = ComposeUIViewController {
        if (environment["PHASE1_STATE"] != null) {
            PhaseOneApp(fixture, darkTheme = environment["PHASE1_THEME"] == "dark")
        } else {
            val testNamespace = if (kotlin.native.Platform.isDebugBinary) environment["TEAMCITY_UI_TEST_VAULT"] as? String else null
            val model = viewModel {
                SharedAppViewModel(
                    VaultAccountStorage(IosKeychainVault(Dispatchers.Default, testNamespace?.let { "app.teamcity.uitest.$it" } ?: "app.teamcity.accounts.v1")),
                    IosSavedNavigationStore(testNamespace?.let { "navigation.uitest.$it" } ?: "navigation.phase2.snapshot.v1"),
                    platform.responseCache
                )
            }
            androidx.compose.runtime.CompositionLocalProvider(LocalNativePlatform provides platform) { PhaseTwoApp(model) }
        }
    }
    return controller
}
