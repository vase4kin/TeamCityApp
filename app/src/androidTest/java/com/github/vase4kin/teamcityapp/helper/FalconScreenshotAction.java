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

package com.github.vase4kin.teamcityapp.helper;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import com.jraska.falcon.FalconSpoon;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Source: https://github.com/square/spoon/issues/214#issuecomment-81979248
 */
public final class FalconScreenshotAction implements ViewAction {
    private final String tag;
    private final String testClass;
    private final String testMethod;

    public FalconScreenshotAction(String tag, String testClass, String testMethod) {
        this.tag = tag;
        this.testClass = testClass;
        this.testMethod = testMethod;
    }

    @Override
    public Matcher<View> getConstraints() {
        return Matchers.any(View.class);
    }

    @Override
    public String getDescription() {
        return "Taking a screenshot using falcon.";
    }

    @Override
    public void perform(UiController uiController, View view) {
        FalconSpoon.screenshot(getActivity(view), tag, testClass, testMethod);
    }

    private static Activity getActivity(View view) {
        Context context = view.getContext();
        while (!(context instanceof Activity)) {
            if (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
            } else {
                throw new IllegalStateException("Got a context of class "
                        + context.getClass()
                        + " and I don't know how to get the Activity from it");
            }
        }
        return (Activity) context;
    }

    public static void perform(String tag, String className, String methodName) {
//        onView(isRoot()).perform(new FalconScreenshotAction(tag, className, methodName));
    }
}
