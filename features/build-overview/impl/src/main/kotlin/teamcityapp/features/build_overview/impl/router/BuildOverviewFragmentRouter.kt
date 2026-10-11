/*
 * Copyright 2019 Andrey Tolpeev
 * Copyright 2026 Andrey Tolpeev
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

package teamcityapp.features.build_overview.impl.router

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import javax.inject.Inject
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.features.build_overview.api.*
import teamcityapp.features.build_overview.impl.*
import teamcityapp.features.build_overview.impl.tracker.BuildOverviewTracker
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.utils.requireScreenOwner

class BuildOverviewFragmentRouter @Inject constructor(
    private val fragment: BuildOverviewFragment,
    private val app: BuildOverviewAppRouter,
    private val events: BuildOverviewEvents,
    private val sheets: BottomSheetNavigation,
    private val tracker: BuildOverviewTracker
) : BuildOverviewRouter {
    private val activity get() = fragment.requireActivity().requireScreenOwner<AppCompatActivity>()
    private var sheet: Fragment? = null
    private var callback: FragmentManager.FragmentLifecycleCallbacks? = null
    override val requests get() = events.requests
    override fun loaded(build: BuildLaunchData) = app.buildLoaded(activity, build)
    override fun resumed(build: BuildLaunchData) {
        app.resumed(activity, build)
        activity.supportFragmentManager.findFragmentByTag(TAG)?.let(::watchSheet)
    }
    override fun row(row: OverviewRow) {
        val value = when (val text = row.value) {
            is OverviewText.Literal -> text.value
            else -> activity.getString(text.resource())
        }
        val type = when (row.action) {
            OverviewRowAction.Copy -> SheetMenuType.Default
            OverviewRowAction.Branch -> SheetMenuType.Branch
            OverviewRowAction.Configuration -> SheetMenuType.BuildType
            OverviewRowAction.Project -> SheetMenuType.Project
        }
        val dialog = sheets.createBottomSheetDialog(activity.getString(row.labelRes), value, type)
        watchSheet(dialog)
        dialog.show(activity.supportFragmentManager, TAG)
    }
    private fun watchSheet(dialog: Fragment) {
        if (sheet === dialog) return
        stopWatching()
        sheet = dialog
        app.sheetVisibility(activity, true)
        callback = object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                if (f === sheet) {
                    app.sheetVisibility(activity, false)
                    stopWatching()
                }
            }
        }.also { activity.supportFragmentManager.registerFragmentLifecycleCallbacks(it, false) }
    }
    private fun stopWatching() {
        callback?.let { activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(it) }
        callback = null
        sheet = null
    }
    override fun dispatch(action: BuildOverviewAction, build: BuildLaunchData, branch: String?) {
        tracker.action(action, branch)
        // Host confirmation and outgoing navigation both receive this freshly loaded snapshot.
        app.buildLoaded(activity, build)
        app.dispatch(activity, action, build, branch)
    }
    override fun dispose() {
        if (sheet != null) app.sheetVisibility(activity, false)
        stopWatching()
        app.dispose(activity)
    }
    companion object {
        private const val TAG = "BottomSheet Dialog"
    }
}
