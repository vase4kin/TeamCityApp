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

package com.github.vase4kin.teamcityapp.tests.data;

import androidx.annotation.VisibleForTesting;

import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;
import com.github.vase4kin.teamcityapp.utils.IconUtils;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Impl of {@link TestsDataModel}
 */
public class TestsDataModelImpl implements TestsDataModel {

    /**
     * Load more
     */
    @VisibleForTesting
    static final TestOccurrences.TestOccurrence LOAD_MORE = new TestOccurrences.TestOccurrence() {
        @Override
        public String getId() {
            return UUID.randomUUID().toString();
        }
    };

    private List<TestOccurrences.TestOccurrence> mTests;

    public TestsDataModelImpl(List<TestOccurrences.TestOccurrence> mTests) {
        this.mTests = mTests;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFailed(int position) {
        return mTests.get(position).isFailed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName(int position) {
        return mTests.get(position).getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatusIcon(int position) {
        return IconUtils.getBuildStatusIcon(mTests.get(position).getStatus(), "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHref(int position) {
        return mTests.get(position).getHref();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatus(int position) {
        return mTests.get(position).getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return mTests.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<TestOccurrences.TestOccurrence> iterator() {
        return mTests.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoadMore(int position) {
        return mTests.get(position).equals(LOAD_MORE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLoadMore() {
        mTests.add(LOAD_MORE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadMore() {
        mTests.remove(LOAD_MORE);
    }

    @Override
    public void addMoreBuilds(TestsDataModel dataModel) {
        for (TestOccurrences.TestOccurrence testOccurrence : dataModel) {
            mTests.add(testOccurrence);
        }
    }
}
