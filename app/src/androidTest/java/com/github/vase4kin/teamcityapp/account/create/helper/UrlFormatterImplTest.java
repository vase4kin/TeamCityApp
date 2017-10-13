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

package com.github.vase4kin.teamcityapp.account.create.helper;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class UrlFormatterImplTest {

    private UrlFormatterImpl mUrlFormatter;

    @Before
    public void setUp() throws Exception {
        mUrlFormatter = new UrlFormatterImpl();
    }

    @Test
    public void testFormatServerUrl() throws Exception {
        assertThat(mUrlFormatter.formatServerUrl("https://teamcity.com"), is(equalTo("https://teamcity.com")));
        assertThat(mUrlFormatter.formatServerUrl("https://teamcity.com/"), is(equalTo("https://teamcity.com/")));
    }

    @Test
    public void testFormatServerUrlWithPath() throws Exception {
        assertThat(mUrlFormatter.formatServerUrl("https://teamcity.com/server"), is(equalTo("https://teamcity.com/server/")));
        assertThat(mUrlFormatter.formatServerUrl("https://teamcity.com/server/"), is(equalTo("https://teamcity.com/server/")));
        assertThat(mUrlFormatter.formatServerUrl("https://teamcity.com/server/v2"), is(equalTo("https://teamcity.com/server/v2/")));
        assertThat(mUrlFormatter.formatServerUrl("https://teamcity.com/server/v2/"), is(equalTo("https://teamcity.com/server/v2/")));
    }

    @Test
    public void testFormatUrl() throws Exception {
        assertThat(mUrlFormatter.formatBasicUrl("/app/rest/buildTypes/id:buildType/builds?locator=locator:any"), is(equalTo("app/rest/buildTypes/id:buildType/builds?locator=locator:any")));
        assertThat(mUrlFormatter.formatBasicUrl("app/rest/buildTypes/id:buildType/builds?locator=locator:any"), is(equalTo("app/rest/buildTypes/id:buildType/builds?locator=locator:any")));
    }

}