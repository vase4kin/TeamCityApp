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

package com.github.vase4kin.teamcityapp.overview.data

import android.os.Handler
import android.os.Looper
import android.view.View

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl
import com.github.vase4kin.teamcityapp.build_details.data.OnOverviewRefreshDataEvent
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Delay of posting event
 */
private const val DELAY = 500

/**
 * Impl of [OverViewInteractor]
 */
class OverviewInteractorImpl(private val repository: Repository,
                             private val eventBus: EventBus,
                             private val valueExtractor: OverviewValueExtractor
) : BaseListRxDataManagerImpl<Build, BuildElement>(), OverViewInteractor {

    private var listener: OverViewInteractor.OnOverviewEventsListener? = null

    /**
     * {@inheritDoc}
     */
    override fun setListener(listener: OverViewInteractor.OnOverviewEventsListener) {
        this.listener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun load(url: String,
                      loadingListener: OnLoadingListener<BuildDetails>,
                      update: Boolean) {
        subscriptions.clear()
        repository.build(url, update)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { loadingListener.onSuccess(BuildDetailsImpl(it)) },
                        onError = { loadingListener.onFail(it.message ?: "") }
                )
                .addTo(subscriptions)
    }

    /**
     * {@inheritDoc}
     */
    override fun postStopBuildEvent() {
        eventBus.post(StopBuildEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun postShareBuildInfoEvent() {
        eventBus.post(ShareBuildEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun postRestartBuildEvent() {
        eventBus.post(RestartBuildEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun subscribeToEventBusEvents() {
        eventBus.register(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun unsubsribeFromEventBusEvents() {
        eventBus.unregister(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun postFABGoneEvent() {
        eventBus.post(FloatButtonChangeVisibilityEvent(View.GONE))
    }

    /**
     * {@inheritDoc}
     */
    override fun postFABVisibleEvent() {
        Handler(Looper.getMainLooper()).postDelayed(
                { eventBus.post(FloatButtonChangeVisibilityEvent(View.VISIBLE)) },
                DELAY.toLong())
    }

    /**
     * {@inheritDoc}
     */
    override fun postStartBuildListActivityFilteredByBranchEvent(branchName: String) {
        eventBus.post(StartBuildsListActivityFilteredByBranchEvent(branchName))
    }

    /**
     * {@inheritDoc}
     */
    override fun postStartBuildListActivityEvent() {
        eventBus.post(StartBuildsListActivityEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun postStartProjectActivityEvent() {
        eventBus.post(StartProjectActivityEvent())
    }

    /**
     * {@inheritDoc}
     */
    override val buildDetails: BuildDetails
        get() = valueExtractor.buildDetails

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [OnOverviewRefreshDataEvent]
     */
    @Suppress("unused")
    @Subscribe
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: OnOverviewRefreshDataEvent) {
        listener?.onDataRefreshEvent()
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [NavigateToBuildListFilteredByBranchEvent]
     */
    @Subscribe
    fun onEvent(event: NavigateToBuildListFilteredByBranchEvent) {
        listener?.onNavigateToBuildListEvent(event.branchName)
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [NavigateToBuildListEvent]
     */
    @Suppress("unused")
    @Subscribe
    fun onEvent(@Suppress("UNUSED_PARAMETER") ignored: NavigateToBuildListEvent) {
        listener?.onNavigateToBuildListEvent()
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [NavigateToProjectEvent]
     */
    @Suppress("unused")
    @Subscribe
    fun onEvent(@Suppress("UNUSED_PARAMETER") ignored: NavigateToProjectEvent) {
        listener?.onNavigateToProjectEvent()
    }
}
