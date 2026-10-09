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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
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
        Box(modifier) {
            when (val accounts = state.accounts) {
                AccountListUiState.Loading -> LoadingContent(Modifier.fillMaxSize().testTag("accounts:loading"))

                AccountListUiState.Empty -> Box(Modifier.fillMaxSize().testTag("accounts:empty"))

                AccountListUiState.Error -> CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                    MessageContent(stringResource(R.string.accounts_load_error), Modifier.fillMaxSize()) {
                        TextButton(onClick = onRetry) { Text(stringResource(R.string.accounts_retry)) }
                    }
                }

                is AccountListUiState.Content -> LazyColumn(
                    Modifier.fillMaxSize().testTag("accounts:list"),
                    contentPadding = PaddingValues(bottom = 112.dp)
                ) {
                    items(accounts.accounts, key = { listOf(it.id.serverUrl, it.id.userName).joinToString("\u0000") }) { account ->
                        AccountRow(account, state.canInteract, { onRemove(account.id) }, onSslWarning)
                    }
                }
            }
            if (state.removal is AccountRemovalUiState.Removing) LinearProgressIndicator(Modifier.fillMaxWidth().testTag("accounts:removing"))
            if (state.removal is AccountRemovalUiState.Error) {
                Surface(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = TeamCityDimensions.contentPadding, end = TeamCityDimensions.contentPadding, bottom = 112.dp).testTag("accounts:remove_error"),
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.small,
                    shadowElevation = 2.dp
                ) {
                    Column(Modifier.padding(TeamCityDimensions.contentPadding)) {
                        Text(stringResource(R.string.accounts_remove_error), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        TextButton(onClick = onRetryRemoval) { Text(stringResource(R.string.accounts_retry)) }
                    }
                }
            }
            TeamCityFloatingActionButton(
                onClick = { if (state.canInteract) onCreateAccount() },
                modifier = Modifier.align(Alignment.BottomEnd).padding(TeamCityDimensions.contentPadding).testTag("accounts:add").semantics { if (!state.canInteract) disabled() }
            ) { Icon(painterResource(ThemeR.drawable.ic_add_black_24dp), stringResource(R.string.accounts_add), Modifier.size(TeamCityDimensions.iconSize)) }
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

@Composable
private fun AccountRow(account: ManagedAccount, enabled: Boolean, onRemove: () -> Unit, onSslWarning: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(
                Modifier.fillMaxWidth().testTag(accountTag(account.id)).clickable(enabled = enabled, role = Role.Button, onClick = onRemove)
                    .padding(horizontal = TeamCityDimensions.contentPadding * 2, vertical = TeamCityDimensions.contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(if (account.isSslDisabled) R.drawable.ic_account_alert else R.drawable.ic_account),
                    null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(TeamCityDimensions.iconSize)
                )
                Column(Modifier.weight(1f).padding(start = TeamCityDimensions.contentPadding)) {
                    Text(account.id.userName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(account.id.serverUrl, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (account.isSslDisabled) {
                        val configuration = LocalViewConfiguration.current
                        // Match the legacy warning's bounds. Expanding its touch target would
                        // intercept taps on the server label immediately above it.
                        val warningConfiguration = remember(configuration) {
                            object : ViewConfiguration by configuration {
                                override val minimumTouchTargetSize = DpSize.Zero
                            }
                        }
                        CompositionLocalProvider(LocalViewConfiguration provides warningConfiguration) {
                            Text(
                                stringResource(R.string.text_account_un_secure_ssl_),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = TeamCityDimensions.extraSmallSpacing).clickable(enabled = enabled, role = Role.Button, onClick = onSslWarning)
                            )
                        }
                    }
                }
            }
            HorizontalDivider(Modifier.padding(horizontal = TeamCityDimensions.contentPadding))
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
    val locale = LocalLocale.current.platformLocale
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
                    Text(stringResource(R.string.dialog_remove_active_account_positive_negative_text).uppercase(locale))
                }
            }
        } else {
            null
        },
        confirmButton = {
            TextButton(onClick = if (warning) onDismiss else onConfirm) {
                Text(stringResource(if (warning) android.R.string.ok else R.string.dialog_remove_active_account_positive_button_text).uppercase(locale))
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
