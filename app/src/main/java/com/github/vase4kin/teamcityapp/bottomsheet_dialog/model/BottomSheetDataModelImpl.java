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

import android.graphics.drawable.Drawable;

import java.util.Arrays;
import java.util.List;

/**
 * impl of {@link BottomSheetDataModel}
 */
public class BottomSheetDataModelImpl implements BottomSheetDataModel {

    private final List<BottomSheetItem> items;

    public BottomSheetDataModelImpl(List<BottomSheetItem> items) {
        this.items = items;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle(int position) {
        return items.get(position).getTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription(int position) {
        return items.get(position).getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileName(int position) {
        String url = getDescription(position);
        List<String> segments = Arrays.asList(url.split("/"));
        if (segments.isEmpty()) {
            return url;
        }
        return segments.get(segments.size() - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Drawable getIcon(int position) {
        return items.get(position).getIcon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasCopyAction(int position) {
        return items.get(position).getType() == BottomSheetItem.TYPE_COPY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasBranchAction(int position) {
        return items.get(position).getType() == BottomSheetItem.TYPE_BRANCH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasBuildTypeAction(int position) {
        return items.get(position).getType() == BottomSheetItem.TYPE_BUILD_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProjectAction(int position) {
        return items.get(position).getType() == BottomSheetItem.TYPE_PROJECT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasArtifactOpenAction(int position) {
        return items.get(position).getType() == BottomSheetItem.TYPE_ARTIFACT_OPEN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasArtifactDownloadAction(int position) {
        return items.get(position).getType() == BottomSheetItem.TYPE_ARTIFACT_DOWNLOAD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasArtifactOpenInBrowserAction(int position) {
        return items.get(position).getType() == BottomSheetItem.TYPE_ARTIFACT_OPEN_IN_BROWSER;
    }
}
