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

package com.github.vase4kin.teamcityapp.artifact.view

import com.github.vase4kin.teamcityapp.artifact.api.File
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView

/**
 * View to manage [ArtifactListFragment]
 */
interface ArtifactView : BaseListView<ArtifactDataModel> {

    /**
     * Show artifact loading progress
     */
    fun showProgressDialog()

    /**
     * Hide artifact loading progress
     */
    fun dismissProgressDialog()

    /**
     * Show bottom sheet with all options available
     *
     * @param artifactFile - Artifact file
     */
    fun showFullBottomSheet(artifactFile: File)

    /**
     * Show bottom sheet with folder options available
     *
     * @param artifactFile - Artifact file
     */
    fun showFolderBottomSheet(artifactFile: File)

    /**
     * Show bottom sheet with browser options available
     *
     * @param artifactFile - Artifact file
     */
    fun showBrowserBottomSheet(artifactFile: File)

    /**
     * Show bottom sheet with default options available
     *
     * @param artifactFile - Artifact file
     */
    fun showDefaultBottomSheet(artifactFile: File)

    /**
     * @param listener - Listener to receive view callbacks on [com.github.vase4kin.teamcityapp.artifact.presenter.ArtifactPresenterImpl]
     */
    fun setOnArtifactPresenterListener(listener: OnArtifactPresenterListener)

    /**
     * Show denied permissions dialogue
     */
    fun showPermissionsDeniedDialog()

    /**
     * Show requested permissions dialogue
     *
     * @param onPermissionsDialogListener - Listener to handle callbacks on [com.github.vase4kin.teamcityapp.artifact.presenter.ArtifactPresenterImpl]
     */
    fun showPermissionsInfoDialog(onPermissionsDialogListener: OnPermissionsDialogListener)

    /**
     * Show install packages permissions dialogue
     *
     * @param onPermissionsDialogListener - Listener to handle callbacks on [com.github.vase4kin.teamcityapp.artifact.presenter.ArtifactPresenterImpl]
     */
    fun showInstallPackagesPermissionsInfoDialog(onPermissionsDialogListener: OnPermissionsDialogListener)
}
