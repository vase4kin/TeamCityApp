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

package teamcityapp.libraries.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/**
 * UI boundary for existing routes whose ViewModels read arguments from SavedStateHandle.
 * Feature implementations translate their plain typed destination into these defaults. The
 * decorated entry still owns its factory, saved-state registry and ViewModel store; restored
 * handle values take precedence over incoming defaults.
 */
@Composable
fun DestinationArguments(arguments: Bundle, content: @Composable () -> Unit) {
    val owner = checkNotNull(LocalViewModelStoreOwner.current)
    val argumentOwner = remember(owner, arguments) { ArgumentViewModelStoreOwner(owner, arguments) }
    CompositionLocalProvider(LocalViewModelStoreOwner provides argumentOwner, content = content)
}

private class ArgumentViewModelStoreOwner(
    private val owner: ViewModelStoreOwner,
    arguments: Bundle
) : ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory {
    private val defaults = Bundle(arguments)
    private val factoryOwner = checkNotNull(owner as? HasDefaultViewModelProviderFactory) {
        "Destination arguments require a decorated ViewModel owner"
    }
    override val viewModelStore: ViewModelStore get() = owner.viewModelStore
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory get() = factoryOwner.defaultViewModelProviderFactory
    override val defaultViewModelCreationExtras: CreationExtras
        get() = MutableCreationExtras(factoryOwner.defaultViewModelCreationExtras).apply { this[DEFAULT_ARGS_KEY] = defaults }
}
