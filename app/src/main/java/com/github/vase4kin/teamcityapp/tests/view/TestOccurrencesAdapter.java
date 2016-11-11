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

package com.github.vase4kin.teamcityapp.tests.view;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.adapter.ViewLoadMore;
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModel;
import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Tests adapter
 */
public class TestOccurrencesAdapter extends RecyclerView.Adapter<TestOccurrencesAdapter.TestOccurrenceViewHolder> implements ViewLoadMore<TestsDataModel> {

    private TestsDataModel mDataModel;
    private OnTestOccurrenceClickListener mOnClickListener;

    private LoadMore mLoadMore = new LoadMore() {
        @Override
        public String getId() {
            return "012345731";
        }
    };

    public TestOccurrencesAdapter(TestsDataModel mDataModel, OnTestOccurrenceClickListener mOnClickListener) {
        this.mDataModel = mDataModel;
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataModel.isLoadMore(position)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return mDataModel.getItemCount();
    }

    @Override
    public TestOccurrenceViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        int layout;
        switch (viewType) {
            case 0:
                layout = R.layout.item_test_occurence_list;
                break;
            case 1:
            default:
                layout = R.layout.item_load_more;
                break;
        }
        final View v = inflater.inflate(layout, viewGroup, false);
        return new TestOccurrenceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final TestOccurrenceViewHolder holder, int position) {
        if (holder.mTextView != null) {
            holder.mTextView.setText(mDataModel.getName(position));
        }
        if (holder.mIcon != null) {
            holder.mIcon.setText(mDataModel.getStatusIcon(position));
        }
        if (mDataModel.isFailed(position)) {
            if (holder.mContainer != null) {
                final int adapterPosition = position;
                holder.mContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnClickListener.onFailedTestClick(mDataModel.getHref(adapterPosition));
                    }
                });
            }
        }
    }

    public static class TestOccurrenceViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.container)
        FrameLayout mContainer;
        @Nullable
        @BindView(R.id.itemTitle)
        TextView mTextView;
        @Nullable
        @BindView(R.id.itemIcon)
        IconTextView mIcon;

        public TestOccurrenceViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLoadMore() {
        mDataModel.add(mLoadMore);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadMore() {
        mDataModel.remove(mLoadMore);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMoreBuilds(TestsDataModel dataModel) {
        mDataModel.add(dataModel);
    }

    public static class LoadMore extends TestOccurrences.TestOccurrence {
    }
}
