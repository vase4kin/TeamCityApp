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

package com.github.vase4kin.teamcityapp.account.create.view;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule;
import com.github.vase4kin.teamcityapp.root.view.RootProjectsActivity;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import it.cosenonjaviste.daggermock.DaggerMockRule;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasFlags;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.vase4kin.teamcityapp.dagger.modules.Mocks.URL;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CreateAccountActivity} with mocked internet connection
 */
@RunWith(AndroidJUnit4.class)
public class CreateAccountActivityTest {

    private static final String INPUT_URL = URL.replace("https://", "");

    @Rule
    public DaggerMockRule<AppComponent> mDaggerRule = new DaggerMockRule<>(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .provides(SharedUserStorage.class, SharedUserStorage.init(InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(new DaggerMockRule.ComponentSetter<AppComponent>() {
                @Override
                public void setComponent(AppComponent appComponent) {
                    TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                    app.setAppInjector(appComponent);
                }
            });

    @Rule
    public CustomIntentsTestRule<CreateAccountActivity> mActivityRule = new CustomIntentsTestRule<>(CreateAccountActivity.class);

    @Captor
    private ArgumentCaptor<Callback> mCallbackArgumentCaptor;

    @Mock
    private AppModule mAppModule;

    @Mock
    private OkHttpClient mClient;

    @Mock
    private UserAccount mUserAccount;

    @Mock
    private Call mCall;

    @Before
    public void setUp() {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        app.getAppInjector().sharedUserStorage().clearAll();
        when(mClient.newCall(Matchers.any(Request.class))).thenReturn(mCall);
        mActivityRule.launchActivity(null);
    }

    /**
     * Verifies that user can be logged in as guest user with correct account url
     */
    @Test
    public void testUserCanCreateGuestUserAccountWithCorrectUrl() throws Throwable {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                mCallbackArgumentCaptor.getValue().onResponse(
                        mCall,
                        new Response.Builder()
                                .request(new Request.Builder().url(URL).build())
                                .protocol(Protocol.HTTP_1_0)
                                .code(200)
                                .build());
                return null;
            }
        }).when(mCall).enqueue(mCallbackArgumentCaptor.capture());

        onView(withId(R.id.teamcity_url)).perform(typeText(INPUT_URL), closeSoftKeyboard());
        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.action_create)).perform(click());

        intended(allOf(
                hasComponent(RootProjectsActivity.class.getName()),
                hasFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                hasExtras(allOf(
                        hasEntry(equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED), equalTo(true)),
                        hasEntry(equalTo(BundleExtractorValues.IS_REQUIRED_TO_RELOAD), equalTo(true)))),
                toPackage("com.github.vase4kin.teamcityapp.mock")));

        SharedUserStorage storageUtils = SharedUserStorage.init(mActivityRule.getActivity());
        assertThat(storageUtils.hasGuestAccountWithUrl(URL), is(true));
        assertThat(storageUtils.getActiveUser().getTeamcityUrl(), is(URL));
    }

    /**
     * Verifies that user can be logged in with correct account url and credentials
     */
    @Ignore
    @Test
    public void testUserCanCreateUserAccountWithCorrectUrlAndCredentials() throws Throwable {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                mCallbackArgumentCaptor.getValue().onResponse(
                        mCall,
                        new Response.Builder()
                                .request(new Request.Builder().url(URL).build())
                                .protocol(Protocol.HTTP_1_0)
                                .code(200)
                                .build());
                return null;
            }
        }).when(mCall).enqueue(mCallbackArgumentCaptor.capture());

        onView(withId(R.id.teamcity_url)).perform(typeText(INPUT_URL), closeSoftKeyboard());
        onView(withId(R.id.user_name)).perform(typeText("user"), pressImeActionButton());
        onView(withId(R.id.password)).perform(typeText("pass"), pressImeActionButton());

        intended(allOf(
                hasComponent(RootProjectsActivity.class.getName()),
                hasExtras(hasEntry(equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED), equalTo(true)))));

        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        SharedUserStorage storageUtils = app.getRestApiInjector().sharedUserStorage();
        assertThat(storageUtils.hasAccountWithUrl(URL, "user", "pass"), is(true));
        assertThat(storageUtils.getActiveUser().getTeamcityUrl(), is(URL));
    }

    /**
     * Verifies that user can be notified with error message if servers returns smth bad
     */
    @Test
    public void testUserIsNotifiedIfServerReturnsBadResponse() throws IOException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                mCallbackArgumentCaptor.getValue().onResponse(
                        mCall,
                        new Response.Builder()
                                .request(new Request.Builder().url(URL).build())
                                .protocol(Protocol.HTTP_1_0)
                                .code(404)
                                .message("Client Error")
                                .build());
                return null;
            }
        }).when(mCall).enqueue(mCallbackArgumentCaptor.capture());

        onView(withId(R.id.teamcity_url)).perform(typeText(URL), closeSoftKeyboard());
        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.action_create)).perform(click());
        onView(withText(containsString("Client Error"))).check(matches(isDisplayed()));
    }
}