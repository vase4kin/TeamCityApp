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

package com.github.vase4kin.teamcityapp.navigation

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import teamcityapp.libraries.accounts.AccountStorage
import teamcityapp.libraries.network.ResponseCacheStore
import teamcityapp.shared.SavedNavigationStore
import teamcityapp.shared.SharedAppViewModel

private val Context.sharedNavigation by preferencesDataStore(name = "shared_navigation_v1")

@Singleton
class AndroidSavedNavigationStore @Inject constructor(@ApplicationContext context: Context) : SavedNavigationStore {
    private val data = context.sharedNavigation
    private val snapshot = stringPreferencesKey("public_snapshot")
    override suspend fun read(): String? = data.data.first()[snapshot]
    override suspend fun write(snapshot: String) {
        data.edit { it[this.snapshot] = snapshot }
    }
}

@HiltViewModel
class AndroidSharedAppViewModel @Inject constructor(storage: AccountStorage, navigation: AndroidSavedNavigationStore, cache: ResponseCacheStore) : SharedAppViewModel(storage, navigation, cache)
