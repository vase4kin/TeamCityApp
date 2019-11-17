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

package com.github.vase4kin.teamcityapp.account.manage.view

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.github.vase4kin.teamcityapp.storage.UsersFactory
import it.cosenonjaviste.daggermock.DaggerMockRule
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Spy

@RunWith(AndroidJUnit4::class)
class ManageAccountsActivityTest {

    @Rule
    @JvmField
    val restComponentDaggerRule: DaggerMockRule<RestApiComponent> =
        DaggerMockRule(RestApiComponent::class.java, RestApiModule(Mocks.URL))
            .addComponentDependency(
                AppComponent::class.java,
                AppModule(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication)
            )
            .set { restApiComponent ->
                val app =
                    InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
                app.setRestApiInjector(restApiComponent)
            }

    @Rule
    @JvmField
    val activityRule = CustomIntentsTestRule(HomeActivity::class.java)

    @Spy
    val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    private val storage: SharedUserStorage
        get() {
            val app =
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
            return app.appInjector.sharedUserStorage()
        }

    companion object {
        @BeforeClass
        fun disableOnboarding() {
            TestUtils.disableOnboarding()
        }
    }

    @Before
    fun setUp() {
        val storage = storage
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    @Test
    fun testUserCanSeeUserAccount() {
        // Launch activity
        activityRule.launchActivity(null)

        // List has item with header
        Espresso.onView(ViewMatchers.withId(R.id.my_recycler_view))
            .check(TestUtils.hasItemsCount(1))
        // Checking the user name
        Espresso.onView(
            RecyclerViewMatcher.withRecyclerView(R.id.my_recycler_view).atPositionOnView(
                0,
                R.id.title
            )
        )
            .check(ViewAssertions.matches(ViewMatchers.withText(UsersFactory.GUEST_USER_USER_NAME)))
        // Checking the user teamcity server url
        Espresso.onView(
            RecyclerViewMatcher.withRecyclerView(R.id.my_recycler_view).atPositionOnView(
                0,
                R.id.subTitle
            )
        )
            .check(ViewAssertions.matches(ViewMatchers.withText(Mocks.URL)))
    }
}