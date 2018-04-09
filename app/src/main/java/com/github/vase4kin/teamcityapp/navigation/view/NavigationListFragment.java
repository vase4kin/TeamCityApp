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

package com.github.vase4kin.teamcityapp.navigation.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.navigation.dagger.DaggerNavigationFragmentComponent;
import com.github.vase4kin.teamcityapp.navigation.dagger.NavigationModule;
import com.github.vase4kin.teamcityapp.navigation.presenter.NavigationPresenterImpl;

import javax.inject.Inject;

/**
 * Navigation fragment to handle first projects screen for {@link com.github.vase4kin.teamcityapp.root.view.RootProjectsActivity}
 */
public class NavigationListFragment extends Fragment {

    @Inject
    NavigationPresenterImpl mPresenter;

    public static NavigationListFragment newInstance(String title, String url) {
        NavigationListFragment fragment = new NavigationListFragment();
        Bundle args = new Bundle();
        args.putString(BundleExtractorValues.URL, url);
        args.putString(BundleExtractorValues.NAME, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // Injecting presenter
        DaggerNavigationFragmentComponent.builder()
                .navigationModule(new NavigationModule(view, getActivity(), getArguments()))
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

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.onResume();
    }
}
