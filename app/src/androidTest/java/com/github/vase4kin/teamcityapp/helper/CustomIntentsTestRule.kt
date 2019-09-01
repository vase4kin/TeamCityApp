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

package com.github.vase4kin.teamcityapp.helper

import android.app.Activity

import androidx.test.espresso.Espresso
import androidx.test.espresso.base.DefaultFailureHandler
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.platform.app.InstrumentationRegistry

import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Simple IntentsTestRule with spoon screenshot support on failure
 */
class CustomIntentsTestRule<T : Activity>(activityClass: Class<T>) :
    IntentsTestRule<T>(activityClass, false, false) {

    override fun apply(base: Statement, description: Description): Statement {
        val testClassName = description.className
        val testMethodName = description.methodName
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        Espresso.setFailureHandler { throwable, matcher ->
            FalconScreenshotAction.perform("failure", testClassName, testMethodName)
            DefaultFailureHandler(context).handle(throwable, matcher)
        }
        return super.apply(base, description)
    }
}
