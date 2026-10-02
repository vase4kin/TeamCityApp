package com.github.vase4kin.teamcityapp.r8

import android.content.Context
import androidx.annotation.Keep
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.api.cache.CacheProviders
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.runbuild.api.Branch
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.github.vase4kin.teamcityapp.storage.api.UsersContainer
import com.google.gson.Gson
import io.reactivex.Single
import io.rx_cache2.internal.RxCache
import io.victoralbertos.jolyglot.GsonSpeaker
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import teamcityapp.features.about.repository.models.ServerInfo
import teamcityapp.features.properties.repository.models.Properties
import teamcityapp.features.test_details.repository.models.TestOccurrence
import teamcityapp.libraries.security.CryptoManagerImpl
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

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
            storage.saveUserAccountAndSetItAsActive("https://server.example", "user", "secret", false,
                object : SharedUserStorage.OnStorageListener {
                    override fun onSuccess() { saved = true }
                    override fun onFail() { error("Account save failed") }
                })
            check(saved)
            val restored = SharedUserStorage.init(context, CryptoManagerImpl()).activeUser
            check(restored.userName == "user" && restored.passwordAsString == "secret")
        } finally {
            preferences.edit().clear().commit()
        }
    }

    @JvmStatic
    fun verifyRetrofitAndCache(context: Context) {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(200).message("OK")
                .body("""{"count":1,"agent":[{"id":"agent","name":"worker"}]}""".toResponseBody("application/json".toMediaType()))
                .build()
        }.build()
        try {
            val service = Retrofit.Builder().baseUrl("https://server.example/").client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .validateEagerly(true).build().create(TeamCityService::class.java)
            val agents = service.listAgents(null, null, null).blockingGet()
            check(agents.count == 1 && agents.agent.single().name == "worker")
            check(agents.agent.single().id == "agent")
            val directory = File(context.cacheDir, "r8-smoke-cache")
            directory.deleteRecursively()
            check(directory.mkdirs())
            try {
                fun providers() = RxCache.Builder().persistence(directory, GsonSpeaker()).using(CacheProviders::class.java)
                val info = ServerInfo("2026.1", "https://server.example")
                check(providers().serverInfo(Single.just(info)).blockingGet() == info)
                check(directory.listFiles()!!.any { it.readText().contains("teamcityapp.features.about.repository.models.ServerInfo") })
                // A fresh instance forces a disk read, with an unavailable upstream.
                val restored = providers().serverInfo(Single.error(IllegalStateException("offline"))).blockingGet()
                check(restored == info)
            } finally {
                directory.deleteRecursively()
            }
        } finally {
            client.connectionPool.evictAll()
            client.dispatcher.executorService.shutdown()
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
    fun onEvent(event: SmokeEvent) { received = event.message }
}
