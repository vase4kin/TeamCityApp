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

package com.github.vase4kin.teamcityapp.navigation.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataManager;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModelImpl;
import com.github.vase4kin.teamcityapp.navigation.extractor.NavigationValueExtractor;
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouter;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationView;
import com.github.vase4kin.teamcityapp.navigation.view.OnNavigationItemClickListener;

import java.util.List;

import javax.inject.Inject;

/**
 * Present to handle logic of {@link com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity}
 */
public class NavigationPresenterImpl extends BaseListPresenterImpl<
        NavigationDataModel, NavigationItem, NavigationView, NavigationDataManager, NavigationValueExtractor> implements OnNavigationItemClickListener {

    private NavigationRouter mRouter;

    @Inject
    NavigationPresenterImpl(@NonNull NavigationView view,
                            @NonNull NavigationDataManager dataManager,
                            @Nullable ViewTracker tracker,
                            @Nullable NavigationValueExtractor valueExtractor,
                            @NonNull NavigationRouter router) {
        super(view, dataManager, tracker, valueExtractor);
        this.mRouter = router;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<NavigationItem>> loadingListener) {
        mDataManager.load(mValueExtractor.getUrl(), loadingListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initViews() {
        super.initViews();
        mView.setTitle(mValueExtractor.getName());
        mView.setNavigationAdapterClickListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NavigationDataModel createModel(List<NavigationItem> data) {
        return new NavigationDataModelImpl(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(NavigationItem navigationItem) {
        if (navigationItem instanceof BuildType) {
            mRouter.startBuildListActivity(navigationItem.getName(), navigationItem.getId());
        } else {
            mRouter.startNavigationActivity(navigationItem.getName(), navigationItem.getHref());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewsDestroyed() {
        super.onViewsDestroyed();
        mDataManager.unsubscribe();
    }
}
