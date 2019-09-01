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

package com.github.vase4kin.teamcityapp.account.create.helper

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class UrlFormatterImplTest {

    private lateinit var urlFormatter: UrlFormatterImpl

    @Before
    fun setUp() {
        urlFormatter = UrlFormatterImpl()
    }

    @Test
    fun testFormatServerUrl() {
        assertThat(
            urlFormatter.formatServerUrl("https://teamcity.com"),
            `is`(equalTo("https://teamcity.com"))
        )
        assertThat(
            urlFormatter.formatServerUrl("https://teamcity.com/"),
            `is`(equalTo("https://teamcity.com/"))
        )
    }

    @Test
    fun testFormatServerUrlWithPath() {
        assertThat(
            urlFormatter.formatServerUrl("https://teamcity.com/server"),
            `is`(equalTo("https://teamcity.com/server/"))
        )
        assertThat(
            urlFormatter.formatServerUrl("https://teamcity.com/server/"),
            `is`(equalTo("https://teamcity.com/server/"))
        )
        assertThat(
            urlFormatter.formatServerUrl("https://teamcity.com/server/v2"),
            `is`(equalTo("https://teamcity.com/server/v2/"))
        )
        assertThat(
            urlFormatter.formatServerUrl("https://teamcity.com/server/v2/"),
            `is`(equalTo("https://teamcity.com/server/v2/"))
        )
    }

    @Test
    fun testFormatUrl() {
        assertThat(
            urlFormatter.formatBasicUrl("/app/rest/buildTypes/id:buildType/builds?locator=locator:any"),
            `is`(equalTo("app/rest/buildTypes/id:buildType/builds?locator=locator:any"))
        )
        assertThat(
            urlFormatter.formatBasicUrl("app/rest/buildTypes/id:buildType/builds?locator=locator:any"),
            `is`(equalTo("app/rest/buildTypes/id:buildType/builds?locator=locator:any"))
        )
    }
}
