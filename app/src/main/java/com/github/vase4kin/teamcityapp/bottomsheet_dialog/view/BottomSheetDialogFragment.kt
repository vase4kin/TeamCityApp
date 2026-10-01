/*
 * Copyright 2020 Andrey Tolpeev
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.presenter.BottomSheetPresenterImpl

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Bottom sheet dialog
 */
@AndroidEntryPoint
class BottomSheetDialogFragment :
    com.google.android.material.bottomsheet.BottomSheetDialogFragment() {

    @Inject
    lateinit var presenterProvider: javax.inject.Provider<BottomSheetPresenterImpl>

    private lateinit var presenter: BottomSheetPresenterImpl

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View.inflate(context, R.layout.dialog_bottom_sheet, null)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = presenterProvider.get()
        presenter.handleOnCreateView()
    }

    override fun onDestroyView() {
        presenter.handleOnDestroyView()
        super.onDestroyView()
    }

    companion object {

        internal const val ARG_TITLE = "arg_title"
        internal const val ARG_DESCRIPTION = "arg_description"
        internal const val ARG_BOTTOM_SHEET_TYPE = "arg_bottom_sheet_type"

        fun createBottomSheetDialog(
            title: String,
            description: String,
            menuType: Int
        ): BottomSheetDialogFragment {
            return createBottomSheetDialog(title, arrayOf(description), menuType)
        }

        fun createBottomSheetDialog(
            title: String,
            descriptions: Array<String>,
            menuType: Int
        ): BottomSheetDialogFragment {
            val bottomSheetDialogFragment = BottomSheetDialogFragment()
            val bundle = Bundle()
            bundle.putString(ARG_TITLE, title)
            bundle.putStringArray(ARG_DESCRIPTION, descriptions)
            bundle.putInt(ARG_BOTTOM_SHEET_TYPE, menuType)
            bottomSheetDialogFragment.arguments = bundle
            return bottomSheetDialogFragment
        }
    }
}
