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

import com.github.vase4kin.teamcityapp.buildlist.api.Build;

import java.io.File;

/**
 * {@link com.github.vase4kin.teamcityapp.artifact.view.ArtifactListFragment} router
 */
public interface ArtifactRouter {

    /**
     * Open artifact with proper activity
     *
     * @param file - Artifact file
     */
    void startFileActivity(File file);

    /**
     * Open artifact file (navigate deeper)
     *
     * @param build        - Build with artifacts
     * @param artifactFile - Artifact file
     */
    void openArtifactFile(Build build, com.github.vase4kin.teamcityapp.artifact.api.File artifactFile);

    /**
     * Unbind customs chrome tabs
     */
    void unbindCustomsTabs();

    /**
     * Start the browser
     *
     * @param build - Build with artifacts
     * @param artifactFile - Artifact file
     */
    void startBrowser(Build build, com.github.vase4kin.teamcityapp.artifact.api.File artifactFile);
}
