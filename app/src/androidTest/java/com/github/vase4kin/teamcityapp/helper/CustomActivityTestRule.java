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
import android.view.View;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.FailureHandler;
import androidx.test.espresso.base.DefaultFailureHandler;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Simple ActivityTestRule with spoon screenshot support on failure
 */
public class CustomActivityTestRule<T extends Activity> extends ActivityTestRule<T> {

    public CustomActivityTestRule(Class<T> activityClass) {
        super(activityClass, false, false);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        final String testClassName = description.getClassName();
        final String testMethodName = description.getMethodName();
        final Context context = InstrumentationRegistry.getTargetContext();
        Espresso.setFailureHandler(new FailureHandler() {
            @Override
            public void handle(Throwable throwable, Matcher<View> matcher) {
                FalconScreenshotAction.perform("failure", testClassName, testMethodName);
                new DefaultFailureHandler(context).handle(throwable, matcher);
            }
        });
        return super.apply(base, description);
    }
}
