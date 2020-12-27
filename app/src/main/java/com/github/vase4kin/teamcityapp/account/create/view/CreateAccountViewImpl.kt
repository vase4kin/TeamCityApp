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

package com.github.vase4kin.teamcityapp.account.create.view

import android.app.Activity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.afollestad.materialdialogs.MaterialDialog
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.github.vase4kin.teamcityapp.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import teamcityapp.libraries.utils.getThemeColor

class CreateAccountViewImpl(private val activity: Activity) : CreateAccountView {

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.teamcity_url)
    lateinit var serverUrl: EditText

    @BindView(R.id.teamcity_url_wrapper)
    lateinit var urlInputLayout: TextInputLayout

    @BindView(R.id.user_field_wrapper)
    lateinit var userNameInputLayout: TextInputLayout

    @BindView(R.id.password_field_wrapper)
    lateinit var passwordInputLayout: TextInputLayout

    @BindView(R.id.user_name)
    lateinit var userName: EditText

    @BindView(R.id.password)
    lateinit var password: EditText

    @BindView(R.id.guest_user_switch)
    lateinit var guestUserSwitch: SwitchMaterial

    @BindView(R.id.disable_ssl_switch)
    lateinit var disableSslSwitch: SwitchMaterial

    @BindView(R.id.btn_login)
    lateinit var fab: ExtendedFloatingActionButton

    private lateinit var unbinder: Unbinder
    private lateinit var discardDialog: MaterialDialog

    /**
     * {@inheritDoc}
     */
    override val isEmailEmpty: Boolean
        get() = TextUtils.isEmpty(serverUrl.text)

    /**
     * {@inheritDoc}
     */
    override fun initViews(listener: CreateAccountView.ViewListener) {
        unbinder = ButterKnife.bind(this, activity)
        initDialogs()
        initToolbar(listener)

        password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                listener.validateUserData(
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
            userNameInputLayout.visibility = if (b) View.GONE else View.VISIBLE
            password.visibility = if (b) View.GONE else View.VISIBLE
            passwordInputLayout.visibility = if (b) View.GONE else View.VISIBLE
            setupViewsRegardingUserType(b, listener)
        }

        setupViewsRegardingUserType(false, listener)

        // Set text selection to the end
        serverUrl.setSelection(serverUrl.text.length)

        disableSslSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                listener.onDisableSslSwitchClick()
            }
        }

        (activity as AppCompatActivity).bindProgressButton(fab)
        fab.attachTextChangeAnimator()
    }

    /**
     * Setup views regarding user type
     *
     * @param isGuestUser - Is guest user enabled
     * @param listener - listener
     */
    private fun setupViewsRegardingUserType(isGuestUser: Boolean, listener: OnValidateListener) {
        if (isGuestUser) {
            // guest user
            serverUrl.imeOptions = EditorInfo.IME_ACTION_DONE
            serverUrl.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (guestUserSwitch.isChecked) {
                        listener.validateGuestUserData(
                            serverUrl.text.toString().trim { it <= ' ' },
                            disableSslSwitch.isChecked
                        )
                    } else {
                        listener.validateUserData(
                            serverUrl.text.toString().trim { it <= ' ' },
                            userName.text.toString().trim { it <= ' ' },
                            password.text.toString().trim { it <= ' ' },
                            disableSslSwitch.isChecked
                        )
                    }
                }
                false
            }
        } else {
            // not guest user
            serverUrl.imeOptions = EditorInfo.IME_ACTION_NEXT
            serverUrl.setOnEditorActionListener(null)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun showError(errorMessage: String) {
        urlInputLayout.error = errorMessage
    }

    /**
     * {@inheritDoc}
     */
    override fun showServerUrlCanNotBeEmptyError() {
        showError(activity.getString(R.string.server_cannot_be_empty))
    }

    /**
     * {@inheritDoc}
     */
    override fun showUserNameCanNotBeEmptyError() {
        showError(activity.getString(R.string.server_user_name_cannot_be_empty))
    }

    /**
     * {@inheritDoc}
     */
    override fun showPasswordCanNotBeEmptyError() {
        showError(activity.getString(R.string.server_password_cannot_be_empty))
    }

    /**
     * {@inheritDoc}
     */
    override fun showCouldNotSaveUserError() {
        showError(activity.getString(R.string.error_save_account))
    }

    /**
     * {@inheritDoc}
     */
    override fun hideError() {
        urlInputLayout.error = null
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroyView() {
        unbinder.unbind()
    }

    /**
     * {@inheritDoc}
     */
    override fun showProgressDialog() {
        fab.showProgress {
            buttonTextRes = R.string.adding_new_account_dialog_title
            progressColor = activity.getThemeColor(R.attr.colorOnPrimary)
        }
        fab.isEnabled = false
    }

    /**
     * {@inheritDoc}
     */
    override fun dismissProgressDialog() {
        fab.hideProgress(R.string.add_new_account_dialog_title)
        fab.isEnabled = true
    }

    /**
     * {@inheritDoc}
     */
    override fun showDiscardDialog() {
        discardDialog.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun finish() {
        activity.finish()
        activity.overridePendingTransition(R.anim.hold, R.anim.slide_out_bottom)
    }

    /**
     * {@inheritDoc}
     */
    override fun showNewAccountExistErrorMessage() {
        showError(activity.resources.getString(R.string.add_new_account_dialog_account_exist_error_message))
    }

    /**
     * Init toolbar
     *
     * @param listener - Listener to receive callbacks
     */
    private fun initToolbar(listener: CreateAccountView.ViewListener) {
        toolbar.setTitle(R.string.add_new_account_dialog_title)
        // For ui testing purpose
        toolbar.setNavigationContentDescription(R.string.navigate_up)
        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        toolbar.setNavigationOnClickListener(OnToolBarNavigationListenerImpl(listener))

        fab.setOnClickListener(
            OnCreateMenuItemClickListenerImpl(
                listener,
                serverUrl,
                userName,
                password,
                guestUserSwitch,
                disableSslSwitch
            )
        )
    }

    /**
     * Init dialogs
     */
    private fun initDialogs() {
        discardDialog = MaterialDialog.Builder(activity)
            .content(R.string.discard_dialog_content)
            .positiveText(R.string.discard_dialog_positive_button_text)
            .callback(object : MaterialDialog.ButtonCallback() {
                override fun onPositive(dialog: MaterialDialog?) {
                    finish()
                }
            })
            .negativeText(R.string.discard_dialog_negative_button_text)
            .build()
    }

    /**
     * {@inheritDoc}
     */
    override fun showDisableSslWarningDialog() {
        MaterialDialog.Builder(activity)
            .title(R.string.warning_ssl_dialog_title)
            .content(R.string.warning_ssl_dialog_content)
            .positiveText(R.string.dialog_ok_title)
            .negativeText(R.string.warning_ssl_dialog_negative)
            .onPositive { dialog, which ->
                disableSslSwitch.isChecked = true
                dialog.dismiss()
            }
            .onNegative { dialog, which ->
                disableSslSwitch.isChecked = false
                dialog.dismiss()
            }
            .canceledOnTouchOutside(false)
            .autoDismiss(false)
            .cancelable(false)
            .show()
    }
}
