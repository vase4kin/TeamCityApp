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

package com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.dagger
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.data.AppQuickFilterRepository
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import teamcityapp.features.filter_bottom_sheet.api.QuickFilterRepository
@Module
@InstallIn(ActivityRetainedComponent::class)
object QuickFilterDataModule {
    @Provides @ActivityRetainedScoped
    fun filterProvider(): FilterProvider = FilterProvider()

    @Provides fun repository(repository: AppQuickFilterRepository): QuickFilterRepository = repository
}
