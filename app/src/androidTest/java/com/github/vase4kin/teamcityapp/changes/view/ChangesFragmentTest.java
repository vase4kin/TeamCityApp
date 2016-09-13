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

package com.github.vase4kin.teamcityapp.changes.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildtabs.view.BuildTabsActivity;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import it.cosenonjaviste.daggermock.DaggerMockRule;
import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView;
import static com.github.vase4kin.teamcityapp.helper.TestUtils.hasItemsCount;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ChangesFragment}
 */
@RunWith(AndroidJUnit4.class)
public class ChangesFragmentTest {

    @Rule
    public DaggerMockRule<AppComponent> mAppComponentDaggerRule = new DaggerMockRule<>(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .provides(SharedUserStorage.class, SharedUserStorage.init(InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
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
            .providesMock(SharedUserStorage.class)
            .set(new DaggerMockRule.ComponentSetter<RestApiComponent>() {
                @Override
                public void setComponent(RestApiComponent restApiComponent) {
                    TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                    app.setRestApiInjector(restApiComponent);
                }
            });

    @Rule
    public CustomActivityTestRule<BuildTabsActivity> mActivityRule = new CustomActivityTestRule<>(BuildTabsActivity.class);

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @Spy
    private Build mBuild = Mocks.successBuild();

    @Test
    public void testUserCanSeeBuildChanges() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Observable.just(mBuild));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking changes tab title
        onView(withText("Changes (1)"))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.changes_recycler_view)).check(hasItemsCount(1));
        // Checking User name
        onView(withRecyclerView(R.id.changes_recycler_view).atPositionOnView(0, R.id.userName)).check(matches(withText("john-117")));
        // Checking comment
        onView(withRecyclerView(R.id.changes_recycler_view).atPositionOnView(0, R.id.itemTitle)).check(matches(withText("Do you believe?")));
        // Checking commit number
        onView(withRecyclerView(R.id.changes_recycler_view).atPositionOnView(0, R.id.itemSubTitle)).check(matches(withText("21312fsd1321")));
        // Checking data
        onView(withRecyclerView(R.id.changes_recycler_view).atPositionOnView(0, R.id.date)).check(matches(withText("30 Jul 16 00:36")));

        // Clicking on change
        onView(withRecyclerView(R.id.changes_recycler_view).atPosition(0)).perform(click());

        // Сhecking dialog content
        onView(withId(R.id.content)).check(matches(withText("john-117 on 30 Jul 16 00:36")));
        // Сheck files
        onView(withText("filename!")).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeChangesFailureMessageIfSmthBadHappens() {
        // Prepare mocks
        when(mTeamCityService.listChanges(anyString())).thenReturn(Observable.<Changes>error(new RuntimeException("Fake error happened!")));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking changes tab title
        onView(withText("Changes (0)"))
                .check(matches(isDisplayed()))
                .perform(click());

        //Checking error
        onView(withText("Fake error happened!")).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeEmptyChangesMessageIfNoChangesAreAvailable() {
        // Prepare mocks
        when(mTeamCityService.listChanges(anyString())).thenReturn(Observable.<Changes>empty());

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild());
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Checking changes tab title
        onView(withText("Changes"))
                .check(matches(isDisplayed()))
                .perform(click());

        //Checking message
        onView(withText(R.string.empty_list_message_changes)).check(matches(isDisplayed()));
    }
}