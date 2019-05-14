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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.dagger.BottomSheetModule;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.dagger.DaggerBottomSheetComponent;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.presenter.BottomSheetPresenterImpl;

import javax.inject.Inject;

/**
 * Bottom sheet dialog
 */
public class BottomSheetDialogFragment extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {

    @Inject
    BottomSheetPresenterImpl presenter;

    public BottomSheetDialogFragment() {
    }

    public static BottomSheetDialogFragment createBottomSheetDialog(String title, String description, int menuType) {
        return createBottomSheetDialog(title, new String[]{description}, menuType);
    }

    public static BottomSheetDialogFragment createBottomSheetDialog(String title, String[] descriptions, int menuType) {
        BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BottomSheetModule.ARG_TITLE, title);
        bundle.putStringArray(BottomSheetModule.ARG_DESCRIPTION, descriptions);
        bundle.putInt(BottomSheetModule.ARG_BOTTOM_SHEET_TYPE, menuType);
        bottomSheetDialogFragment.setArguments(bundle);
        return bottomSheetDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(com.google.android.material.bottomsheet.BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
