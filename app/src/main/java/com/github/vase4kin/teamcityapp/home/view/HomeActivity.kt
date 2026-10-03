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

package com.github.vase4kin.teamcityapp.home.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationInteractor
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationItem
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.drawer.view.DrawerTimeOut
import com.github.vase4kin.teamcityapp.home.presenter.HomePresenterImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import androidx.appcompat.app.AppCompatActivity
import teamcityapp.features.drawer.utils.DrawerActivityStartUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Provider
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    @Inject
    lateinit var presenterProvider: Provider<HomePresenterImpl>

    private var presenterResumed = false

    lateinit var presenter: HomePresenterImpl

    @Inject
    lateinit var sharedUserStorage: SharedUserStorage

    @Inject
    lateinit var appNavigationInteractor: AppNavigationInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = presenterProvider.get()
        setContentView(R.layout.activity_home)
        presenter.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
        presenterResumed = true
    }

    override fun onPause() {
        super.onPause()
        if (presenterResumed) {
            presenter.onPause()
            presenterResumed = false
        }
    }

    override fun onDestroy() {
        disposePresenter()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val bundle = intent.extras ?: return
        val isRequiredToReload = bundle.isRequiredToReload()
        if (isRequiredToReload) {
            val resumePresenter = presenterResumed
            reinitDeps()
            presenter.restartMatrix()
            if (resumePresenter) {
                presenter.onResume()
                presenterResumed = true
            }
        }
        val isTabSelected = bundle.isTabSelected()
        if (isTabSelected) {
            val tabToSelect = bundle.getSelectedTab()
            presenter.selectTab(tabToSelect)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        appNavigationInteractor.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    private fun reinitDeps() {
        disposePresenter()
        (this.applicationContext as TeamCityApplicationBase).buildRestApiInjectorWithBaseUrl(
            sharedUserStorage.activeUser.teamcityUrl
        )
        presenter = presenterProvider.get()
    }

    private fun disposePresenter() {
        if (presenterResumed) {
            presenter.onPause()
            presenterResumed = false
        }
        presenter.onDestroy()
    }

    companion object {

        const val ARG_TAB = "arg_tab"

        fun startForTheFirstStart(activity: Activity) {
            val intent = Intent(activity, HomeActivity::class.java)
            intent.putExtra(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED, true)
            activity.startActivity(intent)
        }

        fun startWhenNewAccountIsCreated(activity: Activity) {
            startWhenSwitchingAccountsFromDrawer(activity)
        }

        fun startWithTabSelected(activity: Activity, navigationItem: AppNavigationItem) {
            val launchIntent = Intent(activity, HomeActivity::class.java)
                .addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_SINGLE_TOP
                ).apply {
                    putExtra(ARG_TAB, navigationItem.ordinal)
                }
            activity.startActivity(launchIntent)
        }

        fun startWhenSwitchingAccountsFromDrawer(activity: Activity) {
            val launchIntent = Intent(activity, HomeActivity::class.java)
                .addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            launchIntent.putExtra(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, true)
            DrawerActivityStartUtils.startActivity(
                launchIntent,
                activity,
                DrawerTimeOut.DELAY_ON_CLOSE
            )
        }

        fun start(activity: Activity) {
            val intent = Intent(activity, HomeActivity::class.java)
            activity.startActivity(intent)
        }
    }
}

private fun Bundle.isRequiredToReload(): Boolean {
    return this.getBoolean(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, false)
}

private fun Bundle.isTabSelected(): Boolean {
    return this.containsKey(HomeActivity.ARG_TAB)
}

private fun Bundle.getSelectedTab(): AppNavigationItem {
    return AppNavigationItem.values()[this.getInt(HomeActivity.ARG_TAB, 0)]
}
