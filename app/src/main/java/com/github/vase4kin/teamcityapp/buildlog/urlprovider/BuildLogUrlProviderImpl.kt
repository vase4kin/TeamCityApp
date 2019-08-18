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

package com.github.vase4kin.teamcityapp.buildlog.urlprovider

import com.github.vase4kin.teamcityapp.buildlog.extractor.BuildLogValueExtractor
import com.github.vase4kin.teamcityapp.storage.api.UserAccount

/**
 * Impl of [BuildLogUrlProvider]
 */
class BuildLogUrlProviderImpl(
    private val valueExtractor: BuildLogValueExtractor,
    private val userAccount: UserAccount
) : BuildLogUrlProvider {

    /**
     * {@inheritDoc}
     */
    override fun provideUrl(): String {
        val serverUrl = String.format(
                BUILD_URL,
                userAccount.teamcityUrl, valueExtractor.buildId)
        return if (userAccount.isGuestUser)
            "$serverUrl&guest=1"
        else
            serverUrl
    }

    companion object {

        private const val BUILD_URL = "%s/viewLog.html?buildId=%s&tab=buildLog"
    }
}
