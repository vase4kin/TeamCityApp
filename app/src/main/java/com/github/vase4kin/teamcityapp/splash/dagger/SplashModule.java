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

package com.github.vase4kin.teamcityapp.splash.dagger;

import android.app.Activity;

import com.github.vase4kin.teamcityapp.splash.data.SplashDataManager;
import com.github.vase4kin.teamcityapp.splash.data.SplashDataManagerImpl;
import com.github.vase4kin.teamcityapp.splash.router.SplashRouter;
import com.github.vase4kin.teamcityapp.splash.router.SplashRouterImpl;
import com.github.vase4kin.teamcityapp.splash.view.SplashView;
import com.github.vase4kin.teamcityapp.splash.view.SplashViewImpl;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import dagger.Module;
import dagger.Provides;

@Module
public class SplashModule {

    private Activity mActivity;

    public SplashModule(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Provides
    SplashRouter providesSplashRouter() {
        return new SplashRouterImpl(mActivity);
    }

    @Provides
    SplashDataManager providesSplashDataManager(SharedUserStorage sharedUserStorage) {
        return new SplashDataManagerImpl(sharedUserStorage);
    }

    @Provides
    SplashView providesSplashView() {
        return new SplashViewImpl(mActivity);
    }
}
