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

package com.github.vase4kin.teamcityapp.filter_builds.router

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity

/**
 * Impl of [FilterBuildsRouter]
 */
class FilterBuildsRouterImpl(private val activity: FilterBuildsActivity) : FilterBuildsRouter {

    /**
     * {@inheritDoc}
     */
    override fun closeOnSuccess(filter: BuildListFilter) {
        val intent = Intent()
        intent.putExtra(FilterBuildsRouter.EXTRA_FILTER, filter)
        activity.setResult(RESULT_OK, intent)
        activity.finish()
    }

    /**
     * {@inheritDoc}
     */
    override fun closeOnCancel() {
        activity.setResult(RESULT_CANCELED, Intent())
        activity.finish()
    }

    /**
     * {@inheritDoc}
     */
    override fun closeOnBackButtonPressed() {
        activity.setResult(RESULT_CANCELED, Intent())
    }
}
