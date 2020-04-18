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

package com.github.vase4kin.teamcityapp.app_navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.vase4kin.teamcityapp.R
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavTransactionOptions

const val ARG_SELECTED_TAB = "arg_selected_tab"

interface AppNavigationInteractor {
    fun initNavigation(savedInstanceState: Bundle?)
    fun onSaveInstanceState(outState: Bundle?)
    fun switchTab(index: Int)
}

class AppNavigationInteractorImpl constructor(
    private val fragmentManager: FragmentManager,
    private val fragmentFactory: FragmentFactory
) : AppNavigationInteractor, FragNavController.RootFragmentListener {

    override val numberOfRootFragments: Int = fragmentFactory.getSize()

    override fun getRootFragment(index: Int): Fragment = fragmentFactory.createFragment(index)

    private val defaultTransition = FragNavTransactionOptions.newBuilder()
        .allowStateLoss(true)
        .build()

    private lateinit var fragNavController: FragNavController

    override fun initNavigation(savedInstanceState: Bundle?) {
        fragNavController = FragNavController(fragmentManager, R.id.container).apply {
            fragmentHideStrategy = FragNavController.DETACH_ON_NAVIGATE_HIDE_ON_SWITCH
            rootFragmentListener = this@AppNavigationInteractorImpl
            defaultTransactionOptions = defaultTransition
        }
        fragNavController.initialize(savedInstanceState = savedInstanceState)
    }

    override fun switchTab(index: Int) {
        fragNavController.switchTab(index)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        fragNavController.onSaveInstanceState(outState)
        outState?.putInt(ARG_SELECTED_TAB, fragNavController.currentStackIndex)
    }
}
