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

/**
 * Data model to manage drawer data
 */
interface DrawerDataModel : Iterable<UserAccount> {

    /**
     * @return Are accounts empty or not?
     */
    val isEmpty: Boolean

    /**
     * Get Account Use name
     *
     * @param position - Adapter position
     * @return User name of the account
     */
    fun getName(position: Int): String

    /**
     * @param position - Adapter position
     * @return The Url of Acccount
     */
    fun getTeamCityUrl(position: Int): String
}
