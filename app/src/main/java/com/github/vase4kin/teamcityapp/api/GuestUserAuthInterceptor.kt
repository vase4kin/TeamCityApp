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

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Guest user interceptor
 *
 *
 * Appending ?guest=1 for each request url
 */
class GuestUserAuthInterceptor : Interceptor {

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val urlWithGuestSupport = chain.request().url()
            .newBuilder()
            .addQueryParameter(QUERY_PARAM, QUERY_VALUE).build()
        val requestWithGuestSupport = chain.request().newBuilder().url(urlWithGuestSupport).build()
        return chain.proceed(requestWithGuestSupport)
    }

    companion object {

        /**
         * Query name
         */
        private const val QUERY_PARAM = "guest"
        /**
         * Query value
         */
        private const val QUERY_VALUE = "1"
    }
}
