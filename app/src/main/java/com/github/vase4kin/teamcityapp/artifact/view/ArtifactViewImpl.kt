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

package com.github.vase4kin.teamcityapp.artifact.view

import android.app.Activity
import android.graphics.Color
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity

import com.afollestad.materialdialogs.MaterialDialog
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.artifact.api.File
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.MenuItemsFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetDialogFragment

private const val TAG_BOTTOM_SHEET = "Tag bottom sheet"

/**
 * Impl of [ArtifactView]
 */
class ArtifactViewImpl(
    view: View,
    activity: Activity,
    @StringRes emptyMessage: Int,
    adapter: ArtifactAdapter
) : BaseListViewImpl<ArtifactDataModel, ArtifactAdapter>(view, activity, emptyMessage, adapter),
    ArtifactView {

    lateinit var progressDialog: MaterialDialog
    private var listener: OnArtifactPresenterListener? = null

    /**
     * {@inheritDoc}
     */
    override fun setOnArtifactPresenterListener(listener: OnArtifactPresenterListener) {
        this.listener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun initViews(listener: BaseListView.ViewListener) {
        super.initViews(listener)
        progressDialog = MaterialDialog.Builder(activity)
            .title(R.string.download_artifact_dialog_title)
            .content(R.string.progress_dialog_content)
            .progress(true, 0)
            .widgetColor(Color.GRAY)
            .autoDismiss(false)
            .negativeText(R.string.text_cancel_button)
            .callback(object : MaterialDialog.ButtonCallback() {
                override fun onNegative(dialog: MaterialDialog?) {
                    progressDialog.dismiss()
                }
            })
            .build()
        progressDialog.setCanceledOnTouchOutside(false)
    }

    /**
     * {@inheritDoc}
     */
    override fun showData(dataModel: ArtifactDataModel) {
        adapter.dataModel = dataModel
        adapter.setOnClickListener(listener!!)
        recyclerView.adapter = adapter
        recyclerView.adapter?.notifyDataSetChanged()
    }

    /**
     * {@inheritDoc}
     */
    override fun showProgressDialog() {
        progressDialog.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun dismissProgressDialog() {
        progressDialog.dismiss()
    }

    /**
     * {@inheritDoc}
     */
    override fun showFullBottomSheet(artifactFile: File) {
        val bottomSheetDialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(
            artifactFile.name,
            arrayOf(artifactFile.content.href, artifactFile.children!!.href),
            MenuItemsFactory.TYPE_ARTIFACT_FULL
        )
        bottomSheetDialogFragment.show(
            (activity as AppCompatActivity).supportFragmentManager,
            TAG_BOTTOM_SHEET
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun showFolderBottomSheet(artifactFile: File) {
        val bottomSheetDialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(
            artifactFile.name, artifactFile.children!!.href, MenuItemsFactory.TYPE_ARTIFACT_FOLDER
        )
        bottomSheetDialogFragment.show(
            (activity as AppCompatActivity).supportFragmentManager,
            TAG_BOTTOM_SHEET
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun showBrowserBottomSheet(artifactFile: File) {
        val bottomSheetDialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(
            artifactFile.name,
            arrayOf(artifactFile.content.href, artifactFile.href),
            MenuItemsFactory.TYPE_ARTIFACT_BROWSER
        )
        bottomSheetDialogFragment.show(
            (activity as AppCompatActivity).supportFragmentManager,
            TAG_BOTTOM_SHEET
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun showDefaultBottomSheet(artifactFile: File) {
        val bottomSheetDialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(
            artifactFile.name, artifactFile.content.href, MenuItemsFactory.TYPE_ARTIFACT_DEFAULT
        )
        bottomSheetDialogFragment.show(
            (activity as AppCompatActivity).supportFragmentManager,
            TAG_BOTTOM_SHEET
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun recyclerViewId(): Int {
        return R.id.artifact_recycler_view
    }

    /**
     * {@inheritDoc}
     */
    override fun showPermissionsDeniedDialog() {
        MaterialDialog.Builder(activity)
            .title(R.string.permissions_dialog_title)
            .content(R.string.permissions_dialog_text_no_permissions)
            .widgetColor(Color.GRAY)
            .positiveText(R.string.dialog_ok_title)
            .show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showPermissionsInfoDialog(onPermissionsDialogListener: OnPermissionsDialogListener) {
        MaterialDialog.Builder(activity)
            .title(R.string.permissions_dialog_title)
            .content(R.string.permissions_dialog_content)
            .widgetColor(Color.GRAY)
            .positiveText(R.string.dialog_ok_title)
            .onPositive { _, _ -> onPermissionsDialogListener.onAllow() }
            .show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showInstallPackagesPermissionsInfoDialog(onPermissionsDialogListener: OnPermissionsDialogListener) {
        MaterialDialog.Builder(activity)
            .title(R.string.permissions_dialog_title)
            .content(R.string.permissions_install_packages_dialog_content)
            .widgetColor(Color.GRAY)
            .positiveText(R.string.dialog_ok_title)
            .onPositive { _, _ -> onPermissionsDialogListener.onAllow() }
            .show()
    }
}
