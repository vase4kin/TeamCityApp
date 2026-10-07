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

package teamcityapp.features.filter_bottom_sheet.impl.dagger
import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.filter_bottom_sheet.api.*
import teamcityapp.features.filter_bottom_sheet.impl.FilterBottomSheetDialogFragment
import teamcityapp.features.filter_bottom_sheet.impl.navigation.FilterBottomSheetNavigationImpl
import teamcityapp.features.filter_bottom_sheet.impl.router.FilterBottomSheetRouter
import teamcityapp.features.filter_bottom_sheet.impl.router.FilterBottomSheetRouterImpl
import teamcityapp.libraries.utils.requireScreenOwner
@Module
@InstallIn(SingletonComponent::class)
object FilterBottomSheetNavigationModule {
    @Provides fun navigation(): FilterBottomSheetNavigation = FilterBottomSheetNavigationImpl()
}

@Module
@InstallIn(FragmentComponent::class)
object FilterBottomSheetRouterModule {
    @Provides fun router(owner: Fragment): FilterBottomSheetRouter = FilterBottomSheetRouterImpl(owner.requireScreenOwner<FilterBottomSheetDialogFragment>())
}
