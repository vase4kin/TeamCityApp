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

package com.github.vase4kin.teamcityapp.agenttabs.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.agenttabs.dagger.AgentsTabsModule;
import com.github.vase4kin.teamcityapp.agenttabs.dagger.DaggerAgentsTabsComponent;
import com.github.vase4kin.teamcityapp.agenttabs.tracker.AgentTabsViewTracker;
import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManager;
import com.github.vase4kin.teamcityapp.base.tabs.presenter.BaseTabsPresenterImpl;
import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModel;
import com.github.vase4kin.teamcityapp.drawer.dagger.DrawerModule;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager;
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter;
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker;
import com.github.vase4kin.teamcityapp.drawer.utils.DrawerActivityStartUtils;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;

import javax.inject.Inject;

/**
 * Manages agents activity
 */
public class AgentTabsActivity extends AppCompatActivity {

    @Inject
    DrawerPresenterImpl<DrawerView, DrawerDataManager, DrawerRouter, DrawerTracker> mDrawerPresenter;
    @Inject
    BaseTabsPresenterImpl<BaseTabsViewModel, BaseTabsDataManager, AgentTabsViewTracker> mBaseTabsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_tabs);
        View view = findViewById(android.R.id.content);

        // Injecting presenters
        DaggerAgentsTabsComponent.builder()
                .drawerModule(new DrawerModule(this, false, DrawerView.AGENTS))
                .agentsTabsModule(new AgentsTabsModule(view, this))
                .restApiComponent(((TeamCityApplication) getApplication()).getRestApiInjector())
                .build()
                .inject(this);

        mDrawerPresenter.onCreate();
        mBaseTabsPresenter.onViewsCreated();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBaseTabsPresenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBaseTabsPresenter.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBaseTabsPresenter.onViewsDestroyed();
    }

    @Override
    public void onBackPressed() {
        mDrawerPresenter.onBackButtonPressed();
    }

    /**
     * Start {@link AgentTabsActivity}
     *
     * @param activity - Activity context
     */
    public static void start(final Activity activity) {
        Intent launchIntent = new Intent(activity, AgentTabsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        DrawerActivityStartUtils.startActivity(launchIntent, activity);
    }
}
