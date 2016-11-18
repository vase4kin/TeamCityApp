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

import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.artifact.api.File;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.joanzapata.iconify.widget.IconTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArtifactViewHolder extends BaseViewHolder<ArtifactDataModel> {

    private static final String FILE_ICON = "{mdi-file}";
    private static final String FOLDER_ICON = "{mdi-folder}";

    @BindView(R.id.container)
    FrameLayout mContainer;
    @BindView(R.id.itemIcon)
    IconTextView mIcon;
    @BindView(R.id.itemTitle)
    TextView mFileName;
    @BindView(R.id.itemSubTitle)
    TextView mSize;

    /**
     * Constructor
     *
     * @param parent group view
     */
    public ArtifactViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artifact_list, parent, false));
        ButterKnife.bind(this, itemView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(ArtifactDataModel dataModel, int position) {
        final File artifactFile = dataModel.getFile(position);
        if (artifactFile.getChildren() != null) {
            mIcon.setText(FOLDER_ICON);
        } else {
            mIcon.setText(FILE_ICON);
        }
        mFileName.setText(artifactFile.getName());
        if (dataModel.hasSize(position)) {
            mSize.setText(Formatter.formatFileSize(mSize.getContext(), artifactFile.getSize()));
        } else {
            mSize.setVisibility(View.GONE);
        }
    }
}
