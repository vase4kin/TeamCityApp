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

package io.rx_cache2.internal;

import com.github.vase4kin.teamcityapp.api.cache.CacheProviders;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.rx_cache2.ConfigProvider;
import java.lang.reflect.Proxy;

/**
 * Small package bridge to RxCache's package-private translator. Keeps the original annotated
 * provider methods and dynamic keys without reflecting into private fields.
 */
public final class CacheProviderProxy {
    private CacheProviderProxy() {}

    public static CacheProviders create(ProcessorProviders processor) {
        ProxyTranslator translator = new ProxyTranslator();
        return (CacheProviders)
                Proxy.newProxyInstance(
                        CacheProviders.class.getClassLoader(),
                        new Class<?>[] {CacheProviders.class},
                        (proxy, method, args) -> {
                            if (method.getDeclaringClass() == Object.class) {
                                switch (method.getName()) {
                                    case "toString":
                                        return "SharedRxCache providers";
                                    case "hashCode":
                                        return System.identityHashCode(proxy);
                                    case "equals":
                                        return proxy == args[0];
                                    default:
                                        throw new UnsupportedOperationException(method.getName());
                                }
                            }
                            ConfigProvider config = translator.processMethod(method, args);
                            // RxCache checks its startup flag before subscribing to a shared,
                            // non-replaying signal. If startup finishes in that gap, this request
                            // completes empty without subscribing to its loader. Re-enter once:
                            // the startup flag is now set and the same config can load normally.
                            return Single.fromObservable(
                                    processor
                                            .process(config)
                                            .switchIfEmpty(
                                                    Observable.defer(
                                                            () -> processor.process(config))));
                        });
    }
}
