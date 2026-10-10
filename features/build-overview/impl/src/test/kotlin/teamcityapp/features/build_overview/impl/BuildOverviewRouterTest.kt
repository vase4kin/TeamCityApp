/*
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

package teamcityapp.features.build_overview.impl

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.features.build_overview.api.*
import teamcityapp.features.build_overview.impl.router.BuildOverviewFragmentRouter
import teamcityapp.features.build_overview.impl.tracker.BuildOverviewTracker

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class BuildOverviewRouterTest {
    private val fragment = mock(BuildOverviewFragment::class.java)
    private val activity = mock(AppCompatActivity::class.java)
    private val app = mock(BuildOverviewAppRouter::class.java)
    private val events = mock(BuildOverviewEvents::class.java)
    private val sheets = mock(BottomSheetNavigation::class.java)
    private val tracker = mock(BuildOverviewTracker::class.java)
    private val fragments = mock(FragmentManager::class.java)
    private val dialog = mock(DialogFragment::class.java)
    private fun router(): BuildOverviewFragmentRouter {
        `when`(fragment.requireActivity()).thenReturn(activity)
        `when`(activity.supportFragmentManager).thenReturn(fragments)
        `when`(activity.getString(anyInt())).thenReturn("Header")
        return BuildOverviewFragmentRouter(fragment, app, events, sheets, tracker)
    }

    @Test fun dispatchPublishesLoadedBuildBeforeHostActionAndKeepsBranch() {
        val router = router()
        val loaded = BuildOverviewFixtures.finished.copy(number = "999", branchName = "fresh-branch")
        router.dispatch(BuildOverviewAction.Configuration, loaded, "fresh-branch")
        val order = inOrder(app)
        order.verify(app).buildLoaded(activity, loaded)
        order.verify(app).dispatch(activity, BuildOverviewAction.Configuration, loaded, "fresh-branch")
        verify(tracker).action(BuildOverviewAction.Configuration, "fresh-branch")
    }

    @Test fun branchRowOpensBranchSheetWithItsExactValue() {
        val router = router()
        `when`(sheets.createBottomSheetDialog("Header", "refs/heads/master", SheetMenuType.Branch)).thenReturn(dialog)
        router.row(overviewRows(BuildOverviewFixtures.finished).single { it.field == OverviewField.Branch })
        verify(sheets).createBottomSheetDialog("Header", "refs/heads/master", SheetMenuType.Branch)
        verify(dialog).show(fragments, "BottomSheet Dialog")
        verify(app).sheetVisibility(activity, true)
    }

    @Test fun ordinaryRowKeepsTheCopyOnlySheet() {
        val router = router()
        `when`(sheets.createBottomSheetDialog("Header", "Build finished successfully", SheetMenuType.Default)).thenReturn(dialog)
        router.row(overviewRows(BuildOverviewFixtures.finished).single { it.field == OverviewField.Result })
        verify(sheets).createBottomSheetDialog("Header", "Build finished successfully", SheetMenuType.Default)
    }

    @Test fun restoredSheetVisibilityAndCallbacksAreViewScoped() {
        val router = router()
        `when`(fragments.findFragmentByTag("BottomSheet Dialog")).thenReturn(dialog)
        router.resumed(BuildOverviewFixtures.finished)
        verify(app).resumed(activity, BuildOverviewFixtures.finished)
        verify(app).sheetVisibility(activity, true)
        router.dispose()
        verify(app).sheetVisibility(activity, false)
        verify(fragments).unregisterFragmentLifecycleCallbacks(any(FragmentManager.FragmentLifecycleCallbacks::class.java))
        verify(app).dispose(activity)
    }
}
