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

package com.github.vase4kin.teamcityapp.remote

interface RemoteService {

    fun isNotChurn(): Boolean

    fun showTryItOut(
        onSuccess: (showTryItOut: Boolean) -> Unit,
        onStart: () -> Unit,
        onFinish: () -> Unit
    )

    fun getTryItOutUrl(): String

    companion object {
        const val PARAMETER_NOT_CHURN = "param_not_churn"
        const val PARAMETER_URL_SHOW_TRY_IT_OUT = "param_url_try_it_out"
        const val PARAMETER_SHOW_TRY_IT_OUT = "param_show_try_it_out"
    }
}
