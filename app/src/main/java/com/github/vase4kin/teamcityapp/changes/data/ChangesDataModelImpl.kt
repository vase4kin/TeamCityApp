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

package com.github.vase4kin.teamcityapp.changes.data

import androidx.annotation.VisibleForTesting
import com.github.vase4kin.teamcityapp.changes.api.Changes
import java.util.*

/**
 * Impl of [ChangesDataModel]
 */
class ChangesDataModelImpl(private val changes: MutableList<Changes.Change>) : ChangesDataModel {

    /**
     * {@inheritDoc}
     */
    override val itemCount: Int
        get() = changes.size

    /**
     * {@inheritDoc}
     */
    override fun getVersion(position: Int): String {
        return changes[position].version
    }

    /**
     * {@inheritDoc}
     */
    override fun getUserName(position: Int): String {
        return changes[position].username
    }

    /**
     * {@inheritDoc}
     */
    override fun getDate(position: Int): String {
        return changes[position].date
    }

    /**
     * {@inheritDoc}
     */
    override fun getComment(position: Int): String {
        return changes[position].comment.trim { it <= ' ' }
    }

    /**
     * {@inheritDoc}
     */
    override fun getFilesCount(position: Int): Int {
        val changeFiles = changes[position].files
        return changeFiles?.files?.size ?: 0
    }

    /**
     * {@inheritDoc}
     */
    override fun getChange(position: Int): Changes.Change {
        return changes[position]
    }

    /**
     * {@inheritDoc}
     */
    override fun isLoadMore(position: Int): Boolean {
        return changes[position] == LOAD_MORE
    }

    /**
     * {@inheritDoc}
     */
    override fun addLoadMore() {
        changes.add(LOAD_MORE)
    }

    /**
     * {@inheritDoc}
     */
    override fun removeLoadMore() {
        changes.remove(LOAD_MORE)
    }

    /**
     * {@inheritDoc}
     */
    override fun addMoreBuilds(dataModel: ChangesDataModel) {
        for (change in dataModel) {
            changes.add(change)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun iterator(): Iterator<Changes.Change> {
        return changes.iterator()
    }

    companion object {

        /**
         * Load more
         */
        @VisibleForTesting
        val LOAD_MORE: Changes.Change = object : Changes.Change() {
            override fun getId(): String {
                return UUID.randomUUID().toString()
            }
        }
    }
}
