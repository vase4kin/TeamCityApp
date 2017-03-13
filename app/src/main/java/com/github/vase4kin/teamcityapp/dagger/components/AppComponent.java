/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.dagger.components;

import android.content.Context;

import com.github.vase4kin.teamcityapp.api.cache.CacheProviders;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import de.greenrobot.event.EventBus;
import io.rx_cache.internal.RxCache;
import okhttp3.OkHttpClient;

import static com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_AUTH;
import static com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    Context context();

    SharedUserStorage sharedUserStorage();

    @Named(CLIENT_BASE)
    OkHttpClient baseOkHttpClient();

    @Named(CLIENT_AUTH)
    OkHttpClient authOkHttpClient();

    EventBus eventBus();

    RxCache rxCache();

    CacheProviders providers();
}
