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

package com.github.vase4kin.teamcityapp.buildlog.data

/**
 * Build log interactor
 */
interface BuildLogInteractor {

    /**
     * @return true if active user is guest
     */
    val isGuestUser: Boolean

    /**
     * @return true if dialog is shown
     */
    val isAuthDialogShown: Boolean

    /**
     * @return {true} if ssl is enabled for user
     */
    val isSslDisabled: Boolean

    /**
     * Set dialog status as shown
     *
     * @param isShown - status
     */
    fun setAuthDialogStatus(isShown: Boolean)
}
