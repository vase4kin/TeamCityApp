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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.dagger.BottomSheetModule;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.dagger.DaggerBottomSheetComponent;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.presenter.BottomSheetPresenterImpl;

import javax.inject.Inject;

/**
 * Bottom sheet dialog
 */
public class BottomSheetDialog extends BottomSheetDialogFragment {

    @Inject
    BottomSheetPresenterImpl presenter;

    public BottomSheetDialog() {
    }

    public static BottomSheetDialog createBottomSheetDialog(String title, String description, int menuType) {
        return createBottomSheetDialog(title, new String[]{description}, menuType);
    }

    public static BottomSheetDialog createBottomSheetDialog(String title, String[] descriptions, int menuType) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog();
        Bundle bundle = new Bundle();
        bundle.putString(BottomSheetModule.ARG_TITLE, title);
        bundle.putStringArray(BottomSheetModule.ARG_DESCRIPTION, descriptions);
        bundle.putInt(BottomSheetModule.ARG_BOTTOM_SHEET_TYPE, menuType);
        bottomSheetDialog.setArguments(bundle);
        return bottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.dialog_bottom_sheet, null);

        // Injecting presenter
        DaggerBottomSheetComponent.builder()
                .bottomSheetModule(new BottomSheetModule(view, this))
                .appComponent(((TeamCityApplication) getActivity().getApplication()).getAppInjector())
                .build()
                .inject(this);

        presenter.handleOnCreateView();
        return view;
    }

    @Override
    public void onDestroyView() {
        presenter.handleOnDestroyView();
        super.onDestroyView();
    }
}
