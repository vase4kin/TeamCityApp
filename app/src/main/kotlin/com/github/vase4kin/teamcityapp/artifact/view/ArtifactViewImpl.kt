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
import android.content.DialogInterface
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.artifact.api.File
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl
import com.github.vase4kin.teamcityapp.utils.createProgressDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import teamcityapp.features.bottom_sheet.api.BottomSheetNavigation
import teamcityapp.features.bottom_sheet.api.SheetMenuType

private const val TAG_BOTTOM_SHEET = "Tag bottom sheet"

/**
 * Impl of [ArtifactView]
 */
class ArtifactViewImpl(
    view: View,
    activity: Activity,
    @StringRes emptyMessage: Int,
    adapter: ArtifactAdapter,
    private val featureNavigation: BottomSheetNavigation
) : BaseListViewImpl<ArtifactDataModel, ArtifactAdapter>(view, activity, emptyMessage, adapter),
    ArtifactView {

    lateinit var progressDialog: AlertDialog
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
        progressDialog = createProgressDialog(activity, R.string.progress_dialog_content, cancelable = true)
        progressDialog.setTitle(R.string.download_artifact_dialog_title)
        progressDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            activity.getText(R.string.text_cancel_button)
        ) { dialog, _ -> dialog.dismiss() }
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
        val bottomSheetDialogFragment = featureNavigation.createBottomSheetDialog(
            artifactFile.name,
            arrayOf(artifactFile.content.href, artifactFile.children!!.href),
            SheetMenuType.ArtifactFull
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
        val bottomSheetDialogFragment = featureNavigation.createBottomSheetDialog(
            artifactFile.name,
            artifactFile.children!!.href,
            SheetMenuType.ArtifactFolder
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
        val bottomSheetDialogFragment = featureNavigation.createBottomSheetDialog(
            artifactFile.name,
            arrayOf(artifactFile.content.href, artifactFile.href),
            SheetMenuType.ArtifactBrowser
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
        val bottomSheetDialogFragment = featureNavigation.createBottomSheetDialog(
            artifactFile.name,
            artifactFile.content.href,
            SheetMenuType.ArtifactDefault
        )
        bottomSheetDialogFragment.show(
            (activity as AppCompatActivity).supportFragmentManager,
            TAG_BOTTOM_SHEET
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun recyclerViewId(): Int = R.id.artifact_recycler_view

    /**
     * {@inheritDoc}
     */
    override fun showPermissionsDeniedDialog() {
        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.permissions_dialog_title)
            .setMessage(R.string.permissions_dialog_text_no_permissions)
            .setPositiveButton(R.string.dialog_ok_title, null)
            .show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showPermissionsInfoDialog(onPermissionsDialogListener: OnPermissionsDialogListener) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.permissions_dialog_title)
            .setMessage(R.string.permissions_dialog_content)
            .setPositiveButton(R.string.dialog_ok_title) { _, _ -> onPermissionsDialogListener.onAllow() }
            .show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showInstallPackagesPermissionsInfoDialog(onPermissionsDialogListener: OnPermissionsDialogListener) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.permissions_dialog_title)
            .setMessage(R.string.permissions_install_packages_dialog_content)
            .setPositiveButton(R.string.dialog_ok_title) { _, _ -> onPermissionsDialogListener.onAllow() }
            .show()
    }
}
