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

package com.github.vase4kin.teamcityapp.agents.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.agents.api.Agent;
import com.github.vase4kin.teamcityapp.agents.api.Agents;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.base.tabs.data.OnTextTabChangeEvent;

import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Impl of {@link AgentsDataManager}
 */
public class AgentsDataManagerImpl extends BaseListRxDataManagerImpl<Agents, Agent> implements AgentsDataManager {

    @NonNull
    private Repository mRepository;
    @Nullable
    private EventBus mEventBus;

    /**
     * @param repository - Repository api service
     * @param eventBus   - Event bus
     */
    public AgentsDataManagerImpl(@NonNull Repository repository, @Nullable EventBus eventBus) {
        this.mRepository = repository;
        this.mEventBus = eventBus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(Boolean includeDisconnected,
                     @NonNull OnLoadingListener<List<Agent>> loadingListener,
                     boolean update) {
        load(mRepository.listAgents(includeDisconnected, null, update), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadCount(@NonNull final OnLoadingListener<Integer> loadingListener) {
        mSubscriptions.clear();
        Subscription subscription = mRepository.listAgents(null, "count", false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Agents>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingListener.onSuccess(0);
                    }

                    @Override
                    public void onNext(Agents response) {
                        loadingListener.onSuccess(response.getCount());
                    }
                });
        mSubscriptions.add(subscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postUpdateTabTitleEvent(int size, int type) {
        if (mEventBus != null) {
            mEventBus.post(new OnTextTabChangeEvent(size, type));
        }
    }
}
