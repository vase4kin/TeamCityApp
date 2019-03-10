/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.base.list.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.api.interfaces.Collectible

import io.reactivex.Single

/**
 * Base data manger to handle server operations with rx
 *
 * @param <T> - Collection of items
 * @param <D> - Single item </D></T> */
interface BaseListRxDataManager<T : Collectible<D>, D> {

    /**
     * load data from server
     *
     * @param call            - Retrofit call
     * @param loadingListener - Listener to handle call callbacks
     */
    fun load(call: Single<T>, loadingListener: OnLoadingListener<List<D>>)

    /**
     * Unsubscribe rx subscriptions
     */
    fun unsubscribe()
}
