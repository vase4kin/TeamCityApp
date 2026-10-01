/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.dagger.components

import com.github.vase4kin.teamcityapp.account.create.dagger.UrlFormatterModule
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.dagger.scopes.UserScope
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Component
import org.greenrobot.eventbus.EventBus
import teamcityapp.libraries.cache_manager.CacheManager
import teamcityapp.libraries.onboarding.OnboardingManager
import teamcityapp.libraries.remote.RemoteService

@UserScope
@Component(
    dependencies = [AppComponent::class],
    modules = [
        RestApiModule::class,
        UrlFormatterModule::class,
    ]
)
interface RestApiComponent {

    fun teamCityService(): TeamCityService

    fun repository(): Repository

    fun cacheManager(): CacheManager

    fun sharedUserStorage(): SharedUserStorage

    fun eventBus(): EventBus

    fun firebaseAnalytics(): FirebaseAnalytics

    fun onboardingManager(): OnboardingManager

    fun remoteService(): RemoteService
}
