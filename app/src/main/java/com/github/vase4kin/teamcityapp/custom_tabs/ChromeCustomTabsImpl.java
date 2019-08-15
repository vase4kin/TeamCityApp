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

package com.github.vase4kin.teamcityapp.custom_tabs;

import android.app.Activity;
import android.content.ComponentName;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.core.content.ContextCompat;

import com.github.vase4kin.teamcityapp.R;

/**
 * Impl of {@link ChromeCustomTabs}
 */
public class ChromeCustomTabsImpl implements ChromeCustomTabs {

    private static final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";

    private CustomTabsClient client;
    private CustomTabsSession customTabsSession;
    private CustomTabsServiceConnection customTabsServiceConnection;
    private CustomTabsIntent customTabsIntent;

    private Activity activity;

    public ChromeCustomTabsImpl(Activity activity) {
        this.activity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initCustomsTabs() {
        /*
            Setup Chrome Custom Tabs
         */
        customTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {

                //Pre-warming
                client = customTabsClient;
                client.warmup(0L);
                //Initialize a session as soon as possible.
                customTabsSession = client.newSession(null);

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                client = null;
            }
        };

        CustomTabsClient.bindCustomTabsService(activity, CUSTOM_TAB_PACKAGE_NAME, customTabsServiceConnection);

        customTabsIntent = new CustomTabsIntent.Builder(customTabsSession)
                .setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary))
                .setShowTitle(true)
                .build();
        /*
            End custom tabs setup
         */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindCustomsTabs() {
        if (null == customTabsServiceConnection) return;
        activity.unbindService(customTabsServiceConnection);
        client = null;
        customTabsSession = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void launchUrl(@NonNull String url) {
        customTabsIntent.launchUrl(activity, Uri.parse(url));
    }
}
