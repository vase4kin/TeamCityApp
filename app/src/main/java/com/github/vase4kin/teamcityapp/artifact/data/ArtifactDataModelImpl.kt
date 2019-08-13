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

package com.github.vase4kin.teamcityapp.artifact.data

import com.github.vase4kin.teamcityapp.artifact.api.File

/**
 * Impl of [ArtifactDataModel]
 */
class ArtifactDataModelImpl(private val files: List<File>) : ArtifactDataModel {

    /**
     * {@inheritDoc}
     */
    override fun getSize(position: Int): Long {
        return files[position].size
    }

    /**
     * {@inheritDoc}
     */
    override fun getFile(position: Int): File {
        return files[position]
    }

    /**
     * {@inheritDoc}
     */
    override fun getItemCount(): Int {
        return files.size
    }

    /**
     * {@inheritDoc}
     */
    override fun hasSize(position: Int): Boolean {
        return files[position].size != 0L
    }
}
