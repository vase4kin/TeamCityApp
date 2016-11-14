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

package com.github.vase4kin.teamcityapp.artifact.presenter;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.artifact.api.File;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataManager;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModelImpl;
import com.github.vase4kin.teamcityapp.artifact.data.OnArtifactTabChangeEventListener;
import com.github.vase4kin.teamcityapp.artifact.extractor.ArtifactValueExtractor;
import com.github.vase4kin.teamcityapp.artifact.permissions.OnPermissionsResultListener;
import com.github.vase4kin.teamcityapp.artifact.permissions.PermissionManager;
import com.github.vase4kin.teamcityapp.artifact.router.ArtifactRouter;
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactView;
import com.github.vase4kin.teamcityapp.artifact.view.OnArtifactPresenterListener;
import com.github.vase4kin.teamcityapp.artifact.view.OnPermissionsDialogListener;
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;

import java.util.List;

import javax.inject.Inject;

public class ArtifactPresenterImpl extends BaseListPresenterImpl<
        ArtifactDataModel,
        File,
        ArtifactView,
        ArtifactDataManager,
        ViewTracker,
        ArtifactValueExtractor>
        implements ArtifactPresenter, OnArtifactPresenterListener, OnArtifactTabChangeEventListener {

    private File mArtifactFile;

    private ArtifactRouter mRouter;
    private PermissionManager mPermissionManager;

    @Inject
    ArtifactPresenterImpl(@NonNull ArtifactView view,
                          @NonNull ArtifactDataManager dataManager,
                          @NonNull ViewTracker tracker,
                          @NonNull ArtifactValueExtractor valueExtractor,
                          ArtifactRouter router,
                          PermissionManager permissionManager) {
        super(view, dataManager, tracker, valueExtractor);
        this.mRouter = router;
        this.mPermissionManager = permissionManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ArtifactDataModel createModel(List<File> data) {
        return new ArtifactDataModelImpl(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<File>> loadingListener) {
        mDataManager.load(mValueExtractor.getUrl(), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initViews() {
        super.initViews();
        mView.setOnArtifactPresenterListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(File artifactFile) {
        if (artifactFile.hasChildren()) {
            mRouter.openArtifactFile(mValueExtractor.getBuild(), artifactFile);
        } else if (isBrowserUrl(artifactFile.getHref())) {
            mRouter.startBrowser(mValueExtractor.getBuild(), artifactFile);
        } else if (!artifactFile.hasChildren()) {
            downloadArtifactFile(artifactFile);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLongClick(final File artifactFile) {
        if (!artifactFile.isFolder() && artifactFile.hasChildren()) {
            mView.showFullBottomSheet(artifactFile);
        } else if (artifactFile.isFolder()) {
            mView.showFolderBottomSheet(artifactFile);
        } else if (isBrowserUrl(artifactFile.getHref())) {
            mView.showBrowserBottomSheet(artifactFile);
        } else {
            mView.showDefaultBottomSheet(artifactFile);
        }
    }

    /**
     * Check if the url is the browser url
     *
     * @param url - Artifact url
     * @return Boolean
     */
    private boolean isBrowserUrl(String url) {
        return url.contains(".html") || url.contains(".htm");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsDestroyed() {
        super.onViewsDestroyed();
        mRouter.unbindCustomsTabs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void downloadArtifactFile() {
        downloadArtifactFile(mArtifactFile);
    }

    /**
     * {@inheritDoc}
     */
    public void downloadArtifactFile(File artifactFile) {
        mArtifactFile = artifactFile;
        if (!mPermissionManager.isWriteStoragePermissionsGranted()) {
            if (mPermissionManager.isNeedToShowInfoPermissionsDialog()) {
                mView.showPermissionsInfoDialog(new OnPermissionsDialogListener() {
                    @Override
                    public void onAllow() {
                        mPermissionManager.requestWriteStoragePermissions();
                    }
                });
            } else {
                mPermissionManager.requestWriteStoragePermissions();
            }
        } else {
            mView.showProgressDialog();
            mDataManager.downloadArtifact(
                    artifactFile.getContent().getHref(),
                    artifactFile.getName(),
                    new OnLoadingListener<java.io.File>() {
                        @Override
                        public void onSuccess(java.io.File data) {
                            mView.dismissProgressDialog();
                            mRouter.startFileActivity(data);
                        }

                        @Override
                        public void onFail(String errorMessage) {
                            mView.dismissProgressDialog();
                            showRetryDownloadArtifactSnackBar();
                        }
                    });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openArtifactFile(File artifactFile) {
        mRouter.openArtifactFile(mValueExtractor.getBuild(), artifactFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startBrowser(File artifactFile) {
        mRouter.startBrowser(mValueExtractor.getBuild(), artifactFile);
    }

    /**
     * Show retry download artifact snack bar for load artifact error
     */
    private void showRetryDownloadArtifactSnackBar() {
        mView.showRetryDownloadArtifactSnackBar(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unSubscribe() {
        mDataManager.unsubscribe();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        mDataManager.registerEventBus();
        mDataManager.setListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void OnStop() {
        mDataManager.unregisterEventBus();
        mDataManager.setListener(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEventHappen() {
        mView.onArtifactTabChangeEvent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mPermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults, new OnPermissionsResultListener() {
            @Override
            public void onGranted() {
                downloadArtifactFile();
            }

            @Override
            public void onDenied() {
                mView.showPermissionsDeniedDialog();
            }
        });
    }
}
