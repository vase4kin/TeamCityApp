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

package com.github.vase4kin.teamcityapp.navigation

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class RootActivitySavedStateTest {
    private val fragmentsKey = "android:support:fragments"
    private val registryKey = "androidx.lifecycle.BundlableSavedStateRegistry.key"

    @Test fun commonRootRejectsRetiredSerializerStateAndKeepsCurrentOwnerState() {
        val old = Bundle().apply { putString("compose", "retired") }
        assertNull(RootActivitySavedState.forSharedRoot(old))
        old.putBoolean(RootActivitySavedState.SHARED_ROOT, true)
        assertEquals("retired", RootActivitySavedState.forSharedRoot(old)!!.getString("compose"))
        assertNull(RootActivitySavedState.forSharedRoot(null))
    }

    @Test fun freshLaunchesAndSameAccountRestorationAcceptTheirInput() {
        assertTrue(RootActivitySavedState.canRestoreLaunch(null, "server|alice"))
        assertFalse(RootActivitySavedState.accountChanged(null, "server|alice"))
        val saved = Bundle().apply { putString(RootActivitySavedState.ACCOUNT_KEY, "server|alice") }
        assertTrue(RootActivitySavedState.canRestoreLaunch(saved, "server|alice"))
        assertFalse(RootActivitySavedState.accountChanged(saved, "server|alice"))
    }

    @Test fun missingAccountOrChangedUserOrServerCannotReplayAnOldLaunch() {
        assertFalse(RootActivitySavedState.canRestoreLaunch(Bundle(), "server|alice"))
        assertFalse(RootActivitySavedState.accountChanged(Bundle(), "server|alice"))
        val saved = Bundle().apply { putString(RootActivitySavedState.ACCOUNT_KEY, "server|alice") }
        assertFalse(RootActivitySavedState.canRestoreLaunch(saved, "server|bob"))
        assertFalse(RootActivitySavedState.canRestoreLaunch(saved, "other-server|alice"))
        assertFalse(RootActivitySavedState.canRestoreLaunch(saved, "signed-out"))
        assertTrue(RootActivitySavedState.accountChanged(saved, "server|bob"))
        assertTrue(RootActivitySavedState.accountChanged(saved, "other-server|alice"))
        assertTrue(RootActivitySavedState.accountChanged(saved, "signed-out"))
    }

    @Test fun aNewAccountsLaunchCanRestoreWhileTheSavedUiStillBelongsToThePreviousUser() {
        val saved = Bundle().apply {
            putString(RootActivitySavedState.ACCOUNT_KEY, "server|alice")
            putString(RootActivitySavedState.LAUNCH_ACCOUNT_KEY, "server|bob")
        }
        assertTrue(RootActivitySavedState.canRestoreLaunch(saved, "server|bob"))
        assertTrue(RootActivitySavedState.accountChanged(saved, "server|bob"))
        assertFalse(RootActivitySavedState.canRestoreLaunch(saved, "server|alice"))
        assertFalse(RootActivitySavedState.canRestoreLaunch(saved, "signed-out"))
    }

    @Test fun theSavedLaunchPayloadIsBoundToItsOwnAccountBeforeAnyDecoding() {
        val saved = Bundle().apply {
            putString(RootActivitySavedState.ACCOUNT_KEY, "server|alice")
            putString(RootActivitySavedState.LAUNCH_ACCOUNT_KEY, "server|bob")
            putString(RootActivitySavedState.LAUNCH_DESTINATION, "bob-current-destination")
        }
        assertEquals("bob-current-destination", RootActivitySavedState.restoredLaunch(saved, "server|bob")?.payload)
        assertNull(RootActivitySavedState.restoredLaunch(saved, "server|alice"))
        assertNull(RootActivitySavedState.restoredLaunch(saved, "signed-out"))
        saved.putString(RootActivitySavedState.LAUNCH_DESTINATION, null)
        assertNotNull(RootActivitySavedState.restoredLaunch(saved, "server|bob"))
        assertNull(RootActivitySavedState.restoredLaunch(saved, "server|bob")?.payload)
        saved.remove(RootActivitySavedState.LAUNCH_DESTINATION)
        assertNull(RootActivitySavedState.restoredLaunch(saved, "server|bob"))
    }

    @Test fun legacyFragmentsAreRemovedWithoutMutatingInputOrOtherSavedState() {
        val registry = Bundle().apply {
            putBundle(fragmentsKey, Bundle().apply { putString("old", "fragment-state") })
            putBundle("compose-navigation", Bundle().apply { putString("stack", "saved-visits-and-results") })
        }
        val original = Bundle().apply {
            putBundle(fragmentsKey, Bundle())
            putBundle(registryKey, registry)
            putString(RootActivitySavedState.ACCOUNT_KEY, "server|alice")
            putString("platform-state", "keep")
        }
        val root = RootActivitySavedState.forRoot(original)!!
        assertNotSame(original, root)
        assertFalse(root.containsKey(fragmentsKey))
        assertFalse(root.getBundle(registryKey)!!.containsKey(fragmentsKey))
        assertEquals("saved-visits-and-results", root.getBundle(registryKey)!!.getBundle("compose-navigation")!!.getString("stack"))
        assertEquals("server|alice", root.getString(RootActivitySavedState.ACCOUNT_KEY))
        assertEquals("keep", root.getString("platform-state"))
        assertTrue(original.containsKey(fragmentsKey))
        assertTrue(registry.containsKey(fragmentsKey))
        assertSame(registry, original.getBundle(registryKey))
        assertNull(RootActivitySavedState.forRoot(null))
    }

    @Test fun aRemovedFragmentClassCannotBeInstantiatedFromRetiredFrameworkState() {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).setup()
        val state = Bundle()
        try {
            controller.get().supportFragmentManager.beginTransaction().add(Fragment(), "retired-host").commitNow()
            controller.saveInstanceState(state)
        } finally {
            controller.pause().stop().destroy()
        }
        val retired = state.getBundle(registryKey)!!.getBundle(fragmentsKey)!!
        val fragmentState = retired.getBundle(retired.keySet().single { it.startsWith("fragment_") })!!.getParcelable<android.os.Parcelable>("state")!!
        ReflectionHelpers.setField(fragmentState, "mClassName", "removed.feature.RetiredFragment")
        val broken = Robolectric.buildActivity(FragmentActivity::class.java)
        assertThrows(Fragment.InstantiationException::class.java) { broken.create(state) }
        val root = Robolectric.buildActivity(FragmentActivity::class.java)
        try {
            root.create(RootActivitySavedState.forRoot(state)).start().resume()
            assertTrue(root.get().supportFragmentManager.fragments.isEmpty())
        } finally {
            root.pause().stop().destroy()
        }
    }
}
