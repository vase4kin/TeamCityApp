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

package com.github.vase4kin.teamcityapp.favorites.view;

import android.content.Intent;
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
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule;
import com.github.vase4kin.teamcityapp.helper.TestUtils;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.root.view.RootProjectsActivity;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import it.cosenonjaviste.daggermock.DaggerMockRule;
import rx.Observable;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasFlags;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.hasItemsCount;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarTitle;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class FavoritesActivityTest {

    @Rule
    public DaggerMockRule<AppComponent> daggerRule = new DaggerMockRule<>(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(new DaggerMockRule.ComponentSetter<AppComponent>() {
                @Override
                public void setComponent(AppComponent appComponent) {
                    TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                    app.setAppInjector(appComponent);
                }
            });

    @Rule
    public DaggerMockRule<RestApiComponent> restComponentDaggerRule = new DaggerMockRule<>(RestApiComponent.class, new RestApiModule(Mocks.URL))
            .addComponentDependency(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(new DaggerMockRule.ComponentSetter<RestApiComponent>() {
                @Override
                public void setComponent(RestApiComponent restApiComponent) {
                    TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                    app.setRestApiInjector(restApiComponent);
                }
            });

    @Rule
    public CustomIntentsTestRule<FavoritesActivity> activityRule = new CustomIntentsTestRule<>(FavoritesActivity.class);

    @Spy
    private TeamCityService teamCityService = new FakeTeamCityServiceImpl();

    @BeforeClass
    public static void disableOnboarding() {
        TestUtils.disableOnboarding();
    }

    @Before
    public void setUp() {
        SharedUserStorage storage = getStorage();
        storage.clearAll();
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false);
    }

    @Test
    public void testUserCanSeeFavoritesListIfSmthBadHappensInFavoritesLoading() {
        // prepare mocks
        when(teamCityService.buildType(anyString())).thenReturn(Observable.<BuildType>error(new RuntimeException("smth bad happend!")));
        getStorage().addBuildTypeToFavorites("id");
        getStorage().addBuildTypeToFavorites("id2");
        getStorage().addBuildTypeToFavorites("id3");

        // launch activity
        activityRule.launchActivity(null);

        // Check empty list
        onView(withId(android.R.id.empty)).check(matches(isDisplayed())).check(matches(withText(R.string.empty_list_message_favorites)));
    }

    @Test
    public void testUserCanSeeLoadEmptyListMessageIfFavoritesIsEmpty() {
        // launch activity
        activityRule.launchActivity(null);

        // Checking toolbar title
        matchToolbarTitle("Favorites (0)");

        // Check the list is empty
        onView(withId(android.R.id.empty)).check(matches(isDisplayed())).check(matches(withText(R.string.empty_list_message_favorites)));
    }

    @Test
    public void testUserCanClickOnFabAndSeeProjects() {
        // launch activity
        activityRule.launchActivity(null);

        // click on fab
        onView(withId(R.id.floating_action_button)).perform(click());

        // check snack bar text
        onView(withText(R.string.text_info_add)).check(matches(isDisplayed()));

        // click on snack bar action
        onView(withText(R.string.text_info_add_action)).perform(click());

        // check activity is opened
        intended(allOf(
                hasComponent(RootProjectsActivity.class.getName()),
                hasFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                        | FLAG_ACTIVITY_SINGLE_TOP)));

    }

    @Test
    public void testUserCanAddBuildTypeToFavorites() {
        //prepare mocks
        when(teamCityService.buildType(Mocks.buildTypeMock().getId())).thenReturn(Observable.just(Mocks.buildTypeMock()));

        // launch activity
        activityRule.launchActivity(null);

        // click on fab
        onView(withId(R.id.floating_action_button)).perform(click());

        // click on snack bar action
        onView(withText(R.string.text_info_add_action)).perform(click());

        // Open build type
        onView(withText("build type"))
                .perform(click());

        // Click on add to favorites
        onView(withId(R.id.add_to_favorites)).perform(click());

        // check snack bar text
        onView(withText(R.string.text_add_to_favorites)).check(matches(isDisplayed()));

        // click on snack bar action
        onView(withText(R.string.text_view_favorites)).perform(click());

        // check activity is opened
        intended(allOf(
                hasComponent(FavoritesActivity.class.getName()),
                hasFlags(FLAG_ACTIVITY_SINGLE_TOP)));

        // Checking toolbar title
        matchToolbarTitle("Favorites (1)");

        // List has item with header
        onView(withId(R.id.favorites_recycler_view)).check(hasItemsCount(2));
        // Checking header 1
        onView(withRecyclerView(R.id.favorites_recycler_view).atPositionOnView(0, R.id.section_text))
                .check(matches(withText("Secret project")));
        // Checking adapter item 1
        onView(withRecyclerView(R.id.favorites_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText("build type")));
    }

    private SharedUserStorage getStorage() {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        return app.getAppInjector().sharedUserStorage();
    }
}