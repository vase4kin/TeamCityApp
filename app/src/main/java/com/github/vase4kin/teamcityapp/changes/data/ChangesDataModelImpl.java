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

package com.github.vase4kin.teamcityapp.changes.data;

import com.github.vase4kin.teamcityapp.changes.api.ChangeFiles;
import com.github.vase4kin.teamcityapp.changes.api.Changes;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Impl of {@link ChangesDataModel}
 */
public class ChangesDataModelImpl implements ChangesDataModel {

    /**
     * Load more
     */
    private static final Changes.Change LOAD_MORE = new Changes.Change() {
        @Override
        public String getId() {
            return UUID.randomUUID().toString();
        }
    };

    private List<Changes.Change> mChanges;

    public ChangesDataModelImpl(List<Changes.Change> mChanges) {
        this.mChanges = mChanges;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion(int position) {
        return mChanges.get(position).getVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserName(int position) {
        return mChanges.get(position).getUsername();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDate(int position) {
        return mChanges.get(position).getDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComment(int position) {
        return mChanges.get(position).getComment().trim();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFilesCount(int position) {
        ChangeFiles changeFiles = mChanges.get(position).getFiles();
        if (changeFiles == null) {
            return 0;
        } else {
            return changeFiles.getFiles().size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Changes.Change getChange(int position) {
        return mChanges.get(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoadMore(int position) {
        return mChanges.get(position).equals(LOAD_MORE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLoadMore() {
        mChanges.add(LOAD_MORE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadMore() {
        mChanges.remove(LOAD_MORE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMoreBuilds(ChangesDataModel dataModel) {
        for (Changes.Change change : dataModel) {
            mChanges.add(change);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return mChanges.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Changes.Change> iterator() {
        return mChanges.iterator();
    }
}
