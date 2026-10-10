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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import teamcityapp.features.build_history.api.BuildHistoryPrompt

/** Bounds come from this screen's actual controls, including edge-to-edge insets and resizing. */
@Composable
internal fun HistoryCoachmark(state: OnboardingState.Available, anchor: Rect, onDismiss: () -> Unit) {
    val prompt = state.prompt ?: return
    val title = stringResource(
        when (prompt) {
            BuildHistoryPrompt.Run -> R.string.history_prompt_run_title
            BuildHistoryPrompt.Filter -> R.string.history_prompt_filter_title
            BuildHistoryPrompt.Favorite -> R.string.history_prompt_favorite_title
        }
    )
    val description = stringResource(
        when (prompt) {
            BuildHistoryPrompt.Run -> R.string.history_prompt_run_description
            BuildHistoryPrompt.Filter -> R.string.history_prompt_filter_description
            BuildHistoryPrompt.Favorite -> R.string.history_prompt_favorite_description
        }
    )
    val density = LocalDensity.current
    BoxWithConstraints(
        Modifier.fillMaxSize().testTag("history:coachmark:$prompt").semantics { paneTitle = title }
            .pointerInput(state.saving) { detectTapGestures { if (!state.saving) onDismiss() } }
    ) {
        Canvas(Modifier.matchParentSize()) {
            val highlighted = anchor.inflate(8.dp.toPx())
            val mask = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, size.width, size.height))
                addRoundRect(RoundRect(highlighted, CornerRadius(16.dp.toPx())))
            }
            drawPath(mask, Color.Black.copy(alpha = 0.72f))
            drawRoundRect(Color.White, highlighted.topLeft, highlighted.size, CornerRadius(16.dp.toPx()), style = Stroke(2.dp.toPx()))
        }
        val inset = 16.dp
        val targetTop = with(density) { anchor.top.toDp() }
        val targetBottom = with(density) { anchor.bottom.toDp() }
        // Run's explanation sits above the FAB; toolbar explanations start below their target.
        val topSpace = if (prompt == BuildHistoryPrompt.Run) inset else targetBottom + 16.dp
        val bottomSpace = if (prompt == BuildHistoryPrompt.Run) maxHeight - targetTop + 16.dp else inset
        Box(
            Modifier.fillMaxSize().padding(start = inset, end = inset, top = topSpace, bottom = bottomSpace),
            contentAlignment = if (prompt == BuildHistoryPrompt.Run) Alignment.BottomEnd else Alignment.TopEnd
        ) {
            Card(Modifier.widthIn(max = 480.dp).fillMaxWidth()) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineSmall)
                    Text(description, style = MaterialTheme.typography.bodyLarge)
                    if (state.saveFailed) Text(stringResource(R.string.history_prompt_save_failed), color = MaterialTheme.colorScheme.error)
                    Button(onClick = onDismiss, enabled = !state.saving, modifier = Modifier.align(Alignment.End).testTag("history:prompt-dismiss")) {
                        if (state.saving) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(if (state.saveFailed) R.string.history_retry else R.string.history_got_it))
                        }
                    }
                }
            }
        }
    }
}
