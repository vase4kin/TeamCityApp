/*
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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.integration
import com.github.vase4kin.teamcityapp.artifact.data.*
import com.github.vase4kin.teamcityapp.overview.data.*
import javax.inject.Inject
import org.greenrobot.eventbus.EventBus
import teamcityapp.features.bottom_sheet.api.*
class AppBottomSheetActions @Inject constructor(private val eventBus: EventBus) : BottomSheetAppActions {
    override fun dispatch(item: SheetItem) {
        when (item.action) {
            SheetAction.Copy -> eventBus.post(TextCopiedEvent())
            SheetAction.Branch -> eventBus.post(NavigateToBuildListFilteredByBranchEvent(item.description))
            SheetAction.BuildType -> eventBus.post(NavigateToBuildListEvent())
            SheetAction.Project -> eventBus.post(NavigateToProjectEvent())
            SheetAction.ArtifactDownload -> eventBus.post(ArtifactDownloadEvent(item.fileName, item.description))
            SheetAction.ArtifactOpen -> eventBus.post(ArtifactOpenEvent(item.fileName, item.description))
            SheetAction.ArtifactBrowser -> eventBus.post(ArtifactOpenInBrowserEvent(item.description))
        }
    }
}
