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

package com.github.vase4kin.teamcityapp.properties.data

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.properties.api.Properties
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * Impl of [PropertiesDataManager]
 */
class PropertiesDataManagerImpl : BaseListRxDataManagerImpl<Properties, Properties.Property>(), PropertiesDataManager {

    /**
     * {@inheritDoc}
     */
    override fun load(buildDetails: BuildDetails, loadingListener: OnLoadingListener<List<Properties.Property>>) {
        // Getting properties from the build
        val properties = buildDetails.properties
        if (properties == null) {
            loadingListener.onSuccess(emptyList())
            return
        }
        Observable.just(properties)
            .flatMap { Observable.fromIterable(it.objects) }
            .toList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { loadingListener.onSuccess(it) },
                onError = { loadingListener.onFail(it.message ?: "") }
            ).addTo(subscriptions)
    }
}
