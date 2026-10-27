/*
 * Copyright 2026 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.navigation

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import teamcityapp.shared.AnalyticsEvent
import teamcityapp.shared.AnalyticsValue
import teamcityapp.shared.NativeDiagnostics

internal class AndroidDiagnostics(private val analytics: FirebaseAnalytics) : NativeDiagnostics {
    constructor(context: Context) : this(FirebaseAnalytics.getInstance(context.applicationContext))

    override fun event(name: String) {
        require(name.length <= 80 && name.all { it.isLowerCase() || it == '_' })
        analytics.logEvent(name, null)
    }

    override fun analytics(record: AnalyticsEvent) {
        val parameters = record.parameters.takeIf { it.isNotEmpty() }?.let { values ->
            Bundle().apply {
                values.forEach { (name, value) ->
                    when (value) {
                        is AnalyticsValue.Text -> putString(name, value.value)
                        is AnalyticsValue.Flag -> putLong(name, if (value.value) 1L else 0L)
                    }
                }
            }
        }
        analytics.logEvent(record.name, parameters)
    }
}
