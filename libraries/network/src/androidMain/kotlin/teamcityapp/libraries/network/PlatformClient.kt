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
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

actual fun platformTeamCityClient(ignoreSslIssues: Boolean): HttpClient = HttpClient(OkHttp) {
    followRedirects = false
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 30_000
    }
    engine {
        config {
            followRedirects(false)
            followSslRedirects(false)
            if (ignoreSslIssues) {
                val trust = object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
                    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                }
                val context = SSLContext.getInstance("TLS").apply { init(null, arrayOf(trust), SecureRandom()) }
                sslSocketFactory(context.socketFactory, trust)
                hostnameVerifier { _, _ -> true }
            }
        }
    }
}
