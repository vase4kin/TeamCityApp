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

package teamcityapp.features.change.view

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.parcel.Parcelize
import teamcityapp.features.change.R
import teamcityapp.features.change.databinding.ActivityChangeBinding
import teamcityapp.features.change.viewmodel.ChangeViewModel
import javax.inject.Inject

const val ARG_BUNDLE_DATA = "ARG_BUNDLE_DATA"

class ChangeActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModel: ChangeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityChangeBinding>(
            this,
            R.layout.activity_change
        )
            .apply {
                viewmodel = this@ChangeActivity.viewModel
                lifecycle.addObserver(this@ChangeActivity.viewModel)
            }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.hold, R.anim.slide_out_bottom)
    }

    @Parcelize
    data class ChangeFile(
        val fileName: String,
        val type: String
    ) : Parcelable

    companion object {

        @JvmStatic
        fun start(
            activity: AppCompatActivity,
            commitName: String,
            userName: String,
            date: String,
            changeFileNames: List<Pair<String, String>>,
            version: String,
            webUrl: String,
            changeId: String
        ) {
            val intent = Intent(activity, ChangeActivity::class.java)
            intent.putExtra(
                ARG_BUNDLE_DATA,
                BundleData(
                    id = changeId,
                    commitName = commitName,
                    userName = userName,
                    date = date,
                    changeFileNames = changeFileNames.map {
                        ChangeFile(
                            fileName = it.first,
                            type = it.second
                        )
                    },
                    version = version,
                    webUrl = webUrl
                )
            )
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold)
        }

        @Parcelize
        data class BundleData(
            val id: String,
            val commitName: String,
            val userName: String,
            val date: String,
            val changeFileNames: List<ChangeFile>,
            val version: String,
            val webUrl: String
        ) : Parcelable
    }
}
