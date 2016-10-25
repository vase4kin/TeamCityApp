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
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class LoginViewImpl implements LoginView {

    /**
     * Animation startup delay
     */
    private static final int STARTUP_DELAY = 300;

    /**
     * Animation delay for logo
     */
    private static final int ANIM_ITEM_DURATION = 1000;

    /**
     * Animation delay for small items such as buttons
     */
    private static final int ITEM_DELAY = 300;

    @BindColor(R.color.md_white_1000)
    int mWhiteColor;

    @BindColor(R.color.colorPrimary)
    int mPrimaryColor;

    @BindColor(R.color.login_text_error_color)
    int mOrangeColor;

    @BindView(R.id.img_logo)
    ImageView mLogoImageView;

    @BindView(R.id.container)
    ViewGroup mContainer;

    @BindView(R.id.teamcity_url)
    EditText mServerUrl;

    @BindView(R.id.teamcity_url_wrapper)
    TextInputLayout mServerUrlWrapperLayout;

    @BindView(R.id.user_name)
    EditText mUserName;

    @BindView(R.id.password)
    EditText mPassword;

    @BindView(R.id.guest_user_switch)
    Switch mGuestUserSwitch;

    @BindView(R.id.btn_login)
    Button mLoginButton;

    private Unbinder mUnbinder;

    private Activity mActivity;
    private MaterialDialog mProgressDialog;

    public LoginViewImpl(Activity mActivity) {
        this.mActivity = mActivity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(final OnLoginButtonClickListener listener) {
        mUnbinder = ButterKnife.bind(this, mActivity);

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
                            mPassword.getText().toString().trim());
                    return true;
                }
                return false;
            }
        });

        mGuestUserSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mUserName.setVisibility(b ? View.GONE : View.VISIBLE);
                mPassword.setVisibility(b ? View.GONE : View.VISIBLE);
                setupViewsRegardingUserType(b, listener);
                hideKeyboard();
            }
        });

        setupViewsRegardingUserType(false, listener);

        //Set text selection to the end
        mServerUrl.setSelection(mServerUrl.getText().length());
    }

    /**
     * Setup views regarding user type
     *
     * @param isGuestUser - Is guest user enabled
     * @param listener    - listener
     */
    private void setupViewsRegardingUserType(boolean isGuestUser, final OnLoginButtonClickListener listener) {
        if (isGuestUser) {
            // guest user
            mServerUrl.setImeOptions(EditorInfo.IME_ACTION_DONE);
            mServerUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        listener.onGuestUserLoginButtonClick(v.getText().toString().trim());
                        return true;
                    }
                    return false;
                }
            });
            mLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onGuestUserLoginButtonClick(
                            mServerUrl.getText().toString().trim());
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
                            mPassword.getText().toString().trim());
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
    public void animate() {
        ViewCompat.animate(mLogoImageView)
                .translationY(mActivity.getResources().getInteger(R.integer.logo_move))
                .setStartDelay(STARTUP_DELAY)
                .setDuration(ANIM_ITEM_DURATION).setInterpolator(
                new DecelerateInterpolator(1.2f)).start();

        for (int i = 0; i < mContainer.getChildCount(); i++) {
            View v = mContainer.getChildAt(i);
            ViewPropertyAnimatorCompat viewAnimator = ViewCompat.animate(v)
                    .translationY(0).alpha(1)
                    .setStartDelay((ITEM_DELAY * i) + 500)
                    .setDuration(1000);

            viewAnimator.setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindViews() {
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
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            return;
        }

        animate();
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
     * Set error with string resource id
     *
     * @param errorMessage - Error message resource id
     */
    private void setError(@StringRes int errorMessage) {
        String errorMessageString = mActivity.getString(errorMessage);
        mServerUrlWrapperLayout.setError(errorMessageString);
    }
}
