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
import androidx.appcompat.app.AppCompatActivity

/**
 * Impl of [PermissionManager]
 */
class PermissionManagerImpl(@Suppress("UNUSED_PARAMETER") activity: AppCompatActivity) : PermissionManager {

    /**
     * {@inheritDoc}
     */
    override val isWriteStoragePermissionsGranted: Boolean
        get() = true

    /**
     * {@inheritDoc}
     */
    override val isInstallPackagesPermissionGranted: Boolean
        get() = true

    /**
     * {@inheritDoc}
     */
    override fun requestWriteStoragePermissions() = Unit

    /**
     * {@inheritDoc}
     */
    override fun requestInstallPackagesPermission() = Unit

    /**
     * {@inheritDoc}
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
        onPermissionsResultListener: OnPermissionsResultListener
    ) {
        if (requestCode == PermissionManager.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionsResultListener.onGranted()
            } else {
                onPermissionsResultListener.onDenied()
            }
        }
    }
}
