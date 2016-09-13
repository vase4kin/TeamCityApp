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

package com.github.vase4kin.teamcityapp.account.create.view;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CreateAccountViewImpl implements CreateAccountView {

    @BindView(R.id.teamcity_url)
    EditText mUrlEditText;

    @BindView(R.id.teamcity_url_wrapper)
    TextInputLayout mUrlInputLayout;

    private Unbinder mUnbinder;

    private Activity mActivity;
    private MaterialDialog mProgressDialog;
    private MaterialDialog mDiscardDialog;

    public CreateAccountViewImpl(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(OnCreateAccountPresenterListener listener) {
        mUnbinder = ButterKnife.bind(this, mActivity);
        initDialogs();
        initToolbar(listener);
        mUrlEditText.setOnFocusChangeListener(new OnDialogFocusChangeListenerImpl(mActivity));
        mUrlEditText.setOnEditorActionListener(new OnDoneActionListenerImpl(listener));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setErrorText(String errorMessage) {
        mUrlInputLayout.setError(errorMessage);
        dismissProgressDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
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
    public void showDiscardDialog() {
        mDiscardDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmailEmpty() {
        return TextUtils.isEmpty(mUrlEditText.getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finish() {
        mActivity.finish();
        mActivity.overridePendingTransition(R.anim.hold, R.anim.slide_out_bottom);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showNewAccountExistErrorMessage() {
        setErrorText(mActivity.getResources().getString(R.string.create_new_account_dialog_account_exist_error_message));
    }

    /**
     * Init toolbar
     *
     * @param listener - Listener to receive callbacks
     */
    private void initToolbar(OnCreateAccountPresenterListener listener) {
        final Toolbar toolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.create_new_account_dialog_title);
        // For ui testing purpose
        toolbar.setNavigationContentDescription(R.string.navigate_up);
        toolbar.setNavigationIcon(new IconDrawable(mActivity, MaterialIcons.md_close).color(Color.WHITE).actionBarSize());
        toolbar.setNavigationOnClickListener(new OnToolBarNavigationListenerImpl(listener));
        toolbar.inflateMenu(R.menu.menu_create_account_dialog);
        toolbar.setOnMenuItemClickListener(new OnCreateMenuItemClickListenerImpl(listener, mUrlEditText));
    }

    /**
     * Init dialogs
     */
    private void initDialogs() {
        mProgressDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.progress_dialog_title)
                .content(R.string.progress_dialog_content)
                .progress(true, 0)
                .widgetColor(Color.GRAY)
                .autoDismiss(false)
                .build();

        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        mDiscardDialog = new MaterialDialog.Builder(mActivity)
                .content(R.string.discard_dialog_content)
                .positiveText(R.string.discard_dialog_positive_button_text)
                .positiveColor(mActivity.getResources().getColor(R.color.md_blue_A100))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        finish();
                    }
                })
                .negativeText(R.string.discard_dialog_negative_button_text)
                .negativeColor(mActivity.getResources().getColor(R.color.md_blue_A100))
                .build();
    }
}
