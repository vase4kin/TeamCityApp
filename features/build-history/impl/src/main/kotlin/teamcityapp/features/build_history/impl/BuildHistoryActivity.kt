/*
 * Copyright 2019 Andrey Tolpeev
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.build_history.impl

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable
import javax.inject.Inject
import teamcityapp.features.build_history.api.BuildHistoryFilterAdapter
import teamcityapp.features.build_history.api.BuildHistoryNavigation
import teamcityapp.features.build_history.impl.router.BuildHistoryActivityRouter
import teamcityapp.features.filter_builds.api.navigation.FilterBuildsNavigation
import teamcityapp.features.run_build.api.navigation.RunBuildNavigation
import teamcityapp.libraries.theme.TeamCitySystemBars
import teamcityapp.libraries.theme.TeamCityTheme

@AndroidEntryPoint
class BuildHistoryActivity : AppCompatActivity() {
    @Inject lateinit var router: BuildHistoryActivityRouter

    @Inject lateinit var filterAdapter: BuildHistoryFilterAdapter
    private val viewModel: BuildHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Installed callers still pass the legacy Serializable. Only the normalized locator
        // enters retained feature state; saved state takes precedence after recreation.
        if (!intent.hasExtra(BuildHistoryNavigation.EXTRA_LOCATOR)) {
            IntentCompat.getSerializableExtra(intent, BuildHistoryNavigation.EXTRA_LEGACY_FILTER, Serializable::class.java)?.let {
                intent.putExtra(BuildHistoryNavigation.EXTRA_LOCATOR, filterAdapter.locator(it))
            }
        }
        intent.removeExtra(BuildHistoryNavigation.EXTRA_LEGACY_FILTER)
        enableEdgeToEdge()
        setContent {
            TeamCityTheme {
                TeamCitySystemBars(window)
                BuildHistoryRoute(router, viewModel)
            }
        }
    }

    @Deprecated("Compatibility with the existing feature result contracts")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            RunBuildNavigation.REQUEST_CODE -> viewModel.onQueuedBuildResult(data?.getStringExtra(RunBuildNavigation.EXTRA_HREF).orEmpty())

            FilterBuildsNavigation.REQUEST_CODE -> {
                data?.let { IntentCompat.getSerializableExtra(it, FilterBuildsNavigation.EXTRA_FILTER, Serializable::class.java) }?.let {
                    viewModel.applyFilter(filterAdapter.locator(it))
                }
            }
        }
    }
    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right)
    }
    override fun onDestroy() {
        if (isChangingConfigurations) viewModel.onConfigurationRecreation()
        super.onDestroy()
    }
}
