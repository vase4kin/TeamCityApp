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

package com.github.vase4kin.teamcityapp.favorites.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.drawer.dagger.CustomDrawerModule;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager;
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter;
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker;
import com.github.vase4kin.teamcityapp.drawer.utils.DrawerActivityStartUtils;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView;
import com.github.vase4kin.teamcityapp.favorites.dagger.DaggerFavoritesComponent;
import com.github.vase4kin.teamcityapp.favorites.dagger.FavoritesModule;
import com.github.vase4kin.teamcityapp.favorites.presenter.FavoritesPresenterImpl;

import javax.inject.Inject;

/**
 * Screen for favorite build types
 */
public class FavoritesActivity extends AppCompatActivity {

    @Inject
    DrawerPresenterImpl<DrawerView, DrawerDataManager, DrawerRouter, DrawerTracker> drawerPresenter;
    @Inject
    FavoritesPresenterImpl favoritesPresenter;

    public static void start(@NonNull Activity activity) {
        Intent launchIntent = new Intent(activity, FavoritesActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        DrawerActivityStartUtils.startActivity(launchIntent, activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        View view = findViewById(android.R.id.content);

        // Injecting presenters
        DaggerFavoritesComponent.builder()
                .customDrawerModule(new CustomDrawerModule(this, false, DrawerView.FAVORITES))
                .favoritesModule(new FavoritesModule(view, this))
                .restApiComponent(((TeamCityApplication) getApplication()).getRestApiInjector())
                .build()
                .inject(this);

        drawerPresenter.onCreate();
        favoritesPresenter.onViewsCreated();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        favoritesPresenter.onViewsDestroyed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        favoritesPresenter.onResume();
    }

    @Override
    public void onBackPressed() {
        drawerPresenter.onBackButtonPressed();
    }
}
