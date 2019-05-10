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

package com.github.vase4kin.teamcityapp.snapshot_dependencies.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.snapshot_dependencies.presenter.SnapshotDependenciesPresenterImpl
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Snapshot dependencies build lust fragment
 */
class SnapshotDependenciesFragment : Fragment() {

    @Inject
    lateinit var presenter: SnapshotDependenciesPresenterImpl

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_snapshot_dependencies, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AndroidSupportInjection.inject(this)
        presenter.onViewsCreated()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onViewsDestroyed()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    companion object {

        fun newInstance(id: String): SnapshotDependenciesFragment {
            val fragment = SnapshotDependenciesFragment()
            val args = Bundle()
            args.putString(BundleExtractorValues.ID, id)
            fragment.arguments = args
            return fragment
        }
    }
}
