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

package teamcityapp.features.tests.impl.dagger

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.tests.api.TestsAppRouter
import teamcityapp.features.tests.api.TestsNavigation
import teamcityapp.features.tests.impl.TestsFragment
import teamcityapp.features.tests.impl.navigation.TestsNavigationImpl
import teamcityapp.features.tests.impl.router.TestsRouter
import teamcityapp.features.tests.impl.router.TestsRouterImpl
import teamcityapp.libraries.utils.requireScreenOwner

@Module
@InstallIn(SingletonComponent::class)
abstract class TestsNavigationModule {
    @Binds abstract fun navigation(implementation: TestsNavigationImpl): TestsNavigation
}

@Module
@InstallIn(FragmentComponent::class)
object TestsRouterModule {
    @Provides fun router(owner: Fragment, appRouter: TestsAppRouter): TestsRouter {
        owner.requireScreenOwner<TestsFragment>()
        return TestsRouterImpl(appRouter)
    }
}
