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

package com.github.vase4kin.teamcityapp.tests.api;

import com.github.vase4kin.teamcityapp.api.interfaces.Collectible;

import java.util.Collections;
import java.util.List;

import teamcityapp.features.test_details.repository.models.TestOccurrence;
import teamcityapp.libraries.api.BaseObject;

/**
 * TestOccurrences
 */
public class TestOccurrences extends BaseObject implements Collectible<TestOccurrence> {

    private int count;
    private int passed;
    private int failed;
    private int ignored;
    private int newFailed;

    private String nextHref;

    private List<TestOccurrence> testOccurrence;

    @Override
    public List<TestOccurrence> getObjects() {
        return testOccurrence == null ? Collections.<TestOccurrence>emptyList() : testOccurrence;
    }

    public int getCount() {
        return count;
    }

    public int getPassed() {
        return passed;
    }

    public int getFailed() {
        return failed;
    }

    public int getIgnored() {
        return ignored;
    }

    public String getNextHref() {
        return nextHref;
    }

    public TestOccurrences(int passed, int failed, int ignored, String href) {
        this.passed = passed;
        this.failed = failed;
        this.ignored = ignored;
        this.href = href;
    }

    public TestOccurrences(List<TestOccurrence> testOccurrence) {
        this.testOccurrence = testOccurrence;
    }

    public TestOccurrences(int count) {
        this.count = count;
    }
}
