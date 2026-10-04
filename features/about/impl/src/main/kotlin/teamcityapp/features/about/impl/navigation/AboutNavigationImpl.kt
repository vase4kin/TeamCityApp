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

package teamcityapp.features.about.impl.navigation

import android.app.Activity
import android.content.Intent
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.about.api.navigation.AboutNavigation
import teamcityapp.features.about.impl.AboutActivity
import javax.inject.Inject

class AboutNavigationImpl @Inject constructor() : AboutNavigation {
    override fun open(activity: Activity) {
        activity.startActivity(Intent(activity, AboutActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AboutNavigationModule {
    @Binds
    abstract fun bindNavigation(implementation: AboutNavigationImpl): AboutNavigation
}
