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

package com.github.vase4kin.teamcityapp.drawer.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataModelImpl
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView
import com.github.vase4kin.teamcityapp.drawer.view.OnDrawerPresenterListener
import com.github.vase4kin.teamcityapp.storage.api.UserAccount

import javax.inject.Inject

/**
 * Impl of [DrawerPresenter]
 */
open class DrawerPresenterImpl<V : DrawerView, DM : DrawerDataManager, DR : DrawerRouter, DT : DrawerTracker>
@Inject constructor(protected var view: V,
                    protected var dataManager: DM,
                    private val router: DR,
                    protected var tracker: DT) : DrawerPresenter, OnDrawerPresenterListener {

    /**
     * {@inheritDoc}
     */
    override fun onCreate() {
        view.initViews(this)
        loadData()
        if (!view.isModelEmpty) {
            loadNotificationsCount()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroy() {
        dataManager.unsubscribe()
    }

    /**
     * {@inheritDoc}
     */
    override fun onBackButtonPressed() {
        view.backButtonPressed()
    }

    /**
     * {@inheritDoc}
     */
    override fun setActiveUser(url: String, userName: String) {
        dataManager.setActiveUser(url, userName)
    }

    /**
     * {@inheritDoc}
     */
    override fun isActiveProfile(url: String, userName: String): Boolean {
        return dataManager.isActiveUser(url, userName)
    }

    /**
     * {@inheritDoc}
     */
    override fun onDrawerSlide() {
        loadNotificationsCount()
    }

    /**
     * Load all required counts
     */
    protected open fun loadNotificationsCount() {
        loadAgentsCount()
    }

    /**
     * Load drawer data
     */
    protected fun loadData() {
        dataManager.load(object : OnLoadingListener<List<UserAccount>> {
            override fun onSuccess(data: List<UserAccount>) {
                view.showData(DrawerDataModelImpl(data))
            }

            override fun onFail(errorMessage: String) {}
        })
    }

    /**
     * Load agents count
     */
    private fun loadAgentsCount() {
        dataManager.loadConnectedAgentsCount(object : OnLoadingListener<Int> {
            override fun onSuccess(data: Int) {
                view.updateAgentsBadge(data)
            }

            override fun onFail(errorMessage: String) {

            }
        })
    }

    /**
     * {@inheritDoc}
     */
    override fun onUserChange() {
        tracker.trackChangeAccount()
    }

    /**
     * {@inheritDoc}
     */
    override fun startHomeActivity() {
        router.startHomeActivity()
    }

    /**
     * {@inheritDoc}
     */
    override fun startRootProjectsActivityWhenSwitchingAccounts() {
        router.startRootProjectsActivityWhenSwitchingAccounts()
    }

    /**
     * {@inheritDoc}
     */
    override fun startAccountListActivity() {
        router.startAccountListActivity()
    }

    /**
     * {@inheritDoc}
     */
    override fun startAgentActivity() {
        router.startAgentActivity()
    }

    /**
     * {@inheritDoc}
     */
    override fun startAboutActivity() {
        router.startAboutActivity()
    }
}
