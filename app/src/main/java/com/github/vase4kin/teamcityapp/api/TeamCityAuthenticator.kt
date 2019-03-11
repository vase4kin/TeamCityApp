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

package com.github.vase4kin.teamcityapp.api

import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import com.github.vase4kin.teamcityapp.storage.api.passwordAsString

import okhttp3.Credentials
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Authenticator
 */
class TeamCityAuthenticator(private val mUserAccount: UserAccount) : okhttp3.Authenticator {

    /**
     * {@inheritDoc}
     */
    override fun authenticate(route: Route, response: Response): Request? {
        // by default do three attempts
        if (responseCount(response) >= ATTEMPTS_COUNT) {
            return null // If we've failed 3 times, give up.
        }
        // Use user credentials
        val credential = Credentials.basic(mUserAccount.userName, mUserAccount.passwordAsString())
        return response.request().newBuilder()
                .header(AUTHORIZATION, credential)
                .build()
    }

    /**
     * Response count
     *
     * @param response - Response
     * @return count of response tries failed by 401
     */
    private fun responseCount(response: Response?): Int {
        var actualResponse = response
        var result = 1
        while ((actualResponse) != null) {
            actualResponse = actualResponse.priorResponse()
            result++
        }
        return result
    }

}

/**
 * Count of attempts on 401
 */
private const val ATTEMPTS_COUNT = 3