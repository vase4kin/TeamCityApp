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
import android.content.ContextWrapper
import android.view.View

import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction

import com.jraska.falcon.FalconSpoon

import org.hamcrest.Matcher
import org.hamcrest.Matchers

/**
 * Source: https://github.com/square/spoon/issues/214#issuecomment-81979248
 */
class FalconScreenshotAction(
    private val tag: String,
    private val testClass: String,
    private val testMethod: String
) : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return Matchers.any(View::class.java)
    }

    override fun getDescription(): String {
        return "Taking a screenshot using falcon."
    }

    override fun perform(uiController: UiController, view: View) {
        FalconSpoon.screenshot(getActivity(view), tag, testClass, testMethod)
    }

    companion object {

        @JvmStatic
        private fun getActivity(view: View): Activity {
            var context = view.context
            while (context !is Activity) {
                if (context is ContextWrapper) {
                    context = context.baseContext
                } else {
                    throw IllegalStateException(
                        "Got a context of class " +
                            context.javaClass +
                            " and I don't know how to get the Activity from it"
                    )
                }
            }
            return context
        }

        @JvmStatic
        fun perform(tag: String, className: String, methodName: String) {
            //        onView(isRoot()).perform(new FalconScreenshotAction(tag, className, methodName));
        }
    }
}
