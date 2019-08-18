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

package com.github.vase4kin.teamcityapp.account.create.helper

import android.net.Uri
import android.text.TextUtils
import javax.inject.Inject

/**
 * Impl of [UrlFormatter]
 */
class UrlFormatterImpl @Inject constructor() : UrlFormatter {

    /**
     * {@inheritDoc}
     */
    override fun formatServerUrl(serverUrl: String): String {
        val serverUri = Uri.parse(serverUrl)
        val hasNoPathSegment = TextUtils.isEmpty(serverUri.lastPathSegment)
        return if (hasNoPathSegment) {
            // Url without path - http://teamcity.com
            serverUri.toString()
        } else {
            // Url with path - https://teamcity.com/server
            // To build such url with retrofit we need to add / at the end
            serverUri.buildUpon().appendPath("").toString()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun formatBasicUrl(url: String): String {
        // Check that url has leading slash
        return if (url.startsWith("/")) {
            url.replaceFirst("/".toRegex(), "")
        } else {
            url
        }
    }
}
