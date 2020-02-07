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

package com.github.vase4kin.teamcityapp.login.dagger

import android.content.Context
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManager
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManagerImpl
import com.github.vase4kin.teamcityapp.account.create.helper.UrlFormatter
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE_UNSAFE
import com.github.vase4kin.teamcityapp.login.router.LoginRouter
import com.github.vase4kin.teamcityapp.login.router.LoginRouterImpl
import com.github.vase4kin.teamcityapp.login.tracker.LoginTracker
import com.github.vase4kin.teamcityapp.login.tracker.LoginTrackerImpl
import com.github.vase4kin.teamcityapp.login.view.LoginActivity
import com.github.vase4kin.teamcityapp.login.view.LoginView
import com.github.vase4kin.teamcityapp.login.view.LoginViewImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Named

@Module
object LoginModule {

    @JvmStatic
    @Provides
    fun providesLoginView(activity: LoginActivity): LoginView {
        return LoginViewImpl(activity)
    }

    @JvmStatic
    @Provides
    fun providesCreateAccountDataManager(
        context: Context,
        @Named(CLIENT_BASE) baseOkHttpClient: OkHttpClient,
        @Named(CLIENT_BASE_UNSAFE) unsafeBaseOkHttpClient: OkHttpClient,
        sharedUserStorage: SharedUserStorage,
        urlFormatter: UrlFormatter
    ): CreateAccountDataManager {
        return CreateAccountDataManagerImpl(
            context, baseOkHttpClient, unsafeBaseOkHttpClient, sharedUserStorage, urlFormatter
        )
    }

    @JvmStatic
    @Provides
    fun providesLoginRouter(activity: LoginActivity): LoginRouter {
        return LoginRouterImpl(activity)
    }

    @JvmStatic
    @Provides
    fun providesFirebaseLoginTracker(firebaseAnalytics: FirebaseAnalytics): LoginTracker {
        return LoginTrackerImpl(firebaseAnalytics)
    }
}
