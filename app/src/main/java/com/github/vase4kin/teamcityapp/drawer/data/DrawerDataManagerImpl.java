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

package com.github.vase4kin.teamcityapp.drawer.data;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManager;
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManagerImpl;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.queue.data.BuildQueueDataManagerImpl;
import com.github.vase4kin.teamcityapp.runningbuilds.data.RunningBuildsDataManager;
import com.github.vase4kin.teamcityapp.runningbuilds.data.RunningBuildsDataManagerImpl;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Impl of {@link DrawerDataManager}
 */
public class DrawerDataManagerImpl implements DrawerDataManager {

    protected final SharedUserStorage sharedUserStorage;
    private final RunningBuildsDataManager runningBuildsDataManager;
    private final RunningBuildsDataManager queuedBuildsDataManager;
    private final AgentsDataManager agentsDataManager;

    public DrawerDataManagerImpl(Repository repository,
                                 SharedUserStorage sharedUserStorage,
                                 EventBus eventBus) {
        this.sharedUserStorage = sharedUserStorage;
        this.runningBuildsDataManager = new RunningBuildsDataManagerImpl(repository, sharedUserStorage);
        this.queuedBuildsDataManager = new BuildQueueDataManagerImpl(repository, sharedUserStorage);
        this.agentsDataManager = new AgentsDataManagerImpl(repository, eventBus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(OnLoadingListener<List<UserAccount>> loadingListener) {
        loadingListener.onSuccess(sharedUserStorage.getUserAccounts());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActiveUser(String url, String userName) {
        sharedUserStorage.setUserActive(url, userName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActiveUser(String url, String userName) {
        UserAccount userAccount = sharedUserStorage.getActiveUser();
        return userAccount.getTeamcityUrl().equals(url) &&
                userAccount.getUserName().equals(userName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadRunningBuildsCount(final OnLoadingListener<Integer> loadingListener) {
        runningBuildsDataManager.loadCount(loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFavoriteRunningBuildsCount(OnLoadingListener<Integer> loadingListener) {
        runningBuildsDataManager.loadFavoritesCount(loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadConnectedAgentsCount(final OnLoadingListener<Integer> loadingListener) {
        agentsDataManager.loadCount(loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadBuildQueueCount(final OnLoadingListener<Integer> loadingListener) {
        queuedBuildsDataManager.loadCount(loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFavoritesCount() {
        return sharedUserStorage.getFavoriteBuildTypeIds().size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe() {
        runningBuildsDataManager.unsubscribe();
        agentsDataManager.unsubscribe();
        queuedBuildsDataManager.unsubscribe();
    }
}
