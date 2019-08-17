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

package com.github.vase4kin.teamcityapp.tests.data

import androidx.annotation.VisibleForTesting
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import com.github.vase4kin.teamcityapp.utils.IconUtils
import java.util.*

/**
 * Impl of [TestsDataModel]
 */
class TestsDataModelImpl(private val tests: MutableList<TestOccurrences.TestOccurrence>) : TestsDataModel {

    /**
     * {@inheritDoc}
     */
    override val itemCount: Int
        get() = tests.size

    /**
     * {@inheritDoc}
     */
    override fun isFailed(position: Int): Boolean {
        return tests[position].isFailed
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(position: Int): String {
        return tests[position].name
    }

    /**
     * {@inheritDoc}
     */
    override fun getStatusIcon(position: Int): String {
        return IconUtils.getBuildStatusIcon(tests[position].status, "")
    }

    /**
     * {@inheritDoc}
     */
    override fun getHref(position: Int): String {
        return tests[position].href
    }

    /**
     * {@inheritDoc}
     */
    override fun getStatus(position: Int): String {
        return tests[position].status
    }

    /**
     * {@inheritDoc}
     */
    override fun iterator(): Iterator<TestOccurrences.TestOccurrence> {
        return tests.iterator()
    }

    /**
     * {@inheritDoc}
     */
    override fun isLoadMore(position: Int): Boolean {
        return tests[position] == LOAD_MORE
    }

    /**
     * {@inheritDoc}
     */
    override fun addLoadMore() {
        tests.add(LOAD_MORE)
    }

    /**
     * {@inheritDoc}
     */
    override fun removeLoadMore() {
        tests.remove(LOAD_MORE)
    }

    override fun addMoreBuilds(dataModel: TestsDataModel) {
        for (testOccurrence in dataModel) {
            tests.add(testOccurrence)
        }
    }

    companion object {

        /**
         * Load more
         */
        @VisibleForTesting
        val LOAD_MORE: TestOccurrences.TestOccurrence = object : TestOccurrences.TestOccurrence() {
            override fun getId(): String {
                return UUID.randomUUID().toString()
            }
        }
    }
}
