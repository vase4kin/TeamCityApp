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

package com.github.vase4kin.teamcityapp.account.create.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.account.create.presenter.CreateAccountPresenterImpl
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Create account activity
 */
class CreateAccountActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var presenter: CreateAccountPresenterImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        presenter.handleOnCreateView()
    }

    override fun onDestroy() {
        presenter.handleOnDestroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        presenter.handleOnResume()
    }

    override fun onBackPressed() {
        presenter.finish()
    }

    companion object {

        fun start(activity: Activity) {
            val intent = Intent(activity, CreateAccountActivity::class.java)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold)
        }
    }
}
