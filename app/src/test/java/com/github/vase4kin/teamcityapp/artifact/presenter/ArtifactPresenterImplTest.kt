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

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.artifact.api.File
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataManager
import com.github.vase4kin.teamcityapp.artifact.extractor.ArtifactValueExtractor
import com.github.vase4kin.teamcityapp.artifact.permissions.OnPermissionsResultListener
import com.github.vase4kin.teamcityapp.artifact.permissions.PermissionManager
import com.github.vase4kin.teamcityapp.artifact.router.ArtifactRouter
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactView
import com.github.vase4kin.teamcityapp.artifact.view.OnPermissionsDialogListener
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.utils.any
import com.github.vase4kin.teamcityapp.utils.capture
import com.github.vase4kin.teamcityapp.utils.eq
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Matchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.runners.MockitoJUnitRunner
import java.util.ArrayList

@RunWith(MockitoJUnitRunner::class)
class ArtifactPresenterImplTest {

    @Captor
    private lateinit var onPermissionsDialogListenerArgumentCaptor: ArgumentCaptor<OnPermissionsDialogListener>

    @Captor
    private lateinit var argumentCaptor: ArgumentCaptor<OnLoadingListener<java.io.File>>

    @Captor
    private lateinit var onPermissionsResultListenerArgumentCaptor: ArgumentCaptor<OnPermissionsResultListener>

    @Mock
    private lateinit var children: File.Children

    @Mock
    private lateinit var content: File.Content

    @Mock
    private lateinit var ioFile: java.io.File

    @Mock
    private lateinit var file: File

    @Mock
    private lateinit var loadingListener: OnLoadingListener<List<File>>

    @Mock
    private lateinit var buildDetails: BuildDetails

    @Mock
    private lateinit var view: ArtifactView

    @Mock
    private lateinit var dataManager: ArtifactDataManager

    @Mock
    private lateinit var router: ArtifactRouter

    @Mock
    private lateinit var valueExtractor: ArtifactValueExtractor

    @Mock
    private lateinit var permissionManager: PermissionManager

    @Mock
    private lateinit var tracker: ViewTracker

    private lateinit var presenter: ArtifactPresenterImpl

    @Before
    fun setUp() {
        presenter = ArtifactPresenterImpl(view, dataManager, tracker, valueExtractor, router, permissionManager)
    }

    @Test
    fun testCreateModel() {
        val files = ArrayList<File>()
        files.add(file)
        val dataModel = presenter.createModel(files)
        assertThat(dataModel.getFile(0), `is`(file))
        assertThat(dataModel.itemCount, `is`(1))
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testLoadData() {
        `when`(valueExtractor.url).thenReturn("url")
        presenter.loadData(loadingListener, false)
        verify(valueExtractor).url
        verify(dataManager).load(eq("url"), eq(loadingListener), eq(false))
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testInitViews() {
        presenter.initViews()
        verify(view).setOnArtifactPresenterListener(eq(presenter))
    }

    @Test
    fun testOnClickIfHasChildren() {
        `when`(file.hasChildren()).thenReturn(true)
        `when`(valueExtractor.buildDetails).thenReturn(buildDetails)
        presenter.onClick(file)
        verify(view).showFullBottomSheet(eq(file))
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testOnClickIfHasChildrenAndIsFolder() {
        `when`(file.hasChildren()).thenReturn(true)
        `when`(file.isFolder).thenReturn(true)
        `when`(file.children).thenReturn(children)
        `when`(file.name).thenReturn("name")
        `when`(children.href).thenReturn("url")
        `when`(valueExtractor.buildDetails).thenReturn(buildDetails)
        presenter.onClick(file)
        verify(valueExtractor).buildDetails
        verify(router).openArtifactFile(eq("name"), eq(buildDetails), eq("url"))
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testOnClickIfBrowserUrl() {
        `when`(file.hasChildren()).thenReturn(false)
        `when`(file.href).thenReturn(".html")
        `when`(valueExtractor.buildDetails).thenReturn(buildDetails)
        presenter.onClick(file)
        verify(view).showBrowserBottomSheet(eq(file))

        `when`(file.href).thenReturn(".htm")
        presenter.onClick(file)
        verify(view, times(2)).showBrowserBottomSheet(eq(file))

        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testOnClickIfHasNoChildrenUrl() {
        `when`(content.href).thenReturn("url")
        `when`(file.hasChildren()).thenReturn(false)
        `when`(file.href).thenReturn("url")
        `when`(file.hasChildren()).thenReturn(false)
        `when`(file.content).thenReturn(content)
        `when`(file.name).thenReturn("name")
        `when`(valueExtractor.buildDetails).thenReturn(buildDetails)
        `when`(permissionManager.isWriteStoragePermissionsGranted).thenReturn(true)
        `when`(dataManager.isTheFileApk(anyString())).thenReturn(false)

        presenter.onClick(file)
        verify(view).showDefaultBottomSheet(eq(file))

        presenter.fileName = "name"
        presenter.fileHref = "url"
        presenter.downloadArtifactFile()
        verify(view).showProgressDialog()
        verify(permissionManager).isWriteStoragePermissionsGranted
        verify(dataManager).isTheFileApk(eq("name"))
        verify(dataManager).downloadArtifact(eq("url"), eq("name"), capture(argumentCaptor))

        val onLoadingListener = argumentCaptor.value
        onLoadingListener.onSuccess(ioFile)
        verify(view).dismissProgressDialog()
        verify(router).startFileActivity(eq(ioFile))

        onLoadingListener.onFail("error")
        verify(view, times(2)).dismissProgressDialog()
        verify(dataManager).postArtifactErrorDownloadingEvent()

        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testOnLongClickIfFileHasChildrenAndNotAFolder() {
        `when`(file.hasChildren()).thenReturn(true)
        `when`(file.isFolder).thenReturn(false)
        presenter.onLongClick(file)
        verify(view).showFullBottomSheet(eq(file))
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, tracker)
    }

    @Test
    fun testOnLongClickIfFileIsAFolder() {
        `when`(file.hasChildren()).thenReturn(false)
        `when`(file.isFolder).thenReturn(true)
        presenter.onLongClick(file)
        verify(view).showFolderBottomSheet(eq(file))
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testOnLongClickIfFileHasBrowserUrl() {
        `when`(file.hasChildren()).thenReturn(false)
        `when`(file.isFolder).thenReturn(false)
        `when`(file.href).thenReturn(".htm")
        presenter.onLongClick(file)
        verify(view).showBrowserBottomSheet(eq(file))
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testOnLongClick() {
        `when`(file.hasChildren()).thenReturn(false)
        `when`(file.isFolder).thenReturn(false)
        `when`(file.href).thenReturn("url")
        presenter.onLongClick(file)
        verify(view).showDefaultBottomSheet(eq(file))
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testOnViewsDestroyed() {
        presenter.onViewsDestroyed()
        verify(router).unbindCustomsTabs()
    }

    @Test
    fun testOpenArtifactFile() {
        `when`(valueExtractor.buildDetails).thenReturn(buildDetails)
        presenter.onOpenArtifactEvent("name", "href")
        verify(valueExtractor).buildDetails
        verify(router).openArtifactFile(eq("name"), eq(buildDetails), eq("href"))
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testOnPause() {
        presenter.onPause()
        verify(dataManager).unregisterEventBus()
        verify(dataManager).setListener(null)
    }

    @Test
    fun testOnResume() {
        presenter.onResume()
        verify(dataManager).registerEventBus()
        verify(dataManager).setListener(presenter)
    }

    @Test
    fun testStartBrowser() {
        `when`(valueExtractor.buildDetails).thenReturn(buildDetails)
        presenter.onStartBrowserEvent("url")
        verify(valueExtractor).buildDetails
        verify(router).startBrowser(eq(buildDetails), eq("url"))
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testHandleOnRequestPermissionsResult() {
        `when`(permissionManager.isWriteStoragePermissionsGranted).thenReturn(false)
        val requestCode = 12
        val permissions = arrayOf("123")
        val grantResults = intArrayOf(12, 123)
        presenter.fileName = ""
        presenter.fileHref = ""
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
        verify(permissionManager).onRequestPermissionsResult(
            eq(requestCode),
            eq(permissions),
            eq(grantResults),
            capture(onPermissionsResultListenerArgumentCaptor)
        )
        val listener = onPermissionsResultListenerArgumentCaptor.value
        listener.onDenied()
        verify(view).showPermissionsDeniedDialog()
        listener.onGranted()
        verify(permissionManager).isWriteStoragePermissionsGranted
        verify(view).showPermissionsInfoDialog(capture(onPermissionsDialogListenerArgumentCaptor))
        val onPermissionsDialogListener = onPermissionsDialogListenerArgumentCaptor.value
        onPermissionsDialogListener.onAllow()
        verify(permissionManager).requestWriteStoragePermissions()
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testDownloadArtifactFileIfUserDoesNotHaveRequiredPermissionsAndNeedToShowTheDialogInfo() {
        `when`(permissionManager.isWriteStoragePermissionsGranted).thenReturn(false)
        presenter.onDownloadArtifactEvent("", "")
        verify(permissionManager).isWriteStoragePermissionsGranted
        verify(view).showPermissionsInfoDialog(capture(onPermissionsDialogListenerArgumentCaptor))
        val listener = onPermissionsDialogListenerArgumentCaptor.value
        listener.onAllow()
        verify(permissionManager).requestWriteStoragePermissions()
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testDownloadArtifactFileIfUserDoesNotHaveRequiredPermissionsAndNoNeedToShowTheDialogInfo() {
        `when`(permissionManager.isWriteStoragePermissionsGranted).thenReturn(false)
        presenter.onDownloadArtifactEvent("", "")
        verify(permissionManager).isWriteStoragePermissionsGranted
        verify(view).showPermissionsInfoDialog(capture(onPermissionsDialogListenerArgumentCaptor))
        val listener = onPermissionsDialogListenerArgumentCaptor.value
        listener.onAllow()
        verify(permissionManager).requestWriteStoragePermissions()
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testDownloadArtifactFileIfThFileIsApk() {
        `when`(permissionManager.isWriteStoragePermissionsGranted).thenReturn(true)
        `when`(permissionManager.isInstallPackagesPermissionGranted).thenReturn(false)
        `when`(dataManager.isTheFileApk(anyString())).thenReturn(true)
        presenter.onDownloadArtifactEvent("", "")
        verify(permissionManager).isWriteStoragePermissionsGranted
        verify(dataManager).isTheFileApk(eq(""))
        verify(permissionManager).isInstallPackagesPermissionGranted
        verify(view).showInstallPackagesPermissionsInfoDialog(capture(onPermissionsDialogListenerArgumentCaptor))
        val listener = onPermissionsDialogListenerArgumentCaptor.value
        listener.onAllow()
        verify(permissionManager).requestInstallPackagesPermission()
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }

    @Test
    fun testDownloadArtifactFileIfThFileIsApk2() {
        `when`(permissionManager.isWriteStoragePermissionsGranted).thenReturn(true)
        `when`(permissionManager.isInstallPackagesPermissionGranted).thenReturn(true)
        `when`(dataManager.isTheFileApk(eq("name.apk"))).thenReturn(true)
        presenter.onDownloadArtifactEvent("name.apk", "url")
        verify(permissionManager).isWriteStoragePermissionsGranted
        verify(dataManager).isTheFileApk(eq("name.apk"))
        verify(permissionManager).isInstallPackagesPermissionGranted
        verify(view).showProgressDialog()
        verify(dataManager).downloadArtifact(eq("url"), eq("name.apk"), any())
        verifyNoMoreInteractions(view, dataManager, router, valueExtractor, permissionManager, tracker)
    }
}
