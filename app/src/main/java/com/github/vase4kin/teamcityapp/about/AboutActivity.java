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

package com.github.vase4kin.teamcityapp.about;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.about.dagger.DaggerAboutPageComponent;
import com.github.vase4kin.teamcityapp.drawer.dagger.DrawerModule;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager;
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter;
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker;
import com.github.vase4kin.teamcityapp.drawer.utils.DrawerActivityStartUtils;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.mikepenz.aboutlibraries.LibsBuilder;

import javax.inject.Inject;

/**
 * About screen activity
 */
public class AboutActivity extends AppCompatActivity {

    @Inject
    DrawerPresenterImpl<DrawerView, DrawerDataManager, DrawerRouter, DrawerTracker> mDrawerPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Injecting DrawerPresenterImpl to activity
        DaggerAboutPageComponent.builder()
                .drawerModule(new DrawerModule(this, false, DrawerView.ABOUT))
                .restApiComponent(((TeamCityApplication) getApplication()).getRestApiInjector())
                .build()
                .inject(this);

        mDrawerPresenter.onCreate();

        // About library fragment
        Fragment aboutLibrary = new LibsBuilder()
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withLicenseShown(true)
                .withAboutDescription(getString(R.string.about_app_description))
                .supportFragment();

        // Commit fragment to container
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.about_library_container, aboutLibrary)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        mDrawerPresenter.onBackButtonPressed();
    }

    /**
     * Start About activity with Flag {@link Intent#FLAG_ACTIVITY_SINGLE_TOP}
     */
    public static void start(Activity activity) {
        Intent launchIntent = new Intent(activity, AboutActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        DrawerActivityStartUtils.startActivity(launchIntent, activity);
    }
}
