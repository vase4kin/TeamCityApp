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

package com.github.vase4kin.teamcityapp.tests.view

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.tests.presenter.TestsPresenterImpl
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Fragment to manage build tests
 */
class TestOccurrencesFragment : Fragment() {

    @Inject
    lateinit var presenter: TestsPresenterImpl

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AndroidSupportInjection.inject(this)
        setHasOptionsMenu(true)
        presenter.onViewsCreated()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onViewsDestroyed()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        presenter.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        presenter.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return presenter.onOptionsItemSelected(item)
    }

    companion object {

        fun newInstance(url: String, passed: Int, failed: Int, ignored: Int): TestOccurrencesFragment {
            val fragment = TestOccurrencesFragment()
            val args = Bundle()
            args.putString(BundleExtractorValues.URL, url)
            args.putInt(BundleExtractorValues.PASSED_COUNT_PARAM, passed)
            args.putInt(BundleExtractorValues.FAILED_COUNT_PARAM, failed)
            args.putInt(BundleExtractorValues.IGNORED_COUNT_PARAM, ignored)
            fragment.arguments = args
            return fragment
        }
    }
}
