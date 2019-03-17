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

package com.github.vase4kin.teamcityapp.testdetails.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;

import io.reactivex.Single;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.matchToolbarTitle;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link TestDetailsActivity}
 */
@RunWith(AndroidJUnit4.class)
public class TestDetailsActivityTest {

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
    public CustomActivityTestRule<TestDetailsActivity> mActivityRule = new CustomActivityTestRule<>(TestDetailsActivity.class);

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @Mock
    private TestOccurrences.TestOccurrence mTest;

    @Test
    public void testUserSeesTestDetails() throws Exception {
        // Prepare mocks
        when(mTest.getDetails()).thenReturn("Test details");
        when(mTeamCityService.testOccurrence(anyString())).thenReturn(Single.just(mTest));

        // Prepare intent
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putString(BundleExtractorValues.TEST_URL, "/test");
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking toolbar title
        matchToolbarTitle("Details");

        // check details
        onView(withId(R.id.test_occurrence_details)).check(matches(allOf(withText("Test details"), isDisplayed())));
    }

    @Test
    public void testUserSeesNoDataIfTestDetailsAreNotProvided() throws Exception {
        // Prepare mocks
        when(mTest.getDetails()).thenReturn("");
        when(mTeamCityService.testOccurrence(anyString())).thenReturn(Single.just(mTest));

        // Prepare intent
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putString(BundleExtractorValues.TEST_URL, "/test");
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking toolbar title
        matchToolbarTitle("Details");

        // check no data message
        onView(withId(R.id.empty_title)).check(matches(allOf(withText(R.string.text_empty_test_details), isDisplayed())));
    }

    @Test
    public void testUserSeesErrorMessageIfDetailsIsNotLoaded() throws Exception {
        // Prepare mocks
        when(mTeamCityService.testOccurrence(anyString())).thenReturn(Single.<TestOccurrences.TestOccurrence>error(new RuntimeException("Errror!")));

        // Prepare intent
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putString(BundleExtractorValues.TEST_URL, "/test");
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking toolbar title
        matchToolbarTitle("Details");

        // check details error
        onView(withText("Errror!")).check(matches(isDisplayed()));
    }

}