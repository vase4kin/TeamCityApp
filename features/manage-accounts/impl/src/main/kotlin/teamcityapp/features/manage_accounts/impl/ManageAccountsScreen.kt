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

package teamcityapp.features.manage_accounts.impl

import android.text.Spanned
import android.text.style.URLSpan
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.manage_accounts.api.ManagedAccount
import teamcityapp.features.manage_accounts.api.ManagedAccountId
import teamcityapp.libraries.resources.R as SharedR
import teamcityapp.libraries.theme.*
import teamcityapp.libraries.theme.R as ThemeR

internal fun accountTag(id: ManagedAccountId) = "accounts:row:${id.serverUrl}:${id.userName}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAccountsScreen(
    state: ManageAccountsUiState,
    onRemove: (ManagedAccountId) -> Unit,
    onSslWarning: () -> Unit,
    onCreateAccount: () -> Unit,
    onClose: () -> Unit,
    onRetry: () -> Unit = {},
    onRetryRemoval: () -> Unit = {},
    dialog: ManageAccountsDialog = ManageAccountsDialog.None,
    onDismissDialog: () -> Unit = {},
    onConfirmRemoval: (ManagedAccountId) -> Unit = {}
) {
    TeamCityScreen(
        stringResource(R.string.title_activity_account_list),
        onClose,
        navigation = ScreenNavigation.Back,
        scrollToolbarWithContent = true
    ) { modifier ->
        BoxWithConstraints(modifier.testTag("accounts:body")) {
            val removalFailed = state.removal is AccountRemovalUiState.Error
            val reserveActionLane = removalFailed || state.accounts == AccountListUiState.Error
            var addActionHeight by remember { mutableIntStateOf(0) }
            val addActionSpace = if (addActionHeight == 0) 112.dp else with(LocalDensity.current) { addActionHeight.toDp() }
            val feedbackMaxHeight = maxHeight / 3
            Column(Modifier.fillMaxSize().padding(bottom = if (reserveActionLane) addActionSpace else 0.dp)) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (val accounts = state.accounts) {
                        AccountListUiState.Loading -> LoadingContent(Modifier.fillMaxSize().testTag("accounts:loading"))

                        AccountListUiState.Empty -> MessageContent(stringResource(R.string.accounts_empty), Modifier.fillMaxSize().testTag("accounts:empty"))

                        AccountListUiState.Error -> ErrorContent(Modifier.fillMaxSize(), onRetry, message = stringResource(R.string.accounts_load_error))

                        is AccountListUiState.Content -> LazyColumn(
                            Modifier.fillMaxSize().testTag("accounts:list"),
                            contentPadding = PaddingValues(bottom = if (removalFailed) 0.dp else addActionSpace)
                        ) {
                            items(accounts.accounts, key = { listOf(it.id.serverUrl, it.id.userName).joinToString("\u0000") }) { account ->
                                AccountRow(account, state.canInteract, { onRemove(account.id) }, onSslWarning)
                            }
                        }
                    }
                }
                if (removalFailed) {
                    ErrorNotice(
                        stringResource(R.string.accounts_remove_error),
                        onRetryRemoval,
                        Modifier.fillMaxWidth().heightIn(max = feedbackMaxHeight).padding(horizontal = TeamCityDimensions.contentPadding).testTag("accounts:remove_error")
                    )
                }
            }
            if (state.removal is AccountRemovalUiState.Removing) LinearProgressIndicator(Modifier.fillMaxWidth().testTag("accounts:removing"))
            TeamCityExtendedFloatingActionButton(
                onClick = { if (state.canInteract) onCreateAccount() },
                modifier = Modifier.align(Alignment.BottomEnd).onSizeChanged { addActionHeight = it.height }.padding(TeamCityDimensions.contentPadding).testTag("accounts:add").semantics { if (!state.canInteract) disabled() }
            ) {
                Icon(painterResource(ThemeR.drawable.ic_add_black_24dp), null, Modifier.size(TeamCityDimensions.iconSize))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.accounts_add))
            }
        }
    }
    if (state.canInteract) {
        when (dialog) {
            ManageAccountsDialog.None -> Unit
            ManageAccountsDialog.SslWarning -> AccountsDialog(true, onDismissDialog, {})
            is ManageAccountsDialog.ConfirmRemoval -> AccountsDialog(false, onDismissDialog) { onConfirmRemoval(dialog.id) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountRow(account: ManagedAccount, enabled: Boolean, onRemove: () -> Unit, onSslWarning: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val content = if (account.isActive) colors.onPrimaryContainer else colors.onSurface
    val current = stringResource(R.string.accounts_current)
    val removeDescription = stringResource(R.string.accounts_remove_description, account.id.userName, account.id.serverUrl)
    Card(
        Modifier.fillMaxWidth().padding(horizontal = TeamCityDimensions.contentPadding, vertical = TeamCityDimensions.smallSpacing).testTag(accountTag(account.id)),
        colors = CardDefaults.cardColors(containerColor = if (account.isActive) colors.primaryContainer else colors.surfaceContainerHigh, contentColor = content),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(TeamCityDimensions.contentPadding), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing)) {
                Row(
                    Modifier.weight(1f).semantics(mergeDescendants = true) { if (account.isActive) stateDescription = current }.testTag("${accountTag(account.id)}:identity"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing + TeamCityDimensions.extraSmallSpacing)
                ) {
                    Surface(Modifier.size(TeamCityDimensions.minimumTouchTarget), color = colors.surfaceContainerLowest, shape = MaterialTheme.shapes.medium) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(painterResource(R.drawable.ic_account), null, Modifier.size(TeamCityDimensions.iconSize), tint = colors.primary)
                        }
                    }
                    FlowRow(
                        Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing),
                        verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.extraSmallSpacing)
                    ) {
                        Text(account.id.userName, style = MaterialTheme.typography.titleMedium, color = content)
                        if (account.isActive) {
                            Surface(
                                Modifier.testTag("${accountTag(account.id)}:current").clearAndSetSemantics {},
                                color = colors.primary.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(TeamCityDimensions.smallSpacing + TeamCityDimensions.extraSmallSpacing)
                            ) {
                                Row(
                                    Modifier.padding(horizontal = TeamCityDimensions.smallSpacing, vertical = TeamCityDimensions.extraSmallSpacing),
                                    horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.extraSmallSpacing),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(painterResource(R.drawable.ic_check), null, Modifier.size(TeamCityDimensions.contentPadding), tint = content)
                                    Text(stringResource(R.string.accounts_current_short), style = MaterialTheme.typography.labelMedium, color = content)
                                }
                            }
                        }
                    }
                }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                    tooltip = { PlainTooltip { Text(stringResource(R.string.accounts_remove)) } },
                    state = rememberTooltipState(),
                    enableUserInput = enabled
                ) {
                    FilledTonalIconButton(
                        onClick = onRemove,
                        enabled = enabled,
                        modifier = Modifier.size(TeamCityDimensions.minimumTouchTarget).testTag("${accountTag(account.id)}:remove"),
                        shape = MaterialTheme.shapes.medium,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = colors.errorContainer.copy(alpha = 0.35f), contentColor = colors.error)
                    ) {
                        Icon(painterResource(R.drawable.ic_delete), removeDescription, Modifier.size(TeamCityDimensions.iconSize))
                    }
                }
            }
            Text(account.id.serverUrl, Modifier.fillMaxWidth().testTag("${accountTag(account.id)}:url"), style = MaterialTheme.typography.bodyMedium, color = if (account.isActive) content else colors.onSurfaceVariant)
            if (account.isSslDisabled) {
                TextButton(onSslWarning, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = TeamCityDimensions.minimumTouchTarget).testTag("${accountTag(account.id)}:ssl"), colors = ButtonDefaults.textButtonColors(contentColor = teamCityStatusColors().warning.onContainer, containerColor = teamCityStatusColors().warning.container)) {
                    Text(stringResource(R.string.text_account_un_secure_ssl_), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun warningText() = run {
    val resources = LocalResources.current
    val text = resources.getText(SharedR.string.warning_ssl_dialog_content)
    val linkColor = MaterialTheme.colorScheme.primary
    remember(text, linkColor) {
        buildAnnotatedString {
            append(text.toString())
            if (text is Spanned) {
                text.getSpans(0, text.length, URLSpan::class.java).forEach { span ->
                    addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), text.getSpanStart(span), text.getSpanEnd(span))
                }
            }
        }
    }
}

@Composable
private fun AccountsDialog(warning: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("accounts:dialog"),
        title = if (warning) {
            { Text(stringResource(SharedR.string.warning_ssl_dialog_title)) }
        } else {
            null
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (warning) {
                    Text(warningText(), style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text(stringResource(R.string.dialog_remove_not_active_account_positive_content_text), style = MaterialTheme.typography.bodyLarge)
                }
            }
        },
        dismissButton = if (!warning) {
            {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dialog_remove_active_account_positive_negative_text))
                }
            }
        } else {
            null
        },
        confirmButton = {
            TextButton(onClick = if (warning) onDismiss else onConfirm) {
                Text(stringResource(if (warning) android.R.string.ok else R.string.dialog_remove_active_account_positive_button_text))
            }
        }
    )
}

@Preview @Composable
private fun AccountsPreview() {
    TeamCityTheme { ManageAccountsScreen(ManageAccountsUiState(AccountListUiState.Content(listOf(ManagedAccount(ManagedAccountId("https://teamcity.example", "Guest user"), true, true)))), {}, {}, {}, {}) }
}

@Preview @Composable
private fun WarningPreview() {
    TeamCityTheme { AccountsDialog(true, {}, {}) }
}
