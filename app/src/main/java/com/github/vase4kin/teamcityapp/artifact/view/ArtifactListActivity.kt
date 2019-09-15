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

package com.github.vase4kin.teamcityapp.artifact.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.artifact.extractor.ArtifactValueExtractor
import com.github.vase4kin.teamcityapp.artifact.presenter.ArtifactPresenterImpl
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.utils.initToolbar
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class ArtifactListActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var valueExtractor: ArtifactValueExtractor
    @Inject
    lateinit var presenter: ArtifactPresenterImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artifact_list)
        initToolbar()
        setTitle()
        presenter.onViewsCreated()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onViewsDestroyed()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setTitle() {
        val actionBar = supportActionBar ?: return
        actionBar.title = valueExtractor.name
    }

    companion object {

        fun start(
            artifactName: String,
            build: Build,
            url: String,
            activity: Activity
        ) {
            val bundle = Bundle()
            bundle.putSerializable(BundleExtractorValues.BUILD, build)
            bundle.putString(BundleExtractorValues.URL, url)
            bundle.putString(BundleExtractorValues.NAME, artifactName)
            val intent = Intent(activity, ArtifactListActivity::class.java)
            intent.putExtras(bundle)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
        }
    }
}
