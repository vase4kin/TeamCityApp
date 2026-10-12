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

package com.github.vase4kin.teamcityapp.di.features.manage_accounts

import com.github.vase4kin.teamcityapp.integration.session.ApplicationAccountSession
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.manage_accounts.api.ManageAccountsRepository
import teamcityapp.features.manage_accounts.impl.data.DefaultManageAccountsRepository
import teamcityapp.libraries.session.AccountSession

@Module
@InstallIn(SingletonComponent::class)
abstract class ManageAccountsDataModule {
    @Binds abstract fun repository(impl: DefaultManageAccountsRepository): ManageAccountsRepository

    @Binds abstract fun session(impl: ApplicationAccountSession): AccountSession
}
