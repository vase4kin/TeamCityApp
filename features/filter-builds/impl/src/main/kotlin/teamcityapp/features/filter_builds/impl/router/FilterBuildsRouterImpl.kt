/*
 * Copyright 2026 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamcityapp.features.filter_builds.impl.router
import android.app.Activity
import android.content.Intent
import teamcityapp.features.filter_builds.api.BuildFilter
import teamcityapp.features.filter_builds.api.FilterBuildsResultAdapter
import teamcityapp.features.filter_builds.api.navigation.FilterBuildsNavigation
import teamcityapp.features.filter_builds.impl.FilterBuildsActivity
class FilterBuildsRouterImpl(private val activity: FilterBuildsActivity, private val results: FilterBuildsResultAdapter) : FilterBuildsRouter {
    override fun close() {
        activity.setResult(Activity.RESULT_CANCELED, Intent())
        activity.finish()
    }
    override fun apply(filter: BuildFilter) {
        activity.setResult(Activity.RESULT_OK, Intent().putExtra(FilterBuildsNavigation.EXTRA_FILTER, results.serialize(filter)))
        activity.finish()
    }
}
