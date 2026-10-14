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

import com.github.vase4kin.teamcityapp.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import teamcityapp.libraries.remote.RemoteService

private const val CACHE = 43200L

class RemoteServiceImpl(
    private val remoteConfig: FirebaseRemoteConfig
) : RemoteService {

    init {
        fetch()
    }

    override fun isNotChurn(): Boolean {
        fetch()
        return remoteConfig.getBoolean(RemoteService.PARAMETER_NOT_CHURN)
    }

    private fun fetch() {
        var cacheExpiration = CACHE
        if (BuildConfig.DEBUG) {
            cacheExpiration = 0
        }
        remoteConfig.fetch(cacheExpiration).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                remoteConfig.activate()
            } else {
                val exception = task.exception
                logException(exception)
            }
        }
    }

    private fun logException(exception: Exception?) {
        if (exception != null) {
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
    }
}
