/*
 * Copyright 2026 Andrey Tolpeev
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

package teamcityapp.libraries.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import platform.Security.*

@OptIn(ExperimentalForeignApi::class)
actual fun platformTeamCityClient(ignoreSslIssues: Boolean): HttpClient = HttpClient(Darwin) {
    followRedirects = false
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        socketTimeoutMillis = 30_000
    }
    engine {
        configureSession {
            URLCredentialStorage = null
            HTTPCookieStorage = null
            HTTPShouldSetCookies = false
            URLCache = null
        }
        if (ignoreSslIssues) {
            handleChallenge { _, _, challenge, completion ->
                val trust = challenge.protectionSpace.serverTrust
                if (challenge.protectionSpace.authenticationMethod == NSURLAuthenticationMethodServerTrust && trust != null) {
                    completion(NSURLSessionAuthChallengeUseCredential, NSURLCredential.credentialForTrust(trust))
                } else {
                    completion(NSURLSessionAuthChallengePerformDefaultHandling, null)
                }
            }
        }
    }
}
