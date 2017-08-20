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

/**
 * Menu item of bottom sheet
 */
public class BottomSheetItem {

    public static final int TYPE_COPY = 0;
    public static final int TYPE_BRANCH = 1;
    public static final int TYPE_ARTIFACT_OPEN = 2;
    public static final int TYPE_ARTIFACT_DOWNLOAD = 3;
    public static final int TYPE_ARTIFACT_OPEN_IN_BROWSER = 4;

    private final int type;
    private final String title;
    private final String description;
    private final Drawable icon;

    public BottomSheetItem(int type, String title, String description, Drawable icon) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.icon = icon;
    }

    /**
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return icon
     */
    public Drawable getIcon() {
        return icon;
    }

    /**
     * @return menu item type
     */
    public int getType() {
        return type;
    }

    /**
     * @return menu item description
     */
    public String getDescription() {
        return description;
    }
}
