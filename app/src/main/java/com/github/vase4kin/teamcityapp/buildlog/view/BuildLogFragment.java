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

package com.github.vase4kin.teamcityapp.buildlog.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.buildlog.dagger.BuildLogModule;
import com.github.vase4kin.teamcityapp.buildlog.dagger.DaggerBuildLogComponent;
import com.github.vase4kin.teamcityapp.buildlog.presenter.BuildLogPresenterImpl;

import javax.inject.Inject;

/**
 * Build log fragment
 */
public class BuildLogFragment extends Fragment {

    @Inject
    BuildLogPresenterImpl mPresenter;

    public static BuildLogFragment newInstance(String buildId) {
        BuildLogFragment fragment = new BuildLogFragment();
        Bundle args = new Bundle();
        args.putString(BundleExtractorValues.BUILD_ID, buildId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_build_log, container, false);

        // Injecting presenter
        DaggerBuildLogComponent.builder()
                .buildLogModule(new BuildLogModule(view, this))
                .appComponent(((TeamCityApplication) getActivity().getApplication()).getAppInjector())
                .build()
                .inject(this);

        mPresenter.onCreateViews();

        return view;
    }

    @Override
    public void onDestroyView() {
        mPresenter.onDestroyViews();
        super.onDestroyView();
    }
}
