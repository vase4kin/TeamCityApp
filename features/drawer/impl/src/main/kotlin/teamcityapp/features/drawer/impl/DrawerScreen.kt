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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.drawer.api.*
import teamcityapp.libraries.resources.R as SharedR
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
        Column {
            BottomSheetDefaults.DragHandle(Modifier.align(Alignment.CenterHorizontally))
            LazyColumn(
                Modifier.fillMaxWidth().weight(1f, fill = false).testTag("drawer:list")
                    .nestedScroll(rememberNestedScrollInteropConnection()),
                state = listState,
                contentPadding = PaddingValues(
                    start = TeamCityDimensions.contentPadding,
                    end = TeamCityDimensions.contentPadding,
                    bottom = TeamCityDimensions.sectionSpacing
                )
            ) {
                when (val accounts = state.accounts) {
                    DrawerAccountsUiState.Loading -> item("loading") {
                        Row(Modifier.fillMaxWidth().padding(TeamCityDimensions.contentPadding).testTag("drawer:loading"), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(TeamCityDimensions.iconSize), strokeWidth = 2.dp)
                            Spacer(Modifier.width(TeamCityDimensions.contentPadding))
                            Text(stringResource(R.string.drawer_loading), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    DrawerAccountsUiState.Error -> item("load_error") { DrawerMessage(R.string.drawer_load_error, R.string.drawer_retry, onRetry, "drawer:load_error") }

                    DrawerAccountsUiState.Empty -> item("empty") { Box(Modifier.testTag("drawer:empty")) }

                    is DrawerAccountsUiState.Content -> {
                        items(accounts.accounts.filter { it.isActive }, key = { accountTag(it.id) }) { DrawerAccountRow(it, state.canInteract, onSelect) }
                    }
                }
                item("accounts_divider") {
                    DrawerDivider()
                }
                val inactive = (state.accounts as? DrawerAccountsUiState.Content)?.accounts.orEmpty().filterNot { it.isActive }
                items(inactive, key = { accountTag(it.id) }) { DrawerAccountRow(it, state.canInteract, onSelect) }
                if (state.selection is AccountSwitchUiState.Switching) {
                    item("switching") {
                        Column(Modifier.testTag("drawer:switching")) {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                            Text(stringResource(R.string.drawer_switching), Modifier.padding(TeamCityDimensions.contentPadding), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                if (state.selection is AccountSwitchUiState.Error) item("switch_error") { DrawerMessage(R.string.drawer_switch_error, R.string.drawer_retry, onRetrySelection, "drawer:switch_error") }
                if (state.selection == AccountSwitchUiState.Missing) item("missing") { DrawerMessage(R.string.drawer_missing_account, R.string.drawer_dismiss, onDismissMissing, "drawer:missing") }
                item("add") { DrawerMenu(R.drawable.ic_accounts_add, stringResource(R.string.text_add_account), "drawer:add", state.canInteract, onAddAccount) }
                item("manage") { DrawerMenu(R.drawable.ic_accounts, stringResource(R.string.text_manage_accounts), "drawer:manage", state.canInteract, onManageAccounts) }
                item("divider_settings") { DrawerDivider() }
                item("settings") { DrawerMenu(R.drawable.ic_settings_black_24dp, stringResource(SharedR.string.drawer_item_settings), "drawer:settings", state.canInteract, onSettings) }
                item("divider_about") { DrawerDivider() }
                item("about") { DrawerMenu(ThemeR.drawable.ic_info_outline_black_24dp, stringResource(SharedR.string.drawer_item_about), "drawer:about", state.canInteract, onAbout) }
                item("divider_footer") { DrawerDivider() }
                item("footer") { DrawerFooter(state.canInteract, onPrivacy, onRate) }
            }
        }
    }
}

@Composable
private fun DrawerAccountRow(account: DrawerAccount, enabled: Boolean, onSelect: (DrawerAccountId) -> Unit) {
    val clickable = if (!account.isActive) Modifier.clickable(enabled = enabled, role = Role.Button) { onSelect(account.id) } else Modifier
    Row(Modifier.fillMaxWidth().then(clickable).padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing).testTag(accountTag(account.id)), verticalAlignment = Alignment.CenterVertically) {
        val icon = when {
            account.isActive -> R.drawable.ic_account_check_outline
            account.isSslDisabled -> R.drawable.ic_account_alert_outline
            else -> R.drawable.ic_account_outline
        }
        val description = when {
            account.isActive -> stringResource(R.string.drawer_active_account)
            account.isSslDisabled -> stringResource(R.string.drawer_ssl_disabled)
            else -> null
        }
        Icon(painterResource(icon), description, Modifier.size(TeamCityDimensions.iconSize), tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(TeamCityDimensions.contentPadding))
        Column(Modifier.weight(1f)) {
            Text(account.id.userName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(account.id.serverUrl, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DrawerMenu(icon: Int, label: String, tag: String, enabled: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(enabled = enabled, role = Role.Button, onClick = onClick).heightIn(min = TeamCityDimensions.minimumTouchTarget)
            .padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing).testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(icon), null, Modifier.size(TeamCityDimensions.iconSize), tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(TeamCityDimensions.contentPadding))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable private fun DrawerDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun DrawerMessage(message: Int, action: Int, onAction: () -> Unit, tag: String) {
    Column(Modifier.fillMaxWidth().padding(TeamCityDimensions.contentPadding).testTag(tag)) {
        Text(stringResource(message), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        TextButton(onClick = onAction) { Text(stringResource(action)) }
    }
}

@Composable
private fun DrawerFooter(enabled: Boolean, onPrivacy: () -> Unit, onRate: () -> Unit) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = TeamCityDimensions.extraSmallSpacing, bottom = TeamCityDimensions.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onPrivacy,
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(contentColor = color),
            modifier = Modifier.weight(1f).testTag("drawer:privacy")
        ) {
            Text(stringResource(SharedR.string.about_app_text_privacy), style = MaterialTheme.typography.bodyMedium)
        }
        Text(stringResource(R.string.text_divider), color = color, style = MaterialTheme.typography.bodyMedium)
        TextButton(
            onClick = onRate,
            enabled = enabled,
            colors = ButtonDefaults.textButtonColors(contentColor = color),
            modifier = Modifier.weight(1f).testTag("drawer:rate")
        ) {
            Text(stringResource(R.string.text_rate_the_app), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun DrawerPreview() {
    TeamCityTheme { DrawerScreen(DrawerUiState(DrawerAccountsUiState.Content(listOf(DrawerAccount(DrawerAccountId("https://teamcity.example", "Guest user"), true, false)))), {}, {}, {}, {}, {}, {}, {}) }
}
