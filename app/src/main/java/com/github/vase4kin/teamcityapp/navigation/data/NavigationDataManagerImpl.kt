/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.navigation.data

import android.content.Context
import android.content.SharedPreferences

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem
import com.github.vase4kin.teamcityapp.navigation.api.NavigationNode
import com.github.vase4kin.teamcityapp.remote.RemoteService

/**
 * Impl of [NavigationDataManager]
 */
class NavigationDataManagerImpl(
        private val mRepository: Repository,
        context: Context, private val remoteService: RemoteService
) : BaseListRxDataManagerImpl<NavigationNode, NavigationItem>(), NavigationDataManager {

    private val sharedPreferences: SharedPreferences

    private val isRated: Boolean
        get() = sharedPreferences.getBoolean(KEY_RATED, false)

    init {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * {@inheritDoc}
     */
    override fun load(id: String, update: Boolean, loadingListener: OnLoadingListener<List<NavigationItem>>) {
        load(mRepository.listBuildTypes(id, update), loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun showRateTheApp(): Boolean {
        return !isRated && remoteService.isNotChurn
    }

    /**
     * {@inheritDoc}
     */
    override fun saveRateCancelClickedOn() {
        saveRatedState()
    }

    /**
     * {@inheritDoc}
     */
    override fun saveRateNowClickedOn() {
        saveRatedState()
    }

    private fun saveRatedState() {
        sharedPreferences.edit().putBoolean(KEY_RATED, true).apply()
    }

    companion object {

        private const val PREF_NAME = "rateTheAppPref"
        private const val KEY_RATED = "rated"
    }
}
