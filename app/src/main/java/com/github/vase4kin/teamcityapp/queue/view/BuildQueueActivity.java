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

package com.github.vase4kin.teamcityapp.queue.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.drawer.dagger.DrawerModule;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager;
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter;
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker;
import com.github.vase4kin.teamcityapp.drawer.utils.DrawerActivityStartUtils;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.github.vase4kin.teamcityapp.queue.dagger.DaggerQueuedListComponent;
import com.github.vase4kin.teamcityapp.queue.dagger.QueuedListModule;
import com.github.vase4kin.teamcityapp.runningbuilds.presenter.RunningBuildsListPresenterImpl;

import javax.inject.Inject;

/**
 * Activity to manage Build queue
 */
public class BuildQueueActivity extends AppCompatActivity {

    @Inject
    DrawerPresenterImpl<DrawerView, DrawerDataManager, DrawerRouter, DrawerTracker> mDrawerPresenter;
    @Inject
    RunningBuildsListPresenterImpl mBuildListPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_list);
        View view = findViewById(android.R.id.content);

        // Injecting presenter
        DaggerQueuedListComponent.builder()
                .drawerModule(new DrawerModule(this, false, DrawerView.BUILD_QUEUE))
                .queuedListModule(new QueuedListModule(view, this))
                .restApiComponent(((TeamCityApplication) getApplication()).getRestApiInjector())
                .build()
                .inject(this);

        mDrawerPresenter.onCreate();
        mBuildListPresenter.onViewsCreated();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBuildListPresenter.onViewsDestroyed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBuildListPresenter.onResume();
    }

    @Override
    public void onBackPressed() {
        mDrawerPresenter.onBackButtonPressed();
    }

    /**
     * Start {@link BuildQueueActivity}
     *
     * @param activity - Activity context
     */
    public static void start(final Activity activity) {
        Intent launchIntent = new Intent(activity, BuildQueueActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        DrawerActivityStartUtils.startActivity(launchIntent, activity);
    }
}
