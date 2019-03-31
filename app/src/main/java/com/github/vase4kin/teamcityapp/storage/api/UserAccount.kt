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

package com.github.vase4kin.teamcityapp.storage.api

import com.github.vase4kin.teamcityapp.base.api.Jsonable

private const val EMPTY_PASSWORD = ""

/**
 * User account
 */
data class UserAccount(
        val teamcityUrl: String,
        val userName: String,
        val isGuestUser: Boolean) : Jsonable {

    var passwordAsBytes: ByteArray = EMPTY_PASSWORD.toByteArray()
    var isActive: Boolean = false
    var isSslDisabled: Boolean = false
    var favoriteBuildTypes: MutableMap<String, FavoriteBuildTypeInfo> = mutableMapOf()

    override fun getId(): String {
        return teamcityUrl
    }

    data class FavoriteBuildTypeInfo(val sinceBuild: String = "",
                                     val isNotificationsEnabled: Boolean = true)
}

fun UserAccount.addBuildType(buildTypeId: String) {
    favoriteBuildTypes[buildTypeId] = UserAccount.FavoriteBuildTypeInfo()
}

fun UserAccount.passwordAsString() = String(passwordAsBytes)


