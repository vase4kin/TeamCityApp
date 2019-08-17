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

import com.crashlytics.android.Crashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

import io.fabric.sdk.android.Fabric

class RemoteServiceImpl(private val remoteConfig: FirebaseRemoteConfig) : RemoteService {

    override fun isNotChurn(): Boolean {
        fetch()
        return remoteConfig.getBoolean(RemoteService.PARAMETER_NOT_CHURN)
    }

    private fun fetch() {
        var cacheExpiration = CACHE
        if (remoteConfig.info.configSettings.isDeveloperModeEnabled) {
            cacheExpiration = 0
        }
        remoteConfig.fetch(cacheExpiration).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                remoteConfig.activateFetched()
            } else {
                val exception = task.exception
                if (exception != null && Fabric.isInitialized()) {
                    Crashlytics.logException(task.exception)
                }
            }
        }
    }

    companion object {

        private const val CACHE = 43200L
    }
}
