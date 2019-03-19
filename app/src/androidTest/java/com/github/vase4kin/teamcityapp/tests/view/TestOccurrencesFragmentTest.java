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

package com.github.vase4kin.teamcityapp.tests.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule;
import com.github.vase4kin.teamcityapp.helper.TestUtils;
import com.github.vase4kin.teamcityapp.testdetails.view.TestDetailsActivity;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import io.reactivex.Single;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.assertNoUnverifiedIntents;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.hasItemsCount;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link TestOccurrencesFragment}
 */
@RunWith(AndroidJUnit4.class)
public class TestOccurrencesFragmentTest {

    private static final String BUILD_TYPE_NAME = "name";

    @Rule
    public DaggerMockRule<AppComponent> mAppComponentDaggerRule = new DaggerMockRule<>(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(new DaggerMockRule.ComponentSetter<AppComponent>() {
                @Override
                public void setComponent(AppComponent appComponent) {
                    TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                    app.setAppInjector(appComponent);
                }
            });

    @Rule
    public DaggerMockRule<RestApiComponent> mRestComponentDaggerRule = new DaggerMockRule<>(RestApiComponent.class, new RestApiModule(Mocks.URL))
            .addComponentDependency(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(new DaggerMockRule.ComponentSetter<RestApiComponent>() {
                @Override
                public void setComponent(RestApiComponent restApiComponent) {
                    TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                    app.setRestApiInjector(restApiComponent);
                }
            });

    @Rule
    public CustomIntentsTestRule<BuildDetailsActivity> mActivityRule = new CustomIntentsTestRule<>(BuildDetailsActivity.class);

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @Spy
    private Build mBuild = Mocks.failedBuild();

    @BeforeClass
    public static void disableOnboarding() {
        TestUtils.disableOnboarding();
    }

    @Test
    public void testUserCanSeeBuildFailedTests() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking tests tab title
        onView(withText("Tests (16)"))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .perform(click());

        // Check failed tests
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(0, R.id.section_text))
                .check(matches(withText("Failed (2)")));
        onView(withId(R.id.tests_recycler_view)).check(hasItemsCount(3));
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText("Test 1")));
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(2, R.id.itemTitle))
                .check(matches(withText("Test 6")));
    }

    @Test
    public void testUserCanBeNavigatedToFailedTest() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking tests tab title
        onView(withText("Tests (16)"))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .perform(click());

        // Click on first failed test
        onView(withRecyclerView(R.id.tests_recycler_view).atPosition(1)).perform(click());

        // Check recorded intent
        intended(allOf(
                hasComponent(TestDetailsActivity.class.getName()),
                hasExtras(
                        hasEntry(
                                equalTo(BundleExtractorValues.TEST_URL),
                                equalTo("/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)")))));
    }

    @Test
    public void testUserCanSeeEmptyFailedTestsMessage() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));
        when(mTeamCityService.listTestOccurrences("/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:FAILURE,count:10")).thenReturn(Single.just(new TestOccurrences(0)));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking tests tab title
        onView(withText("Tests (16)"))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .perform(click());

        // Check failed tests empty message
        onView(withText(R.string.empty_passed_tests)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeePassedTests() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking tests tab title
        onView(withText("Tests (16)"))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .perform(click());

        // Open menu
        openActionBarOverflowOrOptionsMenu(mActivityRule.getActivity());

        // Show passed tests
        onView(withText("Show passed")).perform(click());

        // Check passed tests
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(0, R.id.section_text))
                .check(matches(withText("Passed (10)")));
        onView(withId(R.id.tests_recycler_view)).check(hasItemsCount(3));
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText("Test 5")));
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(2, R.id.itemTitle))
                .check(matches(withText("Test 2")));
    }

    @Test
    public void testUserCanNotInteractWithPassedTests() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking tests tab title
        onView(withText("Tests (16)"))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .perform(click());

        // Open menu
        openActionBarOverflowOrOptionsMenu(mActivityRule.getActivity());

        // Show passed tests
        onView(withText("Show passed")).perform(click());

        // Click on first passed test
        onView(withRecyclerView(R.id.tests_recycler_view).atPosition(1)).perform(click());

        // Check no intent were started
        assertNoUnverifiedIntents();
    }

    @Test
    public void testUserCanSeeIgnoredTests() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking tests tab title
        onView(withText("Tests (16)"))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .perform(click());

        // Open menu
        openActionBarOverflowOrOptionsMenu(mActivityRule.getActivity());

        // Show ignored tests
        onView(withText("Show ignored")).perform(click());

        // Check ignored tests
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(0, R.id.section_text))
                .check(matches(withText("Ignored (4)")));
        onView(withId(R.id.tests_recycler_view)).check(hasItemsCount(3));
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(1, R.id.itemTitle))
                .check(matches(withText("Test 4")));
        onView(withRecyclerView(R.id.tests_recycler_view).atPositionOnView(2, R.id.itemTitle))
                .check(matches(withText("Test 9")));
    }

    @Test
    public void testUserCanNotInteractWithIgnoredTests() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild());
        b.putString(BundleExtractorValues.NAME, BUILD_TYPE_NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking tests tab title
        onView(withText("Tests (16)"))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .perform(click());

        // Open menu
        openActionBarOverflowOrOptionsMenu(mActivityRule.getActivity());

        // Show ignored tests
        onView(withText("Show passed")).perform(click());

        // Click on first ignored test
        onView(withRecyclerView(R.id.tests_recycler_view).atPosition(1)).perform(click());

        // Check no intent were started
        assertNoUnverifiedIntents();
    }
}