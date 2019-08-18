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

package com.github.vase4kin.teamcityapp.buildlist.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.buildlist.api.Build

/**
 * Single build interactor
 */
interface BuildInteractor {

    /**
     * Load build by by href
     *
     * @param href - Build href
     * @param loadingListener Listener to receive callbacks on [com.github.vase4kin.teamcityapp.buildlist.presenter.BuildListPresenterImpl]
     */
    fun loadBuild(href: String, loadingListener: OnLoadingListener<Build>)

    /**
     * Unsubscribe rx subscriptions
     */
    fun unsubscribe()
}
