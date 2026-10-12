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

package teamcityapp.libraries.theme

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview

/** Coordinates are measured from real controls, then translated into the overlay's host. */
@Stable
class CoachmarkAnchors {
    private val positions = mutableStateMapOf<String, Rect>()
    internal var origin by mutableStateOf(Offset.Zero)
    internal fun record(key: String, bounds: Rect) {
        positions[key] = bounds
    }
    internal fun remove(key: String) {
        positions.remove(key)
    }
    fun bounds(key: String): Rect? = positions[key]?.takeIf { it.width > 0 && it.height > 0 }?.translate(-origin)
}

val LocalCoachmarkAnchors = staticCompositionLocalOf<CoachmarkAnchors?> { null }
const val DRAWER_COACHMARK_ANCHOR = "navigation:drawer"

fun Modifier.coachmarkHost(anchors: CoachmarkAnchors): Modifier = onGloballyPositioned { anchors.origin = it.boundsInRoot().topLeft }

@Composable
fun Modifier.coachmarkAnchor(key: String, anchors: CoachmarkAnchors? = LocalCoachmarkAnchors.current): Modifier {
    DisposableEffect(anchors, key) { onDispose { anchors?.remove(key) } }
    return if (anchors == null) this else onGloballyPositioned { anchors.record(key, it.boundsInRoot()) }
}

enum class CoachmarkPlacement { Above, Below }

/** Shared button spotlight and explanation used by Home, History and Build Details. */
@Composable
fun Coachmark(
    title: String,
    description: String,
    dismissLabel: String,
    anchor: Rect,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissModifier: Modifier = Modifier,
    placement: CoachmarkPlacement = CoachmarkPlacement.Below,
    saving: Boolean = false,
    saveFailure: String? = null
) {
    BackHandler { if (!saving) onDismiss() }
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier.fillMaxSize().semantics { paneTitle = title }
            .pointerInput(saving, onDismiss) { detectTapGestures { if (!saving) onDismiss() } }
    ) {
        Canvas(Modifier.matchParentSize()) {
            val highlighted = anchor.inflate(TeamCityDimensions.smallSpacing.toPx())
            val mask = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, size.width, size.height))
                addRoundRect(RoundRect(highlighted, CornerRadius(TeamCityDimensions.coachmarkCornerRadius.toPx())))
            }
            drawPath(mask, Color.Black.copy(alpha = 0.72f))
            drawRoundRect(Color.White, highlighted.topLeft, highlighted.size, CornerRadius(TeamCityDimensions.coachmarkCornerRadius.toPx()), style = Stroke(TeamCityDimensions.coachmarkStrokeWidth.toPx()))
        }
        val inset = TeamCityDimensions.contentPadding
        val targetTop = with(density) { anchor.top.toDp() }
        val targetBottom = with(density) { anchor.bottom.toDp() }
        val above = placement == CoachmarkPlacement.Above
        val alignStart = anchor.center.x < with(density) { maxWidth.toPx() } / 2
        val topSpace = if (above) inset else targetBottom + inset
        val bottomSpace = if (above) maxHeight - targetTop + inset else inset
        Box(
            Modifier.fillMaxSize().padding(start = inset, end = inset, top = topSpace, bottom = bottomSpace),
            contentAlignment = when {
                above && alignStart -> Alignment.BottomStart
                above -> Alignment.BottomEnd
                alignStart -> Alignment.TopStart
                else -> Alignment.TopEnd
            }
        ) {
            Card(Modifier.widthIn(max = TeamCityDimensions.messageMaxWidth).fillMaxWidth()) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(TeamCityDimensions.largeContentPadding), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.mediumSpacing)) {
                    Text(title, style = MaterialTheme.typography.headlineSmall)
                    Text(description, style = MaterialTheme.typography.bodyLarge)
                    saveFailure?.let { ErrorNotice(it, modifier = Modifier.fillMaxWidth()) }
                    Button(onClick = onDismiss, enabled = !saving, modifier = Modifier.align(Alignment.End).then(dismissModifier)) {
                        if (saving) {
                            CircularProgressIndicator(Modifier.size(TeamCityDimensions.compactProgressIndicatorSize), strokeWidth = TeamCityDimensions.progressStrokeWidth)
                        } else {
                            Text(dismissLabel)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CoachmarkPreview() {
    TeamCityTheme {
        Coachmark("Explore menu", "Open this menu for build actions.", "Continue", Rect(280f, 24f, 328f, 72f), {})
    }
}
