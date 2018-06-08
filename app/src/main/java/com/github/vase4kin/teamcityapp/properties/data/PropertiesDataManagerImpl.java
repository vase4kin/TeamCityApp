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

package com.github.vase4kin.teamcityapp.properties.data;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.properties.api.Properties;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Impl of {@link PropertiesDataManager}
 */
public class PropertiesDataManagerImpl extends BaseListRxDataManagerImpl<Properties, Properties.Property> implements PropertiesDataManager {

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull final BuildDetails buildDetails, final OnLoadingListener<List<Properties.Property>> loadingListener) {
        // Getting properties from the build
        Properties properties = buildDetails.getProperties();
        Observable.just(properties)
                .flatMap(new Func1<Properties, Observable<Properties>>() {
                    @Override
                    public Observable<Properties> call(Properties properties) {
                        return properties == null
                                ? Observable.<Properties>empty()
                                : Observable.just(properties);
                    }
                })
                .defaultIfEmpty(new Properties(Collections.<Properties.Property>emptyList()))
                .flatMap(new Func1<Properties, Observable<List<Properties.Property>>>() {
                    @Override
                    public Observable<List<Properties.Property>> call(Properties properties) {
                        return Observable.from(properties.getObjects()).toList();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Properties.Property>>() {
                    @Override
                    public void call(List<Properties.Property> objects) {
                        loadingListener.onSuccess(objects);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        loadingListener.onFail(throwable.getMessage());
                    }
                });
    }
}
