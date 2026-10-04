/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.test_details.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import teamcityapp.features.test_details.R
import teamcityapp.features.test_details.viewmodel.TEST_URL_KEY
import teamcityapp.features.test_details.viewmodel.TestDetailsUiState
import teamcityapp.features.test_details.viewmodel.TestDetailsViewModel
import teamcityapp.libraries.theme.TeamCityTheme

/**
 * Activity to manage test details
 */
@AndroidEntryPoint
class TestDetailsActivity : AppCompatActivity() {

    private val viewModel: TestDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(state) {
                if (state == TestDetailsUiState.InvalidInput) {
                    Toast.makeText(this@TestDetailsActivity, R.string.error_view_error_text, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            TeamCityTheme { TestDetailsScreen(state, viewModel::retry, ::finish) }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.hold, R.anim.slide_out_bottom)
    }

    companion object {

        const val ARG_TEST_URL = TEST_URL_KEY

        /**
         * Open failed test activity
         *
         * @param url - Url to failed test
         * @param activity - Activity context
         */
        fun openFailedTest(url: String, activity: Activity) {
            val intent = Intent(activity, TestDetailsActivity::class.java)
            intent.putExtra(ARG_TEST_URL, url)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold)
        }
    }
}
