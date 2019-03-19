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

package com.github.vase4kin.teamcityapp.login.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.login.dagger.DaggerLoginComponent;
import com.github.vase4kin.teamcityapp.login.dagger.LoginModule;
import com.github.vase4kin.teamcityapp.login.presenter.LoginPresenterImpl;

import javax.inject.Inject;

/**
 * Manages first login screen
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Injected presenter
     */
    @Inject
    LoginPresenterImpl mPresenter;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Injecting LoginPresenterImpl to activity
        DaggerLoginComponent.builder()
                .loginModule(new LoginModule(this))
                .appComponent(((TeamCityApplication) getApplication()).getAppInjector())
                .build()
                .inject(this);

        mPresenter.onCreate();
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    /**
     * Start LoginActivity
     */
    public static void start(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
    }
}
