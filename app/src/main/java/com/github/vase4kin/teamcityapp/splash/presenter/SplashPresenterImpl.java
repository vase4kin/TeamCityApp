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

package com.github.vase4kin.teamcityapp.splash.presenter;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.splash.data.SplashDataManager;
import com.github.vase4kin.teamcityapp.splash.router.SplashRouter;
import com.github.vase4kin.teamcityapp.splash.view.SplashView;

import javax.inject.Inject;

/**
 * impl of {@link SplashPresenter}
 */
public class SplashPresenterImpl implements SplashPresenter {

    private SplashRouter mRouter;
    private SplashDataManager mDataManager;
    private SplashView mView;

    @Inject
    SplashPresenterImpl(@NonNull SplashRouter mRouter,
                        @NonNull SplashDataManager mDataManager,
                        @NonNull SplashView view) {
        this.mRouter = mRouter;
        this.mDataManager = mDataManager;
        this.mView = view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        if (mDataManager.hasUserAccounts()) {
            mRouter.openProjectsRootPage();
        } else {
            mRouter.openLoginPage();
        }
        mView.close();
    }
}
