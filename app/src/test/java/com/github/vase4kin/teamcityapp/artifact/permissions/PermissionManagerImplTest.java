/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.artifact.permissions;

import android.content.pm.PackageManager;

import androidx.fragment.app.Fragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class PermissionManagerImplTest {

    @Mock
    private Fragment mFragment;

    @Mock
    private OnPermissionsResultListener mOnPermissionsResultListener;

    private PermissionManager mManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mManager = new PermissionManagerImpl(mFragment);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mOnPermissionsResultListener, mFragment);
    }

    @Test
    public void testOnRequestPermissionsResultIfRequestCodeIsNotProper() throws Exception {
        int requestCode = 12;
        String[] permissions = new String[]{"123"};
        int[] grantResults = new int[]{12, 123};
        mManager.onRequestPermissionsResult(requestCode, permissions, grantResults, mOnPermissionsResultListener);
    }

    @Test
    public void testOnRequestPermissionsResultIfRequestCodeIsProperButNoGrantResultsReceived() throws Exception {
        int requestCode = 42;
        String[] permissions = new String[]{"123"};
        int[] grantResults = new int[]{};
        mManager.onRequestPermissionsResult(requestCode, permissions, grantResults, mOnPermissionsResultListener);
        verify(mOnPermissionsResultListener).onDenied();
    }

    @Test
    public void testOnRequestPermissionsResultIfRequestCodeIsProperButGrantResultsAreReceived() throws Exception {
        int requestCode = 42;
        String[] permissions = new String[]{"123"};
        int[] grantResults = new int[]{PackageManager.PERMISSION_GRANTED};
        mManager.onRequestPermissionsResult(requestCode, permissions, grantResults, mOnPermissionsResultListener);
        verify(mOnPermissionsResultListener).onGranted();
    }

}