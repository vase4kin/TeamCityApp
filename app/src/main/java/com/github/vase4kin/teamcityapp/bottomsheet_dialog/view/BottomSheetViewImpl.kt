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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.view

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Impl of [BottomSheetView]
 */
class BottomSheetViewImpl(
    private val view: View,
    private val fragment: BottomSheetDialogFragment,
    private val adapter: BottomSheetAdapter
) : BottomSheetView {

    @BindView(R.id.my_recycler_view)
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.bs_main_title)
    lateinit var mainTitle: TextView

    private lateinit var unbinder: Unbinder

    /**
     * {@inheritDoc}
     */
    override fun initViews(
        listener: BottomSheetView.OnBottomSheetClickListener,
        dataModel: BottomSheetDataModel,
        title: String
    ) {
        unbinder = ButterKnife.bind(this, view)
        mainTitle.text = title
        adapter.dataModel = dataModel
        adapter.listener = listener
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.adapter = adapter
    }

    /**
     * {@inheritDoc}
     */
    override fun unbindViews() {
        unbinder.unbind()
    }

    /**
     * {@inheritDoc}
     */
    override fun close() {
        fragment.dismiss()
    }
}
