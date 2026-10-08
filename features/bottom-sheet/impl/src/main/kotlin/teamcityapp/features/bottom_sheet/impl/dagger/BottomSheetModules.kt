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

package teamcityapp.features.bottom_sheet.impl.dagger
import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.features.bottom_sheet.impl.BottomSheetDialogFragment
import teamcityapp.features.bottom_sheet.impl.navigation.BottomSheetNavigationImpl
import teamcityapp.features.bottom_sheet.impl.router.BottomSheetRouter
import teamcityapp.features.bottom_sheet.impl.router.BottomSheetRouterImpl
import teamcityapp.libraries.utils.requireScreenOwner
@Module
@InstallIn(SingletonComponent::class)
object BottomSheetNavigationModule {
    @Provides fun navigation(): BottomSheetNavigation = BottomSheetNavigationImpl()
}

@Module
@InstallIn(FragmentComponent::class)
object BottomSheetRouterModule {
    @Provides fun router(owner: Fragment, actions: BottomSheetAppActions): BottomSheetRouter = BottomSheetRouterImpl(owner.requireScreenOwner<BottomSheetDialogFragment>(), actions)
}
