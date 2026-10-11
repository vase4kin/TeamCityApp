/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.build_details.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import com.github.vase4kin.teamcityapp.builds.data.homeBuildAccountKey
import com.github.vase4kin.teamcityapp.builds.data.requireCurrentHomeBuildAccount
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.coroutines.IoDispatcher

/** Existing wire contracts remain behind a cancellable, account-checked coroutine boundary. */
class ComposeBuildDetailsRepository @Inject constructor(
    private val repositories: Provider<Repository>,
    private val storage: SharedUserStorage,
    private val mapper: AppBuildLaunchMapper,
    @IoDispatcher private val io: CoroutineDispatcher
) : BuildDetailsRepository {
    override suspend fun triggeredByCurrentUser(build: BuildLaunchData): Boolean = withContext(io) {
        val account = storage.activeUser
        requireCurrentHomeBuildAccount(storage, account.homeBuildAccountKey())
        build.triggered?.let { it.type == "user" && it.user?.username == account.userName } == true
    }
    override suspend fun cancel(build: BuildLaunchData, reAddToQueue: Boolean): BuildDetailsMutation<BuildLaunchData> = mutation {
        val response = cancelBuild(build.href, BuildCancelRequest(reAddToQueue)).await()
        mapper.toLaunchData(response)
    }
    override suspend fun restart(build: BuildLaunchData): BuildDetailsMutation<String> = mutation {
        // Restart preserves the installed branch/parameter request, using the refreshed configuration.
        val request = Build().also {
            it.buildTypeId = requireNotNull(build.buildTypeId)
            it.branchName = build.branchName
            it.properties = mapper.toLegacyBuild(build).properties
        }
        queueBuild(request).await().href
    }
    override suspend fun queuedBuild(href: String): BuildLaunchData = accountOperation { mapper.toLaunchData(build(href, false).await()) }

    private suspend fun <T> mutation(block: suspend Repository.() -> T): BuildDetailsMutation<T> = try {
        BuildDetailsMutation.Success(accountOperation(block))
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: HttpException) {
        if (error.code() == 403) BuildDetailsMutation.Forbidden else BuildDetailsMutation.Failed
    } catch (_: Exception) {
        BuildDetailsMutation.Failed
    }
    private suspend fun <T> accountOperation(block: suspend Repository.() -> T): T = withContext(io) {
        val accountKey = storage.activeUser.homeBuildAccountKey()
        requireCurrentHomeBuildAccount(storage, accountKey)
        val session = repositories.get()
        requireCurrentHomeBuildAccount(storage, accountKey)
        val value = try {
            block(session)
        } catch (error: Exception) {
            requireCurrentHomeBuildAccount(storage, accountKey)
            throw error
        }
        requireCurrentHomeBuildAccount(storage, accountKey)
        value
    }
}
