/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.login.dagger;

import android.app.Activity;
import android.content.Context;

import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManager;
import com.github.vase4kin.teamcityapp.account.create.data.CreateAccountDataManagerImpl;
import com.github.vase4kin.teamcityapp.account.create.helper.UrlFormatter;
import com.github.vase4kin.teamcityapp.login.router.LoginRouter;
import com.github.vase4kin.teamcityapp.login.router.LoginRouterImpl;
import com.github.vase4kin.teamcityapp.login.tracker.FabricLoginTrackerImpl;
import com.github.vase4kin.teamcityapp.login.tracker.FirebaseLoginTrackerImpl;
import com.github.vase4kin.teamcityapp.login.tracker.LoginTracker;
import com.github.vase4kin.teamcityapp.login.tracker.LoginTrackerImpl;
import com.github.vase4kin.teamcityapp.login.view.LoginView;
import com.github.vase4kin.teamcityapp.login.view.LoginViewImpl;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Set;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import okhttp3.OkHttpClient;

import static com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE;

@Module
public class LoginModule {

    private Activity mActivity;

    public LoginModule(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Provides
    LoginView providesLoginView() {
        return new LoginViewImpl(mActivity);
    }

    @Provides
    CreateAccountDataManager providesCreateAccountDataManager(Context context,
                                                              @Named(CLIENT_BASE) OkHttpClient okHttpClient,
                                                              SharedUserStorage sharedUserStorage,
                                                              UrlFormatter urlFormatter) {
        return new CreateAccountDataManagerImpl(context, okHttpClient, sharedUserStorage, urlFormatter);
    }

    @Provides
    LoginRouter providesLoginRouter() {
        return new LoginRouterImpl(mActivity);
    }

    @IntoSet
    @Provides
    LoginTracker providesFabricLoginTracker() {
        return new FabricLoginTrackerImpl();
    }

    @IntoSet
    @Provides
    LoginTracker providesFirebaseLoginTracker(FirebaseAnalytics firebaseAnalytics) {
        return new FirebaseLoginTrackerImpl(firebaseAnalytics);
    }

    @Provides
    LoginTracker providesLoginTracker(Set<LoginTracker> trackers) {
        return new LoginTrackerImpl(trackers);
    }

}
