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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.presenter

import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetInteractor
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetView
import javax.inject.Inject

/**
 * Impl of [BottomSheetPresenter]
 */
class BottomSheetPresenterImpl @Inject constructor(
    private val view: BottomSheetView,
    private val interactor: BottomSheetInteractor
) : BottomSheetPresenter, BottomSheetView.OnBottomSheetClickListener {

    /**
     * {@inheritDoc}
     */
    override fun handleOnCreateView() {
        val model = interactor.bottomSheetDataModel
        val title = interactor.title
        view.initViews(this, model, title)
    }

    /**
     * {@inheritDoc}
     */
    override fun handleOnDestroyView() {
        view.unbindViews()
    }

    /**
     * {@inheritDoc}
     */
    override fun onCopyActionClick(text: String) {
        interactor.copyTextToClipBoard(text)
        interactor.postTextCopiedEvent()
        view.close()
    }

    /**
     * {@inheritDoc}
     */
    override fun onShowBuildsActionClick(branchName: String) {
        interactor.postNavigateToBuildListEvent(branchName)
        view.close()
    }

    /**
     * {@inheritDoc}
     */
    override fun onShowBuildTypeActionClick() {
        interactor.postNavigateToBuildTypeEvent()
        view.close()
    }

    /**
     * {@inheritDoc}
     */
    override fun onShowProjectActionClick() {
        interactor.postNavigateToProjectEvent()
        view.close()
    }

    /**
     * {@inheritDoc}
     */
    override fun onArtifactDownloadActionClick(fileName: String, href: String) {
        interactor.postArtifactDownloadEvent(fileName, href)
        view.close()
    }

    /**
     * {@inheritDoc}
     */
    override fun onArtifactOpenActionClick(fileName: String, href: String) {
        interactor.postArtifactOpenEvent(fileName, href)
        view.close()
    }

    /**
     * {@inheritDoc}
     */
    override fun onArtifactBrowserOpenActionClick(href: String) {
        interactor.postArtifactOpenInBrowserEvent(href)
        view.close()
    }
}
