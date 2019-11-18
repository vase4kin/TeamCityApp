/*
 * Copyright 2019 Andrey Tolpeev
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

import android.content.Context
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.cache.CacheManager
import com.github.vase4kin.teamcityapp.api.cache.CacheProviders
import com.github.vase4kin.teamcityapp.dagger.modules.AppActivityBindingModule
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_AUTH
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE_UNSAFE
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager
import com.github.vase4kin.teamcityapp.remote.RemoteService
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
import teamcityapp.libraries.storage.Storage
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, AndroidInjectionModule::class, AppActivityBindingModule::class])
interface AppComponent : AndroidInjector<TeamCityApplication> {

    fun context(): Context

    fun sharedUserStorage(): SharedUserStorage

    fun storage(): Storage

    @Named(CLIENT_BASE)
    fun baseOkHttpClient(): OkHttpClient

    @Named(CLIENT_BASE_UNSAFE)
    fun unsafeBaseOkHttpClient(): OkHttpClient

    @Named(CLIENT_AUTH)
    fun authOkHttpClient(): OkHttpClient

    fun eventBus(): EventBus

    fun cacheManager(): CacheManager

    fun providers(): CacheProviders

    fun firebaseAnalytics(): FirebaseAnalytics

    fun onboardingManager(): OnboardingManager

    fun remoteService(): RemoteService
}
