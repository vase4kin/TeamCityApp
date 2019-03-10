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

package com.github.vase4kin.teamcityapp.overview.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
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
class OverviewInteractorImpl(private val mRepository: Repository,
                             private val mEventBus: EventBus,
                             private val mValueExtractor: BaseValueExtractor,
                             private val mContext: Context) : BaseListRxDataManagerImpl<Build, BuildElement>(), OverViewInteractor {

    private var mListener: OverViewInteractor.OnOverviewEventsListener? = null

    /**
     * {@inheritDoc}
     */
    override fun setListener(listener: OverViewInteractor.OnOverviewEventsListener) {
        this.mListener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun load(url: String,
                      loadingListener: OnLoadingListener<BuildDetails>,
                      update: Boolean) {
        subscriptions.clear()
        mRepository.build(url, update)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { loadingListener.onSuccess(BuildDetailsImpl(it)) },
                        onError = { loadingListener.onFail(it.message) }
                )
                .addTo(subscriptions)
    }

    /**
     * {@inheritDoc}
     */
    override fun postStopBuildEvent() {
        mEventBus.post(StopBuildEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun postShareBuildInfoEvent() {
        mEventBus.post(ShareBuildEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun postRestartBuildEvent() {
        mEventBus.post(RestartBuildEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun subscribeToEventBusEvents() {
        mEventBus.register(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun unsubsribeFromEventBusEvents() {
        mEventBus.unregister(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun postFABGoneEvent() {
        mEventBus.post(FloatButtonChangeVisibilityEvent(View.GONE))
    }

    /**
     * {@inheritDoc}
     */
    override fun postFABVisibleEvent() {
        Handler(Looper.getMainLooper()).postDelayed(
                { mEventBus.post(FloatButtonChangeVisibilityEvent(View.VISIBLE)) },
                DELAY.toLong())
    }

    /**
     * {@inheritDoc}
     */
    override fun postStartBuildListActivityFilteredByBranchEvent(branchName: String) {
        mEventBus.post(StartBuildsListActivityFilteredByBranchEvent(branchName))
    }

    /**
     * {@inheritDoc}
     */
    override fun postStartBuildListActivityEvent() {
        mEventBus.post(StartBuildsListActivityEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun postStartProjectActivityEvent() {
        mEventBus.post(StartProjectActivityEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun getBuildDetails(): BuildDetails {
        return mValueExtractor.buildDetails
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [OnOverviewRefreshDataEvent]
     */
    @Subscribe
    fun onEvent(event: OnOverviewRefreshDataEvent) {
        mListener?.onDataRefreshEvent()
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [NavigateToBuildListFilteredByBranchEvent]
     */
    @Subscribe
    fun onEvent(event: NavigateToBuildListFilteredByBranchEvent) {
        mListener?.onNavigateToBuildListEvent(event.branchName)
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [NavigateToBuildListEvent]
     */
    @Subscribe
    fun onEvent(ignored: NavigateToBuildListEvent) {
        mListener?.onNavigateToBuildListEvent()
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [NavigateToProjectEvent]
     */
    @Subscribe
    fun onEvent(ignored: NavigateToProjectEvent) {
        mListener?.onNavigateToProjectEvent()
    }
}
