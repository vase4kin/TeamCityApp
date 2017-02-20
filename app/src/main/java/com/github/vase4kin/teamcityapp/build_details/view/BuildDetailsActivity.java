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

package com.github.vase4kin.teamcityapp.build_details.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.build_details.dagger.BuildDetailsModule;
import com.github.vase4kin.teamcityapp.build_details.dagger.DaggerBuildDetailsComponent;
import com.github.vase4kin.teamcityapp.build_details.presenter.BuildDetailsDrawerPresenterImpl;
import com.github.vase4kin.teamcityapp.build_details.presenter.BuildDetailsPresenterImpl;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.drawer.dagger.CustomDrawerModule;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;

import javax.inject.Inject;

/**
 * Activity to manage build details info
 */
public class BuildDetailsActivity extends AppCompatActivity {

    @Inject
    BuildDetailsDrawerPresenterImpl mDrawerPresenter;
    @Inject
    BuildDetailsPresenterImpl mBuildTabsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build);
        injectPresenters();
        mDrawerPresenter.onCreate();
        mBuildTabsPresenter.onViewsCreated();
    }

    /**
     * Inject presenters
     */
    private void injectPresenters() {
        View view = findViewById(android.R.id.content);
        DaggerBuildDetailsComponent.builder()
                .customDrawerModule(new CustomDrawerModule(this, true, DrawerView.PROJECTS))
                .buildDetailsModule(new BuildDetailsModule(view, this))
                .restApiComponent(((TeamCityApplication) getApplication()).getRestApiInjector())
                .build()
                .inject(this);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        injectPresenters();
        mBuildTabsPresenter.onViewsCreated();
    }

    /**
     * Open {@link this} activity
     *
     * @param activity      - Activity
     * @param build         - Build to be passed
     * @param buildTypeName - Build type name
     */
    public static void start(Activity activity, Build build, @Nullable String buildTypeName) {
        Intent intent = new Intent(activity, BuildDetailsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, build);
        b.putString(BundleExtractorValues.NAME, buildTypeName);
        intent.putExtras(b);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }
}
