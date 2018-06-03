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

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

/**
 * Impl of {@link PermissionManager}
 */
public class PermissionManagerImpl implements PermissionManager {

    private Fragment mFragment;

    public PermissionManagerImpl(Fragment fragment) {
        this.mFragment = fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWriteStoragePermissionsGranted() {
        return ActivityCompat.checkSelfPermission(mFragment.requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestWriteStoragePermissions() {
        mFragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInstallPackagesPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return mFragment.requireContext().getPackageManager().canRequestPackageInstalls();
        } else {
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.O)
    @Override
    public void requestInstallPackagesPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        Uri packageUri = Uri.parse("package:" + mFragment.requireContext().getPackageName());
        intent.setData(packageUri);
        mFragment.startActivity(intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults,
                                           OnPermissionsResultListener onPermissionsResultListener) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionsResultListener.onGranted();
                } else {
                    onPermissionsResultListener.onDenied();
                }
        }
    }
}
