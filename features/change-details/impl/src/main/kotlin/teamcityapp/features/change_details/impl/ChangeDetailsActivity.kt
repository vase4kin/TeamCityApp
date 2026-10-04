/*
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

package teamcityapp.features.change_details.impl

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import teamcityapp.features.change_details.impl.router.ChangeDetailsRouter
import teamcityapp.libraries.theme.TeamCityTheme
import javax.inject.Inject

@AndroidEntryPoint
class ChangeDetailsActivity : AppCompatActivity() {
    @Inject lateinit var router: ChangeDetailsRouter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TeamCityTheme { ChangeDetailsRoute(router) } }
    }
    override fun finish() {
        super.finish()
        overridePendingTransition(teamcityapp.libraries.utils.R.anim.hold, teamcityapp.libraries.utils.R.anim.slide_out_bottom)
    }
}
