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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatActivity
import teamcityapp.features.manage_accounts.R
import teamcityapp.features.manage_accounts.databinding.ActivityManageAccountsBinding
import teamcityapp.features.manage_accounts.stateholder.ManageAccountsStateHolder
import teamcityapp.libraries.utils.initToolbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Manages account list
 */
@AndroidEntryPoint
class ManageAccountsActivity : AppCompatActivity() {

    @Inject
    lateinit var stateHolder: ManageAccountsStateHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityManageAccountsBinding>(
            this,
            R.layout.activity_manage_accounts
        ).apply {
            stateHolder = this@ManageAccountsActivity.stateHolder
        }
        lifecycle.addObserver(stateHolder)
        initToolbar()
    }

    /**
     * Show account ssl disabled dialog
     */
    fun showSslDisabledInfoDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.warning_ssl_dialog_title)
            .setMessage(R.string.warning_ssl_dialog_content)
            .setPositiveButton(R.string.dialog_ok_title, null)
            .show()
    }

    /**
     * Show account remove dialog
     */
    fun showRemoveAccountDialog(onAccountRemove: () -> Unit) {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.dialog_remove_not_active_account_positive_content_text)
            .setPositiveButton(R.string.dialog_remove_active_account_positive_button_text) { _, _ ->
                onAccountRemove()
            }
            .setNegativeButton(R.string.dialog_remove_active_account_positive_negative_text, null)
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
