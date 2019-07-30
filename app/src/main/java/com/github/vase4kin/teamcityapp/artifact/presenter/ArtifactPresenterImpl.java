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

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.artifact.api.File;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataManager;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModel;
import com.github.vase4kin.teamcityapp.artifact.data.ArtifactDataModelImpl;
import com.github.vase4kin.teamcityapp.artifact.data.OnArtifactEventListener;
import com.github.vase4kin.teamcityapp.artifact.extractor.ArtifactValueExtractor;
import com.github.vase4kin.teamcityapp.artifact.permissions.OnPermissionsResultListener;
import com.github.vase4kin.teamcityapp.artifact.permissions.PermissionManager;
import com.github.vase4kin.teamcityapp.artifact.router.ArtifactRouter;
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactView;
import com.github.vase4kin.teamcityapp.artifact.view.OnArtifactPresenterListener;
import com.github.vase4kin.teamcityapp.artifact.view.OnPermissionsDialogListener;
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl;
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;

import java.util.List;

import javax.inject.Inject;

public class ArtifactPresenterImpl extends BaseListPresenterImpl<
        ArtifactDataModel,
        File,
        ArtifactView,
        ArtifactDataManager,
        ViewTracker,
        ArtifactValueExtractor>
        implements ArtifactPresenter, OnArtifactPresenterListener, OnArtifactEventListener {

    @VisibleForTesting
    String fileName;
    @VisibleForTesting
    String fileHref;

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
    protected void loadData(@NonNull OnLoadingListener<List<File>> loadingListener, boolean update) {
        dataManager.load(valueExtractor.getUrl(), loadingListener, update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initViews() {
        super.initViews();
        view.setOnArtifactPresenterListener(this);
        dataManager.registerEventBus();
        dataManager.setListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(File artifactFile) {
        if (artifactFile.hasChildren() && artifactFile.isFolder()) {
            String href = artifactFile.getChildren().getHref();
            mRouter.openArtifactFile(valueExtractor.getBuildDetails(), href);
            unRegister();
        } else {
            onLongClick(artifactFile);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLongClick(final File artifactFile) {
        if (!artifactFile.isFolder() && artifactFile.hasChildren()) {
            view.showFullBottomSheet(artifactFile);
        } else if (artifactFile.isFolder()) {
            view.showFolderBottomSheet(artifactFile);
        } else if (isBrowserUrl(artifactFile.getHref())) {
            view.showBrowserBottomSheet(artifactFile);
        } else {
            view.showDefaultBottomSheet(artifactFile);
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
        onDownloadArtifactEvent(fileName, fileHref);
    }

    /**
     * Show retry download artifact snack bar for load artifact error
     */
    private void showRetryDownloadArtifactSnackBar() {
        dataManager.postArtifactErrorDownloadingEvent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unSubscribe() {
        dataManager.unsubscribe();
    }

    private void unRegister() {
        dataManager.unregisterEventBus();
        dataManager.setListener(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDownloadArtifactEvent(String fileName, String href) {
        this.fileName = fileName;
        this.fileHref = href;
        if (!mPermissionManager.isWriteStoragePermissionsGranted()) {
            view.showPermissionsInfoDialog(new OnPermissionsDialogListener() {
                @Override
                public void onAllow() {
                    mPermissionManager.requestWriteStoragePermissions();
                }
            });
        } else {
            if (dataManager.isTheFileApk(fileName)
                    && !mPermissionManager.isInstallPackagesPermissionGranted()) {
                view.showInstallPackagesPermissionsInfoDialog(new OnPermissionsDialogListener() {
                    @Override
                    public void onAllow() {
                        mPermissionManager.requestInstallPackagesPermission();
                    }
                });
            } else {
                view.showProgressDialog();
                dataManager.downloadArtifact(
                        href,
                        fileName,
                        new OnLoadingListener<java.io.File>() {
                            @Override
                            public void onSuccess(java.io.File data) {
                                view.dismissProgressDialog();
                                // TODO: Snack bar with file downloaded
                                mRouter.startFileActivity(data);
                            }

                            @Override
                            public void onFail(String errorMessage) {
                                view.dismissProgressDialog();
                                showRetryDownloadArtifactSnackBar();
                            }
                        });
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpenArtifactEvent(String href) {
        mRouter.openArtifactFile(valueExtractor.getBuildDetails(), href);
        unRegister();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartBrowserEvent(String href) {
        mRouter.startBrowser(valueExtractor.getBuildDetails(), href);
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
                view.showPermissionsDeniedDialog();
            }
        });
    }
}
