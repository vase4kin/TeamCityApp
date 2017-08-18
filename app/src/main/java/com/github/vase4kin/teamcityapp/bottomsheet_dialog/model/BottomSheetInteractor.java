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

import com.github.vase4kin.teamcityapp.overview.data.StartBuildsListActivityFilteredByBranchEvent;
import com.github.vase4kin.teamcityapp.overview.data.TextCopiedEvent;

/**
 * Interactor for bottom sheet
 */
public interface BottomSheetInteractor {

    /**
     * @return data model
     */
    BottomSheetDataModel getBottomSheetDataModel();

    /**
     * @return title of bottom sheet
     */
    String getTitle();

    /**
     * Copy text to the clipboard
     *
     * @param textToCopy - text to copy
     */
    void copyTextToClipBoard(String textToCopy);

    /**
     * Post {@link TextCopiedEvent}
     */
    void postTextCopiedEvent();

    /**
     * Post {@link StartBuildsListActivityFilteredByBranchEvent}
     *
     * @param branchName - branch name
     */
    void postStartBuildListActivityFilteredByBranchEvent(String branchName);
}
