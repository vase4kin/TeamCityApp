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

package com.github.vase4kin.teamcityapp.account.create.view;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;
import com.google.android.material.textfield.TextInputLayout;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CreateAccountViewImpl implements CreateAccountView {

    @BindColor(R.color.md_blue_A100)
    int mBlueColor;

    @BindColor(R.color.md_white_1000)
    int mWhiteColor;

    @BindColor(R.color.colorPrimary)
    int mPrimaryColor;

    @BindColor(R.color.login_text_error_color)
    int mOrangeColor;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.teamcity_url)
    EditText mServerUrl;

    @BindView(R.id.teamcity_url_wrapper)
    TextInputLayout mUrlInputLayout;

    @BindView(R.id.user_field_wrapper)
    TextInputLayout mUserNameInputLayout;

    @BindView(R.id.password_field_wrapper)
    TextInputLayout mPasswordInputLayout;

    @BindView(R.id.user_name)
    EditText mUserName;

    @BindView(R.id.password)
    EditText mPassword;

    @BindView(R.id.guest_user_switch)
    Switch mGuestUserSwitch;

    @BindView(R.id.disable_ssl_switch)
    Switch disableSslSwitch;

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
    public void initViews(@NonNull final ViewListener listener) {
        mUnbinder = ButterKnife.bind(this, mActivity);
        initDialogs();
        initToolbar(listener);

        mPassword.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                listener.validateUserData(
                        mServerUrl.getText().toString().trim(),
                        mUserName.getText().toString().trim(),
                        mPassword.getText().toString().trim(),
                        disableSslSwitch.isChecked());
                return true;
            }
            return false;
        });

        mGuestUserSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            mUserName.setVisibility(b ? View.GONE : View.VISIBLE);
            mUserNameInputLayout.setVisibility(b ? View.GONE : View.VISIBLE);
            mPassword.setVisibility(b ? View.GONE : View.VISIBLE);
            mPasswordInputLayout.setVisibility(b ? View.GONE : View.VISIBLE);
            setupViewsRegardingUserType(b, listener);
        });

        setupViewsRegardingUserType(false, listener);

        //Set text selection to the end
        mServerUrl.setSelection(mServerUrl.getText().length());

        disableSslSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                listener.onDisableSslSwitchClick();
            }
        });
    }

    /**
     * Setup views regarding user type
     *
     * @param isGuestUser - Is guest user enabled
     * @param listener    - listener
     */
    private void setupViewsRegardingUserType(boolean isGuestUser, final OnValidateListener listener) {
        if (isGuestUser) {
            // guest user
            mServerUrl.setImeOptions(EditorInfo.IME_ACTION_DONE);
            mServerUrl.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (mGuestUserSwitch.isChecked()) {
                        listener.validateGuestUserData(
                                mServerUrl.getText().toString().trim(), disableSslSwitch.isChecked());
                    } else {
                        listener.validateUserData(
                                mServerUrl.getText().toString().trim(),
                                mUserName.getText().toString().trim(),
                                mPassword.getText().toString().trim(),
                                disableSslSwitch.isChecked());
                    }
                }
                return false;
            });
        } else {
            // not guest user
            mServerUrl.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            mServerUrl.setOnEditorActionListener(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showError(@NonNull String errorMessage) {
        mUrlInputLayout.setError(errorMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showServerUrlCanNotBeEmptyError() {
        showError(mActivity.getString(R.string.server_cannot_be_empty));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showUserNameCanNotBeEmptyError() {
        showError(mActivity.getString(R.string.server_user_name_cannot_be_empty));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showPasswordCanNotBeEmptyError() {
        showError(mActivity.getString(R.string.server_password_cannot_be_empty));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showCouldNotSaveUserError() {
        showError(mActivity.getString(R.string.error_save_account));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideError() {
        mUrlInputLayout.setError(null);
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
        return TextUtils.isEmpty(mServerUrl.getText());
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
        showError(mActivity.getResources().getString(R.string.add_new_account_dialog_account_exist_error_message));
    }

    /**
     * Init toolbar
     *
     * @param listener - Listener to receive callbacks
     */
    private void initToolbar(final ViewListener listener) {
        mToolbar.setTitle(R.string.add_new_account_dialog_title);
        // For ui testing purpose
        mToolbar.setNavigationContentDescription(R.string.navigate_up);
        mToolbar.setNavigationIcon(new IconDrawable(mActivity, MaterialIcons.md_close).color(Color.WHITE).actionBarSize());
        mToolbar.setNavigationOnClickListener(new OnToolBarNavigationListenerImpl(listener));
        mToolbar.inflateMenu(R.menu.menu_create_account_dialog);
        mToolbar.setOnMenuItemClickListener(new OnCreateMenuItemClickListenerImpl(listener, mServerUrl, mUserName, mPassword, mGuestUserSwitch, disableSslSwitch));
    }

    /**
     * Init dialogs
     */
    private void initDialogs() {
        mProgressDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.progress_dialog_title)
                .content(R.string.progress_dialog_content)
                .progress(true, 0)
                .widgetColor(mWhiteColor)
                .contentColor(mWhiteColor)
                .titleColor(mWhiteColor)
                .autoDismiss(false)
                .backgroundColor(mPrimaryColor)
                .build();

        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        mDiscardDialog = new MaterialDialog.Builder(mActivity)
                .titleColor(mWhiteColor)
                .widgetColor(mWhiteColor)
                .content(R.string.discard_dialog_content)
                .contentColor(mWhiteColor)
                .backgroundColor(mPrimaryColor)
                .positiveText(R.string.discard_dialog_positive_button_text)
                .positiveColor(mOrangeColor)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        finish();
                    }
                })
                .negativeText(R.string.discard_dialog_negative_button_text)
                .negativeColor(mOrangeColor)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showDisableSslWarningDialog() {
        new MaterialDialog.Builder(mActivity)
                .titleColor(mWhiteColor)
                .title(R.string.warning_ssl_dialog_title)
                .content(R.string.warning_ssl_dialog_content)
                .widgetColor(mWhiteColor)
                .contentColor(mWhiteColor)
                .backgroundColor(mPrimaryColor)
                .positiveColor(mOrangeColor)
                .positiveText(R.string.dialog_ok_title)
                .negativeColor(mOrangeColor)
                .negativeText(R.string.warning_ssl_dialog_negative)
                .linkColor(mOrangeColor)
                .onPositive((dialog, which) -> {
                    disableSslSwitch.setChecked(true);
                    dialog.dismiss();
                })
                .onNegative((dialog, which) -> {
                    disableSslSwitch.setChecked(false);
                    dialog.dismiss();
                })
                .canceledOnTouchOutside(false)
                .autoDismiss(false)
                .cancelable(false)
                .show();
    }
}
