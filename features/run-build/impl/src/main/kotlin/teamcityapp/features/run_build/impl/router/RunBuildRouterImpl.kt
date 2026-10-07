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

package teamcityapp.features.run_build.impl.router
import android.app.Activity
import android.content.Intent
import teamcityapp.features.run_build.api.navigation.RunBuildNavigation
import teamcityapp.features.run_build.impl.RunBuildActivity
import teamcityapp.features.run_build.impl.router.RunBuildRouter
class RunBuildRouterImpl(private val activity: RunBuildActivity) : RunBuildRouter {
    override fun close() {
        activity.setResult(Activity.RESULT_CANCELED, Intent())
        activity.finish()
    }

    override fun queued(href: String) {
        activity.setResult(Activity.RESULT_OK, Intent().putExtra(RunBuildNavigation.EXTRA_HREF, href))
        activity.finish()
    }
}
