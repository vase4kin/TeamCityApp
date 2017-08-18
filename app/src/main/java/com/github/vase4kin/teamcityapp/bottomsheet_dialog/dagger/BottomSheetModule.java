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

import android.view.View;

import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModel;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModelImpl;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetInteractor;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetInteractorImpl;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetItem;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetView;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetViewImpl;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;

/**
 * Bottom sheet dialog dependencies
 */
@Module
public class BottomSheetModule {

    private final View view;

    public BottomSheetModule(View view) {
        this.view = view;
    }

    @Provides
    BottomSheetDataModel providesBottomSheetDataModel() {
        List<BottomSheetItem> list = new ArrayList<>();
        list.add(new BottomSheetItem("Copy"));
        list.add(new BottomSheetItem("Check"));
        return new BottomSheetDataModelImpl(list);
    }

    @Provides
    BottomSheetInteractor providesInteractor(BottomSheetDataModel model) {
        return new BottomSheetInteractorImpl("Title", model);
    }

    @Provides
    BottomSheetView providesBottomSheetView() {
        return new BottomSheetViewImpl(view);
    }
}
