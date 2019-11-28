/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.features.manage_accounts.viewmodel

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import org.junit.After
import org.junit.Before
import org.junit.Test
import teamcityapp.features.manage_accounts.router.ManageAccountsRouter
import teamcityapp.features.manage_accounts.tracker.ManageAccountsTracker
import teamcityapp.libraries.cache_manager.CacheManager
import teamcityapp.libraries.storage.Storage
import teamcityapp.libraries.storage.models.UserAccount

class ManageAccountsViewModelTest {

    private val storage: Storage = mock()
    private val router: ManageAccountsRouter = mock()
    private val tracker: ManageAccountsTracker = mock()
    private val showSslDisabledInfoDialog: () -> Unit = mock()
    private val showRemoveAccountDialog: (onAccountRemove: () -> Unit) -> Unit = mock()
    private val cacheManager: CacheManager = mock()
    private val adapter: GroupAdapter<GroupieViewHolder> = mock()
    private val userAccount: UserAccount = mock()

    private lateinit var viewModel: ManageAccountsViewModel

    @Before
    fun setUp() {
        viewModel = ManageAccountsViewModel(
            storage,
            router,
            tracker,
            showSslDisabledInfoDialog,
            showRemoveAccountDialog,
            cacheManager,
            adapter
        )
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(
            storage,
            router,
            tracker,
            showSslDisabledInfoDialog,
            showRemoveAccountDialog,
            cacheManager,
            adapter
        )
    }

    @Test
    fun testOnAccountRemove_OnlyOneAccount() {
        doAnswer { listOf(userAccount) }.whenever(storage).userAccounts
        viewModel.onAccountRemove(userAccount).invoke()
        verify(storage).userAccounts
        verify(tracker).trackAccountRemove()
        verify(storage).removeUserAccount(userAccount)
        verify(cacheManager).evictAllCache()
        verify(router).openLogin()
    }

    @Test
    fun testOnAccountRemove_AccountIsActive() {
        whenever(userAccount.isActive).thenReturn(true)
        doAnswer { listOf(userAccount, mock()) }.whenever(storage).userAccounts
        viewModel.onAccountRemove(userAccount).invoke()
        verify(storage).userAccounts
        verify(tracker).trackAccountRemove()
        verify(storage).removeUserAccount(userAccount)
        verify(storage).setOtherUserActive()
        verify(router).openHome()
    }
}