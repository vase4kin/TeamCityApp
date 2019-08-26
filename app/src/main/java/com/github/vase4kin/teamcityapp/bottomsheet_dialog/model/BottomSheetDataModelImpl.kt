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

import android.graphics.drawable.Drawable

/**
 * impl of [BottomSheetDataModel]
 */
class BottomSheetDataModelImpl(private val items: List<BottomSheetItem>) : BottomSheetDataModel {

    /**
     * {@inheritDoc}
     */
    override val itemCount: Int
        get() = items.size

    /**
     * {@inheritDoc}
     */
    override fun getTitle(position: Int): String {
        return items[position].title
    }

    /**
     * {@inheritDoc}
     */
    override fun getDescription(position: Int): String {
        return items[position].description
    }

    /**
     * {@inheritDoc}
     */
    override fun getFileName(position: Int): String {
        val url = getDescription(position)
        val segments =
            listOf(*url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        return segments[segments.size - 1]
    }

    /**
     * {@inheritDoc}
     */
    override fun getIcon(position: Int): Drawable {
        return items[position].icon
    }

    /**
     * {@inheritDoc}
     */
    override fun hasCopyAction(position: Int): Boolean {
        return items[position].type == BottomSheetItem.TYPE_COPY
    }

    /**
     * {@inheritDoc}
     */
    override fun hasBranchAction(position: Int): Boolean {
        return items[position].type == BottomSheetItem.TYPE_BRANCH
    }

    /**
     * {@inheritDoc}
     */
    override fun hasBuildTypeAction(position: Int): Boolean {
        return items[position].type == BottomSheetItem.TYPE_BUILD_TYPE
    }

    /**
     * {@inheritDoc}
     */
    override fun hasProjectAction(position: Int): Boolean {
        return items[position].type == BottomSheetItem.TYPE_PROJECT
    }

    /**
     * {@inheritDoc}
     */
    override fun hasArtifactOpenAction(position: Int): Boolean {
        return items[position].type == BottomSheetItem.TYPE_ARTIFACT_OPEN
    }

    /**
     * {@inheritDoc}
     */
    override fun hasArtifactDownloadAction(position: Int): Boolean {
        return items[position].type == BottomSheetItem.TYPE_ARTIFACT_DOWNLOAD
    }

    /**
     * {@inheritDoc}
     */
    override fun hasArtifactOpenInBrowserAction(position: Int): Boolean {
        return items[position].type == BottomSheetItem.TYPE_ARTIFACT_OPEN_IN_BROWSER
    }
}
