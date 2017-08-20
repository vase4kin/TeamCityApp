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

package com.github.vase4kin.teamcityapp.artifact.view;

import com.github.vase4kin.teamcityapp.artifact.api.File;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;

/**
 * View to manage {@link ArtifactListFragment}
 */
public interface ArtifactView extends BaseListView<ArtifactDataModel> {

    /**
     * Show artifact loading progress
     */
    void showProgressDialog();

    /**
     * Hide artifact loading progress
     */
    void dismissProgressDialog();

    /**
     * Show bottom sheet with all options available
     *
     * @param artifactFile - Artifact file
     */
    void showFullBottomSheet(File artifactFile);

    /**
     * Show bottom sheet with folder options available
     *
     * @param artifactFile - Artifact file
     */
    void showFolderBottomSheet(File artifactFile);

    /**
     * Show bottom sheet with browser options available
     *
     * @param artifactFile - Artifact file
     */
    void showBrowserBottomSheet(File artifactFile);

    /**
     * Show bottom sheet with default options available
     *
     * @param artifactFile - Artifact file
     */
    void showDefaultBottomSheet(File artifactFile);

    /**
     * @param listener - Listener to receive view callbacks on {@link com.github.vase4kin.teamcityapp.artifact.presenter.ArtifactPresenterImpl}
     */
    void setOnArtifactPresenterListener(OnArtifactPresenterListener listener);

    /**
     * Show denied permissions dialogue
     */
    void showPermissionsDeniedDialog();

    /**
     * Show requested permissions dialogue
     *
     * @param onPermissionsDialogListener - Listener to handle callbacks on {@link com.github.vase4kin.teamcityapp.artifact.presenter.ArtifactPresenterImpl}
     */
    void showPermissionsInfoDialog(OnPermissionsDialogListener onPermissionsDialogListener);
}
