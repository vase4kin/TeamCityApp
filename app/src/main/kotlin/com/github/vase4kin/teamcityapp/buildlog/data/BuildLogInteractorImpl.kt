/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.buildlog.data

import android.content.Context
import android.os.Bundle
import teamcityapp.libraries.storage.Storage
import teamcityapp.libraries.storage.models.UserAccount

/**
 * Impl of [BuildLogInteractor]
 */
class BuildLogInteractorImpl(
    storage: Storage,
    context: Context,
    private val arguments: Bundle?
) : BuildLogInteractor {

    private val activeUserAccount = storage.activeUser
    private val sharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * {@inheritDoc}
     */
    override val isGuestUser: Boolean
        get() = activeUser.isGuestUser

    /**
     * {@inheritDoc}
     */
    override val isAuthDialogShown: Boolean
        get() = sharedPreferences.getBoolean(KEY, false)

    /**
     * {@inheritDoc}
     */
    override val isSslDisabled: Boolean
        get() = activeUserAccount.isSslDisabled

    /**
     * {@inheritDoc}
     */
    override fun setAuthDialogStatus(isShown: Boolean) {
        sharedPreferences.edit().putBoolean(KEY, isShown).apply()
    }

    /**
     * {@inheritDoc}
     */
    override val buildId: String
        get() = arguments?.getString(BuildLogInteractor.BUILD_ID) ?: ""

    override val activeUser: UserAccount
        get() = activeUserAccount

    companion object {
        private const val PREF_NAME = "BuildLogPrefs"
        private const val KEY = "BuildLogDialogShown"
    }
}
