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

package com.github.vase4kin.teamcityapp.cancel_build;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildtabs.api.BuildCancelRequest;
import com.github.vase4kin.teamcityapp.buildtabs.view.BuildTabsActivity;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl;
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;

import it.cosenonjaviste.daggermock.DaggerMockRule;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openContextualActionModeOverflowMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.helper.RecyclerViewMatcher.withRecyclerView;
import static com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor.CODE_FORBIDDEN;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for cancel build feature
 */
@RunWith(AndroidJUnit4.class)
public class CancelBuildTest {

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
    public CustomActivityTestRule<BuildTabsActivity> mActivityRule = new CustomActivityTestRule<>(BuildTabsActivity.class);

    @Spy
    private TeamCityService mTeamCityService = new FakeTeamCityServiceImpl();

    @Spy
    private Build mBuild = Mocks.queuedBuild1();

    @Mock
    private SharedUserStorage mSharedUserStorage;

    @Mock
    ResponseBody mResponseBody;

    @Mock
    private UserAccount mUserAccount;

    @Before
    public void setUp() throws Exception {
        when(mSharedUserStorage.getActiveUser()).thenReturn(mUserAccount);
        when(mUserAccount.getUserName()).thenReturn("code-hater");
    }

    @Test
    public void testUserCanRemoveQueuedBuildFromQueueWhichWasStartedByHim() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Observable.just(mBuild))
                .thenReturn(Observable.just(Mocks.queuedBuild2()));
        when(mTeamCityService.cancelBuild(anyString(), Matchers.any(BuildCancelRequest.class))).thenReturn(Observable.just(Mocks.queuedBuild2()));
        when(mUserAccount.getUserName()).thenReturn("code-lover");

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.queuedBuild1());
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Opening context menu
        openContextualActionModeOverflowMenu();

        // Click on context menu option
        onView(withText(R.string.text_menu_remove_build_from_queue)).perform(click());

        // Check dialog text is displayed
        onView(withText(R.string.text_remove_build_from_queue)).check(matches(isDisplayed()));

        // Click on cancel build
        onView(withText(R.string.text_remove_from_queue_button)).perform(click());

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_removed_from_queue)).check(matches(isDisplayed()));

        // Checking Result was changed
        onView(withRecyclerView(R.id.overview_recycler_view)
                .atPositionOnView(0, R.id.itemTitle))
                .check(matches(withText("This build will not start because there are no compatible agents which can run it")));
    }

    @Test
    public void testUserCanRemoveQueuedBuildFromQueueWhichWasStartedNotByHim() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Observable.just(mBuild))
                .thenReturn(Observable.just(Mocks.queuedBuild2()));
        when(mTeamCityService.cancelBuild(anyString(), Matchers.any(BuildCancelRequest.class))).thenReturn(Observable.just(Mocks.queuedBuild2()));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.queuedBuild1());
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Opening context menu
        openContextualActionModeOverflowMenu();

        // Click on context menu option
        onView(withText(R.string.text_menu_remove_build_from_queue)).perform(click());

        // Check dialog text is displayed
        onView(withText(R.string.text_remove_build_from_queue_2)).check(matches(isDisplayed()));

        // Click on cancel build
        onView(withText(R.string.text_remove_from_queue_button)).perform(click());

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_removed_from_queue)).check(matches(isDisplayed()));

        // Checking Result was changed
        onView(withRecyclerView(R.id.overview_recycler_view)
                .atPositionOnView(0, R.id.itemTitle))
                .check(matches(withText("This build will not start because there are no compatible agents which can run it")));
    }

    @Test
    public void testUserCanSeeForbiddenErrorWhenRemovingBuildFromQueue() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Observable.just(mBuild));
        HttpException httpException = new HttpException(Response.<Build>error(CODE_FORBIDDEN, mResponseBody));
        when(mTeamCityService.cancelBuild(anyString(), Matchers.any(BuildCancelRequest.class))).thenReturn(Observable.<Build>error(httpException));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.queuedBuild1());
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Opening context menu
        openContextualActionModeOverflowMenu();

        // Click on context menu option
        onView(withText(R.string.text_menu_remove_build_from_queue)).perform(click());

        // Click on cancel build
        onView(withText(R.string.text_remove_from_queue_button)).perform(click());

        // Check snack bar is displayed
        onView(withText(R.string.error_remove_build_from_queue_forbidden_error)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeServerErrorWhenRemovingBuildFromQueue() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Observable.just(mBuild));
        HttpException httpException = new HttpException(Response.<Build>error(500, mResponseBody));
        when(mTeamCityService.cancelBuild(anyString(), Matchers.any(BuildCancelRequest.class))).thenReturn(Observable.<Build>error(httpException));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.queuedBuild1());
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Opening context menu
        openContextualActionModeOverflowMenu();

        // Click on context menu option
        onView(withText(R.string.text_menu_remove_build_from_queue)).perform(click());

        // Click on cancel build
        onView(withText(R.string.text_remove_from_queue_button)).perform(click());

        // Check snack bar is displayed
        onView(withText(R.string.error_base_remove_build_from_queue_error)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanStopBuildWhichWasStartedByNotHim() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Observable.just(Mocks.runningBuild()))
                .thenReturn(Observable.just(Mocks.failedBuild()));
        when(mTeamCityService.cancelBuild(anyString(), Matchers.any(BuildCancelRequest.class))).thenReturn(Observable.just(Mocks.failedBuild()));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.runningBuild());
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Opening context menu
        openContextualActionModeOverflowMenu();

        // Click on context menu option
        onView(withText(R.string.text_menu_stop_build)).perform(click());

        // Check dialog text is displayed
        onView(withText(R.string.text_stop_the_build_2)).check(matches(isDisplayed()));

        // Click on cancel build
        onView(withText(R.string.text_stop_button)).perform(click());

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_stopped)).check(matches(isDisplayed()));

        // Checking Result was changed
        onView(withRecyclerView(R.id.overview_recycler_view)
                .atPositionOnView(0, R.id.itemTitle))
                .check(matches(withText("Error with smth")));
    }

    @Test
    public void testUserCanStopBuildWhichWasStartedByHim() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Observable.just(Mocks.runningBuild()))
                .thenReturn(Observable.just(Mocks.failedBuild()));
        when(mTeamCityService.cancelBuild(anyString(), Matchers.any(BuildCancelRequest.class))).thenReturn(Observable.just(Mocks.failedBuild()));
        when(mUserAccount.getUserName()).thenReturn("code-lover");

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.runningBuild());
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Opening context menu
        openContextualActionModeOverflowMenu();

        // Click on context menu option
        onView(withText(R.string.text_menu_stop_build)).perform(click());

        // Check dialog text is displayed
        onView(withText(R.string.text_stop_the_build)).check(matches(isDisplayed()));

        // Click on cancel build
        onView(withText(R.string.text_stop_button)).perform(click());

        // Check snack bar is displayed
        onView(withText(R.string.text_build_is_stopped)).check(matches(isDisplayed()));

        // Checking Result was changed
        onView(withRecyclerView(R.id.overview_recycler_view)
                .atPositionOnView(0, R.id.itemTitle))
                .check(matches(withText("Error with smth")));
    }

    @Test
    public void testUserCanSeeForbiddenErrorWhenStoppingBuild() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Observable.just(Mocks.runningBuild()));
        HttpException httpException = new HttpException(Response.<Build>error(CODE_FORBIDDEN, mResponseBody));
        when(mTeamCityService.cancelBuild(anyString(), Matchers.any(BuildCancelRequest.class))).thenReturn(Observable.<Build>error(httpException));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.runningBuild());
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Opening context menu
        openContextualActionModeOverflowMenu();

        // Click on context menu option
        onView(withText(R.string.text_menu_stop_build)).perform(click());

        // Click on cancel build
        onView(withText(R.string.text_stop_button)).perform(click());

        // Check snack bar is displayed
        onView(withText(R.string.error_stop_build_forbidden_error)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserCanSeeServerErrorWhenStoppingBuild() {
        // Prepare mocks
        when(mTeamCityService.build(anyString())).thenReturn(Observable.just(Mocks.runningBuild()));
        HttpException httpException = new HttpException(Response.<Build>error(500, mResponseBody));
        when(mTeamCityService.cancelBuild(anyString(), Matchers.any(BuildCancelRequest.class))).thenReturn(Observable.<Build>error(httpException));

        // Prepare intent
        // <! ---------------------------------------------------------------------- !>
        // Passing build object to activity, had to create it for real, Can't pass mock object as serializable in bundle :(
        // <! ---------------------------------------------------------------------- !>
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(BundleExtractorValues.BUILD, Mocks.runningBuild());
        intent.putExtras(b);

        // Start activity
        mActivityRule.launchActivity(intent);

        // Opening context menu
        openContextualActionModeOverflowMenu();

        // Click on context menu option
        onView(withText(R.string.text_menu_stop_build)).perform(click());

        // Click on cancel build
        onView(withText(R.string.text_stop_button)).perform(click());

        // Check snack bar is displayed
        onView(withText(R.string.error_base_stop_build_error)).check(matches(isDisplayed()));
    }
}