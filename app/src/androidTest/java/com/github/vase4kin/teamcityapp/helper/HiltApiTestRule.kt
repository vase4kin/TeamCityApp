package com.github.vase4kin.teamcityapp.helper

import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.dagger.components.DaggerRestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
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
        app.setApiGraphFactoryForTesting { baseUrl ->
            DaggerRestApiComponent.builder()
                .appComponent(app.appInjector)
                .restApiModule(object : RestApiModule(baseUrl) {
                    override fun provideTeamCityService(okHttpClient: OkHttpClient): TeamCityService = service()
                })
                .build()
        }
    }
}
