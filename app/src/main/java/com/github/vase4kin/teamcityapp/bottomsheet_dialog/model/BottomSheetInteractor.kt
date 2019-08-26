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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.model

import com.github.vase4kin.teamcityapp.overview.data.NavigateToBuildListFilteredByBranchEvent
import com.github.vase4kin.teamcityapp.overview.data.TextCopiedEvent

/**
 * Interactor for bottom sheet
 */
interface BottomSheetInteractor {

    /**
     * @return data model
     */
    val bottomSheetDataModel: BottomSheetDataModel

    /**
     * @return title of bottom sheet
     */
    val title: String

    /**
     * Copy text to the clipboard
     *
     * @param textToCopy - text to copy
     */
    fun copyTextToClipBoard(textToCopy: String)

    /**
     * Post [TextCopiedEvent]
     */
    fun postTextCopiedEvent()

    /**
     * Post [NavigateToBuildListFilteredByBranchEvent]
     *
     * @param branchName - branch name
     */
    fun postNavigateToBuildListEvent(branchName: String)

    /**
     * Post [com.github.vase4kin.teamcityapp.overview.data.NavigateToBuildListEvent]
     */
    fun postNavigateToBuildTypeEvent()

    /**
     * Post [com.github.vase4kin.teamcityapp.overview.data.NavigateToProjectEvent]
     */
    fun postNavigateToProjectEvent()

    /**
     * Post [com.github.vase4kin.teamcityapp.artifact.data.ArtifactDownloadEvent]
     */
    fun postArtifactDownloadEvent(fileName: String, href: String)

    /**
     * Post [com.github.vase4kin.teamcityapp.artifact.data.ArtifactOpenEvent]
     */
    fun postArtifactOpenEvent(href: String)

    /**
     * Post [com.github.vase4kin.teamcityapp.artifact.data.ArtifactOpenInBrowserEvent]
     */
    fun postArtifactOpenInBrowserEvent(href: String)
}
