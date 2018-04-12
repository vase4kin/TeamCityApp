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

package com.github.vase4kin.teamcityapp.favorites.presenter;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenterImpl;
import com.github.vase4kin.teamcityapp.favorites.interactor.FavoritesInteractor;
import com.github.vase4kin.teamcityapp.favorites.view.FavoritesView;
import com.github.vase4kin.teamcityapp.navigation.api.BuildType;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModelImpl;
import com.github.vase4kin.teamcityapp.navigation.extractor.NavigationValueExtractor;
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouter;
import com.github.vase4kin.teamcityapp.navigation.tracker.NavigationTracker;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;

import java.util.List;

import javax.inject.Inject;

/**
 * Presenter to manage logic of favorites list
 */
public class FavoritesPresenterImpl extends BaseListPresenterImpl<
        NavigationDataModel,
        NavigationItem,
        FavoritesView,
        FavoritesInteractor,
        NavigationTracker,
        NavigationValueExtractor> implements FavoritesView.ViewListener {

    private final NavigationRouter router;
    private final OnboardingManager onboardingManager;

    @Inject
    FavoritesPresenterImpl(@NonNull FavoritesView view,
                           @NonNull FavoritesInteractor interactor,
                           @NonNull NavigationTracker tracker,
                           @NonNull NavigationValueExtractor valueExtractor,
                           NavigationRouter router,
                           OnboardingManager onboardingManager) {
        super(view, interactor, tracker, valueExtractor);
        this.router = router;
        this.onboardingManager = onboardingManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadData(@NonNull OnLoadingListener<List<NavigationItem>> loadingListener, boolean update) {
        mDataManager.loadFavorites(loadingListener, update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initViews() {
        super.initViews();
        mView.setViewListener(this);
        if (!onboardingManager.isAddFavPromptShown()) {
            mView.showAddFavPrompt(new OnboardingManager.OnPromptShownListener() {
                @Override
                public void onPromptShown() {
                    onboardingManager.saveAddFavPromptShown();
                }
            });
        }
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
            router.startBuildListActivity(navigationItem.getName(), navigationItem.getId());
        } else {
            router.startNavigationActivity(navigationItem.getName(), navigationItem.getId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFabClick() {
        mView.showInfoSnackbar();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSnackBarAction() {
        router.startProjectActivity();
    }

    @Override
    protected void onSuccessCallBack(List<NavigationItem> data) {
        super.onSuccessCallBack(data);
        int favorites = mDataManager.getFavoritesCount();
        mView.updateTitleCount(favorites);
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
