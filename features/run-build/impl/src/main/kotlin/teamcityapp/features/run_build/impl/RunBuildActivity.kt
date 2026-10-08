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

package teamcityapp.features.run_build.impl

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import teamcityapp.features.run_build.impl.RunBuildRoute
import teamcityapp.features.run_build.impl.router.RunBuildRouter
import teamcityapp.libraries.resources.R as SharedR
import teamcityapp.libraries.theme.TeamCityTheme

@AndroidEntryPoint
class RunBuildActivity : AppCompatActivity() {
    @Inject lateinit var router: RunBuildRouter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TeamCityTheme(legacyColors = true) { RunBuildRoute(router) } }
    }
    override fun finish() {
        super.finish()
        overridePendingTransition(SharedR.anim.hold, SharedR.anim.slide_out_bottom)
    }
}
