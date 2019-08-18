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

package com.github.vase4kin.teamcityapp.artifact.permissions

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PermissionManagerImplTest {

    @Mock
    private lateinit var fragment: Fragment

    @Mock
    private lateinit var onPermissionsResultListener: OnPermissionsResultListener

    private lateinit var manager: PermissionManager

    @Before
    fun setUp() {
        manager = PermissionManagerImpl(fragment)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(onPermissionsResultListener, fragment)
    }

    @Test
    fun testOnRequestPermissionsResultIfRequestCodeIsNotProper() {
        val requestCode = 12
        val permissions = arrayOf("123")
        val grantResults = intArrayOf(12, 123)
        manager.onRequestPermissionsResult(requestCode, permissions, grantResults, onPermissionsResultListener)
    }

    @Test
    fun testOnRequestPermissionsResultIfRequestCodeIsProperButNoGrantResultsReceived() {
        val requestCode = 42
        val permissions = arrayOf("123")
        val grantResults = intArrayOf()
        manager.onRequestPermissionsResult(requestCode, permissions, grantResults, onPermissionsResultListener)
        verify(onPermissionsResultListener).onDenied()
    }

    @Test
    fun testOnRequestPermissionsResultIfRequestCodeIsProperButGrantResultsAreReceived() {
        val requestCode = 42
        val permissions = arrayOf("123")
        val grantResults = intArrayOf(PackageManager.PERMISSION_GRANTED)
        manager.onRequestPermissionsResult(requestCode, permissions, grantResults, onPermissionsResultListener)
        verify(onPermissionsResultListener).onGranted()
    }
}
