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

package com.github.vase4kin.teamcityapp.account.create.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.account.create.dagger.CreateAccountModule;
import com.github.vase4kin.teamcityapp.account.create.dagger.DaggerCreateAccountComponent;
import com.github.vase4kin.teamcityapp.account.create.presenter.CreateAccountPresenterImpl;

import javax.inject.Inject;

/**
 * Create account activity
 */
public class CreateAccountActivity extends AppCompatActivity {

    @Inject
    CreateAccountPresenterImpl mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Injecting CreateAccountPresenterImpl to activity
        DaggerCreateAccountComponent.builder()
                .createAccountModule(new CreateAccountModule(this))
                .appComponent(((TeamCityApplication) getApplication()).getAppInjector())
                .build()
                .inject(this);

        mPresenter.handleOnCreateView();
    }

    @Override
    protected void onDestroy() {
        mPresenter.handleOnDestroy();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.handleOnResume();
    }

    @Override
    public void onBackPressed() {
        mPresenter.finish();
    }
}
