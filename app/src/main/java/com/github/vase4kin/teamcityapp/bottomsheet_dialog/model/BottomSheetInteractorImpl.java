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

import com.github.vase4kin.teamcityapp.overview.data.NavigateToBuildListEvent;
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
        eventBus.post(new NavigateToBuildListEvent(branchName));
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
