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

package com.github.vase4kin.teamcityapp.buildlog.urlprovider;

import com.github.vase4kin.teamcityapp.buildlog.extractor.BuildLogValueExtractor;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

/**
 * Impl of {@link BuildLogUrlProvider}
 */
public class BuildLogUrlProviderImpl implements BuildLogUrlProvider {

    private static final String BUILD_URL = "%s/viewLog.html?buildId=%s&tab=buildLog";

    private BuildLogValueExtractor mValueExtractor;
    private UserAccount mUserAccount;

    public BuildLogUrlProviderImpl(BuildLogValueExtractor valueExtractor, UserAccount userAccount) {
        this.mValueExtractor = valueExtractor;
        mUserAccount = userAccount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String provideUrl() {
        String serverUrl = String.format(
                BUILD_URL,
                mUserAccount.getTeamcityUrl(), mValueExtractor.getBuildId());
        return mUserAccount.isGuestUser()
                ? serverUrl.concat("&guest=1")
                : serverUrl;
    }
}
