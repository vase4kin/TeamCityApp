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

package com.github.vase4kin.teamcityapp.overview.view;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.overview.dagger.DaggerOverviewComponent;
import com.github.vase4kin.teamcityapp.overview.dagger.OverviewModule;
import com.github.vase4kin.teamcityapp.overview.presenter.OverviewPresenterImpl;

import javax.inject.Inject;

/**
 * Fragment to handle Build overview screen
 */
public class OverviewFragment extends Fragment {

    @Inject
    OverviewPresenterImpl mPresenter;

    public static OverviewFragment newInstance(Build build) {
        OverviewFragment fragment = new OverviewFragment();
        Bundle args = new Bundle();
        args.putSerializable(BundleExtractorValues.BUILD, build);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview_list, container, false);
        setHasOptionsMenu(true);

        // Injecting presenter
        DaggerOverviewComponent.builder()
                .overviewModule(new OverviewModule(view, this))
                .restApiComponent(((TeamCityApplication) getActivity().getApplication()).getRestApiInjector())
                .build()
                .inject(this);

        mPresenter.onCreate();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.onStart();
    }

    @Override
    public void onStop() {
        mPresenter.onStop();
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mPresenter.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mPresenter.onOptionsItemSelected(item);
    }
}
