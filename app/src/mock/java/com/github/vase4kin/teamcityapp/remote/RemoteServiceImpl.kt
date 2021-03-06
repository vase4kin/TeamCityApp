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

package com.github.vase4kin.teamcityapp.remote

import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import teamcityapp.libraries.remote.RemoteService

class RemoteServiceImpl(
    @Suppress("UNUSED_PARAMETER") remoteConfig: FirebaseRemoteConfig
) : RemoteService {

    var showTryItOut: Boolean = false
    var showTryItOutUrl: String = Mocks.URL

    override fun isNotChurn(): Boolean = false

    override fun showTryItOut(
        onSuccess: (showTryItOut: Boolean) -> Unit,
        onStart: () -> Unit,
        onFinish: () -> Unit
    ) {
        onStart()
        onFinish()
        onSuccess(showTryItOut)
    }

    override fun getTryItOutUrl(): String = showTryItOutUrl
}
