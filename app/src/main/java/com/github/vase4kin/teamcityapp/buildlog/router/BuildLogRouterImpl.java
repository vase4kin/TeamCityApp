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

package com.github.vase4kin.teamcityapp.buildlog.router;

import android.app.Activity;

import com.github.vase4kin.teamcityapp.custom_tabs.ChromeCustomTabs;
import com.github.vase4kin.teamcityapp.custom_tabs.ChromeCustomTabsImpl;

public class BuildLogRouterImpl implements BuildLogRouter {

    private final ChromeCustomTabs chromeCustomTabs;

    public BuildLogRouterImpl(Activity activity) {
        this.chromeCustomTabs = new ChromeCustomTabsImpl(activity);
        this.chromeCustomTabs.initCustomsTabs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openUrl(String url) {
        chromeCustomTabs.launchUrl(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindCustomsTabs() {
        chromeCustomTabs.unbindCustomsTabs();
    }
}
