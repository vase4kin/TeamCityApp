package teamcityapp.features.test_details.view

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import teamcityapp.features.test_details.repository.models.TestOccurrence

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TestDetailsActivityTest {
    @JvmField @Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    private val service: TeamCityService = spy(FakeTeamCityServiceImpl())
    @JvmField @Rule(order = 1) val apiRule = HiltApiTestRule(hiltRule) { service }
    @JvmField @Rule(order = 2) val compose = createEmptyComposeRule()

    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase

    @Before fun setUp() {
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    private fun launch() = ActivityScenario.launch<TestDetailsActivity>(
        Intent(app, TestDetailsActivity::class.java).putExtra(TestDetailsActivity.ARG_TEST_URL, "/test")
    )

    private fun awaitText(text: String) {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(text).assertIsDisplayed()
    }

    @Test fun testUserSeesTestDetails() {
        val text = "<assertion> & failure\nTest details"
        `when`(service.testOccurrence(anyString())).thenReturn(Single.just(TestOccurrence(text)))
        launch().use {
            awaitText(text)
            compose.onNodeWithText("Details").assertIsDisplayed()
        }
    }

    @Test fun testUserSeesNoDataIfTestDetailsAreNotProvided() {
        `when`(service.testOccurrence(anyString())).thenReturn(Single.just(TestOccurrence("")))
        launch().use { awaitText("No test details") }
    }

    @Test fun testUserSeesErrorMessageIfDetailsIsNotLoaded() {
        `when`(service.testOccurrence(anyString())).thenReturn(Single.error(RuntimeException("offline")))
        launch().use {
            awaitText(app.getString(teamcityapp.features.test_details.R.string.error_view_error_text))
            `when`(service.testOccurrence(anyString())).thenReturn(Single.just(TestOccurrence("Recovered")))
            compose.onNodeWithText(app.getString(teamcityapp.features.test_details.R.string.error_view_retry_button_text)).performClick()
            awaitText("Recovered")
        }
    }

    @Test fun recreationKeepsTheViewModelAndPendingRequest() {
        val request = SingleSubject.create<TestOccurrence>()
        `when`(service.testOccurrence(anyString())).thenReturn(request)
        launch().use { scenario ->
            compose.waitUntil(5_000) { request.hasObservers() }
            scenario.recreate()
            request.onSuccess(TestOccurrence("After rotation"))
            awaitText("After rotation")
            verify(service, times(1)).testOccurrence(anyString())
        }
    }
    @Test fun missingUrlFinishesWithoutResolvingAccountApi() {
        app.appInjector.sharedUserStorage().clearAll()
        ActivityScenario.launch<TestDetailsActivity>(Intent(app, TestDetailsActivity::class.java)).use { scenario ->
            compose.waitUntil(5_000) { scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
            verify(service, never()).testOccurrence(anyString())
        }
    }

    @Test fun closeReturnsToCaller() {
        `when`(service.testOccurrence(anyString())).thenReturn(Single.just(TestOccurrence("Details to close")))
        launch().use { scenario ->
            awaitText("Details to close")
            compose.onNodeWithContentDescription("Close").performClick()
            compose.waitUntil(5_000) { scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
        }
    }

}
