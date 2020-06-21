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

package teamcityapp.features.manage_accounts.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.afollestad.materialdialogs.MaterialDialog
import dagger.android.support.DaggerAppCompatActivity
import teamcityapp.features.manage_accounts.R
import teamcityapp.features.manage_accounts.databinding.ActivityManageAccountsBinding
import teamcityapp.features.manage_accounts.viewmodel.ManageAccountsViewModel
import teamcityapp.libraries.utils.initToolbar
import javax.inject.Inject

/**
 * Manages account list
 */
class ManageAccountsActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModel: ManageAccountsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityManageAccountsBinding>(
            this,
            R.layout.activity_manage_accounts
        ).apply {
            viewmodel = this@ManageAccountsActivity.viewModel
        }
        lifecycle.addObserver(viewModel)
        initToolbar()
    }

    /**
     * Show account ssl disabled dialog
     */
    fun showSslDisabledInfoDialog() {
        MaterialDialog.Builder(this)
            .title(R.string.warning_ssl_dialog_title)
            .content(R.string.warning_ssl_dialog_content)
            .positiveText(R.string.dialog_ok_title)
            .callback(object : MaterialDialog.ButtonCallback() {
                override fun onPositive(dialog: MaterialDialog?) {
                    dialog?.dismiss()
                }
            })
            .build()
            .show()
    }

    /**
     * Show account remove dialog
     */
    fun showRemoveAccountDialog(onAccountRemove: () -> Unit) {
        MaterialDialog.Builder(this)
            .content(R.string.dialog_remove_not_active_account_positive_content_text)
            .positiveText(R.string.dialog_remove_active_account_positive_button_text)
            .callback(object : MaterialDialog.ButtonCallback() {
                override fun onPositive(dialog: MaterialDialog?) {
                    onAccountRemove()
                }
            })
            .negativeText(R.string.dialog_remove_active_account_positive_negative_text)
            .build()
            .show()
    }

    companion object {

        /**
         * Start account list activity
         *
         * @param activity - Activity context
         */
        fun start(activity: Activity) {
            val launchIntent = Intent(activity, ManageAccountsActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            activity.startActivity(launchIntent)
        }
    }
}
