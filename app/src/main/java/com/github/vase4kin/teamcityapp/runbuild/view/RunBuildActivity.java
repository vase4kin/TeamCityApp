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

package com.github.vase4kin.teamcityapp.runbuild.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.runbuild.dagger.DaggerRunBuildComponent;
import com.github.vase4kin.teamcityapp.runbuild.dagger.RunBuildModule;
import com.github.vase4kin.teamcityapp.runbuild.presenter.RunBuildPresenterImpl;

import javax.inject.Inject;

import static com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractorKt.EXTRA_BUILD_TYPE_ID;

/**
 * Run build activity
 */
public class RunBuildActivity extends AppCompatActivity {

    /**
     * Activity request code
     */
    public static final int REQUEST_CODE = 489;

    @Inject
    RunBuildPresenterImpl mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_build);

        // Injecting presenter
        DaggerRunBuildComponent.builder()
                .runBuildModule(new RunBuildModule(this))
                .restApiComponent(((TeamCityApplication) getApplication()).getRestApiInjector())
                .build()
                .inject(this);

        mPresenter.onCreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold, R.anim.slide_out_bottom);
    }

    @Override
    public void onBackPressed() {
        mPresenter.onBackPressed();
        super.onBackPressed();
    }

    /**
     * Start run build activity
     *
     * @param activity - Activity instance
     */
    public static void startForResult(@NonNull Activity activity, String buildTypeId) {
        Intent intent = new Intent(activity, RunBuildActivity.class);
        intent.putExtra(EXTRA_BUILD_TYPE_ID, buildTypeId);
        activity.startActivityForResult(intent, REQUEST_CODE);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold);
    }
}
