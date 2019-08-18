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

/**
 * Manages permissions handling for [com.github.vase4kin.teamcityapp.artifact.view.ArtifactListFragment]
 */
interface PermissionManager {

    /**
     * Check is permissions are granted
     */
    val isWriteStoragePermissionsGranted: Boolean

    /**
     * Check is permissions are granted
     */
    val isInstallPackagesPermissionGranted: Boolean

    /**
     * Request write storage permissions
     */
    fun requestWriteStoragePermissions()

    /**
     * Request write storage permissions
     */
    fun requestInstallPackagesPermission()

    /**
     * Callback to handle on request fragment permissions callbacks
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
        onPermissionsResultListener: OnPermissionsResultListener
    )

    companion object {
        const val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 42
    }
}
