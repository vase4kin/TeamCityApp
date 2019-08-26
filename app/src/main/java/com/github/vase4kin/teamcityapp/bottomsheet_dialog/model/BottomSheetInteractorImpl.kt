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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDownloadEvent
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactOpenEvent
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactOpenInBrowserEvent
import com.github.vase4kin.teamcityapp.overview.data.NavigateToBuildListEvent
import com.github.vase4kin.teamcityapp.overview.data.NavigateToBuildListFilteredByBranchEvent
import com.github.vase4kin.teamcityapp.overview.data.NavigateToProjectEvent
import com.github.vase4kin.teamcityapp.overview.data.TextCopiedEvent
import org.greenrobot.eventbus.EventBus

/**
 * Impl of [BottomSheetInteractor]
 */
class BottomSheetInteractorImpl(
    override val title: String,
    private val model: BottomSheetDataModel,
    private val context: Context,
    private val eventBus: EventBus
) : BottomSheetInteractor {

    /**
     * {@inheritDoc}
     */
    override val bottomSheetDataModel: BottomSheetDataModel
        get() = model

    /**
     * {@inheritDoc}
     */
    override fun postTextCopiedEvent() {
        eventBus.post(TextCopiedEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun postNavigateToBuildListEvent(branchName: String) {
        eventBus.post(NavigateToBuildListFilteredByBranchEvent(branchName))
    }

    /**
     * {@inheritDoc}
     */
    override fun postNavigateToBuildTypeEvent() {
        eventBus.post(NavigateToBuildListEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun postNavigateToProjectEvent() {
        eventBus.post(NavigateToProjectEvent())
    }

    /**
     * {@inheritDoc}
     */
    override fun postArtifactDownloadEvent(fileName: String, href: String) {
        eventBus.post(ArtifactDownloadEvent(fileName, href))
    }

    /**
     * {@inheritDoc}
     */
    override fun postArtifactOpenEvent(href: String) {
        eventBus.post(ArtifactOpenEvent(href))
    }

    /**
     * {@inheritDoc}
     */
    override fun postArtifactOpenInBrowserEvent(href: String) {
        eventBus.post(ArtifactOpenInBrowserEvent(href))
    }

    /**
     * {@inheritDoc}
     */
    override fun copyTextToClipBoard(textToCopy: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("", textToCopy)
        clipboard.primaryClip = clip
    }
}
