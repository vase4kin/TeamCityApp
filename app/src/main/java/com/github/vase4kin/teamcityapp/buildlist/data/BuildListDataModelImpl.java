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
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl;

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
    static final BuildDetails LOAD_MORE = new BuildDetailsImpl(new Build() {
        @Override
        public String getId() {
            return UUID.randomUUID().toString();
        }
    });

    private List<BuildDetails> mBuildDetailsList;

    public BuildListDataModelImpl(List<BuildDetails> builds) {
        this.mBuildDetailsList = builds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBranchName(int position) {
        return mBuildDetailsList.get(position).getBranchName();
    }

    @Override
    public boolean hasBranch(int position) {
        return mBuildDetailsList.get(position).getBranchName() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBuildStatusIcon(int position) {
        return mBuildDetailsList.get(position).getStatusIcon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatusText(int position) {
        return mBuildDetailsList.get(position).getStatusText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBuildNumber(int position) {
        String buildNumber = mBuildDetailsList.get(position).getNumber();
        return buildNumber != null
                ? String.format("#%s", buildNumber)
                : "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Build getBuild(int position) {
        return mBuildDetailsList.get(position).toBuild();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoadMore(int position) {
        return mBuildDetailsList.get(position).equals(LOAD_MORE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLoadMore() {
        mBuildDetailsList.add(LOAD_MORE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadMore() {
        mBuildDetailsList.remove(LOAD_MORE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMoreBuilds(BuildListDataModel dataModel) {
        for (BuildDetails build : dataModel) {
            mBuildDetailsList.add(build);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStartDate(int position) {
        return mBuildDetailsList.get(position).getStartDateFormattedAsHeader();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBuildTypeId(int position) {
        return mBuildDetailsList.get(position).getBuildTypeId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasBuildTypeInfo(int position) {
        return mBuildDetailsList.get(position).hasBuildTypeInfo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBuildTypeFullName(int position) {
        return mBuildDetailsList.get(position).getBuildTypeFullName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBuildTypeName(int position) {
        return mBuildDetailsList.get(position).getBuildTypeName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPersonal(int position) {
        return mBuildDetailsList.get(position).isPersonal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPinned(int position) {
        return mBuildDetailsList.get(position).isPinned();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isQueued(int position) {
        return mBuildDetailsList.get(position).isQueued();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return mBuildDetailsList.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<BuildDetails> iterator() {
        return mBuildDetailsList.iterator();
    }
}
