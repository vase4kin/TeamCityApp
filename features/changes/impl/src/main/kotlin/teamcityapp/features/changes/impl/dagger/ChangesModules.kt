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

package teamcityapp.features.changes.impl.dagger

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.changes.api.ChangesAppRouter
import teamcityapp.features.changes.api.ChangesNavigation
import teamcityapp.features.changes.impl.ChangesFragment
import teamcityapp.features.changes.impl.navigation.ChangesNavigationImpl
import teamcityapp.features.changes.impl.router.ChangesRouter
import teamcityapp.features.changes.impl.router.ChangesRouterImpl
import teamcityapp.libraries.utils.requireScreenOwner

@Module
@InstallIn(SingletonComponent::class)
abstract class ChangesNavigationModule {
    @Binds abstract fun navigation(implementation: ChangesNavigationImpl): ChangesNavigation
}

@Module
@InstallIn(FragmentComponent::class)
object ChangesRouterModule {
    @Provides fun router(owner: Fragment, appRouter: ChangesAppRouter): ChangesRouter {
        owner.requireScreenOwner<ChangesFragment>()
        return ChangesRouterImpl(appRouter)
    }
}
