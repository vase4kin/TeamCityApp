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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items;

import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetItem;

import java.util.List;

/**
 * Menu items factory
 */
public interface MenuItemsFactory {

    /**
     * Default menu type
     */
    int TYPE_DEFAULT = 0;

    /**
     * Branch menu type
     */
    int TYPE_BRANCH = 1;

    /**
     * Artifact default menu type
     */
    int TYPE_ARTIFACT_DEFAULT = 2;

    /**
     * Artifact browser menu type
     */
    int TYPE_ARTIFACT_BROWSER = 3;

    /**
     * Artifact folder menu type
     */
    int TYPE_ARTIFACT_FOLDER = 4;

    /**
     * Artifact full menu type
     */
    int TYPE_ARTIFACT_FULL = 5;


    /**
     * @return list of menu items
     */
    List<BottomSheetItem> createMenuItems();
}
