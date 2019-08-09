package com.github.vase4kin.teamcityapp.app_navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.vase4kin.teamcityapp.R
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavTransactionOptions

interface AppNavigationInteractor {
    fun initNavigation()
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

    override fun initNavigation() {
        fragNavController = FragNavController(fragmentManager, R.id.container).apply {
            fragmentHideStrategy = FragNavController.DETACH_ON_NAVIGATE_HIDE_ON_SWITCH
            rootFragmentListener = this@AppNavigationInteractorImpl
            defaultTransactionOptions = defaultTransition
        }
        fragNavController.initialize(FragNavController.TAB1)
    }

    override fun switchTab(index: Int) {
        fragNavController.switchTab(index)
    }
}