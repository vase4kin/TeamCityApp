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

package com.github.vase4kin.teamcityapp.custom_tabs;

import android.app.Activity;
import android.content.ComponentName;
import android.net.Uri;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;

import com.github.vase4kin.teamcityapp.R;

/**
 * Impl of {@link ChromeCustomTabs}
 */
public class ChromeCustomTabsImpl implements ChromeCustomTabs {

    private static final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";

    private CustomTabsClient mClient;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsServiceConnection mCustomTabsServiceConnection;
    private CustomTabsIntent customTabsIntent;

    private Activity mActivity;

    public ChromeCustomTabsImpl(Activity mActivity) {
        this.mActivity = mActivity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initCustomsTabs() {
        /*
            Setup Chrome Custom Tabs
         */
        mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {

                //Pre-warming
                mClient = customTabsClient;
                mClient.warmup(0L);
                //Initialize a session as soon as possible.
                mCustomTabsSession = mClient.newSession(null);

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mClient = null;
            }
        };

        CustomTabsClient.bindCustomTabsService(mActivity, CUSTOM_TAB_PACKAGE_NAME, mCustomTabsServiceConnection);

        customTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .setToolbarColor(ContextCompat.getColor(mActivity, R.color.colorPrimary))
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
        if (null == mCustomTabsServiceConnection) return;
        mActivity.unbindService(mCustomTabsServiceConnection);
        mClient = null;
        mCustomTabsSession = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void launchUrl(String url) {
        customTabsIntent.launchUrl(mActivity, Uri.parse(url));
    }
}
