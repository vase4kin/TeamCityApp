/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.agenttabs.view;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.hasItemsCount;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarTitle;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Tests for {@link AgentTabsActivity}
 */
@RunWith(AndroidJUnit4.class)
public class AgentTabsActivityTest {

    @Rule
    public DaggerMockRule<RestApiComponent> mDaggerRule = new DaggerMockRule<>(RestApiComponent.class, new RestApiModule(Mocks.URL))
            .addComponentDependency(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(new DaggerMockRule.ComponentSetter<RestApiComponent>() {
                @Override
                public void setComponent(RestApiComponent restApiComponent) {
                    TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                    app.setRestApiInjector(restApiComponent);
                }
            });

    @Rule
    public CustomActivityTestRule<AgentTabsActivity> mActivityRule = new CustomActivityTestRule<>(AgentTabsActivity.class);

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @Test
    public void testUserCanSeeSuccessfullyLoadedAgents() throws Exception {
        mActivityRule.launchActivity(null);

        // checking toolbar title
        matchToolbarTitle("Agents");
        // checking connected tab title
        onView(withText("Connected (3)"))
                .check(matches(isDisplayed()));
        // checking connected
        onView(allOf(withId(R.id.connected_agents_recycler_view), isDisplayed()))
                .check(hasItemsCount(3));
        onView(allOf(withRecyclerView(R.id.connected_agents_recycler_view).atPositionOnView(0, R.id.itemTitle), isDisplayed()))
                .check(matches(withText("agent 1")));
        onView(withRecyclerView(R.id.connected_agents_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText("agent 2")));
        onView(withRecyclerView(R.id.connected_agents_recycler_view).atPositionOnView(2, R.id.itemTitle))
                .check(matches(withText("agent 3")));

        // checking disconnected tab title
        onView(withText("Disconnected (1)"))
                .check(matches(isDisplayed()))
                .perform(click());
        // checking disconnected
        onView(allOf(withId(R.id.disconnected_agents_recycler_view), isDisplayed()))
                .check(hasItemsCount(1));
        onView(withRecyclerView(R.id.disconnected_agents_recycler_view).atPositionOnView(0, R.id.itemTitle))
                .check(matches(withText("Mac mini 3434")));
    }
}