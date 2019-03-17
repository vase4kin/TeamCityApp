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

package com.github.vase4kin.teamcityapp.testdetails.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.testdetails.dagger.DaggerTestDetailsComponent;
import com.github.vase4kin.teamcityapp.testdetails.dagger.TestDetailsModule;
import com.github.vase4kin.teamcityapp.testdetails.presenter.TestDetailsPresenterImpl;

import javax.inject.Inject;

/**
 * Activity to manage test details
 */
public class TestDetailsActivity extends AppCompatActivity {

    @Inject
    TestDetailsPresenterImpl mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_test_details);

        // Injecting presenter
        DaggerTestDetailsComponent.builder()
                .testDetailsModule(new TestDetailsModule(this))
                .restApiComponent(((TeamCityApplication) getApplication()).getRestApiInjector())
                .build()
                .inject(this);

        mPresenter.onCreate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    public void onBackPressed() {
        mPresenter.onBackPressed();
    }

    /**
     * Open failed test activity
     *
     * @param url      - Url to failed test
     * @param activity - Activity context
     */
    public static void openFailedTest(String url, Activity activity) {
        Intent intent = new Intent(activity, TestDetailsActivity.class);
        intent.putExtra(BundleExtractorValues.TEST_URL, url);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold);
    }
}
