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

package teamcityapp.features.build_history.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import teamcityapp.features.build_history.api.BuildHistoryPrompt
import teamcityapp.libraries.theme.Coachmark
import teamcityapp.libraries.theme.CoachmarkPlacement

@Composable
internal fun HistoryCoachmark(state: OnboardingState.Available, anchor: Rect, onDismiss: () -> Unit) {
    val prompt = state.prompt ?: return
    Coachmark(
        title = stringResource(state.titleRes ?: return),
        description = stringResource(state.descriptionRes ?: return),
        dismissLabel = stringResource(state.dismissLabelRes),
        anchor = anchor,
        onDismiss = onDismiss,
        modifier = Modifier.testTag("history:coachmark:$prompt"),
        dismissModifier = Modifier.testTag("history:prompt-dismiss"),
        placement = if (prompt == BuildHistoryPrompt.Run) CoachmarkPlacement.Above else CoachmarkPlacement.Below,
        saving = state.saving,
        saveFailure = if (state.saveFailed) stringResource(R.string.history_prompt_save_failed) else null
    )
}
