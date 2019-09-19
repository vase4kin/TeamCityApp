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

package com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.tracker.FilterBottomSheetTracker
import com.github.vase4kin.teamcityapp.home.data.FilterAppliedEvent
import dagger.android.support.AndroidSupportInjection
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * Bottom sheet dialog
 */
class FilterBottomSheetDialogFragment : com.google.android.material.bottomsheet.BottomSheetDialogFragment() {

    @Inject
    lateinit var filterProvider: FilterProvider
    @Inject
    lateinit var eventBus: EventBus
    @Inject
    lateinit var bottomSheetTracker: FilterBottomSheetTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = View.inflate(context, R.layout.dialog_bottom_sheet_filter, null)
        val arguments = arguments
        val filter = Filter.values()[arguments?.getInt(ARG_CODE, 0) ?: 0]
        val description = when (filter) {
            Filter.RUNNING_ALL, Filter.QUEUE_ALL -> R.string.text_show_favorites
            Filter.QUEUE_FAVORITES -> R.string.text_show_queued
            Filter.RUNNING_FAVORITES -> R.string.text_show_running
            Filter.AGENTS_CONNECTED -> R.string.text_show_disconnected
            Filter.AGENTS_DISCONNECTED -> R.string.text_show_connected
        }
        val descriptionTextView = view.findViewById<TextView>(R.id.text)
        descriptionTextView.setText(description)
        val title = when {
            filter.isRunning -> R.string.title_filter_running_builds
            filter.isQueued -> R.string.title_filter_queued_builds
            filter.isAgents -> R.string.title_filter_agents
            else -> R.string.text_filters
        }
        val titleTextView = view.findViewById<TextView>(R.id.main_title)
        titleTextView.setText(title)
        descriptionTextView.setOnClickListener {
            when {
                filter.isRunning -> {
                    val oppositeFilter = filter.opposite()
                    filterProvider.runningBuildsFilter = oppositeFilter
                    eventBus.post(FilterAppliedEvent(filter))
                    bottomSheetTracker.trackRunningBuildsFilterSelected(oppositeFilter)
                }
                filter.isQueued -> {
                    val oppositeFilter = filter.opposite()
                    filterProvider.queuedBuildsFilter = oppositeFilter
                    eventBus.post(FilterAppliedEvent(oppositeFilter))
                    bottomSheetTracker.trackQueuedBuildsFilterSelected(oppositeFilter)
                }
                filter.isAgents -> {
                    val oppositeFilter = filter.opposite()
                    filterProvider.agentsFilter = oppositeFilter
                    eventBus.post(FilterAppliedEvent(oppositeFilter))
                    bottomSheetTracker.trackAgentsFilterSelected(oppositeFilter)
                }
            }
            this@FilterBottomSheetDialogFragment.dismiss()
        }
        return view
    }

    companion object {

        const val ARG_CODE = "arg_code"

        fun createBottomSheetDialog(code: Int): FilterBottomSheetDialogFragment {
            val filterBottomSheetDialogFragment = FilterBottomSheetDialogFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_CODE, code)
            filterBottomSheetDialogFragment.arguments = bundle
            return filterBottomSheetDialogFragment
        }
    }
}
