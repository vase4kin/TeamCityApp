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

package com.github.vase4kin.teamcityapp.runbuild.router;

import android.content.Intent;

import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildActivity;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Impl of {@link RunBuildRouter}
 */
public class RunBuildRouterImpl implements RunBuildRouter {

    private RunBuildActivity mActivity;

    public RunBuildRouterImpl(RunBuildActivity activity) {
        this.mActivity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeOnSuccess(String queuedBuildHref) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_HREF, queuedBuildHref);
        mActivity.setResult(RESULT_OK, intent);
        mActivity.finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeOnCancel() {
        mActivity.setResult(RESULT_CANCELED, new Intent());
        mActivity.finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeOnBackButtonPressed() {
        mActivity.setResult(RESULT_CANCELED, new Intent());
    }
}
