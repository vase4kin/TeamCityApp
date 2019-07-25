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

package com.github.vase4kin.teamcityapp.home.router;

import androidx.appcompat.app.AppCompatActivity;

import com.github.vase4kin.teamcityapp.account.manage.view.AccountListActivity;
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouterImpl;

/**
 * Impl of {@link HomeRouter}
 */
public class HomeRouterImpl extends DrawerRouterImpl implements HomeRouter {

    public HomeRouterImpl(AppCompatActivity activity) {
        super(activity);
    }

    /**
     * On resume activity
     */
    @Override
    public void openAccountsList() {
        AccountListActivity.Companion.start(mActivity);
    }
}