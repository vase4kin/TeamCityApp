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

package com.github.vase4kin.teamcityapp.login.view;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.crypto.CryptoManager;
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
 * Tests for {@link LoginActivity} with mocked internet connection
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private static final String INPUT_URL = URL.replace("https://", "");

    @Rule
    public DaggerMockRule<AppComponent> mDaggerRule = new DaggerMockRule<>(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(new DaggerMockRule.ComponentSetter<AppComponent>() {
                @Override
                public void setComponent(AppComponent appComponent) {
                    TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                    app.setAppInjector(appComponent);
                }
            });

    @Rule
    public CustomIntentsTestRule<LoginActivity> mActivityRule = new CustomIntentsTestRule<>(LoginActivity.class);

    @Captor
    private ArgumentCaptor<Callback> mCallbackArgumentCaptor;

    @Mock
    private AppModule mAppModule;

    @Mock
    private OkHttpClient mClient;

    @Mock
    private UserAccount mUserAccount;

    @Mock
    private CryptoManager mCryptoManager;

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
        final String urlWithPath = "https://teamcity.com/server";
        String savedUrl = urlWithPath.concat("/");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                mCallbackArgumentCaptor.getValue().onResponse(
                        mCall,
                        new Response.Builder()
                                .request(new Request.Builder().url(urlWithPath).build())
                                .protocol(Protocol.HTTP_1_0)
                                .code(200)
                                .build());
                return null;
            }
        }).when(mCall).enqueue(mCallbackArgumentCaptor.capture());

        onView(withId(R.id.teamcity_url)).perform(typeText(urlWithPath.replace("https://", "")), closeSoftKeyboard());
        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.btn_login)).perform(click());

        intended(allOf(
                hasComponent(RootProjectsActivity.class.getName()),
                hasExtras(hasEntry(equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED), equalTo(true)))));

        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        SharedUserStorage storageUtils = app.getRestApiInjector().sharedUserStorage();
        assertThat(storageUtils.hasGuestAccountWithUrl(savedUrl), is(true));
        assertThat(storageUtils.getActiveUser().getTeamcityUrl(), is(savedUrl));
    }

    /**
     * Verifies that user can be logged in as guest with correct account url
     */
    @Test
    public void testUserCanCreateAccountWithCorrectUrlByImeButton() throws Throwable {
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

        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.teamcity_url)).perform(typeText(INPUT_URL), pressImeActionButton());

        intended(allOf(
                hasComponent(RootProjectsActivity.class.getName()),
                hasExtras(hasEntry(equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED), equalTo(true)))));

        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        SharedUserStorage storageUtils = app.getRestApiInjector().sharedUserStorage();
        assertThat(storageUtils.hasGuestAccountWithUrl(URL), is(true));
        assertThat(storageUtils.getActiveUser().getTeamcityUrl(), is(URL));
    }

    /**
     * Verifies that user can be logged in as guest with correct account url
     */
    @Test
    public void testUserCanCreateAccountWithCorrectUrlWhichContainsPathByImeButton() throws Throwable {

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

        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.teamcity_url)).perform(typeText(INPUT_URL), pressImeActionButton());

        intended(allOf(
                hasComponent(RootProjectsActivity.class.getName()),
                hasExtras(hasEntry(equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED), equalTo(true)))));

        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        SharedUserStorage storageUtils = app.getRestApiInjector().sharedUserStorage();
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
        assertThat(storageUtils.hasAccountWithUrl(URL, "user"), is(true));
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

        onView(withId(R.id.teamcity_url)).perform(typeText(INPUT_URL), closeSoftKeyboard());
        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.btn_login)).perform(click());
        onView(withText(containsString("Client Error"))).check(matches(isDisplayed()));
    }

    /**
     * Verifies that user can be notified with dialog info for 401 errors
     */
    @Test
    public void testUserIsNotifiedIfServerReturns401Request() throws IOException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                mCallbackArgumentCaptor.getValue().onResponse(
                        mCall,
                        new Response.Builder()
                                .request(new Request.Builder().url(URL).build())
                                .protocol(Protocol.HTTP_1_0)
                                .code(401)
                                .message("Unauthorized")
                                .build());
                return null;
            }
        }).when(mCall).enqueue(mCallbackArgumentCaptor.capture());

        onView(withId(R.id.teamcity_url)).perform(typeText(INPUT_URL), closeSoftKeyboard());
        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.btn_login)).perform(click());
        onView(withText(R.string.info_unauthorized_dialog_title)).check(matches(isDisplayed()));
        onView(withText(R.string.info_unauthorized_dialog_content)).check(matches(isDisplayed()));
    }

    @Ignore
    @Test
    public void testUserCanNotCreateAccountIfDataWasNotSaved() throws Throwable {
        // You know what to do
    }
}