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

package teamcityapp.features.manage_accounts.impl.dagger

import android.app.Activity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.manage_accounts.api.navigation.ManageAccountsNavigation
import teamcityapp.features.manage_accounts.api.router.ManageAccountsAppRouter
import teamcityapp.features.manage_accounts.impl.navigation.ManageAccountsNavigationImpl
import teamcityapp.features.manage_accounts.impl.router.ManageAccountsRouter
import teamcityapp.features.manage_accounts.impl.router.ManageAccountsRouterImpl

@Module
@InstallIn(SingletonComponent::class)
object ManageAccountsModule {
    @Provides fun navigation(): ManageAccountsNavigation = ManageAccountsNavigationImpl()
}

@Module
@InstallIn(ActivityComponent::class)
object ManageAccountsActivityModule {
    @Provides fun router(activity: Activity, appRouter: ManageAccountsAppRouter): ManageAccountsRouter = ManageAccountsRouterImpl(activity, appRouter)
}
