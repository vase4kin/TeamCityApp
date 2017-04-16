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

package com.github.vase4kin.teamcityapp.navigation.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.drawer.dagger.CustomDrawerModule;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager;
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter;
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.github.vase4kin.teamcityapp.navigation.dagger.DaggerNavigationComponent;
import com.github.vase4kin.teamcityapp.navigation.dagger.NavigationModule;
import com.github.vase4kin.teamcityapp.navigation.presenter.NavigationPresenterImpl;

import javax.inject.Inject;

/**
 * Activity to manage navigation between projects and build types
 */
public class NavigationActivity extends AppCompatActivity {

    @Inject
    DrawerPresenterImpl<DrawerView, DrawerDataManager, DrawerRouter, DrawerTracker> mDrawerPresenter;
    @Inject
    NavigationPresenterImpl mNavigationPresenter;

    public static void start(@NonNull String name, @NonNull String url, @NonNull Activity activity) {
        Bundle bundle = new Bundle();
        bundle.putString(BundleExtractorValues.NAME, name);
        Intent intent = new Intent(activity, NavigationActivity.class);
        bundle.putString(BundleExtractorValues.URL, url);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_list);
        View view = findViewById(android.R.id.content);

        // Injecting presenters
        DaggerNavigationComponent.builder()
                .customDrawerModule(new CustomDrawerModule(this, true, DrawerView.PROJECTS))
                .navigationModule(new NavigationModule(view, this, getIntent().getExtras()))
                .restApiComponent(((TeamCityApplication) getApplication()).getRestApiInjector())
                .build()
                .inject(this);

        mDrawerPresenter.onCreate();
        mNavigationPresenter.onViewsCreated();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNavigationPresenter.onViewsDestroyed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNavigationPresenter.onResume();
    }

    @Override
    public void onBackPressed() {
        mDrawerPresenter.onBackButtonPressed();
    }
}
