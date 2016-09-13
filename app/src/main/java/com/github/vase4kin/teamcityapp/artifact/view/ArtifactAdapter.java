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

package com.github.vase4kin.teamcityapp.artifact.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.artifact.api.File;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel;
import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Adapter for artifact files
 */
public class ArtifactAdapter extends RecyclerView.Adapter<ArtifactAdapter.ArtifactViewHolder> {

    private static final String FILE_ICON = "{mdi-file}";
    private static final String FOLDER_ICON = "{mdi-folder}";

    private ArtifactDataModel mDataModel;
    private OnArtifactPresenterListener onClickListener;
    private Context mContext;

    public ArtifactAdapter(ArtifactDataModel mDataModel, OnArtifactPresenterListener onClickListener, Context mContext) {
        this.mDataModel = mDataModel;
        this.onClickListener = onClickListener;
        this.mContext = mContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int position) {
        if (mDataModel.getSize(position) == 0) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return mDataModel.getItemCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArtifactViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        final View v = inflater.inflate(viewType == 0 ? R.layout.item_artifact_list : R.layout.item_artifact_list_without_size_, viewGroup, false);
        return new ArtifactViewHolder(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(ArtifactViewHolder holder, int position) {
        final File artifactFile = mDataModel.getFile(position);
        holder.setArtifactFile(artifactFile);
        if (artifactFile.getChildren() != null) {
            holder.mIcon.setText(FOLDER_ICON);
        } else {
            holder.mIcon.setText(FILE_ICON);
        }
        if (artifactFile.getSize() != 0 && holder.mSize != null) {
            holder.mSize.setText(Formatter.formatFileSize(mContext, artifactFile.getSize()));
        }
        holder.mFileName.setText(artifactFile.getName());
    }

    /**
     * View holder for artifact filr
     */
    public class ArtifactViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container)
        FrameLayout mContainer;
        @BindView(R.id.itemIcon)
        IconTextView mIcon;
        @BindView(R.id.itemTitle)
        TextView mFileName;
        @Nullable
        @BindView(R.id.itemSubTitle)
        TextView mSize;

        private File artifactFile;

        public ArtifactViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        public void setArtifactFile(File artifactFile) {
            this.artifactFile = artifactFile;
        }

        @OnClick(R.id.container)
        public void click() {
            onClickListener.onClick(artifactFile);
        }

        @SuppressWarnings("SameReturnValue")
        @OnLongClick(R.id.container)
        public boolean longClick() {
            onClickListener.onLongClick(artifactFile);
            return true;
        }
    }
}
