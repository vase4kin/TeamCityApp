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

package com.github.vase4kin.teamcityapp.login.view;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.crypto.CryptoManager;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule;
import com.github.vase4kin.teamcityapp.home.view.HomeActivity;
import com.github.vase4kin.teamcityapp.remote.RemoteServiceImpl;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import it.cosenonjaviste.daggermock.DaggerMockRule;
import okhttp3.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;

import javax.inject.Named;
import java.io.IOException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static com.github.vase4kin.teamcityapp.dagger.modules.AppModule.*;
import static com.github.vase4kin.teamcityapp.dagger.modules.Mocks.URL;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link LoginActivity} with mocked internet connection
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private static final String INPUT_URL = URL.replace("https://", "");
    private static final String MESSAGE_EMPTY = "";

    @Rule
    public DaggerMockRule<AppComponent> mDaggerRule = new DaggerMockRule<>(AppComponent.class, new AppModule((TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()))
            .set(appComponent -> {
                TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
                app.setAppInjector(appComponent);
            });

    @Rule
    public CustomIntentsTestRule<LoginActivity> mActivityRule = new CustomIntentsTestRule<>(LoginActivity.class);

    @Captor
    private ArgumentCaptor<Callback> mCallbackArgumentCaptor;

    @Named(CLIENT_BASE)
    @Mock
    private OkHttpClient okHttpClient;

    @Named(CLIENT_BASE_UNSAFE)
    @Mock
    private OkHttpClient unsafeOkHttpClient;

    @Named(CLIENT_AUTH)
    @Mock
    private OkHttpClient mClientAuth;

    @Mock
    private CryptoManager mCryptoManager;

    @Mock
    private Call mCall;

    @Before
    public void setUp() {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        app.getAppInjector().sharedUserStorage().clearAll();
        when(okHttpClient.newCall(Matchers.any(Request.class))).thenReturn(mCall);
        when(unsafeOkHttpClient.newCall(Matchers.any(Request.class))).thenReturn(mCall);
    }

    /**
     * Verifies that user can be logged in as guest user with correct account url
     */
    @Test
    public void testUserCanCreateGuestUserAccountWithCorrectUrl() throws Throwable {
        final String urlWithPath = "https://teamcity.com/server";
        String savedUrl = urlWithPath.concat("/");
        doAnswer(invocation -> {
            mCallbackArgumentCaptor.getValue().onResponse(
                    mCall,
                    new Response.Builder()
                            .request(new Request.Builder().url(urlWithPath).build())
                            .protocol(Protocol.HTTP_1_0)
                            .message(MESSAGE_EMPTY)
                            .code(200)
                            .build());
            return null;
        }).when(mCall).enqueue(mCallbackArgumentCaptor.capture());

        mActivityRule.launchActivity(null);

        onView(withId(R.id.teamcity_url)).perform(typeText(urlWithPath.replace("https://", "")), closeSoftKeyboard());
        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.btn_login)).perform(click());

        intended(allOf(
                hasComponent(HomeActivity.class.getName()),
                hasExtras(hasEntry(equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED), equalTo(true)))));

        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        SharedUserStorage storageUtils = app.getRestApiInjector().sharedUserStorage();
        assertThat(storageUtils.hasGuestAccountWithUrl(savedUrl), is(true));
        assertThat(storageUtils.getActiveUser().getTeamcityUrl(), is(savedUrl));
        assertThat(storageUtils.getActiveUser().isSslDisabled(), is(false));
    }

    /**
     * Verifies that user can be logged in as guest user with correct account url ignoring ssl
     */
    @Test
    public void testUserCanCreateGuestUserAccountWithCorrectUrlIgnoringSsl() {
        final String urlWithPath = "https://teamcity.com/server";
        String savedUrl = urlWithPath.concat("/");
        doAnswer(invocation -> {
            mCallbackArgumentCaptor.getValue().onResponse(
                    mCall,
                    new Response.Builder()
                            .request(new Request.Builder().url(urlWithPath).build())
                            .protocol(Protocol.HTTP_1_0)
                            .message(MESSAGE_EMPTY)
                            .code(200)
                            .build());
            return null;
        }).when(mCall).enqueue(mCallbackArgumentCaptor.capture());

        mActivityRule.launchActivity(null);

        onView(withId(R.id.teamcity_url)).perform(typeText(urlWithPath.replace("https://", "")), closeSoftKeyboard());
        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.disable_ssl_switch)).perform(click());
        onView(withText(R.string.warning_ssl_dialog_content)).check(matches(isDisplayed()));
        onView(withText(R.string.dialog_ok_title)).perform(click());
        onView(withId(R.id.btn_login)).perform(click());

        intended(allOf(
                hasComponent(HomeActivity.class.getName()),
                hasExtras(hasEntry(equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED), equalTo(true)))));

        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        SharedUserStorage storageUtils = app.getRestApiInjector().sharedUserStorage();
        assertThat(storageUtils.hasGuestAccountWithUrl(savedUrl), is(true));
        assertThat(storageUtils.getActiveUser().getTeamcityUrl(), is(savedUrl));
        assertThat(storageUtils.getActiveUser().isSslDisabled(), is(true));
    }

    /**
     * Verifies that user can be logged in as guest with correct account url
     */
    @Test
    public void testUserCanCreateAccountWithCorrectUrlByImeButton() throws Throwable {
        doAnswer(invocation -> {
            mCallbackArgumentCaptor.getValue().onResponse(
                    mCall,
                    new Response.Builder()
                            .request(new Request.Builder().url(URL).build())
                            .protocol(Protocol.HTTP_1_0)
                            .message(MESSAGE_EMPTY)
                            .code(200)
                            .build());
            return null;
        }).when(mCall).enqueue(mCallbackArgumentCaptor.capture());

        mActivityRule.launchActivity(null);

        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.teamcity_url)).perform(typeText(INPUT_URL), pressImeActionButton());

        intended(allOf(
                hasComponent(HomeActivity.class.getName()),
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

        doAnswer(invocation -> {
            mCallbackArgumentCaptor.getValue().onResponse(
                    mCall,
                    new Response.Builder()
                            .request(new Request.Builder().url(URL).build())
                            .protocol(Protocol.HTTP_1_0)
                            .message(MESSAGE_EMPTY)
                            .code(200)
                            .build());
            return null;
        }).when(mCall).enqueue(mCallbackArgumentCaptor.capture());

        mActivityRule.launchActivity(null);

        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.teamcity_url)).perform(typeText(INPUT_URL), pressImeActionButton());

        intended(allOf(
                hasComponent(HomeActivity.class.getName()),
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
        doAnswer(invocation -> {
            mCallbackArgumentCaptor.getValue().onResponse(
                    mCall,
                    new Response.Builder()
                            .request(new Request.Builder().url(URL).build())
                            .protocol(Protocol.HTTP_1_0)
                            .code(200)
                            .build());
            return null;
        }).when(mCall).enqueue(mCallbackArgumentCaptor.capture());

        mActivityRule.launchActivity(null);

        onView(withId(R.id.teamcity_url)).perform(typeText(INPUT_URL), closeSoftKeyboard());
        onView(withId(R.id.user_name)).perform(typeText("user"), pressImeActionButton());
        onView(withId(R.id.password)).perform(typeText("pass"), pressImeActionButton());

        intended(allOf(
                hasComponent(HomeActivity.class.getName()),
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
        doAnswer(invocation -> {
            mCallbackArgumentCaptor.getValue().onResponse(
                    mCall,
                    new Response.Builder()
                            .request(new Request.Builder().url(URL).build())
                            .protocol(Protocol.HTTP_1_0)
                            .code(404)
                            .message("Client Error")
                            .build());
            return null;
        }).when(mCall).enqueue(mCallbackArgumentCaptor.capture());

        mActivityRule.launchActivity(null);

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
        doAnswer(invocation -> {
            mCallbackArgumentCaptor.getValue().onResponse(
                    mCall,
                    new Response.Builder()
                            .request(new Request.Builder().url(URL).build())
                            .protocol(Protocol.HTTP_1_0)
                            .code(401)
                            .message("Unauthorized")
                            .build());
            return null;
        }).when(mCall).enqueue(mCallbackArgumentCaptor.capture());

        mActivityRule.launchActivity(null);

        onView(withId(R.id.teamcity_url)).perform(typeText(INPUT_URL), closeSoftKeyboard());
        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.btn_login)).perform(click());
        onView(withText(R.string.info_unauthorized_dialog_title)).check(matches(isDisplayed()));
        onView(withText(R.string.info_unauthorized_dialog_content)).check(matches(isDisplayed()));
    }

    /**
     * Verifies that user can be logged in as guest user with correct account url
     */
    @Test
    public void testUserCanCreateGuestUserAccountWithNotSecureUrl() {
        final String urlWithPath = "http://teamcity.com/server";
        String savedUrl = urlWithPath.concat("/");
        doAnswer(invocation -> {
            mCallbackArgumentCaptor.getValue().onResponse(
                    mCall,
                    new Response.Builder()
                            .request(new Request.Builder().url(urlWithPath).build())
                            .protocol(Protocol.HTTP_1_0)
                            .message(MESSAGE_EMPTY)
                            .code(200)
                            .build());
            return null;
        }).when(mCall).enqueue(mCallbackArgumentCaptor.capture());

        mActivityRule.launchActivity(null);

        onView(withId(R.id.teamcity_url)).perform(clearText(), typeText(urlWithPath), closeSoftKeyboard());
        onView(withId(R.id.guest_user_switch)).perform(click());
        onView(withId(R.id.btn_login)).perform(click());

        onView(withText(R.string.warning_ssl_dialog_title)).check(matches(isDisplayed()));
        onView(withText(R.string.server_not_secure_http)).check(matches(isDisplayed()));
        onView(withText(R.string.dialog_ok_title)).perform(click());

        intended(allOf(
                hasComponent(HomeActivity.class.getName()),
                hasExtras(hasEntry(equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED), equalTo(true)))));

        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        SharedUserStorage storageUtils = app.getRestApiInjector().sharedUserStorage();
        assertThat(storageUtils.hasGuestAccountWithUrl(savedUrl), is(true));
        assertThat(storageUtils.getActiveUser().getTeamcityUrl(), is(savedUrl));
        assertThat(storageUtils.getActiveUser().isSslDisabled(), is(false));
    }

    @Ignore
    @Test
    public void testUserCanNotCreateAccountIfDataWasNotSaved() throws Throwable {
        // You know what to do
    }

    @Test
    public void testUserCannotSeeTryItOutIfItIsNotEnabled() {
        setTryItOutValue(false);

        mActivityRule.launchActivity(null);

        onView(withId(R.id.give_it_a_try)).check(matches(not(isDisplayed())));

    }

    @Test
    public void testUserCanSeeTryItOutIfItIsEnabled() {
        final String urlWithPath = "https://test.com/test";
        String savedUrl = urlWithPath.concat("/");
        doAnswer(invocation -> {
            mCallbackArgumentCaptor.getValue().onResponse(
                    mCall,
                    new Response.Builder()
                            .request(new Request.Builder().url(urlWithPath).build())
                            .protocol(Protocol.HTTP_1_0)
                            .message(MESSAGE_EMPTY)
                            .code(200)
                            .build());
            return null;
        }).when(mCall).enqueue(mCallbackArgumentCaptor.capture());

        setTryItOutValue(true);
        setTryItOutValueUrl(urlWithPath);

        mActivityRule.launchActivity(null);

        onView(withId(R.id.give_it_a_try)).check(matches(isDisplayed())).perform(click());

        onView(withText(R.string.text_try_it_out_button)).perform(click());

        intended(allOf(
                hasComponent(HomeActivity.class.getName()),
                hasExtras(hasEntry(equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED), equalTo(true)))));

        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        SharedUserStorage storageUtils = app.getRestApiInjector().sharedUserStorage();
        assertThat(storageUtils.hasGuestAccountWithUrl(savedUrl), is(true));
        assertThat(storageUtils.getActiveUser().getTeamcityUrl(), is(savedUrl));
        assertThat(storageUtils.getActiveUser().isSslDisabled(), is(false));
    }

    private void setTryItOutValue(Boolean value) {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        RemoteServiceImpl remoteService = (RemoteServiceImpl) app.getAppInjector().remoteService();
        remoteService.setShowTryItOut(value);
    }

    private void setTryItOutValueUrl(String value) {
        TeamCityApplication app = (TeamCityApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        RemoteServiceImpl remoteService = (RemoteServiceImpl) app.getAppInjector().remoteService();
        remoteService.setShowTryItOutUrl(value);
    }
}