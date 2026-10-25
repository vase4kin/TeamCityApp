package com.github.vase4kin.teamcityapp.helper

import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.di.components.DaggerRestApiComponent
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.RestApiModule
import dagger.hilt.android.testing.HiltAndroidRule
import okhttp3.OkHttpClient
import org.junit.rules.ExternalResource

/** Initializes Hilt and preserves the existing user-scoped mocked API graph. */
class HiltApiTestRule(
    private val hilt: HiltAndroidRule,
    private val service: () -> TeamCityService = { FakeTeamCityServiceImpl() }
) : ExternalResource() {
    override fun before() {
        hilt.inject()
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        // Each fixture case seeds installed account/preferences state independently. Clear
        // only the mock app's public sidecars so a previous case cannot override those seeds.
        kotlinx.coroutines.runBlocking {
            val state = dagger.hilt.EntryPoints.get(app, SharedRootTestState::class.java)
            state.navigation().write("")
            (state.accounts() as? teamcityapp.libraries.session.AndroidAccountPreferencesStorage)?.resetPreferencesForTesting()
        }
        app.appInjector.themeController().start()
        app.setApiGraphFactoryForTesting { baseUrl ->
            DaggerRestApiComponent.builder()
                .appComponent(app.appInjector)
                .restApiModule(object : RestApiModule(baseUrl) {
                    override fun provideTeamCityService(okHttpClient: OkHttpClient): TeamCityService = service()
                })
                .build()
        }
    }
    override fun after() {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.themeController().stop()
        kotlinx.coroutines.runBlocking { app.appInjector.themePreferencesStore().close() }
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface SharedRootTestState {
    fun accounts(): teamcityapp.libraries.accounts.AccountStorage
    fun navigation(): com.github.vase4kin.teamcityapp.navigation.AndroidSavedNavigationStore
}
