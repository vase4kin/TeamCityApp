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

package com.github.vase4kin.teamcityapp.buildlist.router;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildtabs.view.BuildTabsActivity;
import com.github.vase4kin.teamcityapp.runbuild.RunBuildActivity;

/**
 * impl of {@link BuildListRouter}
 */
public class BuildListRouterImpl implements BuildListRouter {

    private Activity mActivity;

    public BuildListRouterImpl(Activity mActivity) {
        this.mActivity = mActivity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openBuildPage(Build build) {
        // TODO: Move to activity as static public method
        Intent intent = new Intent(mActivity, BuildTabsActivity.class);
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, build);
        intent.putExtras(b);
        mActivity.startActivity(intent);
        mActivity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openRunBuildPage() {
        RunBuildActivity.start(mActivity);
    }
}
