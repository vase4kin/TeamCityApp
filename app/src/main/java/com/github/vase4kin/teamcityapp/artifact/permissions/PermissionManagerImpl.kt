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

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

/**
 * Impl of [PermissionManager]
 */
class PermissionManagerImpl(private val activity: AppCompatActivity) : PermissionManager {

    /**
     * {@inheritDoc}
     */
    override val isWriteStoragePermissionsGranted: Boolean
        get() = ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * {@inheritDoc}
     */
    override val isInstallPackagesPermissionGranted: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.packageManager.canRequestPackageInstalls()
        } else {
            true
        }

    /**
     * {@inheritDoc}
     */
    override fun requestWriteStoragePermissions() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PermissionManager.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
        )
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.O)
    override fun requestInstallPackagesPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
        val packageUri = Uri.parse("package:" + activity.packageName)
        intent.data = packageUri
        activity.startActivity(intent)
    }

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
