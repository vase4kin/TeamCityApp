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

package com.github.vase4kin.teamcityapp.base.tabs.view

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.github.vase4kin.teamcityapp.R
import com.google.android.material.tabs.TabLayout

abstract class BaseTabsViewModelImpl(
    private val view: View,
    protected var activity: AppCompatActivity
) : BaseTabsViewModel {

    @BindView(R.id.viewPager)
    lateinit var viewPager: ViewPager
    @BindView(R.id.tabLayout)
    lateinit var tabLayout: TabLayout

    private lateinit var unbinder: Unbinder
    private lateinit var adapter: FragmentAdapter

    override fun initViews() {
        unbinder = ButterKnife.bind(this, view)
        // Make sure there're no fragments saved in fragment manager (in case the view was reloaded)
        removeAllFragmentsFromFragmentManager()
        adapter = FragmentAdapter(activity.supportFragmentManager, activity)
        addFragments(adapter)
        tabLayout.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }

    /**
     * Remove all fragments from fragment manager
     */
    @SuppressLint("RestrictedApi")
    private fun removeAllFragmentsFromFragmentManager() {
        val fragmentManager = activity.supportFragmentManager
        val fragments = fragmentManager.fragments
        val ft = fragmentManager.beginTransaction()
        for (f in fragments) {
            if (f != null) {
                ft.remove(f)
            }
        }
        ft.commitNow()
    }

    override fun unBindViews() {
        unbinder.unbind()
    }

    abstract override fun addFragments(fragmentAdapter: FragmentAdapter)

    override fun updateTabTitle(tabPosition: Int, newTitle: String) {
        val updatedTabTitle = String.format(
            "%s (%s)",
            adapter.getFragmentContainers()[tabPosition].name,
            newTitle
        )
        val tab = tabLayout.getTabAt(tabPosition)
        if (tab != null) {
            tab.text = updatedTabTitle
        }
    }
}
