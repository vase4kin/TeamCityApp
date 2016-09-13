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

package com.github.vase4kin.teamcityapp.changes.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.changes.dagger.ChangesModule;
import com.github.vase4kin.teamcityapp.changes.dagger.DaggerChangesComponent;
import com.github.vase4kin.teamcityapp.changes.presenter.ChangesPresenterImpl;

import javax.inject.Inject;

/**
 * Fragment to manage changes
 */
public class ChangesFragment extends Fragment {

    @Inject
    ChangesPresenterImpl mPresenter;

    public static Fragment newInstance(@NonNull String url) {
        ChangesFragment fragment = new ChangesFragment();
        Bundle args = new Bundle();
        args.putString(BundleExtractorValues.URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // Injecting presenter to fragment
        DaggerChangesComponent.builder()
                .changesModule(new ChangesModule(view, this))
                .restApiComponent(((TeamCityApplication) getActivity().getApplication()).getRestApiInjector())
                .build()
                .inject(this);

        mPresenter.onViewsCreated();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.onViewsDestroyed();
    }
}
