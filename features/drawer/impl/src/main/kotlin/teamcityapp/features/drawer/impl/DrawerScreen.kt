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

package teamcityapp.features.drawer.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.features.drawer.api.*
import teamcityapp.libraries.resources.R as SharedR
import teamcityapp.libraries.theme.ErrorNotice
import teamcityapp.libraries.theme.R as ThemeR
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme

internal fun accountTag(id: DrawerAccountId) = "drawer:account:${id.serverUrl.length}:${id.serverUrl}:${id.userName}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerScreen(
    state: DrawerUiState,
    onSelect: (DrawerAccountId) -> Unit,
    onAddAccount: () -> Unit,
    onManageAccounts: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onPrivacy: () -> Unit,
    onRate: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onRetrySelection: () -> Unit = {},
    onDismissMissing: () -> Unit = {},
    listState: LazyListState = rememberLazyListState()
) {
    Surface(modifier.widthIn(max = TeamCityDimensions.paneMaxWidth).fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerLow, shape = MaterialTheme.shapes.large) {
        Column(Modifier.navigationBarsPadding()) {
            BottomSheetDefaults.DragHandle(Modifier.align(Alignment.CenterHorizontally))
            LazyColumn(
                Modifier.fillMaxWidth().weight(1f, fill = false).testTag("drawer:list")
                    .nestedScroll(rememberNestedScrollInteropConnection()),
                state = listState,
                contentPadding = PaddingValues(
                    bottom = TeamCityDimensions.sectionSpacing
                )
            ) {
                item("title") { DrawerHeading(state.canInteract, onManageAccounts) }
                when (val accounts = state.accounts) {
                    DrawerAccountsUiState.Loading -> item("loading") {
                        Row(Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.extraLargeSpacing, vertical = TeamCityDimensions.contentPadding).testTag("drawer:loading"), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(TeamCityDimensions.iconSize), strokeWidth = TeamCityDimensions.progressStrokeWidth)
                            Spacer(Modifier.width(TeamCityDimensions.contentPadding))
                            Text(stringResource(requireNotNull(state.accountsMessageRes)), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    DrawerAccountsUiState.Error -> item("load_error") { DrawerFailure(requireNotNull(state.accountsMessageRes), onRetry, "drawer:load_error") }

                    DrawerAccountsUiState.Empty -> item("empty") { Text(stringResource(requireNotNull(state.accountsMessageRes)), Modifier.padding(horizontal = TeamCityDimensions.extraLargeSpacing, vertical = TeamCityDimensions.contentPadding).testTag("drawer:empty"), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }

                    is DrawerAccountsUiState.Content -> {
                        items(accounts.accounts.filter { it.isActive }, key = { accountTag(it.id) }) { DrawerAccountRow(it, state.canInteract, onSelect) }
                    }
                }
                val inactive = (state.accounts as? DrawerAccountsUiState.Content)?.accounts.orEmpty().filterNot { it.isActive }
                items(inactive, key = { accountTag(it.id) }) { DrawerAccountRow(it, state.canInteract, onSelect) }
                if (state.selection is AccountSwitchUiState.Switching) {
                    item("switching") {
                        Column(Modifier.padding(horizontal = TeamCityDimensions.contentPadding).testTag("drawer:switching")) {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                            Text(stringResource(requireNotNull(state.selectionMessageRes)), Modifier.padding(TeamCityDimensions.contentPadding), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                if (state.selection is AccountSwitchUiState.Error) item("switch_error") { DrawerFailure(requireNotNull(state.selectionMessageRes), onRetrySelection, "drawer:switch_error") }
                if (state.selection == AccountSwitchUiState.Missing) item("missing") { DrawerMessage(requireNotNull(state.selectionMessageRes), R.string.drawer_dismiss, onDismissMissing, "drawer:missing") }
                item("add") {
                    FilledTonalButton(
                        onClick = onAddAccount,
                        enabled = state.canInteract,
                        modifier = Modifier.padding(horizontal = TeamCityDimensions.contentPadding).padding(top = TeamCityDimensions.smallSpacing, bottom = TeamCityDimensions.sectionSpacing).fillMaxWidth().heightIn(min = TeamCityDimensions.minimumTouchTarget).testTag("drawer:add"),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.contentPadding)
                    ) {
                        Icon(painterResource(R.drawable.ic_accounts_add), null, Modifier.size(TeamCityDimensions.iconSize))
                        Spacer(Modifier.width(TeamCityDimensions.smallSpacing))
                        Text(stringResource(R.string.text_add_account))
                    }
                }
                item("settings") { DrawerMenu(R.drawable.ic_settings_black_24dp, stringResource(SharedR.string.drawer_item_settings), "drawer:settings", state.canInteract, onSettings, first = true) }
                item("about") { DrawerMenu(ThemeR.drawable.ic_info_outline_black_24dp, stringResource(SharedR.string.drawer_item_about), "drawer:about", state.canInteract, onAbout) }
                item("footer") { DrawerFooter(state.canInteract, onPrivacy, onRate) }
            }
        }
    }
}

@Composable
private fun DrawerHeading(enabled: Boolean, onManage: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.drawer_accounts_title), Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.width(TeamCityDimensions.smallSpacing))
        TextButton(onClick = onManage, enabled = enabled, modifier = Modifier.heightIn(min = TeamCityDimensions.minimumTouchTarget).testTag("drawer:manage")) {
            Icon(painterResource(R.drawable.ic_accounts), null, Modifier.size(TeamCityDimensions.iconSize))
            Spacer(Modifier.width(TeamCityDimensions.extraSmallSpacing))
            Text(stringResource(R.string.drawer_manage))
        }
    }
}

@Composable
private fun DrawerAccountRow(account: DrawerAccount, enabled: Boolean, onSelect: (DrawerAccountId) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val content = if (account.isActive) colors.onPrimaryContainer else colors.onSurface
    val currentAccount = stringResource(R.string.drawer_current_account)
    Surface(
        modifier = Modifier.padding(horizontal = TeamCityDimensions.contentPadding).padding(bottom = TeamCityDimensions.extraSmallSpacing).then(if (account.isActive) Modifier.testTag("drawer:active-card") else Modifier),
        color = if (account.isActive) colors.primaryContainer else colors.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            Modifier.fillMaxWidth().then(if (account.isActive) Modifier.semantics(mergeDescendants = true) { stateDescription = currentAccount } else Modifier.clickable(enabled = enabled, role = Role.Button) { onSelect(account.id) })
                .padding(TeamCityDimensions.contentPadding).testTag(accountTag(account.id)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(Modifier.size(TeamCityDimensions.minimumTouchTarget), color = colors.surfaceContainerLowest, shape = MaterialTheme.shapes.medium) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(if (account.isSslDisabled) R.drawable.ic_account_alert_outline else R.drawable.ic_account_outline), null, Modifier.size(TeamCityDimensions.iconSize), tint = colors.primary)
                }
            }
            Spacer(Modifier.width(TeamCityDimensions.contentPadding))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.extraSmallSpacing)) {
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing),
                    verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.extraSmallSpacing)
                ) {
                    Text(account.id.userName, style = MaterialTheme.typography.titleMedium, color = content)
                    if (account.isActive) {
                        Surface(
                            Modifier.testTag("drawer:current-marker").clearAndSetSemantics {},
                            color = colors.primary.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(TeamCityDimensions.smallCornerRadius)
                        ) {
                            Row(
                                Modifier.padding(horizontal = TeamCityDimensions.smallSpacing, vertical = TeamCityDimensions.extraSmallSpacing),
                                horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.extraSmallSpacing),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(painterResource(R.drawable.ic_check), null, Modifier.size(TeamCityDimensions.smallIconSize), tint = content)
                                Text(stringResource(R.string.drawer_current_short), style = MaterialTheme.typography.labelMedium, color = content)
                            }
                        }
                    }
                }
                Text(account.id.serverUrl, style = MaterialTheme.typography.bodyMedium, color = if (account.isActive) content else colors.onSurfaceVariant)
                if (account.isSslDisabled) Text(stringResource(R.string.drawer_ssl_disabled), style = MaterialTheme.typography.labelMedium, color = content)
            }
        }
    }
}

@Composable
private fun DrawerMenu(icon: Int, label: String, tag: String, enabled: Boolean, onClick: () -> Unit, first: Boolean = false, last: Boolean = false, emphasized: Boolean = false) {
    val outer = TeamCityDimensions.mediumCornerRadius
    val inner = TeamCityDimensions.extraSmallSpacing
    val labelColor = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val iconColor = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = Modifier.padding(horizontal = TeamCityDimensions.contentPadding).then(if (!last) Modifier.padding(bottom = TeamCityDimensions.drawerRowSpacing) else Modifier),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = if (first) outer else inner, topEnd = if (first) outer else inner, bottomStart = if (last) outer else inner, bottomEnd = if (last) outer else inner)
    ) {
        Row(
            Modifier.fillMaxWidth().clickable(enabled = enabled, role = Role.Button, onClick = onClick).heightIn(min = TeamCityDimensions.minimumTouchTarget)
                .padding(TeamCityDimensions.contentPadding).testTag(tag),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(icon), null, Modifier.size(TeamCityDimensions.iconSize), tint = iconColor)
            Spacer(Modifier.width(TeamCityDimensions.contentPadding))
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = labelColor)
            Spacer(Modifier.width(TeamCityDimensions.smallSpacing))
            Icon(painterResource(R.drawable.ic_chevron_right), null, Modifier.size(TeamCityDimensions.iconSize), tint = iconColor)
        }
    }
}

@Composable
private fun DrawerFailure(message: Int, onRetry: () -> Unit, tag: String) {
    ErrorNotice(stringResource(message), onRetry, Modifier.fillMaxWidth().padding(TeamCityDimensions.contentPadding).testTag(tag))
}

@Composable
private fun DrawerMessage(message: Int, action: Int, onAction: () -> Unit, tag: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.extraLargeSpacing, vertical = TeamCityDimensions.contentPadding).testTag(tag)) {
        Text(stringResource(message), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        TextButton(onClick = onAction) { Text(stringResource(action)) }
    }
}

@Composable
private fun DrawerFooter(enabled: Boolean, onPrivacy: () -> Unit, onRate: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        DrawerMenu(R.drawable.ic_star_outline, stringResource(R.string.text_rate_the_app), "drawer:rate", enabled, onRate, emphasized = true)
        DrawerMenu(R.drawable.ic_policy_outline, stringResource(SharedR.string.about_app_text_privacy), "drawer:privacy", enabled, onPrivacy, last = true)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun DrawerPreview() {
    TeamCityTheme { DrawerScreen(DrawerUiState(DrawerAccountsUiState.Content(listOf(DrawerAccount(DrawerAccountId("https://teamcity.example", "Guest user"), true, false)))), {}, {}, {}, {}, {}, {}, {}) }
}
