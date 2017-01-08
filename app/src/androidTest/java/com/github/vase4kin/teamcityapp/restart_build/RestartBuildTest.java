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

package com.github.vase4kin.teamcityapp.restart_build;

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
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule;
import com.github.vase4kin.teamcityapp.properties.api.Properties;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Collections;

import it.cosenonjaviste.daggermock.DaggerMockRule;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openContextualActionModeOverflowMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView;
import static com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor.CODE_FORBIDDEN;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for restart build feature
 */
@RunWith(AndroidJUnit4.class)
public class RestartBuildTest {

    private static final String BRANCH_NAME = "refs/heads/dev";
    private static final String PROPERTY_NAME = "property";
    private static final String PROPERTY_VALUE = "true";

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
    public CustomIntentsTestRule<BuildTabsActivity> mActivityRule = new CustomIntentsTestRule<>(BuildTabsActivity.class);

    @Captor
    private ArgumentCaptor<Build> mBuildArgumentCaptor;

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @Spy
    private Build mBuild = Mocks.failedBuild();

    private Build mBuild2 = Mocks.queuedBuild2();

    @Mock
    ResponseBody mResponseBody;

    @Mock
    private UserAccount mUserAccount;

    @Test
    public void testUserCanRestartBuildWithTheSameParameters() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Observable.just(mBuild))
                .thenReturn(Observable.just(mBuild2));
        when(mTeamCityService.queueBuild(Matchers.any(Build.class))).thenReturn(Observable.just(Mocks.queuedBuild2()));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        Build buildToRestart = Mocks.failedBuild();
        Properties.Property property = new Properties.Property(PROPERTY_NAME, PROPERTY_VALUE);
        buildToRestart.setBranchName(BRANCH_NAME);
        buildToRestart.setProperties(new Properties(Collections.singletonList(property)));
        b.putSerializable(BundleExtractorValues.BUILD, buildToRestart);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Opening context menu
        openContextualActionModeOverflowMenu();

        // Click on context menu option
        onView(withText(R.string.text_menu_restart_build)).perform(click());

        // Check dialog text is displayed
        onView(withText(R.string.text_restart_the_build)).check(matches(isDisplayed()));

        // Click on restart button
        onView(withText(R.string.text_restart_button)).perform(click());

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_restarted)).check(matches(isDisplayed()));

        // Checking passed params
        verify(mTeamCityService).queueBuild(mBuildArgumentCaptor.capture());
        Build capturedBuild = mBuildArgumentCaptor.getValue();
        assertThat(capturedBuild.getBranchName(), is(equalTo(BRANCH_NAME)));
        assertThat(capturedBuild.getProperties().getObjects().size(), is(equalTo(1)));
        Properties.Property capturedProperty = capturedBuild.getProperties().getObjects().get(0);
        assertThat(capturedProperty.getName(), is(equalTo(PROPERTY_NAME)));
        assertThat(capturedProperty.getValue(), is(equalTo(PROPERTY_VALUE)));

        // Click on show button of queued build snack bar
        onView(withText(R.string.text_show_build)).perform(click());

        // Check build is opened
        intended(allOf(
                hasComponent(BuildTabsActivity.class.getName()),
                hasExtras(hasEntry(CoreMatchers.equalTo(BundleExtractorValues.BUILD), CoreMatchers.equalTo(mBuild2)))));

        // Checking Result was changed
        onView(withRecyclerView(R.id.overview_recycler_view)
                .atPositionOnView(0, R.id.itemTitle))
                .check(matches(withText("This build will not start because there are no compatible agents which can run it")));
    }

    @Test
    public void testUserCanRestartBuildWithTheSameParametersButFailedToOpenItThen() {
        // Prepare mocks
        HttpException httpException = new HttpException(Response.<Build>error(500, mResponseBody));
        when(mTeamCityService.build(anyString())).thenReturn(Observable.just(mBuild))
                .thenReturn(Observable.<Build>error(httpException))
                .thenReturn(Observable.<Build>error(httpException));
        when(mTeamCityService.queueBuild(Matchers.any(Build.class))).thenReturn(Observable.just(Mocks.queuedBuild2()));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        Build buildToRestart = Mocks.failedBuild();
        Properties.Property property = new Properties.Property(PROPERTY_NAME, PROPERTY_VALUE);
        buildToRestart.setBranchName(BRANCH_NAME);
        buildToRestart.setProperties(new Properties(Collections.singletonList(property)));
        b.putSerializable(BundleExtractorValues.BUILD, buildToRestart);
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Opening context menu
        openContextualActionModeOverflowMenu();

        // Click on context menu option
        onView(withText(R.string.text_menu_restart_build)).perform(click());

        // Click on restart button
        onView(withText(R.string.text_restart_button)).perform(click());

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_restarted)).check(matches(isDisplayed()));

        // Click on show button of queued build snack bar
        onView(withText(R.string.text_show_build)).perform(click());

        // Check error snack bar
        onView(withText(R.string.error_opening_build)).check(matches(isDisplayed()));

        // Click on retry button
        onView(withText(R.string.download_artifact_retry_snack_bar_retry_button)).perform(click());

        // Check error snack bar
        onView(withText(R.string.error_opening_build)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeForbiddenErrorWhenRestartingBuild() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Observable.just(mBuild));
        HttpException httpException = new HttpException(Response.<Build>error(CODE_FORBIDDEN, mResponseBody));
        when(mTeamCityService.queueBuild(Matchers.any(Build.class))).thenReturn(Observable.<Build>error(httpException));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild());
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Opening context menu
        openContextualActionModeOverflowMenu();

        // Click on context menu option
        onView(withText(R.string.text_menu_restart_build)).perform(click());

        // Click on restart button
        onView(withText(R.string.text_restart_button)).perform(click());

        // Check snack bar is displayed
        onView(withText(R.string.error_restart_build_forbidden_error)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeServerErrorWhenRestartingBuild() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Observable.just(mBuild));
        HttpException httpException = new HttpException(Response.<Build>error(500, mResponseBody));
        when(mTeamCityService.queueBuild(Matchers.any(Build.class))).thenReturn(Observable.<Build>error(httpException));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild());
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Opening context menu
        openContextualActionModeOverflowMenu();

        // Click on context menu option
        onView(withText(R.string.text_menu_restart_build)).perform(click());

        // Click on restart button
        onView(withText(R.string.text_restart_button)).perform(click());

        // Check snack bar is displayed
        onView(withText(R.string.error_base_restart_build_error)).check(matches(isDisplayed()));
    }

}