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

package com.github.vase4kin.teamcityapp.artifact.view;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.artifact.api.File;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.MenuItemsFactory;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetDialogFragment;

/**
 * Impl of {@link ArtifactView}
 */
public class ArtifactViewImpl extends BaseListViewImpl<ArtifactDataModel, ArtifactAdapter> implements ArtifactView {

    private static final String TAG_BOTTOM_SHEET = "Tag bottom sheet";

    private MaterialDialog mProgressDialog;
    private OnArtifactPresenterListener mListener;

    public ArtifactViewImpl(View mView,
                            Activity activity,
                            @StringRes int emptyMessage,
                            ArtifactAdapter adapter) {
        super(mView, activity, emptyMessage, adapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnArtifactPresenterListener(@NonNull OnArtifactPresenterListener listener) {
        this.mListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(@NonNull ViewListener listener) {
        super.initViews(listener);
        mProgressDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.download_artifact_dialog_title)
                .content(R.string.progress_dialog_content)
                .progress(true, 0)
                .widgetColor(Color.GRAY)
                .autoDismiss(false)
                .negativeText(R.string.text_cancel_button)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        mListener.unSubscribe();
                        mProgressDialog.dismiss();
                    }
                })
                .build();
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(ArtifactDataModel dataModel) {
        mAdapter.setDataModel(dataModel);
        mAdapter.setOnClickListener(mListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showProgressDialog() {
        mProgressDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dismissProgressDialog() {
        mProgressDialog.dismiss();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showFullBottomSheet(@NonNull File artifactFile) {
        BottomSheetDialogFragment bottomSheetDialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(
                artifactFile.getName(),
                new String[]{artifactFile.getContent().getHref(), artifactFile.getChildren().getHref()},
                MenuItemsFactory.TYPE_ARTIFACT_FULL);
        bottomSheetDialogFragment.show(((AppCompatActivity) mActivity).getSupportFragmentManager(), TAG_BOTTOM_SHEET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showFolderBottomSheet(@NonNull File artifactFile) {
        BottomSheetDialogFragment bottomSheetDialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(
                artifactFile.getName(), artifactFile.getChildren().getHref(), MenuItemsFactory.TYPE_ARTIFACT_FOLDER);
        bottomSheetDialogFragment.show(((AppCompatActivity) mActivity).getSupportFragmentManager(), TAG_BOTTOM_SHEET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showBrowserBottomSheet(@NonNull File artifactFile) {
        BottomSheetDialogFragment bottomSheetDialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(
                artifactFile.getName(),
                new String[]{artifactFile.getContent().getHref(), artifactFile.getHref()},
                MenuItemsFactory.TYPE_ARTIFACT_BROWSER);
        bottomSheetDialogFragment.show(((AppCompatActivity) mActivity).getSupportFragmentManager(), TAG_BOTTOM_SHEET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showDefaultBottomSheet(File artifactFile) {
        BottomSheetDialogFragment bottomSheetDialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(
                artifactFile.getName(), artifactFile.getContent().getHref(), MenuItemsFactory.TYPE_ARTIFACT_DEFAULT);
        bottomSheetDialogFragment.show(((AppCompatActivity) mActivity).getSupportFragmentManager(), TAG_BOTTOM_SHEET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int recyclerViewId() {
        return R.id.artifact_recycler_view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showPermissionsDeniedDialog() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.permissions_dialog_title)
                .content(R.string.permissions_dialog_text_no_permissions)
                .widgetColor(Color.GRAY)
                .positiveText(R.string.dialog_ok_title)
                .show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showPermissionsInfoDialog(final OnPermissionsDialogListener onPermissionsDialogListener) {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.permissions_dialog_title)
                .content(R.string.permissions_dialog_content)
                .widgetColor(Color.GRAY)
                .positiveText(R.string.dialog_ok_title)
                .onPositive((dialog, which) -> onPermissionsDialogListener.onAllow())
                .show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showInstallPackagesPermissionsInfoDialog(final OnPermissionsDialogListener onPermissionsDialogListener) {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.permissions_dialog_title)
                .content(R.string.permissions_install_packages_dialog_content)
                .widgetColor(Color.GRAY)
                .positiveText(R.string.dialog_ok_title)
                .onPositive((dialog, which) -> onPermissionsDialogListener.onAllow())
                .show();
    }
}
