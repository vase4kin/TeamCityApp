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

package com.github.vase4kin.teamcityapp.build_details.view;

import android.os.Bundle;

import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModel;

/**
 * View interactions of {@link BuildDetailsActivity}
 */
public interface BuildDetailsView extends BaseTabsViewModel {

    /**
     * On save activity state
     *
     * @param bundle - Bundle with state to save
     */
    void onSave(Bundle bundle);

    /**
     * On restore activity state
     *
     * @param bundle - Bundle with saved state
     */
    void onRestore(Bundle bundle);

    /**
     * Show run build float action button
     *
     * Disabled until run build feature is implemented
     */
    void showRunBuildFloatActionButton();

    /**
     * Hide run build float action button
     *
     * Disabled until run build feature is implemented
     */
    void hideRunBuildFloatActionButton();

    /**
     * @param onBuildDetailsViewListener - Listener to handle view changes
     */
    void setOnBuildTabsViewListener(OnBuildDetailsViewListener onBuildDetailsViewListener);

    /**
     * Show you are about to restart build dialog
     */
    void showYouAreAboutToRestartBuildDialog();

    /**
     * Show you are about to stop build dialog
     */
    void showYouAreAboutToStopBuildDialog();

    /**
     * Show you are about to stop build not ran by you dialog
     */
    void showYouAreAboutToStopNotYoursBuildDialog();

    /**
     * Show you are about to remove build from queuedialog
     */
    void showYouAreAboutToRemoveBuildFromQueueDialog();

    /**
     * Show you are about to remove build from queue not triggered by you dialog
     */
    void showYouAreAboutToRemoveBuildFromQueueTriggeredNotByYouDialog();

    /**
     * Show restarting build progress dialog
     */
    void showRestartingBuildProgressDialog();

    /**
     * Hide restarting build progress dialog
     */
    void hideRestartingBuildProgressDialog();

    /**
     * Show stopping build progress dialog
     */
    void showStoppingBuildProgressDialog();

    /**
     * Hide stopping build progress dialog
     */
    void hideStoppingBuildProgressDialog();

    /**
     * Show removing build from queue progress dialog
     */
    void showRemovingBuildFromQueueProgressDialog();

    /**
     * Hide removing build from queue progress dialog
     */
    void hideRemovingBuildFromQueueProgressDialog();

    /**
     * Show build is stopped snack bar
     */
    void showBuildIsStoppedSnackBar();

    /**
     * Show build stop error snack bar
     */
    void showBuildIsStoppedErrorSnackBar();

    /**
     * Show forbidden to stop snack bar
     */
    void showForbiddenToStopBuildSnackBar();

    /**
     * Show build is removed from queue snack bar
     */
    void showBuildIsRemovedFromQueueSnackBar();

    /**
     * Show build remove from queue error snack bar
     */
    void showBuildIsRemovedFromQueueErrorSnackBar();

    /**
     * Show forbidden to remove from queue snack bar
     */
    void showForbiddenToRemoveBuildFromQueueSnackBar();

    /**
     * Show build restart success snack bar
     */
    void showBuildRestartSuccessSnackBar();

    /**
     * Show build restart error snack bar
     */
    void showBuildRestartErrorSnackBar();

    /**
     * Show forbidden to restart snack bar
     */
    void showForbiddenToRestartBuildSnackBar();

    /**
     * Show build loading progress
     */
    void showBuildLoadingProgress();

    /**
     * Hide build loading progress
     */
    void hideBuildLoadingProgress();

    /**
     * Show error opening build snack bar
     */
    void showOpeningBuildErrorSnackBar();

    /**
     * Show text copied snack bar
     */
    void showTextCopiedSnackBar();

    /**
     * Show error downloading artifact snack bar
     */
    void showErrorDownloadingArtifactSnackBar();
}