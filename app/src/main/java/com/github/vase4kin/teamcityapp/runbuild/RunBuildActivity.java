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

package com.github.vase4kin.teamcityapp.runbuild;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListener;
import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListenerImpl;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

/**
 * Run build activity
 */
public class RunBuildActivity extends AppCompatActivity implements OnToolBarNavigationListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_build);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_run_build);
        }

        // For ui testing purpose
        mToolbar.setNavigationContentDescription(R.string.navigate_up);
        mToolbar.setNavigationIcon(new IconDrawable(this, MaterialIcons.md_close).color(Color.WHITE).actionBarSize());
        mToolbar.setNavigationOnClickListener(new OnToolBarNavigationListenerImpl(this));

    }

    @Override
    public void onClick() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold, R.anim.slide_out_bottom);
    }

    /**
     * Start run build activity
     *
     * @param activity - Activity instance
     */
    public static void start(@NonNull Activity activity) {
        Intent intent = new Intent(activity, RunBuildActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold);
    }
}
