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

package com.github.vase4kin.teamcityapp.account.create.dagger

import android.content.Context
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManager
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManagerImpl
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataModel
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataModelImpl
import com.github.vase4kin.teamcityapp.account.create.helper.UrlFormatter
import com.github.vase4kin.teamcityapp.account.create.router.CreateAccountRouter
import com.github.vase4kin.teamcityapp.account.create.router.CreateAccountRouterImpl
import com.github.vase4kin.teamcityapp.account.create.tracker.CreateAccountTracker
import com.github.vase4kin.teamcityapp.account.create.tracker.CreateAccountTrackerImpl
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountView
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountViewImpl
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE_UNSAFE
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Named

@Module
class CreateAccountModule {

    @Provides
    fun providesCreateAccountView(activity: CreateAccountActivity): CreateAccountView {
        return CreateAccountViewImpl(activity)
    }

    @Provides
    fun providesCreateAccountDataManager(
        context: Context,
        @Named(CLIENT_BASE) okHttpClient: OkHttpClient,
        @Named(CLIENT_BASE_UNSAFE) unsafeOkHttpClient: OkHttpClient,
        sharedUserStorage: SharedUserStorage,
        urlFormatter: UrlFormatter
    ): CreateAccountDataManager {
        return CreateAccountDataManagerImpl(context, okHttpClient, unsafeOkHttpClient, sharedUserStorage, urlFormatter)
    }

    @Provides
    fun providesCreateAccountDataModel(sharedUserStorage: SharedUserStorage): CreateAccountDataModel {
        return CreateAccountDataModelImpl(sharedUserStorage)
    }

    @Provides
    fun providesCreateAccountRouter(activity: CreateAccountActivity): CreateAccountRouter {
        return CreateAccountRouterImpl(activity)
    }

    @Provides
    fun providesFirebaseCreateAccountTracker(firebaseAnalytics: FirebaseAnalytics): CreateAccountTracker {
        return CreateAccountTrackerImpl(firebaseAnalytics)
    }
}
