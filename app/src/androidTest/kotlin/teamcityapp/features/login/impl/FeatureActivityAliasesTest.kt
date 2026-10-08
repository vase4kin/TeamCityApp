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

package teamcityapp.features.login.impl
import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class FeatureActivityAliasesTest {
    @Test fun legacyComponentsResolveToFeatureOwnedActivities() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val targets = mapOf(
            "com.github.vase4kin.teamcityapp.login.view.LoginActivity" to "teamcityapp.features.login.impl.LoginActivity",
            "com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity" to "teamcityapp.features.create_account.impl.CreateAccountActivity",
            "com.github.vase4kin.teamcityapp.runbuild.view.RunBuildActivity" to "teamcityapp.features.run_build.impl.RunBuildActivity",
            "com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity" to "teamcityapp.features.filter_builds.impl.FilterBuildsActivity"
        )
        targets.forEach { (legacy, target) ->
            val alias = context.packageManager.getActivityInfo(ComponentName(context, legacy), 0)
            assertEquals(target, alias.targetActivity)
            assertFalse(alias.exported)
            val activity = context.packageManager.getActivityInfo(ComponentName(context, target), 0)
            assertFalse(activity.exported)
            if (legacy.contains("login.view")) assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, activity.screenOrientation)
        }
    }
}
