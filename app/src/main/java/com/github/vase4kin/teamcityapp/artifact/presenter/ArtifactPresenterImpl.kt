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

package com.github.vase4kin.teamcityapp.artifact.presenter

import androidx.annotation.VisibleForTesting

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.artifact.api.File
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataManager
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModelImpl
import com.github.vase4kin.teamcityapp.artifact.data.OnArtifactEventListener
import com.github.vase4kin.teamcityapp.artifact.extractor.ArtifactValueExtractor
import com.github.vase4kin.teamcityapp.artifact.permissions.OnPermissionsResultListener
import com.github.vase4kin.teamcityapp.artifact.permissions.PermissionManager
import com.github.vase4kin.teamcityapp.artifact.router.ArtifactRouter
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactView
import com.github.vase4kin.teamcityapp.artifact.view.OnArtifactPresenterListener
import com.github.vase4kin.teamcityapp.artifact.view.OnPermissionsDialogListener
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker

import javax.inject.Inject

class ArtifactPresenterImpl @Inject constructor(view: ArtifactView,
                                                dataManager: ArtifactDataManager,
                                                tracker: ViewTracker,
                                                valueExtractor: ArtifactValueExtractor,
                                                private val router: ArtifactRouter,
                                                private val permissionManager: PermissionManager
) : BaseListPresenterImpl<ArtifactDataModel, File, ArtifactView, ArtifactDataManager, ViewTracker, ArtifactValueExtractor>(view, dataManager, tracker, valueExtractor), ArtifactPresenter, OnArtifactPresenterListener, OnArtifactEventListener {

    @VisibleForTesting
    var fileName: String? = null
    @VisibleForTesting
    var fileHref: String? = null

    /**
     * {@inheritDoc}
     */
    public override fun createModel(data: List<File>): ArtifactDataModel {
        return ArtifactDataModelImpl(data)
    }

    /**
     * {@inheritDoc}
     */
    public override fun loadData(loadingListener: OnLoadingListener<List<File>>, update: Boolean) {
        dataManager.load(valueExtractor.url, loadingListener, update)
    }

    /**
     * {@inheritDoc}
     */
    public override fun initViews() {
        super.initViews()
        view.setOnArtifactPresenterListener(this)
        dataManager.registerEventBus()
        dataManager.setListener(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun onClick(artifactFile: File) {
        if (artifactFile.hasChildren() && artifactFile.isFolder) {
            val href = artifactFile.children.href
            router.openArtifactFile(valueExtractor.buildDetails, href)
            unRegister()
        } else {
            onLongClick(artifactFile)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onLongClick(artifactFile: File) {
        if (!artifactFile.isFolder && artifactFile.hasChildren()) {
            view.showFullBottomSheet(artifactFile)
        } else if (artifactFile.isFolder) {
            view.showFolderBottomSheet(artifactFile)
        } else if (isBrowserUrl(artifactFile.href)) {
            view.showBrowserBottomSheet(artifactFile)
        } else {
            view.showDefaultBottomSheet(artifactFile)
        }
    }

    /**
     * Check if the url is the browser url
     *
     * @param url - Artifact url
     * @return Boolean
     */
    private fun isBrowserUrl(url: String): Boolean {
        return url.contains(".html") || url.contains(".htm")
    }

    /**
     * {@inheritDoc}
     */
    override fun onViewsDestroyed() {
        super.onViewsDestroyed()
        router.unbindCustomsTabs()
    }

    /**
     * {@inheritDoc}
     */
    override fun downloadArtifactFile() {
        val fileName = this.fileName
        val fileHref = this.fileHref
        if (fileName == null || fileHref == null) return
        onDownloadArtifactEvent(fileName, fileHref)
    }

    /**
     * Show retry download artifact snack bar for load artifact error
     */
    private fun showRetryDownloadArtifactSnackBar() {
        dataManager.postArtifactErrorDownloadingEvent()
    }

    /**
     * {@inheritDoc}
     */
    override fun unSubscribe() {
        dataManager.unsubscribe()
    }

    private fun unRegister() {
        dataManager.unregisterEventBus()
        dataManager.setListener(null)
    }

    /**
     * {@inheritDoc}
     */
    override fun onDownloadArtifactEvent(fileName: String, href: String) {
        this.fileName = fileName
        this.fileHref = href
        if (!permissionManager.isWriteStoragePermissionsGranted) {
            view.showPermissionsInfoDialog(object : OnPermissionsDialogListener {
                override fun onAllow() {
                    permissionManager.requestWriteStoragePermissions()
                }
            })
        } else {
            if (dataManager.isTheFileApk(fileName) && !permissionManager.isInstallPackagesPermissionGranted) {
                view.showInstallPackagesPermissionsInfoDialog(object : OnPermissionsDialogListener {
                    override fun onAllow() {
                        permissionManager.requestInstallPackagesPermission()
                    }
                })
            } else {
                view.showProgressDialog()
                dataManager.downloadArtifact(
                        href,
                        fileName,
                        object : OnLoadingListener<java.io.File> {
                            override fun onSuccess(data: java.io.File) {
                                view.dismissProgressDialog()
                                router.startFileActivity(data)
                            }

                            override fun onFail(errorMessage: String) {
                                view.dismissProgressDialog()
                                showRetryDownloadArtifactSnackBar()
                            }
                        })
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onOpenArtifactEvent(href: String) {
        router.openArtifactFile(valueExtractor.buildDetails, href)
        unRegister()
    }

    /**
     * {@inheritDoc}
     */
    override fun onStartBrowserEvent(href: String) {
        router.startBrowser(valueExtractor.buildDetails, href)
    }

    /**
     * {@inheritDoc}
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults, object : OnPermissionsResultListener {
            override fun onGranted() {
                downloadArtifactFile()
            }

            override fun onDenied() {
                view.showPermissionsDeniedDialog()
            }
        })
    }
}
