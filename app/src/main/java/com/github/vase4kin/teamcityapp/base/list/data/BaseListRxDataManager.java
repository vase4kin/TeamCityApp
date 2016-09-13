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

package com.github.vase4kin.teamcityapp.base.list.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.interfaces.Collectible;

import java.util.List;

import rx.Observable;

/**
 * Base data manger to handle server operations with rx
 *
 * @param <T> - Collection of items
 * @param <D> - Single item
 */
public interface BaseListRxDataManager<T extends Collectible<D>, D> {

    /**
     * load data from server
     *
     * @param call            - Retrofit call
     * @param loadingListener - Listener to handle call callbacks
     */
    void load(@NonNull Observable<T> call, @NonNull OnLoadingListener<List<D>> loadingListener);

    /**
     * Unsubscribe rx subscriptions
     */
    void unsubscribe();
}
