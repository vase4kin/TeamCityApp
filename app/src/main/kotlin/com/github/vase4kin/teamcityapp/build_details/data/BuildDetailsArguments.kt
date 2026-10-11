/*
 * Copyright 2019 Andrey Tolpeev
 * Copyright 2026 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.build_details.data

import android.os.Bundle
import androidx.core.os.BundleCompat
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.overview.data.BuildDetailsImpl as LegacyBuildDetailsImpl

/** One activity-scoped compatibility snapshot shared by native host view, interactor and Compose adapter. */
class BuildDetailsArguments(bundle: Bundle?) {
    var initialBuild: Build? = null
        private set
    var initialName: String = ""
        private set
    var initialDetails: BuildDetails = BuildDetails.STUB
        private set
    var currentBuild: Build? = null
        private set
    var current: BuildDetails = BuildDetails.STUB
        private set

    init {
        resetIncoming(bundle)
    }

    /** onNewIntent resets the existing scoped instance before the presenter is recreated. */
    fun resetIncoming(bundle: Bundle?) {
        initialBuild = bundle?.let { BundleCompat.getSerializable(it, BundleExtractorValues.BUILD, Build::class.java) }
        initialName = bundle?.getString(BundleExtractorValues.NAME).orEmpty()
        initialDetails = initialBuild?.let(::LegacyBuildDetailsImpl) ?: BuildDetails.STUB
        currentBuild = initialBuild
        current = initialDetails
    }

    fun publishLoaded(build: Build) {
        currentBuild = build
        current = LegacyBuildDetailsImpl(build)
    }

    /** Retain loaded host data even when Overview's retained ViewModel is in a hidden tab. */
    fun saveCurrent(outState: Bundle) {
        outState.putString(SAVED_INITIAL_ID, initialBuild?.id)
        outState.putSerializable(SAVED_CURRENT_BUILD, currentBuild)
    }

    /** Restore before creating the native presenter; incoming tab construction stays unchanged. */
    fun restoreCurrent(savedState: Bundle?) {
        savedState ?: return
        if (savedState.getString(SAVED_INITIAL_ID) != initialBuild?.id) return
        BundleCompat.getSerializable(savedState, SAVED_CURRENT_BUILD, Build::class.java)?.let(::publishLoaded)
    }

    private companion object {
        const val SAVED_INITIAL_ID = "buildDetails.initialBuildId"
        const val SAVED_CURRENT_BUILD = "buildDetails.currentBuild"
    }
}
