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

/**
 * Manages permissions handling for {@link com.github.vase4kin.teamcityapp.artifact.view.ArtifactListFragment}
 */
public interface PermissionManager {

    /**
     * Check is permissions are granted
     */
    boolean isWriteStoragePermissionsGranted();

    /**
     * Need to show permission info dialog
     */
    boolean isNeedToShowInfoPermissionsDialog();

    /**
     * Request write storage permissions
     */
    void requestWriteStoragePermissions();

    /**
     * Callback to handle on request fragment permissions callbacks
     */
    void onRequestPermissionsResult(int requestCode,
                                    String[] permissions,
                                    int[] grantResults,
                                    OnPermissionsResultListener onPermissionsResultListener);

}
