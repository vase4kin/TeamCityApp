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

package com.github.vase4kin.teamcityapp.navigation.router;

import android.support.annotation.NonNull;

/**
 * Router to handle navigation
 */
public interface NavigationRouter {

    /**
     * Start build list activity
     *
     * @param name - Build type title
     * @param id   - Build type id
     */
    void startBuildListActivity(@NonNull String name, @NonNull String id);

    /**
     * Start navigation activity
     * @param name - Project id
     * @param id - Project id
     */
    void startNavigationActivity(@NonNull String name, @NonNull String id);

    /**
     * Start projects activity
     */
    void startProjectActivity();

    void openRateTheApp();
}
