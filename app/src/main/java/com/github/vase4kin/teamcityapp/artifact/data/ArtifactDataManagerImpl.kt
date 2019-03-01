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

package com.github.vase4kin.teamcityapp.artifact.data

import android.os.Environment
import android.webkit.MimeTypeMap
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.artifact.api.File
import com.github.vase4kin.teamcityapp.artifact.api.Files
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import okio.Okio
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

private const val EXT_APK = "apk"

/**
 * Impl of [ArtifactDataManager]
 */
class ArtifactDataManagerImpl(
        private val repository: Repository,
        private val eventBus: EventBus
) : BaseListRxDataManagerImpl<Files, File>(), ArtifactDataManager {

    private var mListener: OnArtifactEventListener? = null

    /**
     * {@inheritDoc}
     */
    override fun load(url: String,
                      loadingListener: OnLoadingListener<List<File>>,
                      update: Boolean) {
        load(repository.listArtifacts(url, "browseArchives:true", update), loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun downloadArtifact(url: String, name: String, loadingListener: OnLoadingListener<java.io.File>) {
        subscriptions.clear()
        repository.downloadFile(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { response ->
                    val downloadedFile = java.io.File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name)
                    try {
                        val sink = Okio.buffer(Okio.sink(downloadedFile))
                        sink.writeAll(response.source())
                        sink.close()
                        Single.just<java.io.File>(downloadedFile)
                    } catch (e: Exception) {
                        Single.error<java.io.File>(e)
                    }
                }
                .subscribeBy(
                        onSuccess = { loadingListener.onSuccess(it) },
                        onError = { loadingListener.onFail(it.message) }
                ).addTo(subscriptions)
    }

    /**
     * {@inheritDoc}
     */
    override fun isTheFileApk(fileName: String): Boolean {
        val ext = MimeTypeMap.getFileExtensionFromUrl(fileName)
        return EXT_APK == ext
    }

    /**
     * {@inheritDoc}
     */
    override fun registerEventBus() {
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun unregisterEventBus() {
        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun setListener(listener: OnArtifactEventListener) {
        this.mListener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun postArtifactErrorDownloadingEvent() {
        eventBus.post(ArtifactErrorDownloadingEvent())
    }

    /**
     * On [ArtifactDownloadEvent]
     */
    @Subscribe
    fun onEvent(event: ArtifactDownloadEvent) {
        mListener?.onDownloadArtifactEvent(event.name, event.value)
    }

    /**
     * On [ArtifactOpenEvent]
     */
    @Subscribe
    fun onEvent(event: ArtifactOpenEvent) {
        mListener?.onOpenArtifactEvent(event.href)
    }

    /**
     * On [ArtifactOpenInBrowserEvent]
     */
    @Subscribe
    fun onEvent(event: ArtifactOpenInBrowserEvent) {
        mListener?.onStartBrowserEvent(event.href)
    }
}
