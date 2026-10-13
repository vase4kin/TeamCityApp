/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.build_details.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BuildDetailsScreen(
    state: BuildDetailsUiState,
    onUp: () -> Unit,
    onAction: (BuildDetailsAction) -> Unit,
    onConfirm: (Boolean) -> Unit,
    onDismissConfirmation: () -> Unit,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
    onDismissPrompt: () -> Unit = {},
    onRetryMetadata: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    BackHandler(state.busy) {}
    val anchors = remember { CoachmarkAnchors() }
    val prompt = state.prompt?.takeIf { !state.busy && state.confirmation == null }
    val promptBounds = prompt?.let { anchors.bounds("details:action:${it.action}") }
    var actionHeight by remember { mutableStateOf(TeamCityDimensions.noSpacing) }
    val density = LocalDensity.current
    BoxWithConstraints(modifier.fillMaxSize().coachmarkHost(anchors)) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).then(if (promptBounds != null) Modifier.clearAndSetSemantics { } else Modifier),
            topBar = {
                TopAppBar(
                    modifier = Modifier.testTag("screen:app-bar"),
                    colors = teamCityTopAppBarColors(),
                    scrollBehavior = scrollBehavior,
                    title = {
                        Column {
                            Text(state.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (state.subtitle.isNotEmpty()) Text(state.subtitle, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    navigationIcon = { IconButton(onClick = onUp, enabled = !state.busy) { Icon(painterResource(R.drawable.ic_details_back), stringResource(R.string.navigate_up)) } }
                )
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                BuildDetailsFloatingActions(
                    state,
                    onAction,
                    anchors,
                    Modifier.widthIn(max = (maxWidth - TeamCityDimensions.extraLargeSpacing).coerceAtLeast(TeamCityDimensions.minimumTouchTarget))
                        .onSizeChanged { actionHeight = with(density) { it.height.toDp() } }
                )
            },
            snackbarHost = { SnackbarHost(snackbar) }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
                if (state.countFailures.isNotEmpty() || state.onboardingFailed) {
                    TextButton(onClick = onRetryMetadata, enabled = !state.busy, modifier = Modifier.testTag("details:retry-metadata")) { Text(stringResource(R.string.retry_build_metadata)) }
                }
                CompositionLocalProvider(LocalFloatingActionClearance provides (actionHeight + TeamCityDimensions.extraLargeSpacing)) {
                    Box(Modifier.weight(1f)) { CompositionLocalProvider(LocalTeamCityAppBarState provides scrollBehavior.state) { content() } }
                }
            }
        }
        if (prompt != null && promptBounds != null) {
            Coachmark(
                title = stringResource(prompt.title),
                description = stringResource(prompt.description),
                dismissLabel = stringResource(R.string.continue_build_tip),
                anchor = promptBounds,
                placement = CoachmarkPlacement.Above,
                onDismiss = onDismissPrompt,
                modifier = Modifier.testTag("details:coachmark:${prompt.prompt}"),
                dismissModifier = Modifier.testTag("details:dismiss-prompt"),
                saving = prompt.saving,
                saveFailure = if (prompt.saveFailed) stringResource(R.string.build_tip_save_failed) else null
            )
        }
    }
    val confirmation = state.confirmation
    if (confirmation != null) {
        // Key checkbox state to the actual confirmed snapshot, including process restoration.
        var reAdd by rememberSaveable(confirmation) { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = onDismissConfirmation,
            text = {
                Column {
                    Text(stringResource(confirmation.message))
                    if (confirmation.kind == ConfirmationKind.Stop) {
                        Row(Modifier.testTag("details:readd").toggleable(value = reAdd, role = Role.Checkbox, onValueChange = { reAdd = it })) {
                            Checkbox(checked = reAdd, onCheckedChange = null, modifier = Modifier.minimumInteractiveComponentSize())
                            Text(stringResource(R.string.text_re_add_build), modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { onConfirm(reAdd) }, modifier = Modifier.testTag("details:confirm")) { Text(stringResource(confirmation.confirmLabel)) } },
            dismissButton = { TextButton(onClick = onDismissConfirmation) { Text(stringResource(R.string.text_cancel_button)) } }
        )
    }
}

@Preview
@Composable
private fun BuildDetailsPreview() {
    TeamCityTheme {
        BuildDetailsScreen(BuildDetailsUiState(BuildLaunchData("42", "/builds/42", number = "42", state = "finished"), configurationName = "Build Android"), {}, {}, {}, {}, remember { SnackbarHostState() }) {
            Surface(Modifier.fillMaxSize()) { Text("Build #42", Modifier.padding(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
private fun BuildDetailsFloatingActions(state: BuildDetailsUiState, onAction: (BuildDetailsAction) -> Unit, anchors: CoachmarkAnchors, modifier: Modifier = Modifier) {
    FlowRow(
        modifier.fillMaxWidth().testTag("details:actions"),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.floatingActions.isNotEmpty()) {
            TeamCityFloatingToolbar(modifier = Modifier.testTag("details:toolbar")) {
                state.floatingActions.forEach { action ->
                    IconButton(onClick = { onAction(action.action) }, enabled = !state.busy, modifier = Modifier.testTag("details:action:${action.action}").coachmarkAnchor("details:action:${action.action}", anchors)) {
                        Icon(painterResource(if (action.action == BuildDetailsAction.Share) R.drawable.ic_details_share else R.drawable.ic_details_browser), stringResource(action.label))
                    }
                }
            }
        }
        val action = state.primaryAction
        TeamCityLoadingExtendedFloatingActionButton(
            onClick = { onAction(action.action) },
            label = stringResource(action.label),
            loadingLabel = stringResource(state.actionLoadingLabelRes),
            loading = state.busy,
            modifier = Modifier.heightIn(min = FloatingToolbarDefaults.ContainerSize).testTag("details:action:${action.action}").coachmarkAnchor("details:action:${action.action}", anchors).then(if (state.busy) Modifier.semantics { disabled() } else Modifier),
            containerColor = if (action.action == BuildDetailsAction.Cancel) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (action.action == BuildDetailsAction.Cancel) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
            icon = { Icon(painterResource(if (action.action == BuildDetailsAction.Cancel) R.drawable.ic_details_stop else R.drawable.ic_details_restart), null) }
        )
    }
}
