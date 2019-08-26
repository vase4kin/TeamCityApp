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

package com.github.vase4kin.teamcityapp.account.manage.view

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.annotation.StringRes
import butterknife.BindView
import butterknife.OnClick
import com.afollestad.materialdialogs.MaterialDialog
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity
import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.storage.api.UserAccount
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.joanzapata.iconify.IconDrawable
import com.joanzapata.iconify.fonts.MaterialIcons

/**
 * View impl for account
 *
 *
 * TODO: move all logic to presenter!
 */
class AccountsViewImpl(
    view: View,
    activity: Activity,
    @StringRes emptyMessage: Int,
    adapter: SimpleSectionedRecyclerViewAdapter<AccountAdapter>
) : BaseListViewImpl<AccountDataModel, SimpleSectionedRecyclerViewAdapter<AccountAdapter>>(
    view,
    activity,
    emptyMessage,
    adapter
), AccountsView {

    @BindView(R.id.floating_action_button)
    lateinit var floatingActionButton: FloatingActionButton
    private lateinit var listener: AccountsView.ViewListener
    private lateinit var dataModel: AccountDataModel

    @OnClick(R.id.floating_action_button)
    fun onClick() {
        showCreateNewAccountDialog()
    }

    /**
     * {@inheritDoc}
     */
    override fun setOnViewListener(listener: AccountsView.ViewListener) {
        this.listener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun initViews(listener: BaseListView.ViewListener) {
        super.initViews(listener)
        // Setting float button icon
        floatingActionButton.setImageDrawable(IconDrawable(activity, MaterialIcons.md_add).color(Color.WHITE))
    }

    /**
     * {@inheritDoc}
     */
    override fun showData(dataModel: AccountDataModel) {
        this.dataModel = dataModel
        dataModel.sort()
        val baseAdapter = adapter.baseAdapter
        baseAdapter.dataModel = dataModel
        baseAdapter.setListener(listener)
        recyclerView.adapter = adapter
    }

    /**
     * {@inheritDoc}
     */
    override fun showRemoveAccountDialog(account: UserAccount, isLastAccount: Boolean) {
        MaterialDialog.Builder(activity)
            .content(R.string.dialog_remove_not_active_account_positive_content_text)
            .positiveText(R.string.dialog_remove_active_account_positive_button_text)
            .callback(object : MaterialDialog.ButtonCallback() {
                override fun onPositive(dialog: MaterialDialog?) {
                    if (isLastAccount) {
                        listener.onLastAccountRemoved(account)
                        return
                    }
                    if (account.isActive) {
                        listener.onActiveAccountRemoved(account)
                    } else {
                        listener.onNotActiveAccountRemoved(account)
                    }
                }
            })
            .negativeText(R.string.dialog_remove_active_account_positive_negative_text)
            .build()
            .show()
    }

    /**
     * {@inheritDoc}
     */
    override fun removeAccount(userAccount: UserAccount) {
        dataModel.remove(userAccount)
        recyclerView.adapter?.notifyDataSetChanged()
    }

    /**
     * Show create new account dialog
     *
     *
     * TODO: move to the router
     */
    private fun showCreateNewAccountDialog() {
        activity.startActivity(Intent(activity, CreateAccountActivity::class.java))
        activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold)
    }

    /**
     * {@inheritDoc}
     */
    override fun recyclerViewId(): Int {
        return R.id.account_recycler_view
    }
}
