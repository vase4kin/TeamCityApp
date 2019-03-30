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

package com.github.vase4kin.teamcityapp.buildlog.urlprovider

import com.github.vase4kin.teamcityapp.buildlog.extractor.BuildLogValueExtractor
import com.github.vase4kin.teamcityapp.storage.UsersFactory
import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class BuildLogUrlProviderImplTest {

    @Mock
    private lateinit var mValueExtractor: BuildLogValueExtractor

    private val userAccount: UserAccount = UsersFactory.EMPTY_USER.copy(teamcityUrl = "http://fake-url.com")

    private lateinit var buildLogUrlProvider: BuildLogUrlProviderImpl

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(mValueExtractor.buildId).thenReturn("1234")
    }

    @Test
    fun testProvideUrlForGuestAccount() {
        buildLogUrlProvider = BuildLogUrlProviderImpl(mValueExtractor, userAccount.copy(isGuestUser = true))
        assertThat(buildLogUrlProvider.provideUrl(), `is`(equalTo("http://fake-url.com/viewLog.html?buildId=1234&tab=buildLog&guest=1")))
    }

    @Test
    fun testProvideUrlForUserAccount() {
        buildLogUrlProvider = BuildLogUrlProviderImpl(mValueExtractor, userAccount.copy(isGuestUser = false))
        assertThat(buildLogUrlProvider.provideUrl(), `is`(equalTo("http://fake-url.com/viewLog.html?buildId=1234&tab=buildLog")))
    }
}