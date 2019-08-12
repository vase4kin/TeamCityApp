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

package com.github.vase4kin.teamcityapp.artifact.data

import android.os.Environment
import android.webkit.MimeTypeMap
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.artifact.api.File
import com.github.vase4kin.teamcityapp.artifact.api.Files
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import okio.Okio
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

private const val EXT_APK = "apk"
private const val LOCATOR = "browseArchives:true"

/**
 * Impl of [ArtifactDataManager]
 */
class ArtifactDataManagerImpl(
        private val repository: Repository,
        private val eventBus: EventBus
) : BaseListRxDataManagerImpl<Files, File>(), ArtifactDataManager {

    private var listener: OnArtifactEventListener? = null

    /**
     * {@inheritDoc}
     */
    override fun load(url: String,
                      loadingListener: OnLoadingListener<List<File>>,
                      update: Boolean) {
        load(repository.listArtifacts(url, LOCATOR, update), loadingListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun downloadArtifact(url: String, name: String, loadingListener: OnLoadingListener<java.io.File>) {
        subscriptions.clear()
        repository.downloadFile(url)
                .subscribeOn(Schedulers.io())
                .map { response ->
                    val downloadedFile = java.io.File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name)
                    val sink = Okio.buffer(Okio.sink(downloadedFile))
                    sink.writeAll(response.source())
                    sink.close()
                    downloadedFile
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { loadingListener.onSuccess(it) },
                        onError = { loadingListener.onFail(it.message ?: "") }
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
    override fun setListener(listener: OnArtifactEventListener?) {
        this.listener = listener
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
        listener?.onDownloadArtifactEvent(event.name, event.value)
    }

    /**
     * On [ArtifactOpenEvent]
     */
    @Subscribe
    fun onEvent(event: ArtifactOpenEvent) {
        listener?.onOpenArtifactEvent(event.href)
    }

    /**
     * On [ArtifactOpenInBrowserEvent]
     */
    @Subscribe
    fun onEvent(event: ArtifactOpenInBrowserEvent) {
        listener?.onStartBrowserEvent(event.href)
    }
}
