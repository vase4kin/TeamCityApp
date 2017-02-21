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

package com.github.vase4kin.teamcityapp.properties.view;

/**
 * Listener to handle on copy clicks
 *
 * TODO: Move logic to presenter
 */
@Deprecated
public interface OnCopyActionClickListener {

    /**
     * On average click
     *
     * @param value - Element value
     */
    void onClick(String value);

    /**
     * On long click
     *
     * @param title - Element title
     * @param value - Element value
     */
    void onLongClick(String title, String value);
}
