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

package com.github.vase4kin.teamcityapp.buildlist.filter;

import android.text.TextUtils;

import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView;

/**
 * Impl of {@link BuildListFilter}
 */
public class BuildListFilterImpl implements BuildListFilter {

    private int mFilterType;
    private String mBranch;
    private boolean mIsPersonal = false;
    private boolean mIsPinned = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFilter(int filter) {
        this.mFilterType = filter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBranch(String branch) {
        this.mBranch = branch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPersonal(boolean isPersonal) {
        this.mIsPersonal = isPersonal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPinned(boolean isPinned) {
        this.mIsPinned = isPinned;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toLocator() {
        StringBuilder locatorBuilder = new StringBuilder();
        switch (mFilterType) {
            case FilterBuildsView.FILTER_SUCCESS:
                locatorBuilder.append("status:SUCCESS");
                break;
            case FilterBuildsView.FILTER_FAILED:
                locatorBuilder.append("status:FAILURE");
                break;
            case FilterBuildsView.FILTER_ERROR:
                locatorBuilder.append("status:ERROR");
                break;
            case FilterBuildsView.FILTER_CANCELLED:
                locatorBuilder.append("canceled:true");
                break;
            case FilterBuildsView.FILTER_FAILED_TO_START:
                locatorBuilder.append("failedToStart:true");
                break;
            case FilterBuildsView.FILTER_RUNNING:
                locatorBuilder.append("running:true");
                break;
            case FilterBuildsView.FILTER_QUEUED:
                locatorBuilder.append("state:queued");
                break;
            case FilterBuildsView.FILTER_NONE:
            default:
                locatorBuilder.append("state:any,canceled:any,failedToStart:any");
                break;
        }
        locatorBuilder.append(",");
        if (!TextUtils.isEmpty(mBranch)) {
            locatorBuilder.append("branch:name:");
            locatorBuilder.append(mBranch);
        } else {
            locatorBuilder.append("branch:default:any");
        }
        locatorBuilder.append(",");
        locatorBuilder.append("personal:");
        locatorBuilder.append(String.valueOf(mIsPersonal));
        locatorBuilder.append(",");
        locatorBuilder.append("pinned:");
        // Queued builds can be shown if only pinned:any, because queued builds can't be pinned
        // ONLY DO SO FOR QUEUED BUILDS FILTER (For now it's expected behavior)
        if (mFilterType == FilterBuildsView.FILTER_QUEUED) {
            locatorBuilder.append("any");
        } else {
            locatorBuilder.append(String.valueOf(mIsPinned));
        }
        // Remove count for queued and running build cause they don't have next href
        if (mFilterType == FilterBuildsView.FILTER_RUNNING) {
            return locatorBuilder.toString();
        }
        if (mFilterType == FilterBuildsView.FILTER_QUEUED) {
            return locatorBuilder.toString();
        }
        locatorBuilder.append(",");
        locatorBuilder.append("count:10");
        return locatorBuilder.toString();
    }
}
