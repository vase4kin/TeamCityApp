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

package com.github.vase4kin.teamcityapp.splash.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.splash.dagger.DaggerSplashComponent;
import com.github.vase4kin.teamcityapp.splash.dagger.SplashModule;
import com.github.vase4kin.teamcityapp.splash.presenter.SplashPresenterImpl;

import javax.inject.Inject;

/**
 * Activity to handle splash screen
 */
public class SplashActivity extends AppCompatActivity {

    @Inject
    SplashPresenterImpl mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Injecting presenter
        DaggerSplashComponent.builder()
                .splashModule(new SplashModule(this))
                .appComponent(((TeamCityApplication) getApplication()).getAppInjector())
                .build()
                .inject(this);

        mPresenter.onCreate();
    }
}
