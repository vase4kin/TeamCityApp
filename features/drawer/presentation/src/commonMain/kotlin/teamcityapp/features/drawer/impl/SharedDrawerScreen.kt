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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.*
import teamcityapp.features.drawer.api.*
import teamcityapp.features.drawer.presentation.resources.*
import teamcityapp.libraries.design_system.resolve
import teamcityapp.libraries.theme.SharedErrorNotice
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityLinearLoadingIndicator
import teamcityapp.libraries.theme.TeamCityLoadingIndicator
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.hideClippedActionFromAccessibility

fun accountTag(id: DrawerAccountId) = id.identityKey?.let { "drawer:account:$it" } ?: "drawer:account:${id.serverUrl.length}:${id.serverUrl}:${id.userName}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedDrawerScreen(
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
    listState: LazyListState = rememberLazyListState(),
    onSignOut: (() -> Unit)? = null
) {
    Surface(modifier.widthIn(max = TeamCityDimensions.paneMaxWidth).fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerLow, shape = MaterialTheme.shapes.large) {
        Column(Modifier.navigationBarsPadding()) {
            LazyColumn(
                Modifier.fillMaxWidth().weight(1f, fill = false).testTag("drawer:list"),
                state = listState,
                contentPadding = PaddingValues(
                    top = TeamCityDimensions.contentPadding,
                    bottom = TeamCityDimensions.sectionSpacing
                )
            ) {
                when (val accounts = state.accounts) {
                    DrawerAccountsUiState.Loading -> item("loading") {
                        Row(Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.extraLargeSpacing, vertical = TeamCityDimensions.contentPadding).testTag("drawer:loading"), verticalAlignment = Alignment.CenterVertically) {
                            TeamCityLoadingIndicator(Modifier.size(TeamCityDimensions.iconSize))
                            Spacer(Modifier.width(TeamCityDimensions.contentPadding))
                            Text(stringResource(requireNotNull(state.accountsMessage)), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    DrawerAccountsUiState.Error -> item("load_error") { DrawerFailure(requireNotNull(state.accountsMessage), onRetry, "drawer:load_error") }

                    DrawerAccountsUiState.Empty -> item("empty") { Text(stringResource(requireNotNull(state.accountsMessage)), Modifier.padding(horizontal = TeamCityDimensions.extraLargeSpacing, vertical = TeamCityDimensions.contentPadding).testTag("drawer:empty"), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }

                    is DrawerAccountsUiState.Content -> {
                        items(accounts.accounts.filter { it.isActive }, key = { accountTag(it.id) }) { DrawerAccountRow(it, state.canInteract, onSelect) }
                    }
                }
                val inactive = (state.accounts as? DrawerAccountsUiState.Content)?.accounts.orEmpty().filterNot { it.isActive }
                items(inactive, key = { accountTag(it.id) }) { DrawerAccountRow(it, state.canInteract, onSelect) }
                if ((state.selection is AccountSwitchUiState.Switching || state.selection == AccountSwitchUiState.SigningOut)) {
                    item("switching") {
                        Column(Modifier.padding(horizontal = TeamCityDimensions.contentPadding).testTag("drawer:switching")) {
                            TeamCityLinearLoadingIndicator(Modifier.fillMaxWidth())
                            Text(stringResource(requireNotNull(state.selectionMessage)), Modifier.padding(TeamCityDimensions.contentPadding), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                if (state.selection is AccountSwitchUiState.Error || state.selection == AccountSwitchUiState.SignOutError) item("switch_error") { DrawerFailure(requireNotNull(state.selectionMessage), onRetrySelection, "drawer:switch_error") }
                if (state.selection == AccountSwitchUiState.Missing) item("missing") { DrawerMessage(requireNotNull(state.selectionMessage), Res.string.drawer_dismiss, onDismissMissing, "drawer:missing") }
                item("add") {
                    Spacer(Modifier.height(TeamCityDimensions.smallSpacing))
                    DrawerMenu(Res.drawable.ic_accounts_add, stringResource(Res.string.text_add_account), "drawer:add", state.canInteract, onAddAccount, first = true, tonal = true)
                }
                item("manage") {
                    DrawerMenu(Res.drawable.ic_accounts, stringResource(Res.string.drawer_manage), "drawer:manage", state.canInteract, onManageAccounts, last = true, tonal = true)
                    Spacer(Modifier.height(TeamCityDimensions.sectionSpacing))
                }
                if (onSignOut != null) item("sign-out") { DrawerMenu(Res.drawable.ic_account_outline, stringResource(Res.string.drawer_sign_out), "accounts:sign-out", state.canInteract, onSignOut, first = true, last = true) }
                item("settings") { DrawerMenu(Res.drawable.ic_settings_black_24dp, stringResource(Res.string.drawer_item_settings), "drawer:settings", state.canInteract, onSettings, first = true) }
                item("about") { DrawerMenu(Res.drawable.ic_info_outline_black_24dp, stringResource(Res.string.drawer_item_about), "drawer:about", state.canInteract, onAbout) }
                item("footer") { DrawerFooter(state.canInteract, onPrivacy, onRate) }
            }
        }
    }
}

@Composable
private fun DrawerAccountRow(account: DrawerAccount, enabled: Boolean, onSelect: (DrawerAccountId) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val content = if (account.isActive) colors.onPrimaryContainer else colors.onSurface
    val currentAccount = stringResource(Res.string.drawer_current_account)
    Surface(
        modifier = Modifier.padding(horizontal = TeamCityDimensions.contentPadding).padding(bottom = TeamCityDimensions.extraSmallSpacing).then(if (account.isActive) Modifier.testTag("drawer:active-card") else Modifier),
        color = if (account.isActive) colors.primaryContainer else colors.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            Modifier.fillMaxWidth().then(if (account.isActive) Modifier.semantics(mergeDescendants = true) { stateDescription = currentAccount } else Modifier.hideClippedActionFromAccessibility(enabled).clickable(enabled = enabled, role = Role.Button) { onSelect(account.id) })
                .padding(TeamCityDimensions.contentPadding).testTag(accountTag(account.id)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(Modifier.size(TeamCityDimensions.minimumTouchTarget), color = colors.surfaceContainerLowest, shape = MaterialTheme.shapes.medium) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(if (account.isSslDisabled) Res.drawable.ic_account_alert_outline else Res.drawable.ic_account_outline), null, Modifier.size(TeamCityDimensions.iconSize), tint = colors.primary)
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
                                Icon(painterResource(Res.drawable.ic_check), null, Modifier.size(TeamCityDimensions.smallIconSize), tint = content)
                                Text(stringResource(Res.string.drawer_current_short), style = MaterialTheme.typography.labelMedium, color = content)
                            }
                        }
                    }
                }
                Text(account.id.serverUrl, style = MaterialTheme.typography.bodyMedium, color = if (account.isActive) content else colors.onSurfaceVariant)
                if (account.isSslDisabled) Text(stringResource(Res.string.drawer_ssl_disabled), style = MaterialTheme.typography.labelMedium, color = content)
            }
        }
    }
}

@Composable
private fun DrawerMenu(icon: org.jetbrains.compose.resources.DrawableResource, label: String, tag: String, enabled: Boolean, onClick: () -> Unit, first: Boolean = false, last: Boolean = false, emphasized: Boolean = false, tonal: Boolean = false) {
    val outer = TeamCityDimensions.mediumCornerRadius
    val inner = TeamCityDimensions.extraSmallSpacing
    val labelColor = if (tonal) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else if (emphasized) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val iconColor = if (tonal) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else if (emphasized) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = Modifier.padding(horizontal = TeamCityDimensions.contentPadding).then(if (!last) Modifier.padding(bottom = TeamCityDimensions.drawerRowSpacing) else Modifier),
        color = if (tonal) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = if (first) outer else inner, topEnd = if (first) outer else inner, bottomStart = if (last) outer else inner, bottomEnd = if (last) outer else inner)
    ) {
        Row(
            Modifier.fillMaxWidth().hideClippedActionFromAccessibility(enabled).clickable(enabled = enabled, role = Role.Button, onClick = onClick).heightIn(min = TeamCityDimensions.minimumTouchTarget)
                .padding(TeamCityDimensions.contentPadding).testTag(tag),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(icon), null, Modifier.size(TeamCityDimensions.iconSize), tint = iconColor)
            Spacer(Modifier.width(TeamCityDimensions.contentPadding))
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = labelColor)
            Spacer(Modifier.width(TeamCityDimensions.smallSpacing))
            Icon(painterResource(Res.drawable.ic_chevron_right), null, Modifier.size(TeamCityDimensions.iconSize), tint = iconColor)
        }
    }
}

@Composable
private fun DrawerFailure(message: org.jetbrains.compose.resources.StringResource, onRetry: () -> Unit, tag: String) {
    SharedErrorNotice(stringResource(message), onRetry, Modifier.fillMaxWidth().padding(TeamCityDimensions.contentPadding).testTag(tag))
}

@Composable
private fun DrawerMessage(message: org.jetbrains.compose.resources.StringResource, action: org.jetbrains.compose.resources.StringResource, onAction: () -> Unit, tag: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.extraLargeSpacing, vertical = TeamCityDimensions.contentPadding).testTag(tag)) {
        Text(stringResource(message), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        TextButton(onClick = onAction) { Text(stringResource(action)) }
    }
}

@Composable
private fun DrawerFooter(enabled: Boolean, onPrivacy: () -> Unit, onRate: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        DrawerMenu(Res.drawable.ic_star_outline, stringResource(Res.string.text_rate_the_app), "drawer:rate", enabled, onRate, emphasized = true)
        DrawerMenu(Res.drawable.ic_policy_outline, stringResource(Res.string.about_app_text_privacy), "drawer:privacy", enabled, onPrivacy, last = true)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun DrawerPreview() {
    TeamCityTheme { SharedDrawerScreen(DrawerUiState(DrawerAccountsUiState.Content(listOf(DrawerAccount(DrawerAccountId("https://teamcity.example", "Guest user"), true, false)))), {}, {}, {}, {}, {}, {}, {}) }
}
