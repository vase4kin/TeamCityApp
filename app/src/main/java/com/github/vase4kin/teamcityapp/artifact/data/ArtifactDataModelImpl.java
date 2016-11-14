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

package com.github.vase4kin.teamcityapp.artifact.data;

import com.github.vase4kin.teamcityapp.artifact.api.File;

import java.util.List;

/**
 * Impl of {@link ArtifactDataModel}
 */
public class ArtifactDataModelImpl implements ArtifactDataModel {

    /**
     * Artifact collection
     */
    private List<File> mFiles;

    public ArtifactDataModelImpl(List<File> mFiles) {
        this.mFiles = mFiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSize(int position) {
        return mFiles.get(position).getSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile(int position) {
        return mFiles.get(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSize(int position) {
        return mFiles.get(position).getSize() != 0;
    }
}
