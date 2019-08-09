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

package com.github.vase4kin.teamcityapp.buildlist.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter
import com.github.vase4kin.teamcityapp.buildlist.presenter.BuildListPresenterImpl
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataManager
import com.github.vase4kin.teamcityapp.drawer.presenter.DrawerPresenterImpl
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter
import com.github.vase4kin.teamcityapp.drawer.tracker.DrawerTracker
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView
import com.github.vase4kin.teamcityapp.filter_builds.router.FilterBuildsRouter
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouter
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildActivity
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Activity to manage build list
 */
class BuildListActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var drawerPresenter: DrawerPresenterImpl<DrawerView, DrawerDataManager, DrawerRouter, DrawerTracker>
    @Inject
    lateinit var presenter: BuildListPresenterImpl<BuildListView, BuildListDataManager>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_build_list)
        drawerPresenter.onCreate()
        presenter.onViewsCreated()
    }

    public override fun onDestroy() {
        presenter.onViewsDestroyed()
        drawerPresenter.onDestroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onBackPressed() {
        drawerPresenter.onBackButtonPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Move that logic to presenter
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == RunBuildActivity.REQUEST_CODE) {
            presenter.onRunBuildActivityResult(data!!.getStringExtra(RunBuildRouter.EXTRA_HREF))
        } else if (requestCode == FilterBuildsActivity.REQUEST_CODE) {
            presenter.onFilterBuildsActivityResult(data!!.getSerializableExtra(FilterBuildsRouter.EXTRA_FILTER) as BuildListFilter)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        presenter.onCreateOptionsMenu(menu, menuInflater)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return presenter.onOptionsItemSelected(item)
    }

    companion object {

        /**
         * Start build list activity
         *
         * @param name     - Build type name
         * @param id       - Build type id
         * @param activity - Activity context
         */
        fun start(name: String, id: String, filter: BuildListFilter?, activity: Activity) {
            val bundle = Bundle()
            bundle.putString(BundleExtractorValues.NAME, name)
            val intent = Intent(activity, BuildListActivity::class.java)
            bundle.putString(BundleExtractorValues.ID, id)
            bundle.putSerializable(BundleExtractorValues.BUILD_LIST_FILTER, filter)
            intent.putExtras(bundle)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
        }
    }
}