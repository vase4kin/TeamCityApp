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

package com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.vase4kin.teamcityapp.R

/**
 * Bottom sheet dialog
 */
class FilterBottomSheetDialogFragment : com.google.android.material.bottomsheet.BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = View.inflate(context, R.layout.dialog_bottom_sheet_filter, null)
        val arguments = arguments
        val filter = Filter.values()[arguments?.getInt(ARG_CODE, 0) ?: 0]
        val text = when (filter) {
            Filter.RUNNING_ALL -> "Show all running builds"
            Filter.QUEUE_ALL -> "Show all queue builds"
            Filter.QUEUE_FAVORITES, Filter.RUNNING_FAVORITES -> "Show only favorites"
        }
        val textView = view.findViewById<TextView>(R.id.text)
        textView.text = text
        textView.setOnClickListener {
            this@FilterBottomSheetDialogFragment.dismiss()
        }
        return view
    }

    companion object {

        const val ARG_CODE = "arg_code"

        fun createBottomSheetDialog(code: Int): FilterBottomSheetDialogFragment {
            val filterBottomSheetDialogFragment = FilterBottomSheetDialogFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_CODE, code)
            filterBottomSheetDialogFragment.arguments = bundle
            return filterBottomSheetDialogFragment
        }
    }
}
