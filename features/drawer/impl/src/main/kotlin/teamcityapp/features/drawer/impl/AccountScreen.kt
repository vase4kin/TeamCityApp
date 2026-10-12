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

package teamcityapp.features.drawer.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.features.drawer.api.DrawerAccountId
import teamcityapp.libraries.theme.*
import teamcityapp.libraries.theme.R as ThemeR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
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
    onNavigateUp: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    BindTeamCityAppBarToList(listState, scrollBehavior.state)
    BackHandler(enabled = !state.canInteract) {}
    Scaffold(modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.account_title), style = MaterialTheme.typography.headlineMedium) },
            navigationIcon = {
                IconButton(onNavigateUp, enabled = state.canInteract, modifier = Modifier.testTag("account:back")) {
                    Icon(painterResource(ThemeR.drawable.ic_arrow_back_24dp), stringResource(R.string.account_back))
                }
            },
            colors = teamCityTopAppBarColors(),
            scrollBehavior = scrollBehavior,
            modifier = Modifier.testTag("screen:app-bar")
        )
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            DrawerScreen(
                state, onSelect, onAddAccount, onManageAccounts, onSettings, onAbout, onPrivacy, onRate,
                modifier = Modifier.fillMaxHeight().testTag("account:content"), onRetry = onRetry, onRetrySelection = onRetrySelection,
                onDismissMissing = onDismissMissing, listState = listState
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountPreview() {
    TeamCityTheme { AccountScreen(DrawerUiState(DrawerAccountsUiState.Empty), {}, {}, {}, {}, {}, {}, {}) }
}
