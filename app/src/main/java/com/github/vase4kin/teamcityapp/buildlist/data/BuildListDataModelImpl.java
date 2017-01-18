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

package com.github.vase4kin.teamcityapp.buildlist.data;

import android.support.annotation.VisibleForTesting;

import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.utils.IconUtils;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Impl of {@link BuildListDataModel}
 */
public class BuildListDataModelImpl implements BuildListDataModel {

    /**
     * Load more
     */
    @VisibleForTesting
    static final Build LOAD_MORE = new Build() {
        @Override
        public String getId() {
            return UUID.randomUUID().toString();
        }
    };

    private List<Build> mBuilds;

    public BuildListDataModelImpl(List<Build> mBuilds) {
        this.mBuilds = mBuilds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBranchName(int position) {
        return mBuilds.get(position).getBranchName();
    }

    @Override
    public boolean hasBranch(int position) {
        return mBuilds.get(position).getBranchName() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBuildStatusIcon(int position) {
        Build build = mBuilds.get(position);
        return IconUtils.getBuildStatusIcon(build.getStatus(), build.getState());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatusText(int position) {
        return mBuilds.get(position).getStatusText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBuildNumber(int position) {
        String buildNumber = mBuilds.get(position).getNumber();
        return buildNumber != null
                ? String.format("#%s", buildNumber)
                : "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Build getBuild(int position) {
        return mBuilds.get(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoadMore(int position) {
        return mBuilds.get(position).equals(LOAD_MORE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLoadMore() {
        mBuilds.add(LOAD_MORE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadMore() {
        mBuilds.remove(LOAD_MORE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMoreBuilds(BuildListDataModel dataModel) {
        for (Build build : dataModel) {
            mBuilds.add(build);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStartDate(int position) {
        return mBuilds.get(position).getStartDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBuildTypeId(int position) {
        return mBuilds.get(position).getBuildTypeId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPersonal(int position) {
        return mBuilds.get(position).isPersonal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPinned(int position) {
        return mBuilds.get(position).isPinned();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isQueued(int position) {
        return mBuilds.get(position).isQueued();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return mBuilds.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Build> iterator() {
        return mBuilds.iterator();
    }
}
