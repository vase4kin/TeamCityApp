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

package com.github.vase4kin.teamcityapp.helper

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.viewpager.widget.ViewPager
import com.github.vase4kin.teamcityapp.R
import com.google.android.material.tabs.TabLayout
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo

/**
 * Taps the actual native tab and waits for its ViewPager transition without starving offscreen
 * Compose roots. The legacy pager retains every page as Android VISIBLE, including clipped pages.
 * Compose's idler can wait for their layout while no drawing or virtual frame is scheduled.
 */
fun ComposeTestRule.tapNativePagerTab(
    title: String,
    activity: Activity? = null,
    @IdRes pagerId: Int = R.id.viewPager,
    @IdRes tabLayoutId: Int = R.id.tabLayout
) {
    val host = runOnUiThread {
        activity ?: ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
            .single { it.findViewById<View>(pagerId) != null }
    }
    val decor = host.window.decorView
    val pager = runOnUiThread { requireNotNull(host.findViewById<ViewPager>(pagerId)) }
    val target = runOnUiThread {
        val tabs = requireNotNull(host.findViewById<TabLayout>(tabLayoutId))
        (0 until tabs.tabCount).single { tabs.getTabAt(it)?.text?.toString() == title }
    }
    val nativeIdle = CountDownLatch(1)
    val nativeState = AtomicInteger(ViewPager.SCROLL_STATE_IDLE)
    val listener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageScrollStateChanged(state: Int) {
            nativeState.set(state)
            if (state == ViewPager.SCROLL_STATE_IDLE && pager.currentItem == target) nativeIdle.countDown()
        }
    }
    // Main-thread-only flag. The pump performs the same public test traversal as Compose's frame
    // clock, including roots that a clipped native pager page will not draw.
    var pumping = true
    lateinit var traversal: Runnable
    traversal = Runnable {
        if (pumping) {
            drainComposeLayout(decor)
            decor.postOnAnimation(traversal)
        }
    }
    val autoAdvance = mainClock.autoAdvance
    mainClock.autoAdvance = false
    try {
        runOnUiThread {
            pager.addOnPageChangeListener(listener)
            drainComposeLayout(decor)
            decor.postOnAnimation(traversal)
        }
        onView(allOf(withText(title), isDescendantOfA(withId(tabLayoutId))))
            .inRoot(withDecorView(equalTo(decor)))
            .perform(scrollTo(), click())
        runOnUiThread {
            // A reselected or already-settled tab need not dispatch another IDLE callback.
            if (nativeState.get() == ViewPager.SCROLL_STATE_IDLE && pager.currentItem == target) nativeIdle.countDown()
        }
        check(nativeIdle.await(10, TimeUnit.SECONDS)) { "Native pager did not settle on tab $title" }
        mainClock.autoAdvance = autoAdvance
        // Apply queued visibility/state changes, then drain layout even if the frame clock no
        // longer has awaiters. No sleeps, disabled idling resources, or production changes.
        mainClock.advanceTimeBy(32)
        runOnUiThread { drainComposeLayout(decor) }
        waitForIdle()
        runOnUiThread {
            check(pager.currentItem == target) { "Native pager selected another tab" }
            val tabs = requireNotNull(host.findViewById<TabLayout>(tabLayoutId))
            check(tabs.selectedTabPosition == target) { "Native tab selection was not retained" }
        }
    } finally {
        mainClock.autoAdvance = autoAdvance
        runOnUiThread {
            pumping = false
            decor.removeCallbacks(traversal)
            pager.removeOnPageChangeListener(listener)
        }
    }
}

private fun drainComposeLayout(view: View) {
    if (view.isAttachedToWindow && view is ViewRootForTest) view.measureAndLayoutForTest()
    if (view is ViewGroup) {
        // Take a snapshot in case traversal applies a pending platform-view layout callback.
        val children = (0 until view.childCount).map(view::getChildAt)
        children.forEach(::drainComposeLayout)
    }
}
