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

package com.github.vase4kin.teamcityapp.buildtabs.view;

import android.os.Bundle;

import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModel;

/**
 * View interactions of {@link BuildTabsActivity}
 */
public interface BuildTabsView extends BaseTabsViewModel {

    /**
     * On save activity state
     *
     * @param bundle - Bundle with state to save
     */
    void onSave(Bundle bundle);

    /**
     * On restore activity state
     *
     * @param bundle - Bundle with saved state
     */
    void onRestore(Bundle bundle);

    /**
     * Show run build float action button
     *
     * Disabled until run build feature is implemented
     */
    void showRunBuildFloatActionButton();

    /**
     * Hide run build float action button
     *
     * Disabled until run build feature is implemented
     */
    void hideRunBuildFloatActionButton();

    /**
     * @param onTabUnSelectListener - Listener to handle tab position changes
     */
    void setOnTabUnSelectListener(OnTabUnSelectListener onTabUnSelectListener);
}