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

package com.github.vase4kin.teamcityapp.api

import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import okhttp3.Credentials
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Authenticator
 */
class TeamCityAuthenticator(private val userAccount: UserAccount) : okhttp3.Authenticator {

    /**
     * {@inheritDoc}
     */
    override fun authenticate(route: Route?, response: Response): Request? {
        // by default do three attempts
        if (response.request().header(AUTHORIZATION) != null) {
            return null // Give up, we've already attempted to authenticate.
        }
        // Use user credentials
        val credential = Credentials.basic(userAccount.userName, userAccount.passwordAsString)
        return response.request().newBuilder()
            .header(AUTHORIZATION, credential)
            .build()
    }
}
