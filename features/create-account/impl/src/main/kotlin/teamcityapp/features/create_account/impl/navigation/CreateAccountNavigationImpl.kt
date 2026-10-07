/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.features.create_account.impl.navigation

import android.app.Activity
import android.content.Intent
import teamcityapp.features.create_account.api.navigation.CreateAccountNavigation
import teamcityapp.features.create_account.impl.CreateAccountActivity
import teamcityapp.libraries.resources.R as SharedR

internal class CreateAccountNavigationImpl : CreateAccountNavigation {
    override fun open(activity: Activity) {
        activity.startActivity(Intent(activity, CreateAccountActivity::class.java))
        activity.overridePendingTransition(SharedR.anim.slide_in_bottom, SharedR.anim.hold)
    }
}
