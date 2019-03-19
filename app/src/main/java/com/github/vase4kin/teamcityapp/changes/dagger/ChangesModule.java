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

package com.github.vase4kin.teamcityapp.changes.dagger;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataManager;
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataManagerImpl;
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel;
import com.github.vase4kin.teamcityapp.changes.extractor.ChangesValueExtractor;
import com.github.vase4kin.teamcityapp.changes.extractor.ChangesValueExtractorImpl;
import com.github.vase4kin.teamcityapp.changes.view.ChangesAdapter;
import com.github.vase4kin.teamcityapp.changes.view.ChangesView;
import com.github.vase4kin.teamcityapp.changes.view.ChangesViewHolderFactory;
import com.github.vase4kin.teamcityapp.changes.view.ChangesViewImpl;
import com.github.vase4kin.teamcityapp.changes.view.LoadMoreViewHolderFactory;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntKey;
import dagger.multibindings.IntoMap;

@Module
public class ChangesModule {

    private View mView;
    private Fragment mFragment;

    public ChangesModule(View mView, Fragment mFragment) {
        this.mView = mView;
        this.mFragment = mFragment;
    }

    @Provides
    ChangesDataManager providesChangesDataManager(Repository repository, EventBus eventBus) {
        return new ChangesDataManagerImpl(repository, eventBus);
    }

    @Provides
    ChangesView providesChangesView(ChangesAdapter changesAdapter) {
        return new ChangesViewImpl(mView, mFragment.getActivity(), R.string.empty_list_message_changes, changesAdapter);
    }

    @Provides
    ChangesValueExtractor providesChangesValueExtractor() {
        return new ChangesValueExtractorImpl(mFragment.getArguments());
    }

    @Provides
    ViewTracker providesViewTracker() {
        return ViewTracker.STUB;
    }

    @Provides
    ChangesAdapter providesChangesAdapter(Map<Integer, ViewHolderFactory<ChangesDataModel>> viewHolderFactories) {
        return new ChangesAdapter(viewHolderFactories);
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_LOAD_MORE)
    @Provides
    ViewHolderFactory<ChangesDataModel> providesLoadMoreViewHolderFactory() {
        return new LoadMoreViewHolderFactory();
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    ViewHolderFactory<ChangesDataModel> providesChangesViewHolderFactory() {
        return new ChangesViewHolderFactory();
    }
}
