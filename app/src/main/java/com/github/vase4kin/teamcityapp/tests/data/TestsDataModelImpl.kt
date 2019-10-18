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
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.tests.api.TEST_STATUS_ERROR
import com.github.vase4kin.teamcityapp.tests.api.TEST_STATUS_FAILURE
import com.github.vase4kin.teamcityapp.tests.api.TEST_STATUS_UNKNOWN
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import java.util.UUID

/**
 * Impl of [TestsDataModel]
 */
class TestsDataModelImpl(private val tests: MutableList<TestOccurrences.TestOccurrence>) :
    TestsDataModel {

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
    override fun getStatusIcon(position: Int): Int {
        return when (tests[position].status) {
            TEST_STATUS_FAILURE -> R.drawable.ic_error_black_24dp
            TEST_STATUS_ERROR -> R.drawable.ic_report_problem_black_24dp
            TEST_STATUS_UNKNOWN -> R.drawable.ic_help_black_24dp
            else -> R.drawable.ic_check_circle_black_24dp
        }
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
        val moreTests = mutableListOf<TestOccurrences.TestOccurrence>()
        dataModel.iterator().forEach {
            moreTests.add(it)
        }
        tests.addAll(moreTests)
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
