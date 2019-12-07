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

package com.github.vase4kin.teamcityapp.login.view

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Switch
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import butterknife.BindColor
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.afollestad.materialdialogs.MaterialDialog
import com.github.vase4kin.teamcityapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class LoginViewImpl(private val activity: Activity) : LoginView {

    @JvmField
    @BindColor(R.color.white)
    var whiteColor: Int = 0
    @JvmField
    @BindColor(R.color.colorPrimary)
    var primaryColor: Int = 0
    @JvmField
    @BindColor(R.color.login_text_error_color)
    var orangeColor: Int = 0
    @BindView(R.id.teamcity_url)
    lateinit var serverUrl: EditText
    @BindView(R.id.teamcity_url_wrapper)
    lateinit var serverUrlWrapperLayout: TextInputLayout
    @BindView(R.id.user_field_wrapper)
    lateinit var userNameWrapperLayout: TextInputLayout
    @BindView(R.id.user_name)
    lateinit var userName: EditText
    @BindView(R.id.password_field_wrapper)
    lateinit var passwordWrapperLayout: TextInputLayout
    @BindView(R.id.password)
    lateinit var password: EditText
    @BindView(R.id.guest_user_switch)
    lateinit var guestUserSwitch: Switch
    @BindView(R.id.disable_ssl_switch)
    lateinit var disableSslSwitch: Switch
    @BindView(R.id.btn_login)
    lateinit var loginButton: Button
    @BindView(R.id.give_it_a_try_view)
    lateinit var tryItOutTextView: View
    @BindView(R.id.give_it_a_try_progress)
    lateinit var progressBar: ProgressBar
    @BindView(R.id.btn_try_it_out)
    lateinit var tryItOutTextButton: MaterialButton

    private lateinit var unbinder: Unbinder
    private lateinit var progressDialog: MaterialDialog

    private var listener: LoginView.ViewListener? = null

    /**
     * {@inheritDoc}
     */
    override fun initViews(listener: LoginView.ViewListener) {
        unbinder = ButterKnife.bind(this, activity)

        this.listener = listener

        progressDialog = MaterialDialog.Builder(activity)
            .content(R.string.text_progress_bar_loading)
            .progress(true, 0)
            .widgetColor(whiteColor)
            .contentColor(whiteColor)
            .autoDismiss(false)
            .backgroundColor(primaryColor)
            .build()
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)

        password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                listener.onUserLoginButtonClick(
                    serverUrl.text.toString().trim { it <= ' ' },
                    userName.text.toString().trim { it <= ' ' },
                    password.text.toString().trim { it <= ' ' },
                    disableSslSwitch.isChecked
                )
            }
            true
        }

        guestUserSwitch.setOnCheckedChangeListener { _, b ->
            userName.visibility = if (b) View.GONE else View.VISIBLE
            userNameWrapperLayout.visibility = if (b) View.GONE else View.VISIBLE
            password.visibility = if (b) View.GONE else View.VISIBLE
            passwordWrapperLayout.visibility = if (b) View.GONE else View.VISIBLE
            setupViewsRegardingUserType(b, listener)
            hideKeyboard()
        }

        setupViewsRegardingUserType(false, listener)

        // Set text selection to the end
        serverUrl.setSelection(serverUrl.text.length)

        disableSslSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                listener.onDisableSslSwitchClick()
            }
        }

        tryItOutTextButton.setOnClickListener { listener.onTryItOutTextClick() }
    }

    /**
     * Setup views regarding user type
     *
     * @param isGuestUser - Is guest user enabled
     * @param listener - listener
     */
    private fun setupViewsRegardingUserType(
        isGuestUser: Boolean,
        listener: LoginView.ViewListener
    ) {
        if (isGuestUser) {
            // guest user
            serverUrl.imeOptions = EditorInfo.IME_ACTION_DONE
            serverUrl.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    listener.onGuestUserLoginButtonClick(
                        v.text.toString().trim { it <= ' ' }, disableSslSwitch.isChecked
                    )
                }
                true
            }
            loginButton.setOnClickListener {
                listener.onGuestUserLoginButtonClick(
                    serverUrl.text.toString().trim { it <= ' ' },
                    disableSslSwitch.isChecked
                )
            }
        } else {
            // not guest user
            serverUrl.imeOptions = EditorInfo.IME_ACTION_NEXT
            serverUrl.setOnEditorActionListener(null)
            loginButton.setOnClickListener {
                listener.onUserLoginButtonClick(
                    serverUrl.text.toString().trim { it <= ' ' },
                    userName.text.toString().trim { it <= ' ' },
                    password.text.toString().trim { it <= ' ' },
                    disableSslSwitch.isChecked
                )
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun close() {
        activity.finish()
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
    override fun unbindViews() {
        listener = null
        dismissAllDialogsOnDestroy()
        unbinder.unbind()
    }

    /**
     * {@inheritDoc}
     */
    override fun showError(errorMessage: String) {
        serverUrlWrapperLayout.error = errorMessage
    }

    override fun hideError() {
        serverUrlWrapperLayout.error = null
    }

    /**
     * {@inheritDoc}
     */
    override fun showServerUrlCanNotBeEmptyError() {
        setError(R.string.server_cannot_be_empty)
    }

    /**
     * {@inheritDoc}
     */
    override fun showUserNameCanNotBeEmptyError() {
        setError(R.string.server_user_name_cannot_be_empty)
    }

    /**
     * {@inheritDoc}
     */
    override fun showPasswordCanNotBeEmptyError() {
        setError(R.string.server_password_cannot_be_empty)
    }

    /**
     * {@inheritDoc}
     */
    override fun showCouldNotSaveUserError() {
        setError(R.string.error_save_account)
    }

    /**
     * {@inheritDoc}
     */
    override fun hideKeyboard() {
        val view = activity.currentFocus
        if (view != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun showUnauthorizedInfoDialog() {
        MaterialDialog.Builder(activity)
            .titleColor(whiteColor)
            .title(R.string.info_unauthorized_dialog_title)
            .content(R.string.info_unauthorized_dialog_content)
            .widgetColor(whiteColor)
            .contentColor(whiteColor)
            .backgroundColor(primaryColor)
            .positiveColor(orangeColor)
            .positiveText(R.string.dialog_ok_title)
            .linkColor(orangeColor)
            .show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showTryItOutDialog(url: String) {
        val content = activity.getString(R.string.info_try_it_out_dialog_content, url)
        val formattedContent = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_COMPACT)
        MaterialDialog.Builder(activity)
            .titleColor(whiteColor)
            .title(R.string.info_try_it_out_title)
            .content(formattedContent)
            .widgetColor(whiteColor)
            .contentColor(whiteColor)
            .backgroundColor(primaryColor)
            .positiveColor(orangeColor)
            .positiveText(R.string.dialog_try_it_out_title)
            .linkColor(orangeColor)
            .negativeColor(orangeColor)
            .negativeText(R.string.warning_ssl_dialog_negative)
            .onPositive { dialog, _ ->
                listener?.onTryItOutActionClick()
                dialog.dismiss()
            }
            .onNegative { dialog, _ ->
                listener?.onDeclineTryItOutActionClick()
                dialog.dismiss()
            }
            .canceledOnTouchOutside(false)
            .autoDismiss(false)
            .cancelable(false)
            .show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showDisableSslWarningDialog() {
        MaterialDialog.Builder(activity)
            .titleColor(whiteColor)
            .title(R.string.warning_ssl_dialog_title)
            .content(R.string.warning_ssl_dialog_content)
            .widgetColor(whiteColor)
            .contentColor(whiteColor)
            .backgroundColor(primaryColor)
            .positiveColor(orangeColor)
            .positiveText(R.string.dialog_ok_title)
            .negativeColor(orangeColor)
            .negativeText(R.string.warning_ssl_dialog_negative)
            .linkColor(orangeColor)
            .onPositive { dialog, _ ->
                disableSslSwitch.isChecked = true
                dialog.dismiss()
            }
            .onNegative { dialog, _ ->
                disableSslSwitch.isChecked = false
                dialog.dismiss()
            }
            .canceledOnTouchOutside(false)
            .autoDismiss(false)
            .cancelable(false)
            .show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showNotSecureConnectionDialog(isGuest: Boolean) {
        MaterialDialog.Builder(activity)
            .titleColor(whiteColor)
            .title(R.string.warning_ssl_dialog_title)
            .content(R.string.server_not_secure_http)
            .widgetColor(whiteColor)
            .contentColor(whiteColor)
            .backgroundColor(primaryColor)
            .positiveColor(orangeColor)
            .positiveText(R.string.dialog_ok_title)
            .negativeColor(orangeColor)
            .negativeText(R.string.warning_ssl_dialog_negative)
            .linkColor(orangeColor)
            .onPositive { dialog, _ ->
                listener?.onAcceptNotSecureConnectionClick(isGuest)
                dialog.dismiss()
            }
            .onNegative { dialog, _ ->
                listener?.onCancelNotSecureConnectionClick()
                dialog.dismiss()
            }
            .canceledOnTouchOutside(false)
            .autoDismiss(false)
            .cancelable(false)
            .show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showTryItOutLoading() {
        progressBar.visibility = View.VISIBLE
    }

    /**
     * {@inheritDoc}
     */
    override fun hideTryItOutLoading() {
        if (::progressBar.isInitialized) {
            progressBar.visibility = View.GONE
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun showTryItOut() {
        if (::tryItOutTextView.isInitialized) {
            tryItOutTextView.visibility = View.VISIBLE
        }
    }

    /**
     * Set error with string resource id
     *
     * @param errorMessage - Error message resource id
     */
    private fun setError(@StringRes errorMessage: Int) {
        val errorMessageString = activity.getString(errorMessage)
        serverUrlWrapperLayout.error = errorMessageString
    }

    /**
     * Dismiss all dialogs on destroy
     */
    private fun dismissAllDialogsOnDestroy() {
        if (::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
}
