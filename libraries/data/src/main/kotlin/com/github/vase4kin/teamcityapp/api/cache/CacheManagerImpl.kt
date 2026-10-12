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

package com.github.vase4kin.teamcityapp.api.cache

import dagger.Lazy
import teamcityapp.libraries.cache_manager.CacheManager

class CacheManagerImpl(
    private val rxCache: SharedRxCache,
    private val providers: Lazy<CacheProviders>
) : CacheManager {
    override fun evictAllCache() {
        // Resolve the same singleton provider used by the API graph before clearing its cache,
        // including when the account API graph has not been created yet.
        providers.get()
        rxCache.evictAll().ignoreElements().blockingAwait()
    }
}
