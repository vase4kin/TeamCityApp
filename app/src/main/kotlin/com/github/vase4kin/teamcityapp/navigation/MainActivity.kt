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

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.libraries.navigation.DestinationRegistry
import teamcityapp.libraries.navigation.NavigationHost
import teamcityapp.libraries.navigation.rememberNavigator
import teamcityapp.libraries.theme.TeamCitySystemBars
import teamcityapp.libraries.theme.TeamCityTheme

/** Single Compose host. Startup and remaining destinations are migrated into this host in subsequent slices. */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var registry: DestinationRegistry
    private val incomingRequest = mutableStateOf<DestinationRequest?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initial = intent.getStringExtra(EXTRA_DESTINATION)?.let(registry::decodeDestination) ?: AboutDestination
        enableEdgeToEdge()
        setContent {
            TeamCityTheme {
                TeamCitySystemBars(window)
                val navigator = rememberNavigator(initial, registry)
                val request = incomingRequest.value
                LaunchedEffect(request) {
                    request?.let { navigator.push(registry.decodeDestination(it.payload)) }
                    incomingRequest.value = null
                }
                NavigationHost(navigator, registry, ::finish)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(EXTRA_DESTINATION)?.let { incomingRequest.value = DestinationRequest(it) }
    }

    private class DestinationRequest(val payload: String)
    companion object {
        const val EXTRA_DESTINATION = "navigation.destination"
    }
}
