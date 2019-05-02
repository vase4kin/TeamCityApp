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

package com.github.vase4kin.teamcityapp.overview.view


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.overview.presenter.OverviewPresenterImpl
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Fragment to handle Build overview screen
 */
class OverviewFragment : Fragment() {

    @Inject
    lateinit var presenter: OverviewPresenterImpl

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_overview_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AndroidSupportInjection.inject(this)
        setHasOptionsMenu(true)
        presenter.onCreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        presenter.onStop()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        presenter.onCreateOptionsMenu(menu!!, inflater!!)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return presenter.onOptionsItemSelected(item!!)
    }

    companion object {

        fun newInstance(build: Build): OverviewFragment {
            val fragment = OverviewFragment()
            val args = Bundle()
            args.putSerializable(BundleExtractorValues.BUILD, build)
            fragment.arguments = args
            return fragment
        }
    }
}
