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

package com.github.vase4kin.teamcityapp.drawer.data

import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner
import org.powermock.api.mockito.PowerMockito.`when`

@RunWith(MockitoJUnitRunner::class)
class DrawerDataModelImplTest {

    @Mock
    private lateinit var user: UserAccount
    private lateinit var dataModel: DrawerDataModelImpl

    @Before
    fun setUp() {
        val users = listOf(user)
        dataModel = DrawerDataModelImpl(users)
    }

    @Test
    fun testGetName() {
        `when`(user.userName).thenReturn("name")
        assertThat(dataModel.getName(0), `is`(equalTo("name")))
    }

    @Test
    fun testGetTeamCityUrl() {
        `when`(user.teamcityUrl).thenReturn("url")
        assertThat(dataModel.getTeamCityUrl(0), `is`(equalTo("url")))
    }

    @Test
    fun testIsEmpty() {
        dataModel = DrawerDataModelImpl(emptyList())
        assertThat(dataModel.isEmpty, `is`(equalTo(true)))
    }
}
