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

package io.rx_cache2.internal.cache;

import com.github.vase4kin.teamcityapp.api.cache.CacheProvidersKt;
import io.rx_cache2.internal.Memory;
import io.rx_cache2.internal.Persistence;
import javax.inject.Inject;

/** Uses RxCache's original key composition without duplicating its disk format. */
public final class BuildCacheKeys extends Action {
    @Inject
    public BuildCacheKeys(Memory memory, Persistence persistence) {
        super(memory, persistence);
    }

    public String buildKey(String href) {
        return composeKey(CacheProvidersKt.BUILD_CACHE_PROVIDER_KEY, href, "");
    }
}
