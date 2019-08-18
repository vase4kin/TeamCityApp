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

package com.github.vase4kin.teamcityapp.properties.data

import androidx.annotation.VisibleForTesting
import com.github.vase4kin.teamcityapp.properties.api.Properties

/**
 * Impl of [PropertiesDataModel]
 */
class PropertiesDataModelImpl(private val properties: List<Properties.Property>) : PropertiesDataModel {

    /**
     * {@inheritDoc}
     */
    override val itemCount: Int
        get() = properties.size

    /**
     * {@inheritDoc}
     */
    override fun getName(position: Int): String {
        return properties[position].name
    }

    /**
     * {@inheritDoc}
     */
    override fun getValue(position: Int): String {
        val value = properties[position].value
        return if (value.isNotEmpty())
            value
        else
            EMPTY
    }

    /**
     * {@inheritDoc}
     */
    override fun isEmpty(position: Int): Boolean {
        return getValue(position) == EMPTY
    }

    companion object {

        @VisibleForTesting
        val EMPTY = "Empty"
    }
}
