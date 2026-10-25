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
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.decodeURLPart
import io.ktor.http.hostWithPort
import io.ktor.utils.io.*
import kotlin.io.encoding.Base64
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.json.*
import teamcityapp.libraries.accounts.AccountSecret

class HttpConsentRequired : IllegalArgumentException("HTTP requires explicit consent")
class TeamCityHttpFailure(val status: Int) : Exception("TeamCity returned HTTP $status")

/** Canonical storage format follows Android's existing root/context trailing-slash convention. */
fun normalizeServerUrl(value: String): String {
    val text = value.trim()
    val url = Url(text)
    require(url.protocol == URLProtocol.HTTP || url.protocol == URLProtocol.HTTPS) { "A TeamCity URL must use HTTP or HTTPS" }
    require(url.host.isNotBlank() && url.user.isNullOrEmpty() && url.password.isNullOrEmpty() && url.parameters.isEmpty() && url.fragment.isEmpty()) { "Provide a server URL without credentials, query or fragment" }
    val normalized = url.toString()
    return if (url.encodedPath.isBlank() || url.encodedPath == "/") {
        if (text.endsWith('/')) normalized else normalized.removeSuffix("/")
    } else if (normalized.endsWith('/')) {
        normalized
    } else {
        "$normalized/"
    }
}

/** Credentials may be sent only to this server/context, after a Basic challenge. */
class TeamCityTransport(private val client: HttpClient, private val account: AccountSecret, httpConsent: Boolean = false, private val cache: TeamCityResponseCache = TeamCityResponseCache(account.identity.key)) {
    private val base = Url(normalizeServerUrl(account.identity.serverUrl))
    private val context = base.encodedPath.trimEnd('/') + "/"
    private var challengedAuthorization: String? = null
    private val json = Json { ignoreUnknownKeys = true }
    init {
        if (base.protocol == URLProtocol.HTTP && !httpConsent) throw HttpConsentRequired()
    }

    fun apiPath(path: String): String = context + (if (account.identity.guest) "guestAuth/" else "httpAuth/") + "app/rest/" + path

    /** Continuation URLs retain their opaque query exactly; only the authentication path is adapted. */
    fun resolve(path: String): Url {
        val candidate = if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("//")) {
            if (path.startsWith("//")) Url("${base.protocol.name}:$path") else Url(path)
        } else {
            Url("${base.protocol.name}://${base.hostWithPort}${if (path.startsWith('/')) path else context + path}")
        }
        require(candidate.protocol == base.protocol && candidate.host == base.host && candidate.port == base.port && candidate.user.isNullOrEmpty() && candidate.password.isNullOrEmpty()) { "A TeamCity link must stay on the active server" }
        require(candidate.encodedPath.startsWith(context) && candidate.fragment.isEmpty()) { "A TeamCity link must stay in the active server context" }
        require(candidate.encodedPath.split('/').flatMap { it.decodeURLPart().split('/', '\\') }.none { it in setOf(".", "..") }) { "A TeamCity link cannot traverse outside its REST endpoint" }
        val relative = candidate.encodedPath.removePrefix(context)
        val withoutAuth = relative.removePrefix("guestAuth/").removePrefix("httpAuth/")
        require(withoutAuth == "app/rest" || withoutAuth.startsWith("app/rest/")) { "A TeamCity link must target its REST API" }
        val authenticatedPath = context + (if (account.identity.guest) "guestAuth/" else "httpAuth/") + withoutAuth
        val query = candidate.encodedQuery.takeIf { it.isNotEmpty() }?.let { "?$it" }.orEmpty()
        return Url("${base.protocol.name}://${base.hostWithPort}$authenticatedPath$query")
    }

    suspend fun get(path: String, forceRefresh: Boolean = false): JsonObject {
        val url = resolve(path).toString()
        return cache.get(url, forceRefresh) { exchange(path, HttpMethod.Get, null) }
    }

    /** Only an explicit Basic challenge may replay a mutation; transport errors are never retried. */
    suspend fun post(path: String, payload: JsonObject): JsonObject {
        cache.invalidate()
        return try {
            exchange(path, HttpMethod.Post, payload)
        } finally {
            cache.invalidate()
        }
    }

    private suspend fun exchange(path: String, verb: HttpMethod, payload: JsonObject?): JsonObject {
        val url = resolve(path)
        currentCoroutineContext().ensureActive()
        val initialAuthorization = challengedAuthorization
        var response = client.request(url) {
            method = verb
            if (payload != null) {
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            header(HttpHeaders.Accept, "application/json")
            initialAuthorization?.let { header(HttpHeaders.Authorization, it) }
        }
        if (response.status.value == 401 && !account.identity.guest && initialAuthorization == null) {
            val challenge = response.headers.getAll(HttpHeaders.WWWAuthenticate).orEmpty().firstOrNull { it.trimStart().startsWith("Basic", ignoreCase = true) }
            if (challenge != null) {
                challengedAuthorization = basicAuthorization(challenge)
                response.bodyAsText() // Release the challenge response before retrying.
                response = client.request(url) {
                    method = verb
                    if (payload != null) {
                        contentType(ContentType.Application.Json)
                        setBody(payload.toString())
                    }
                    header(HttpHeaders.Accept, "application/json")
                    header(HttpHeaders.Authorization, challengedAuthorization!!)
                }
            }
        }
        val body = response.bodyAsText()
        currentCoroutineContext().ensureActive()
        if (response.status.value !in 200..299) throw TeamCityHttpFailure(response.status.value)
        return json.parseToJsonElement(body).jsonObject
    }

    /** A bounded buffer keeps large artifacts off the heap; execute closes on success or cancellation. */
    suspend fun stream(path: String, write: (ByteArray, Int) -> Unit, progress: (Long, Long?) -> Unit) {
        val url = resolve(path)
        suspend fun receive(authorization: String?, mayChallenge: Boolean) {
            var retry = false
            client.prepareRequest(url) {
                method = HttpMethod.Get
                // Large transfers may exceed the JSON deadline while still making steady progress.
                // Retain a socket inactivity limit and explicit screen/account cancellation.
                timeout {
                    requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                    socketTimeoutMillis = 30_000
                }
                header(HttpHeaders.Accept, "application/octet-stream")
                header(HttpHeaders.AcceptEncoding, "identity")
                authorization?.let { header(HttpHeaders.Authorization, it) }
            }.execute { response ->
                val challenge = response.headers.getAll(HttpHeaders.WWWAuthenticate).orEmpty().firstOrNull { it.trimStart().startsWith("Basic", true) }
                if (response.status.value == 401 && mayChallenge && challenge != null) {
                    challengedAuthorization = basicAuthorization(challenge)
                    retry = true
                } else {
                    if (response.status.value !in 200..299) throw TeamCityHttpFailure(response.status.value)
                    // Platform engines may transparently decode HTTP compression; its wire length
                    // then describes compressed bytes, not bytes written to the artifact file.
                    val encoded = response.headers[HttpHeaders.ContentEncoding]?.let { !it.equals("identity", true) } ?: false
                    val total = response.contentLength()?.takeIf { it >= 0 && !encoded }
                    val channel = response.bodyAsChannel()
                    val buffer = ByteArray(8192)
                    var received = 0L
                    progress(received, total)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = channel.readAvailable(buffer, 0, buffer.size)
                        if (count == -1) break
                        if (count == 0) continue
                        write(buffer, count)
                        received += count
                        progress(received, total)
                    }
                    currentCoroutineContext().ensureActive()
                    // Engines need not reject a truncated response themselves.
                    check(total == null || received == total) { "Incomplete artifact response" }
                }
            }
            if (retry) receive(challengedAuthorization, false)
        }
        val initial = challengedAuthorization
        receive(initial, !account.identity.guest && initial == null)
    }

    private fun basicAuthorization(challenge: String): String {
        val raw = "${account.identity.userName}:${account.password}"
        val bytes = if (challenge.contains("charset=\"UTF-8\"", true) || challenge.contains("charset=UTF-8", true)) raw.encodeToByteArray() else ByteArray(raw.length) { index -> raw[index].code.takeIf { it <= 255 }?.toByte() ?: '?'.code.toByte() }
        return "Basic ${Base64.Default.encode(bytes)}"
    }
    suspend fun invalidateCache() = cache.invalidate()
    fun close() = client.close()
}

/** Each account graph constructs its own engine; no global credential interceptor is shared. */
expect fun platformTeamCityClient(ignoreSslIssues: Boolean): HttpClient
