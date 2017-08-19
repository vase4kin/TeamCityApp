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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.dagger;

import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;

import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.BranchMenuItemsFactory;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.DefaultMenuItemsFactory;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.MenuItemsFactory;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModel;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModelImpl;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetInteractor;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetInteractorImpl;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetAdapter;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetItemViewHolderFactory;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetView;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetViewImpl;
import com.github.vase4kin.teamcityapp.overview.tracker.FabricOverviewTrackerImpl;
import com.github.vase4kin.teamcityapp.overview.tracker.FirebaseOverviewTrackerImpl;
import com.github.vase4kin.teamcityapp.overview.tracker.OverviewTracker;
import com.github.vase4kin.teamcityapp.overview.tracker.OverviewTrackerImpl;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.Set;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntKey;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;

/**
 * Bottom sheet dialog dependencies
 */
@Module
public class BottomSheetModule {

    public static final String ARG_TITLE = "arg_title";
    public static final String ARG_DESCRIPTION = "arg_description";
    public static final String ARG_BOTTOM_SHEET_TYPE = "arg_bottom_sheet_type";

    private final View view;
    private final BottomSheetDialogFragment fragment;
    private final int menuType;
    private final String title;
    private final String description;

    public BottomSheetModule(View view, BottomSheetDialogFragment fragment) {
        this.view = view;
        this.fragment = fragment;
        this.menuType = fragment.getArguments().getInt(ARG_BOTTOM_SHEET_TYPE);
        this.title = fragment.getArguments().getString(ARG_TITLE);
        this.description = fragment.getArguments().getString(ARG_DESCRIPTION);
    }

    @Provides
    BottomSheetDataModel providesBottomSheetDataModel(Map<Integer, MenuItemsFactory> menuItemsFactories) {
        return new BottomSheetDataModelImpl(menuItemsFactories.get(menuType).createMenuItems());
    }

    @Provides
    BottomSheetInteractor providesInteractor(BottomSheetDataModel model, EventBus eventBus) {
        return new BottomSheetInteractorImpl(title, model, view.getContext(), eventBus);
    }

    @Provides
    BottomSheetView providesBottomSheetView(BottomSheetAdapter adapter) {
        return new BottomSheetViewImpl(view, fragment, adapter);
    }

    @IntoMap
    @IntKey(DefaultMenuItemsFactory.TYPE_DEFAULT)
    @Provides
    MenuItemsFactory providesDefaultMenu() {
        return new DefaultMenuItemsFactory(view.getContext(), description);
    }

    @IntoMap
    @IntKey(DefaultMenuItemsFactory.TYPE_BRANCH)
    @Provides
    MenuItemsFactory providesBranchMenu() {
        return new BranchMenuItemsFactory(view.getContext(), description);
    }

    @Provides
    BottomSheetAdapter providesAdapter(Map<Integer, ViewHolderFactory<BottomSheetDataModel>> viewHolderFactories) {
        return new BottomSheetAdapter(viewHolderFactories);
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    ViewHolderFactory<BottomSheetDataModel> providesViewHolderFactory() {
        return new BottomSheetItemViewHolderFactory();
    }

    @IntoSet
    @Provides
    OverviewTracker providesFabricViewTracker() {
        return new FabricOverviewTrackerImpl();
    }

    @IntoSet
    @Provides
    OverviewTracker providesFirebaseViewTracker(FirebaseAnalytics firebaseAnalytics) {
        return new FirebaseOverviewTrackerImpl(firebaseAnalytics);
    }

    @Provides
    OverviewTracker providesViewTracker(Set<OverviewTracker> trackers) {
        return new OverviewTrackerImpl(trackers);
    }
}
