package com.github.vase4kin.teamcityapp.r8

import android.content.Context
import androidx.annotation.Keep
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.runbuild.api.Branch
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.github.vase4kin.teamcityapp.storage.api.UsersContainer
import com.google.gson.Gson
import io.ktor.client.engine.mock.respond
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import teamcityapp.features.properties.repository.models.Properties
import teamcityapp.features.test_details.repository.models.TestOccurrence
import teamcityapp.libraries.security.CryptoManagerImpl

// Checks live in the target APK so their dependencies undergo release R8 processing.
// Only this app entry point is kept; test code must not keep app APIs.
@Keep
object R8SmokeChecks {
    @JvmStatic
    fun verifyJsonAndBundles() {
        val gson = Gson()
        val builds = gson.fromJson(
            """{"count":1,"build":[{"id":"42","number":"7","status":"SUCCESS","buildType":{"id":"bt","name":"Build"}}]}""",
            Builds::class.java
        )
        check(builds.count == 1 && builds.objects.single().id == "42")
        check(builds.objects.single().buildType!!.name == "Build")
        check(gson.toJson(BuildCancelRequest(true)).contains("\"isReAddIntoQueue\":true"))
        check(gson.fromJson("""{"name":"main","default":true}""", Branch::class.java).isDefault)
        val properties = gson.fromJson("""{"property":[{"name":"key","value":"value"}]}""", Properties::class.java)
        check(gson.toJson(properties).contains("\"value\":\"value\""))
        val test = gson.fromJson("""{"id":"test","name":"case","status":"SUCCESS"}""", TestOccurrence::class.java)
        check(test.id == "test")
        val bytes = ByteArrayOutputStream()
        ObjectOutputStream(bytes).use { it.writeObject(builds) }
        val restored = ObjectInputStream(ByteArrayInputStream(bytes.toByteArray())).use { it.readObject() as Builds }
        check(restored.objects.single().id == "42")
    }

    @JvmStatic
    fun verifyAccounts(context: Context) {
        val gson = Gson()
        val container = gson.fromJson(
            """{"users":[{"teamcityUrl":"https://server.example","userName":"user","password":[1,2,3],"isGuestUser":false,"isActive":true,"isSslDisabled":true,"buildTypeIds":["bt"]}]}""",
            UsersContainer::class.java
        )
        val account = container.usersAccounts.single()
        check(account.teamcityUrl == "https://server.example" && account.userName == "user")
        check(account.passwordAsBytes.contentEquals(byteArrayOf(1, 2, 3)))
        check(account.isActive && account.isSslDisabled && !account.isGuestUser)
        val json = gson.toJsonTree(container).asJsonObject.getAsJsonArray("users")[0].asJsonObject
        check(json.getAsJsonArray("buildTypeIds")[0].asString == "bt")
        check(json.get("teamcityUrl").asString == "https://server.example")

        // This variant has a separate application ID and isolated preferences/Keystore.
        val preferences = context.getSharedPreferences("UserAccountsKeystoreV1", 0)
        preferences.edit().clear().commit()
        try {
            val storage = SharedUserStorage.init(context, CryptoManagerImpl())
            var saved = false
            storage.saveUserAccountAndSetItAsActive(
                "https://server.example",
                "user",
                "secret",
                false,
                object : SharedUserStorage.OnStorageListener {
                    override fun onSuccess() {
                        saved = true
                    }
                    override fun onFail() {
                        error("Account save failed")
                    }
                }
            )
            check(saved)
            val restored = SharedUserStorage.init(context, CryptoManagerImpl()).activeUser
            check(restored.userName == "user" && restored.passwordAsString == "secret")
        } finally {
            preferences.edit().clear().commit()
        }
    }

    @JvmStatic
    fun verifyKtorAndCache(context: Context) = kotlinx.coroutines.runBlocking {
        val directory = File(context.cacheDir, "r8-smoke-ktor-cache")
        directory.deleteRecursively()
        check(directory.mkdirs())
        val file = File(directory, "responses.json")
        val store = object : teamcityapp.libraries.network.ResponseCacheStore {
            override suspend fun read(): String? = file.takeIf { it.exists() }?.readText()
            override suspend fun write(value: String) {
                file.writeText(value)
            }
        }
        val identity = teamcityapp.libraries.accounts.AccountIdentity("https://server.example", "Guest user", true)
        var requests = 0
        fun api(offline: Boolean): Pair<io.ktor.client.HttpClient, teamcityapp.libraries.network.TeamCityApi> {
            val client = io.ktor.client.HttpClient(
                io.ktor.client.engine.mock.MockEngine {
                    check(!offline) { "Expected a persisted response without networking" }
                    requests++
                    respond(
                        """{"version":"2026.1","webUrl":"https://server.example"}""",
                        io.ktor.http.HttpStatusCode.OK,
                        io.ktor.http.headersOf(io.ktor.http.HttpHeaders.ContentType, "application/json")
                    )
                }
            )
            val cache = teamcityapp.libraries.network.TeamCityResponseCache(identity.key, store)
            val transport = teamcityapp.libraries.network.TeamCityTransport(client, teamcityapp.libraries.accounts.AccountSecret(identity), cache = cache)
            return client to teamcityapp.libraries.network.TeamCityApi(transport)
        }
        try {
            val (online, first) = api(false)
            try {
                check(first.server().version == "2026.1")
            } finally {
                online.close()
            }
            check(requests == 1 && file.exists())
            val (offline, restored) = api(true)
            try {
                check(restored.server().version == "2026.1")
            } finally {
                offline.close()
            }
            check(requests == 1)
        } finally {
            directory.deleteRecursively()
        }
    }

    @JvmStatic
    fun verifyEventBusAndDates() {
        val bus = EventBus.builder().build()
        val subscriber = SmokeSubscriber()
        bus.register(subscriber)
        try {
            bus.post(SmokeEvent("delivered"))
            check(subscriber.received == "delivered")
        } finally {
            bus.unregister(subscriber)
        }
        val zone = DateTimeZone.forID("Europe/Berlin")
        val date = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").withZone(zone).parseDateTime("2026-01-01 12:00")
        check(zone.getOffset(date.millis) == 3_600_000)
    }
}

class SmokeEvent(val message: String)
class SmokeSubscriber {
    var received: String? = null

    @Subscribe
    fun onEvent(event: SmokeEvent) {
        received = event.message
    }
}
