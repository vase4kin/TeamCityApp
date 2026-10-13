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

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview

/** Submission feedback stays in its originating control; full-width actions retain their bounds. */
@Composable
fun TeamCityLoadingButton(
    onClick: () -> Unit,
    label: String,
    loadingLabel: String,
    loading: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outlined: Boolean = false,
    shape: Shape = ButtonDefaults.shape,
    icon: (@Composable () -> Unit)? = null
) {
    val feedback = modifier.semantics {
        contentDescription = label
        if (loading) stateDescription = loadingLabel
    }
    val content: @Composable RowScope.() -> Unit = { LoadingButtonContent(label, loadingLabel, loading, icon) }
    if (outlined) {
        val colors = ButtonDefaults.outlinedButtonColors()
        OutlinedButton(onClick, feedback, enabled && !loading, shape = shape, colors = if (loading) colors.copy(disabledContentColor = colors.contentColor) else colors, content = content)
    } else {
        val colors = ButtonDefaults.buttonColors()
        Button(onClick, feedback, enabled && !loading, shape = shape, colors = if (loading) colors.copy(disabledContainerColor = colors.containerColor, disabledContentColor = colors.contentColor) else colors, content = content)
    }
}

/** Floating actions retain their colors, dimensions and confirmation behavior while busy. */
@Composable
fun TeamCityLoadingExtendedFloatingActionButton(
    onClick: () -> Unit,
    label: String,
    loadingLabel: String,
    loading: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    icon: @Composable () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = { if (enabled && !loading) onClick() },
        modifier = modifier.semantics {
            contentDescription = label
            if (!enabled || loading) disabled()
            if (loading) stateDescription = loadingLabel
        },
        containerColor = containerColor,
        contentColor = contentColor,
        icon = { LoadingButtonIcon(loading, icon) },
        text = { LoadingButtonLabel(label, loadingLabel, loading, reserveLabelWidth = true) }
    )
}

@Composable
private fun RowScope.LoadingButtonContent(label: String, loadingLabel: String, loading: Boolean, icon: (@Composable () -> Unit)?, reserveLabelWidth: Boolean = false) {
    if (loading || icon != null) {
        LoadingButtonIcon(loading, icon)
        Spacer(Modifier.width(TeamCityDimensions.mediumSpacing))
    }
    LoadingButtonLabel(label, loadingLabel, loading, reserveLabelWidth)
}

@Composable
private fun LoadingButtonIcon(loading: Boolean, icon: (@Composable () -> Unit)?) {
    Box(Modifier.size(TeamCityDimensions.compactProgressIndicatorSize), contentAlignment = Alignment.Center) {
        if (loading) TeamCityLoadingIndicator(Modifier.fillMaxSize(), color = LocalContentColor.current) else icon?.invoke()
    }
}

@Composable
private fun LoadingButtonLabel(label: String, loadingLabel: String, loading: Boolean, reserveLabelWidth: Boolean) {
    if (!reserveLabelWidth) {
        Text(if (loading) loadingLabel else label)
        return
    }
    val measurer = rememberTextMeasurer()
    val style = LocalTextStyle.current
    val width = maxOf(measurer.measure(label, style).size.width, measurer.measure(loadingLabel, style).size.width)
    Box(Modifier.width(with(LocalDensity.current) { width.toDp() }), contentAlignment = Alignment.Center) {
        Text(if (loading) loadingLabel else label)
    }
}

@Preview
@Composable
private fun LoadingButtonsPreview() {
    TeamCityTheme {
        Column(verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.contentPadding)) {
            TeamCityLoadingButton({}, "Sign in", "Signing in…", false)
            TeamCityLoadingButton({}, "Sign in", "Signing in…", true)
            TeamCityLoadingExtendedFloatingActionButton({}, "Restart", "Restarting…", true, icon = {})
        }
    }
}
