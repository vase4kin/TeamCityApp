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

package com.github.vase4kin.teamcityapp.login.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class LoginViewImpl implements LoginView {

    @BindColor(R.color.md_white_1000)
    int mWhiteColor;

    @BindColor(R.color.colorPrimary)
    int mPrimaryColor;

    @BindColor(R.color.login_text_error_color)
    int mOrangeColor;

    @BindView(R.id.img_real_logo)
    ImageView mRealLogoImageView;

    @BindView(R.id.teamcity_url)
    EditText mServerUrl;

    @BindView(R.id.teamcity_url_wrapper)
    TextInputLayout mServerUrlWrapperLayout;

    @BindView(R.id.user_field_wrapper)
    TextInputLayout mUserNameWrapperLayout;

    @BindView(R.id.user_name)
    EditText mUserName;

    @BindView(R.id.password_field_wrapper)
    TextInputLayout mPasswordWrapperLayout;

    @BindView(R.id.password)
    EditText mPassword;

    @BindView(R.id.guest_user_switch)
    Switch mGuestUserSwitch;

    @BindView(R.id.disable_ssl_switch)
    Switch disableSslSwitch;

    @BindView(R.id.btn_login)
    Button mLoginButton;

    private Unbinder mUnbinder;

    private Activity mActivity;
    private MaterialDialog mProgressDialog;

    @Nullable
    private ViewListener listener;

    public LoginViewImpl(Activity mActivity) {
        this.mActivity = mActivity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(@NonNull final ViewListener listener) {
        mUnbinder = ButterKnife.bind(this, mActivity);

        this.listener = listener;

        mProgressDialog = new MaterialDialog.Builder(mActivity)
                .content(R.string.text_progress_bar_loading)
                .progress(true, 0)
                .widgetColor(mWhiteColor)
                .contentColor(mWhiteColor)
                .autoDismiss(false)
                .backgroundColor(mPrimaryColor)
                .build();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    listener.onUserLoginButtonClick(
                            mServerUrl.getText().toString().trim(),
                            mUserName.getText().toString().trim(),
                            mPassword.getText().toString().trim(),
                            disableSslSwitch.isChecked());
                    return true;
                }
                return false;
            }
        });

        mGuestUserSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mUserName.setVisibility(b ? View.GONE : View.VISIBLE);
                mUserNameWrapperLayout.setVisibility(b ? View.GONE : View.VISIBLE);
                mPassword.setVisibility(b ? View.GONE : View.VISIBLE);
                mPasswordWrapperLayout.setVisibility(b ? View.GONE : View.VISIBLE);
                setupViewsRegardingUserType(b, listener);
                hideKeyboard();
            }
        });

        setupViewsRegardingUserType(false, listener);

        //Set text selection to the end
        mServerUrl.setSelection(mServerUrl.getText().length());

        disableSslSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    listener.onDisableSslSwitchClick();
                }
            }
        });
    }

    /**
     * Setup views regarding user type
     *
     * @param isGuestUser - Is guest user enabled
     * @param listener    - listener
     */
    private void setupViewsRegardingUserType(boolean isGuestUser,
                                             final ViewListener listener) {
        if (isGuestUser) {
            // guest user
            mServerUrl.setImeOptions(EditorInfo.IME_ACTION_DONE);
            mServerUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        listener.onGuestUserLoginButtonClick(
                                v.getText().toString().trim(), disableSslSwitch.isChecked());
                        return true;
                    }
                    return false;
                }
            });
            mLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onGuestUserLoginButtonClick(
                            mServerUrl.getText().toString().trim(),
                            disableSslSwitch.isChecked());
                }
            });
        } else {
            // not guest user
            mServerUrl.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            mServerUrl.setOnEditorActionListener(null);
            mLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onUserLoginButtonClick(
                            mServerUrl.getText().toString().trim(),
                            mUserName.getText().toString().trim(),
                            mPassword.getText().toString().trim(),
                            disableSslSwitch.isChecked());
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        mActivity.finish();
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
    public void unbindViews() {
        listener = null;
        mUnbinder.unbind();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showError(String errorMessage) {
        mServerUrlWrapperLayout.setError(errorMessage);
    }

    @Override
    public void hideError() {
        mServerUrlWrapperLayout.setError(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showServerUrlCanNotBeEmptyError() {
        setError(R.string.server_cannot_be_empty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showUserNameCanNotBeEmptyError() {
        setError(R.string.server_user_name_cannot_be_empty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showPasswordCanNotBeEmptyError() {
        setError(R.string.server_password_cannot_be_empty);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showCouldNotSaveUserError() {
        setError(R.string.error_save_account);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideKeyboard() {
        View view = mActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showUnauthorizedInfoDialog() {
        new MaterialDialog.Builder(mActivity)
                .titleColor(mWhiteColor)
                .title(R.string.info_unauthorized_dialog_title)
                .content(R.string.info_unauthorized_dialog_content)
                .widgetColor(mWhiteColor)
                .contentColor(mWhiteColor)
                .backgroundColor(mPrimaryColor)
                .positiveColor(mOrangeColor)
                .positiveText(R.string.dialog_ok_title)
                .linkColor(mOrangeColor)
                .show();
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
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        disableSslSwitch.setChecked(true);
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        disableSslSwitch.setChecked(false);
                        dialog.dismiss();
                    }
                })
                .canceledOnTouchOutside(false)
                .autoDismiss(false)
                .cancelable(false)
                .show();
    }

    @Override
    public void showNotSecureConnectionDialog(final boolean isGuest) {
        new MaterialDialog.Builder(mActivity)
                .titleColor(mWhiteColor)
                .title(R.string.warning_ssl_dialog_title)
                .content(R.string.server_not_secure_http)
                .widgetColor(mWhiteColor)
                .contentColor(mWhiteColor)
                .backgroundColor(mPrimaryColor)
                .positiveColor(mOrangeColor)
                .positiveText(R.string.dialog_ok_title)
                .negativeColor(mOrangeColor)
                .negativeText(R.string.warning_ssl_dialog_negative)
                .linkColor(mOrangeColor)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (listener != null) {
                            listener.onAcceptNotSecureConnectionClick(isGuest);
                        }
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (listener != null) {
                            listener.onCancelNotSecureConnectionClick();
                        }
                        dialog.dismiss();
                    }
                })
                .canceledOnTouchOutside(false)
                .autoDismiss(false)
                .cancelable(false)
                .show();
    }

    /**
     * Set error with string resource id
     *
     * @param errorMessage - Error message resource id
     */
    private void setError(@StringRes int errorMessage) {
        String errorMessageString = mActivity.getString(errorMessage);
        mServerUrlWrapperLayout.setError(errorMessageString);
    }
}
