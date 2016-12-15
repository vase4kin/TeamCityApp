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

package com.github.vase4kin.teamcityapp.artifact.router;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.webkit.MimeTypeMap;

import com.github.vase4kin.teamcityapp.BuildConfig;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactListFragment;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.custom_tabs.ChromeCustomTabs;
import com.github.vase4kin.teamcityapp.custom_tabs.ChromeCustomTabsImpl;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import java.io.File;

/**
 * Impl of {@link ArtifactRouter}
 */
public class ArtifactRouterImpl implements ArtifactRouter {

    /**
     * Pattern to open artifacts in the browser
     */
    private static final String FILE_URL_PATTERN = "%s/repository/download/%s/%s:id/%s?guest=1";
    /**
     * All file types
     */
    private static final String ALL_FILES_TYPE = "*/*";

    private SharedUserStorage mSharedUserStorage;
    private AppCompatActivity mActivity;
    private ChromeCustomTabs mChromeCustomTabs;

    public ArtifactRouterImpl(SharedUserStorage mSharedUserStorage, AppCompatActivity activity) {
        this.mSharedUserStorage = mSharedUserStorage;
        this.mActivity = activity;
        mChromeCustomTabs = new ChromeCustomTabsImpl(activity);
        mChromeCustomTabs.initCustomsTabs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startFileActivity(File file) {
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null) {
            type = ALL_FILES_TYPE;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = FileProvider.getUriForFile(mActivity,
                BuildConfig.APPLICATION_ID + ".provider", file);
        intent.setDataAndType(data, type);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        //User couldn't have app with type intent
        try {
            mActivity.startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            intent.setDataAndType(data, ALL_FILES_TYPE);
            mActivity.startActivity(intent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openArtifactFile(Build build, com.github.vase4kin.teamcityapp.artifact.api.File artifactFile) {
        FragmentTransaction fragmentTransaction = mActivity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.artifact_fragment_list, ArtifactListFragment.newInstance(build, artifactFile.getChildren().getHref()));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindCustomsTabs() {
        mChromeCustomTabs.unbindCustomsTabs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startBrowser(Build build, com.github.vase4kin.teamcityapp.artifact.api.File artifactFile) {
        String pathToFile = artifactFile.getHref().split("/metadata/")[1];
        String url = String.format(FILE_URL_PATTERN,
                mSharedUserStorage.getActiveUser().getTeamcityUrl(),
                build.getBuildType().getId(),
                build.getId(),
                pathToFile);
        mChromeCustomTabs.launchUrl(url);
    }
}
