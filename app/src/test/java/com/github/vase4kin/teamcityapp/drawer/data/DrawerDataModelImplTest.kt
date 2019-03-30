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

package com.github.vase4kin.teamcityapp.drawer.data

import com.github.vase4kin.teamcityapp.storage.UsersFactory
import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.*

class DrawerDataModelImplTest {

    private val user: UserAccount = UsersFactory.EMPTY_USER.copy(userName = "name", teamcityUrl = "url")

    private lateinit var dataModel: DrawerDataModelImpl

    @Before
    fun setUp() {
        val users = ArrayList<UserAccount>()
        users.add(user)
        dataModel = DrawerDataModelImpl(users)
    }

    @Test
    @Throws(Exception::class)
    fun testGetName() {
        assertThat(dataModel.getName(0), `is`(equalTo("name")))
    }

    @Test
    @Throws(Exception::class)
    fun testGetTeamCityUrl() {
        assertThat(dataModel.getTeamCityUrl(0), `is`(equalTo("url")))
    }

    @Test
    @Throws(Exception::class)
    fun testIsEmpty() {
        dataModel = DrawerDataModelImpl(emptyList())
        assertThat(dataModel.isEmpty, `is`(equalTo(true)))
    }
}