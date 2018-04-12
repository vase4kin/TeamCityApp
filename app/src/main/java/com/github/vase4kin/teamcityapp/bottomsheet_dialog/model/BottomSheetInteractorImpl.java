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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.model;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDownloadEvent;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactOpenEvent;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactOpenInBrowserEvent;
import com.github.vase4kin.teamcityapp.overview.data.NavigateToBuildListEvent;
import com.github.vase4kin.teamcityapp.overview.data.NavigateToBuildListFilteredByBranchEvent;
import com.github.vase4kin.teamcityapp.overview.data.NavigateToProjectEvent;
import com.github.vase4kin.teamcityapp.overview.data.TextCopiedEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Impl of {@link BottomSheetInteractor}
 */
public class BottomSheetInteractorImpl implements BottomSheetInteractor {

    private final String title;
    private final BottomSheetDataModel model;
    private final Context context;
    private final EventBus eventBus;

    public BottomSheetInteractorImpl(String title, BottomSheetDataModel model, Context context, EventBus eventBus) {
        this.title = title;
        this.model = model;
        this.context = context;
        this.eventBus = eventBus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BottomSheetDataModel getBottomSheetDataModel() {
        return model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postTextCopiedEvent() {
        eventBus.post(new TextCopiedEvent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postNavigateToBuildListEvent(String branchName) {
        eventBus.post(new NavigateToBuildListFilteredByBranchEvent(branchName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postNavigateToBuildTypeEvent() {
        eventBus.post(new NavigateToBuildListEvent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postNavigateToProjectEvent() {
        eventBus.post(new NavigateToProjectEvent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postArtifactDownloadEvent(String fileName, String href) {
        eventBus.post(new ArtifactDownloadEvent(fileName, href));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postArtifactOpenEvent(String href) {
        eventBus.post(new ArtifactOpenEvent(href));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postArtifactOpenInBrowserEvent(String href) {
        eventBus.post(new ArtifactOpenInBrowserEvent(href));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyTextToClipBoard(String textToCopy) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", textToCopy);
        clipboard.setPrimaryClip(clip);
    }
}
