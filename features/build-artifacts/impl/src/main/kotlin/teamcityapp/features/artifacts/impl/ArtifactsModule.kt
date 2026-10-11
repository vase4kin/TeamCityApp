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

package teamcityapp.features.artifacts.impl

import android.app.Activity
import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.artifacts.api.ArtifactsNavigation
import teamcityapp.features.artifacts.impl.tracker.*
import teamcityapp.libraries.utils.requireScreenOwner

@Module
@InstallIn(SingletonComponent::class)
abstract class ArtifactsModule {
    @Binds abstract fun navigation(impl: ArtifactsNavigationImpl): ArtifactsNavigation

    @Binds abstract fun tracker(impl: ArtifactsTrackerImpl): ArtifactsTracker
}

@Module
@InstallIn(ActivityComponent::class)
object ArtifactsActivityModule {
    @Provides fun owner(owner: Activity): ArtifactsActivity = owner.requireScreenOwner<ArtifactsActivity>()
}

@Module
@InstallIn(FragmentComponent::class)
object ArtifactsFragmentModule {
    @Provides fun owner(owner: Fragment): ArtifactsFragment = owner.requireScreenOwner<ArtifactsFragment>()
}
