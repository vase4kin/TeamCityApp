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

package com.github.vase4kin.teamcityapp.buildlog.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.buildlog.viewmodel.BuildLogViewModel
import com.github.vase4kin.teamcityapp.databinding.FragmentBuildLogBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Build log fragment
 */
class BuildLogFragment : DaggerFragment() {

    @Inject
    lateinit var viewModel: BuildLogViewModel

    @Inject
    lateinit var webClient: BuildLogWebViewClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return DataBindingUtil.inflate<FragmentBuildLogBinding>(
            inflater,
            R.layout.fragment_build_log,
            container,
            false
        ).apply {
            viewmodel = this@BuildLogFragment.viewModel
        }.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewLifecycleOwnerLiveData.observe(this) { viewLifecycleOwner ->
            viewLifecycleOwner.lifecycle.run {
                addObserver(viewModel)
                addObserver(webClient)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun initWebView() {
        view?.findViewById<WebView>(R.id.web_view)?.let {
            it.settings.apply {
                javaScriptEnabled = true
                layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
                builtInZoomControls = true
                displayZoomControls = false
            }
            it.webViewClient = this@BuildLogFragment.webClient
        }
    }

    fun loadUrl(url: String) {
        view?.findViewById<WebView>(R.id.web_view)?.loadUrl(url)
    }

    fun evaluateJs(script: String) {
        view?.findViewById<WebView>(R.id.web_view)?.evaluateJavascript(script, null)
    }

    companion object {
        fun newInstance(buildId: String): BuildLogFragment {
            val fragment = BuildLogFragment()
            val args = Bundle()
            args.putString(BundleExtractorValues.BUILD_ID, buildId)
            fragment.arguments = args
            return fragment
        }
    }
}
