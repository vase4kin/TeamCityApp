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

package com.github.vase4kin.teamcityapp.filter_builds;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.github.vase4kin.teamcityapp.R;

/**
 * Activity to manage builds filtering logic
 */
public class FilterBuildsActivity extends AppCompatActivity {

    /**
     * Activity request code
     */
    public static final int REQUEST_CODE = 12921;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_builds);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold, R.anim.slide_out_bottom);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    /**
     * Start run build activity
     *
     * @param activity - Activity instance
     */
    public static void startForResult(@NonNull Activity activity, String buildTypeId) {
        Intent intent = new Intent(activity, FilterBuildsActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold);
    }
}
