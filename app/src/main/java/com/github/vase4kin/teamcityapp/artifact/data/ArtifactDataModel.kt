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
import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel

/**
 * Data model to manage artifacts data
 */
interface ArtifactDataModel : BaseDataModel {

    /**
     * @param position - Adapter position
     * @return Size of artifact
     */
    fun getSize(position: Int): Long

    /**
     * Get artifact file
     *
     * @param position - adapter position
     * @return Artifact file
     */
    fun getFile(position: Int): File

    /**
     * @param position - Adapter position
     * @return true if the artifact file has size
     */
    fun hasSize(position: Int): Boolean
}
