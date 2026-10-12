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

package com.github.vase4kin.teamcityapp.api.cache

import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import io.reactivex.Observable
import io.rx_cache2.MigrationCache
import io.rx_cache2.internal.CacheProviderProxy
import io.rx_cache2.internal.Disk
import io.rx_cache2.internal.Memory
import io.rx_cache2.internal.Persistence
import io.rx_cache2.internal.ProcessorProviders
import io.rx_cache2.internal.ProcessorProvidersBehaviour
import io.rx_cache2.internal.encrypt.BuiltInEncryptor
import io.rx_cache2.internal.encrypt.Encryptor
import io.victoralbertos.jolyglot.JolyglotGenerics
import java.io.File
import javax.inject.Singleton

/**
 * Compatibility boundary for RxCache 1.8.3-2.x, whose builder cannot accept a Memory.
 * Reuses its processor, disk envelopes, provider annotations and default policies with
 * snapshotting memory. No lock spans a network request or changes its Rx cancellation.
 */
class SharedRxCache private constructor(private val component: SharedRxCacheComponent) {
    private val processor by lazy { component.processor() }
    val providers: CacheProviders by lazy { CacheProviderProxy.create(processor) }

    fun evictAll(): Observable<Void> = processor.evictAll()

    companion object {
        @JvmStatic
        fun create(directory: File, serializer: JolyglotGenerics): SharedRxCache {
            require(directory.isDirectory && directory.canWrite()) { "A writable cache directory is required" }
            return SharedRxCache(
                DaggerSharedRxCacheComponent.builder()
                    .directory(directory)
                    .serializer(serializer)
                    .useExpiredData(false)
                    .maxCacheSize(100)
                    .encryptionKey("")
                    .migrations(emptyList())
                    .build()
            )
        }
    }
}

@Singleton
@Component(modules = [SharedRxCacheModule::class])
internal interface SharedRxCacheComponent {
    fun processor(): ProcessorProviders

    @Component.Builder
    interface Builder {
        @BindsInstance fun directory(value: File): Builder

        @BindsInstance fun serializer(value: JolyglotGenerics): Builder

        @BindsInstance fun useExpiredData(value: Boolean): Builder

        @BindsInstance fun maxCacheSize(value: Int): Builder

        @BindsInstance fun encryptionKey(value: String): Builder

        @BindsInstance fun migrations(value: List<MigrationCache>): Builder
        fun build(): SharedRxCacheComponent
    }
}

/** The existing CacheProviders contract has no encryption or migration annotations. */
@Module
@DisableInstallInCheck // Isolated legacy RxCache graph; these unqualified bindings must not enter Hilt.
internal object SharedRxCacheModule {
    @Provides @Singleton
    fun memory(): Memory = SnapshotCacheMemory()

    @Provides @Singleton
    fun persistence(disk: Disk): Persistence = disk

    @Provides @Singleton
    fun encryptor(): Encryptor = BuiltInEncryptor()

    @Provides @Singleton
    fun processor(behavior: ProcessorProvidersBehaviour): ProcessorProviders = behavior
}
