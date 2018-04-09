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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.presenter;

import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModel;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetInteractor;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetView;

import javax.inject.Inject;

/**
 * Impl of {@link BottomSheetPresenter}
 */
public class BottomSheetPresenterImpl implements BottomSheetPresenter, BottomSheetView.OnBottomSheetClickListener {

    private final BottomSheetView view;
    private final BottomSheetInteractor interactor;

    @Inject
    BottomSheetPresenterImpl(BottomSheetView view,
                             BottomSheetInteractor interactor) {
        this.view = view;
        this.interactor = interactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleOnCreateView() {
        BottomSheetDataModel model = interactor.getBottomSheetDataModel();
        String title = interactor.getTitle();
        view.initViews(this, model, title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleOnDestroyView() {
        view.unbindViews();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCopyActionClick(String text) {
        interactor.copyTextToClipBoard(text);
        interactor.postTextCopiedEvent();
        view.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowBuildsActionClick(String branchName) {
        interactor.postNavigateToBuildListEvent(branchName);
        view.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowBuildTypeActionClick() {
        interactor.postNavigateToBuildTypeEvent();
        view.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowProjectActionClick() {
        interactor.postNavigateToProjectEvent();
        view.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onArtifactDownloadActionClick(String fileName, String href) {
        interactor.postArtifactDownloadEvent(fileName, href);
        view.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onArtifactOpenActionClick(String href) {
        interactor.postArtifactOpenEvent(href);
        view.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onArtifactBrowserOpenActionClick(String href) {
        interactor.postArtifactOpenInBrowserEvent(href);
        view.close();
    }
}
