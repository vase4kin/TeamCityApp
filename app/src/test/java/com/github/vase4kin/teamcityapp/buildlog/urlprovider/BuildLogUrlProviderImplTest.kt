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

package com.github.vase4kin.teamcityapp.buildlog.urlprovider

import com.github.vase4kin.teamcityapp.buildlog.extractor.BuildLogValueExtractor
import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BuildLogUrlProviderImplTest {

    @Mock
    private lateinit var valueExtractor: BuildLogValueExtractor
    @Mock
    private lateinit var userAccount: UserAccount
    private lateinit var buildLogUrlProvider: BuildLogUrlProviderImpl

    @Before
    fun setUp() {
        buildLogUrlProvider = BuildLogUrlProviderImpl(valueExtractor, userAccount)
        `when`(valueExtractor.buildId).thenReturn("1234")
        `when`(userAccount.teamcityUrl).thenReturn("http://fake-url.com")
    }

    @Test
    fun testProvideUrlForGuestAccount() {
        `when`(userAccount.isGuestUser).thenReturn(true)
        assertThat(buildLogUrlProvider.provideUrl(), `is`(equalTo("http://fake-url.com/viewLog.html?buildId=1234&tab=buildLog&guest=1")))
    }

    @Test
    fun testProvideUrlForUserAccount() {
        `when`(userAccount.isGuestUser).thenReturn(false)
        assertThat(buildLogUrlProvider.provideUrl(), `is`(equalTo("http://fake-url.com/viewLog.html?buildId=1234&tab=buildLog")))
    }
}