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

package com.github.vase4kin.teamcityapp.build_details.view

import android.os.Bundle
import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModel

/**
 * View interactions of [BuildDetailsActivity]
 */
interface BuildDetailsView : BaseTabsViewModel {

    /**
     * On save activity state
     *
     * @param bundle - Bundle with state to save
     */
    fun onSave(bundle: Bundle?)

    /**
     * On restore activity state
     *
     * @param bundle - Bundle with saved state
     */
    fun onRestore(bundle: Bundle?)

    /**
     * Show run build float action button
     *
     * Disabled until run build feature is implemented
     */
    fun showRunBuildFloatActionButton()

    /**
     * Hide run build float action button
     *
     * Disabled until run build feature is implemented
     */
    fun hideRunBuildFloatActionButton()

    /**
     * @param onBuildDetailsViewListener - Listener to handle view changes
     */
    fun setOnBuildTabsViewListener(onBuildDetailsViewListener: OnBuildDetailsViewListener)

    /**
     * Show you are about to restart build dialog
     */
    fun showYouAreAboutToRestartBuildDialog()

    /**
     * Show you are about to stop build dialog
     */
    fun showYouAreAboutToStopBuildDialog()

    /**
     * Show you are about to stop build not ran by you dialog
     */
    fun showYouAreAboutToStopNotYoursBuildDialog()

    /**
     * Show you are about to remove build from queuedialog
     */
    fun showYouAreAboutToRemoveBuildFromQueueDialog()

    /**
     * Show you are about to remove build from queue not triggered by you dialog
     */
    fun showYouAreAboutToRemoveBuildFromQueueTriggeredNotByYouDialog()

    /**
     * Show restarting build progress dialog
     */
    fun showRestartingBuildProgressDialog()

    /**
     * Hide restarting build progress dialog
     */
    fun hideRestartingBuildProgressDialog()

    /**
     * Show stopping build progress dialog
     */
    fun showStoppingBuildProgressDialog()

    /**
     * Hide stopping build progress dialog
     */
    fun hideStoppingBuildProgressDialog()

    /**
     * Show removing build from queue progress dialog
     */
    fun showRemovingBuildFromQueueProgressDialog()

    /**
     * Hide removing build from queue progress dialog
     */
    fun hideRemovingBuildFromQueueProgressDialog()

    /**
     * Show build is stopped snack bar
     */
    fun showBuildIsStoppedSnackBar()

    /**
     * Show build stop error snack bar
     */
    fun showBuildIsStoppedErrorSnackBar()

    /**
     * Show forbidden to stop snack bar
     */
    fun showForbiddenToStopBuildSnackBar()

    /**
     * Show build is removed from queue snack bar
     */
    fun showBuildIsRemovedFromQueueSnackBar()

    /**
     * Show build remove from queue error snack bar
     */
    fun showBuildIsRemovedFromQueueErrorSnackBar()

    /**
     * Show forbidden to remove from queue snack bar
     */
    fun showForbiddenToRemoveBuildFromQueueSnackBar()

    /**
     * Show build restart success snack bar
     */
    fun showBuildRestartSuccessSnackBar()

    /**
     * Show build restart error snack bar
     */
    fun showBuildRestartErrorSnackBar()

    /**
     * Show forbidden to restart snack bar
     */
    fun showForbiddenToRestartBuildSnackBar()

    /**
     * Show build loading progress
     */
    fun showBuildLoadingProgress()

    /**
     * Hide build loading progress
     */
    fun hideBuildLoadingProgress()

    /**
     * Show error opening build snack bar
     */
    fun showOpeningBuildErrorSnackBar()

    /**
     * Show text copied snack bar
     */
    fun showTextCopiedSnackBar()

    /**
     * Show error downloading artifact snack bar
     */
    fun showErrorDownloadingArtifactSnackBar()
}
