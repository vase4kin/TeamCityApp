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

package com.github.vase4kin.teamcityapp.build_details.data

import android.view.View
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactErrorDownloadingEvent
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManagerImpl
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.overview.data.*
import com.github.vase4kin.teamcityapp.runbuild.interactor.CODE_FORBIDDEN
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.HttpException

/**
 * Impl of [BuildDetailsInteractor]
 */
class BuildDetailsInteractorImpl(eventBus: EventBus,
                                 private val valueExtractor: BaseValueExtractor,
                                 private val sharedUserStorage: SharedUserStorage,
                                 private val repository: Repository
) : BaseTabsDataManagerImpl(eventBus), BuildDetailsInteractor {

    private var listener: OnBuildDetailsEventsListener? = null

    private val subscriptions = CompositeDisposable()

    /**
     * {@inheritDoc}
     */
    override fun setOnBuildTabsEventsListener(listener: OnBuildDetailsEventsListener?) {
        this.listener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun postRefreshOverViewDataEvent() {
        mEventBus.post(OnOverviewRefreshDataEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun isBuildTriggeredByMe(): Boolean {
        return valueExtractor.buildDetails.isTriggeredByUser(sharedUserStorage.activeUser.userName)
    }

    /**
     * {@inheritDoc}
     */
    override fun getBuildDetails(): BuildDetails {
        return valueExtractor.buildDetails
    }

    /**
     * {@inheritDoc}
     */
    override fun getBuildTypeName(): String {
        val buildDetails = valueExtractor.buildDetails
        return if (buildDetails.hasBuildTypeInfo() && buildDetails.buildTypeName != null) {
            buildDetails.buildTypeName
        } else {
            valueExtractor.name
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun getProjectId(): String {
        return valueExtractor.buildDetails.projectId
    }

    /**
     * {@inheritDoc}
     */
    override fun getProjectName(): String {
        return valueExtractor.buildDetails.projectName
    }

    /**
     * {@inheritDoc}
     */
    override fun cancelBuild(loadingListener: LoadingListenerWithForbiddenSupport<Build>, isReAddToTheQueue: Boolean) {
        subscriptions.clear()
        repository.cancelBuild(valueExtractor.buildDetails.href, BuildCancelRequest(isReAddToTheQueue))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { loadingListener.onSuccess(it) },
                        onError = { e ->
                            if (e is HttpException) {
                                if (e.code() == CODE_FORBIDDEN) {
                                    loadingListener.onForbiddenError()
                                } else {
                                    loadingListener.onFail(e.message ?: "")
                                }
                            } else {
                                loadingListener.onFail(e.message ?: "")
                            }
                        }
                )
                .addTo(subscriptions)
    }

    /**
     * {@inheritDoc}
     */
    override fun getWebUrl(): String {
        return valueExtractor.buildDetails.webUrl
    }

    /**
     * {@inheritDoc}
     */
    override fun unsubsribe() {
        subscriptions.clear()
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event FloatButtonChangeVisibilityEvent
     */
    @Subscribe
    fun onEvent(event: FloatButtonChangeVisibilityEvent) {
        if (listener == null) return
        when (event.visibility) {
            View.VISIBLE -> listener!!.onShow()
            View.GONE -> listener!!.onHide()
            else -> {
            }
        }
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [StopBuildEvent]
     */
    @Subscribe
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: StopBuildEvent) {
        listener?.onCancelBuildActionTriggered()
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [ShareBuildEvent]
     */
    @Subscribe
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: ShareBuildEvent) {
        listener?.onShareBuildActionTriggered()
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [com.github.vase4kin.teamcityapp.overview.data.RestartBuildEvent]
     */
    @Subscribe
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: RestartBuildEvent) {
        listener?.onRestartBuildActionTriggered()
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [com.github.vase4kin.teamcityapp.overview.data.TextCopiedEvent]
     */
    @Subscribe
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: TextCopiedEvent) {
        listener?.onTextCopiedActionTriggered()
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [com.github.vase4kin.teamcityapp.overview.data.TextCopiedEvent]
     */
    @Subscribe
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: ArtifactErrorDownloadingEvent) {
        listener?.onErrorDownloadingArtifactActionTriggered()
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [StartBuildsListActivityFilteredByBranchEvent]
     */
    @Subscribe
    fun onEvent(event: StartBuildsListActivityFilteredByBranchEvent) {
        listener?.onStartBuildListActivityFilteredByBranchEventTriggered(event.branchName)
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [StartBuildsListActivityEvent]
     */
    @Suppress("unused")
    @Subscribe
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: StartBuildsListActivityEvent) {
        listener?.onStartBuildListActivityEventTriggered()
    }

    /***
     * Handle receiving post events from [EventBus]
     *
     * @param event [StartProjectActivityEvent]
     */
    @Suppress("unused")
    @Subscribe
    fun onEvent(@Suppress("UNUSED_PARAMETER") event: StartProjectActivityEvent) {
        listener?.onStartProjectActivityEventTriggered()
    }
}
