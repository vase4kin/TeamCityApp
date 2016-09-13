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

package com.github.vase4kin.teamcityapp.artifact.view;

import com.github.vase4kin.teamcityapp.artifact.api.File;

/**
 * Listener to handle view interactions on {@link com.github.vase4kin.teamcityapp.artifact.presenter.ArtifactPresenterImpl}
 */
public interface OnArtifactPresenterListener {

    /**
     * On artifact click
     *
     * @param artifactFile - Artifact file
     */
    void onClick(File artifactFile);

    /**
     * On long artifact click
     *
     * @param artifactFile - Artifact file
     */
    void onLongClick(File artifactFile);

    /**
     * Unsubscribe data manager stuff
     */
    void unSubscribe();

    /**
     * Download artifact
     */
    void downloadArtifactFile();

    /**
     * Download artifact file
     *
     * @param artifactFile - Artifact file
     */
    void downloadArtifactFile(File artifactFile);

    /**
     * Open artifact file
     *
     * @param artifactFile - Artifact file
     */
    void openArtifactFile(File artifactFile);

    /**
     * Open artifact with browser
     *
     * @param artifactFile - Artifact file
     */
    void startBrowser(File artifactFile);
}
