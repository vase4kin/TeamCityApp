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

package com.github.vase4kin.teamcityapp.base.list.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Base view holder
 *
 * @param <BM> - Base data model
 */
public abstract class BaseViewHolder<BM extends BaseDataModel> extends RecyclerView.ViewHolder {

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    /**
     * Bind data
     *
     * @param dataModel - Data model
     * @param position  - item position
     */
    public abstract void bind(BM dataModel, int position);
}
