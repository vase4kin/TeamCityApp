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
import android.content.Intent
import androidx.navigation3.runtime.NavKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import teamcityapp.libraries.navigation.DestinationLauncher
import teamcityapp.libraries.navigation.DestinationRegistration
import teamcityapp.libraries.navigation.DestinationRegistry

class AppDestinationLauncher @Inject constructor(private val registry: DestinationRegistry) : DestinationLauncher {
    override fun open(activity: Activity, destination: NavKey, flags: Int) {
        activity.startActivity(
            Intent(activity, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_DESTINATION, registry.encodeDestination(destination))
                .addFlags(flags or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {
    @Binds abstract fun launcher(implementation: AppDestinationLauncher): DestinationLauncher

    companion object {
        @Provides @Singleton
        fun registry(destinations: Set<@JvmSuppressWildcards DestinationRegistration>): DestinationRegistry = DestinationRegistry(destinations)
    }
}
