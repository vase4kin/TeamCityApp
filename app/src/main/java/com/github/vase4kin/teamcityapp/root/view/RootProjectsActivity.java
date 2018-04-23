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

package com.github.vase4kin.teamcityapp.root.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.drawer.utils.DrawerActivityStartUtils;
import com.github.vase4kin.teamcityapp.root.dagger.DaggerRootComponent;
import com.github.vase4kin.teamcityapp.root.dagger.RootModule;
import com.github.vase4kin.teamcityapp.root.extractor.RootBundleValueManagerImpl;
import com.github.vase4kin.teamcityapp.root.presenter.RootDrawerPresenterImpl;

import javax.inject.Inject;


public class RootProjectsActivity extends AppCompatActivity implements OnAccountSwitchListener {

    static {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // $COVERAGE-IGNORE$
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true); // $COVERAGE-IGNORE$
        }
    }

    @Inject
    RootDrawerPresenterImpl mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root_projects_list);
        injectPresenter();
        mPresenter.onCreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mPresenter.updateRootBundleValueManager(new RootBundleValueManagerImpl(intent.getExtras()));
        mPresenter.onAccountSwitch();
        mPresenter.onNewIntent();
    }

    @Override
    public void onBackPressed() {
        mPresenter.onBackButtonPressed();
    }

    @Override
    public void onAccountSwitch() {
        injectPresenter();
    }

    private void injectPresenter() {
        mPresenter = null;
        DaggerRootComponent.builder()
                .rootModule(new RootModule(this))
                .restApiComponent(((TeamCityApplication) getApplication()).getRestApiInjector())
                .build()
                .inject(this);
    }

    public static void startForTheFirstStart(Activity activity) {
        Intent intent = new Intent(activity, RootProjectsActivity.class);
        intent.putExtra(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED, true);
        activity.startActivity(intent);
    }

    public static void startWhenNewAccountIsCreated(Activity activity) {
        Intent launchIntent = new Intent(activity, RootProjectsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launchIntent.putExtra(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, true);
        launchIntent.putExtra(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED, true);
        activity.startActivity(launchIntent);
    }

    public static void startWhenNavigateToRootFromDrawer(final Activity activity) {
        Intent launchIntent = new Intent(activity, RootProjectsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        DrawerActivityStartUtils.startActivity(launchIntent, activity);
    }

    public static void startWhenSwitchingAccountsFromDrawer(final Activity activity) {
        Intent launchIntent = new Intent(activity, RootProjectsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launchIntent.putExtra(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, true);
        DrawerActivityStartUtils.startActivity(launchIntent, activity);
    }

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, RootProjectsActivity.class);
        activity.startActivity(intent);
    }
}
