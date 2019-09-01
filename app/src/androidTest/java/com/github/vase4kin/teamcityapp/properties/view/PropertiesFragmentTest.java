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

package com.github.vase4kin.teamcityapp.properties.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.azimolabs.conditionwatcher.ConditionWatcher;
import com.azimolabs.conditionwatcher.Instruction;
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
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule;
import com.github.vase4kin.teamcityapp.helper.TestUtils;
import com.github.vase4kin.teamcityapp.properties.api.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import java.util.Collections;

import io.reactivex.Single;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PropertiesFragment}
 */
@RunWith(AndroidJUnit4.class)
public class PropertiesFragmentTest {

    private static final String NAME = "name";
    private static final int TIMEOUT = 5000;

    @Rule
    public DaggerMockRule<RestApiComponent> mRestComponentDaggerRule = new DaggerMockRule<>(RestApiComponent.class, new RestApiModule(Mocks.URL))
            .addComponentDependency(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(restApiComponent -> {
                TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                app.setRestApiInjector(restApiComponent);
            });

    @Rule
    public CustomActivityTestRule<BuildDetailsActivity> mActivityRule = new CustomActivityTestRule<>(BuildDetailsActivity.class);

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @Spy
    private Build mBuild = Mocks.successBuild();

    @BeforeClass
    public static void disableOnboarding() {
        TestUtils.disableOnboarding();
    }

    @Before
    public void setUp() {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        app.getRestApiInjector().sharedUserStorage().clearAll();
        app.getRestApiInjector().sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false);
    }

    @Test
    public void testUserCanSeeBuildProperties() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking properties tab title
        onView(withText("Parameters"))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .perform(click());

        // Check properties
        onView(withId(R.id.properties_recycler_view)).check(TestUtils.hasItemsCount(2));
        onView(withRecyclerView(R.id.properties_recycler_view).atPositionOnView(0, R.id.itemHeader)).check(matches(withText("sdk")));
        onView(withRecyclerView(R.id.properties_recycler_view).atPositionOnView(0, R.id.itemTitle)).check(matches(withText("24")));
        onView(withRecyclerView(R.id.properties_recycler_view).atPositionOnView(1, R.id.itemHeader)).check(matches(withText("userName")));
        onView(withRecyclerView(R.id.properties_recycler_view).atPositionOnView(1, R.id.itemTitle)).check(matches(withText("Murdock")));
    }

    @Test
    public void testUserCanSeeEmptyPropertiesMessageIfPropertiesAreNull() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild(null));
        b.putString(BundleExtractorValues.NAME, NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking properties tab title
        onView(withText("Parameters"))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .perform(click());

        //Checking empty message
        onView(withText(R.string.empty_list_message_parameters)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeEmptyPropertiesMessageIfPropertiesAreEmpty() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild(new Properties(Collections.<Properties.Property>emptyList())));
        b.putString(BundleExtractorValues.NAME, NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking properties tab title
        onView(withText("Parameters"))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .perform(click());

        //Checking empty message
        onView(withText(R.string.empty_list_message_parameters)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanCopyPropertyValueFromTheList() throws Exception {
        ConditionWatcher.setTimeoutLimit(TIMEOUT);
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Single.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        b.putString(BundleExtractorValues.NAME, NAME);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking properties tab title
        onView(withText("Parameters"))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .perform(click());

        // Click on parameter
        onView(withRecyclerView(R.id.properties_recycler_view).atPosition(0))
                .perform(click());

        ConditionWatcher.waitForCondition(new Instruction() {
            @Override
            public String getDescription() {
                return "The parameters menu is not opened";
            }

            @Override
            public boolean checkCondition() {
                boolean isParameterClicked = false;
                try {
                    onView(withText(R.string.build_element_copy)).check(matches(isDisplayed()));
                    isParameterClicked = true;
                } catch (Exception ignored) {
                    onView(withRecyclerView(R.id.properties_recycler_view).atPosition(0))
                            .perform(click());
                }
                return isParameterClicked;
            }
        });

        // Clicking on copy
        onView(withText(R.string.build_element_copy)).perform(click());

        // Checking toast message
        onView(withText(R.string.build_element_copy_text))
                .check(matches(isDisplayed()));
    }

}