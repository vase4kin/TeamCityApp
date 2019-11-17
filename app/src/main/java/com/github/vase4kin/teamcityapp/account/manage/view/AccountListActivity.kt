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
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.account.manage.viewmodel.ManageAccountsViewModel
import com.github.vase4kin.teamcityapp.databinding.ActivityAccountListBinding
import dagger.android.support.DaggerAppCompatActivity
import teamcityapp.libraries.utils.initToolbar
import javax.inject.Inject

/**
 * Manages account list
 */
class AccountListActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModel: ManageAccountsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityAccountListBinding>(
            this,
            R.layout.activity_account_list
        ).apply {
            val injectedViewModel = viewModel
            viewmodel = injectedViewModel
            lifecycle.addObserver(injectedViewModel)
        }
        initToolbar()
    }

    companion object {

        /**
         * Start account list activity
         *
         * @param activity - Activity context
         */
        fun start(activity: Activity) {
            val launchIntent = Intent(activity, AccountListActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            activity.startActivity(launchIntent)
        }
    }
}
