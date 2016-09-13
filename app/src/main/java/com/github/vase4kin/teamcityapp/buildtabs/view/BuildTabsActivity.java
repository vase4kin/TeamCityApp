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

package com.github.vase4kin.teamcityapp.buildtabs.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.buildtabs.dagger.BuildTabsModule;
import com.github.vase4kin.teamcityapp.buildtabs.dagger.DaggerBuildTabsComponent;
import com.github.vase4kin.teamcityapp.buildtabs.presenter.BuildTabsDrawerPresenterImpl;
import com.github.vase4kin.teamcityapp.buildtabs.presenter.BuildTabsPresenterImpl;
import com.github.vase4kin.teamcityapp.drawer.dagger.CustomDrawerModule;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;

import javax.inject.Inject;

/**
 * Activity to manage build details info
 */
public class BuildTabsActivity extends AppCompatActivity {

    @Inject
    BuildTabsDrawerPresenterImpl mDrawerPresenter;
    @Inject
    BuildTabsPresenterImpl mBuildTabsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build);

        View view = findViewById(android.R.id.content);

        // Injecting presenters
        DaggerBuildTabsComponent.builder()
                .customDrawerModule(new CustomDrawerModule(this, true, DrawerView.PROJECTS))
                .buildTabsModule(new BuildTabsModule(view, this))
                .restApiComponent(((TeamCityApplication) getApplication()).getRestApiInjector())
                .build()
                .inject(this);

        mDrawerPresenter.onCreate();
        mBuildTabsPresenter.onViewsCreated();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBuildTabsPresenter.onViewsDestroyed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBuildTabsPresenter.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mBuildTabsPresenter.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mBuildTabsPresenter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mBuildTabsPresenter.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        mDrawerPresenter.onBackButtonPressed();
    }
}
