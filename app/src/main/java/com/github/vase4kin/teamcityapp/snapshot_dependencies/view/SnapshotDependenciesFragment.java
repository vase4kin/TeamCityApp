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

package com.github.vase4kin.teamcityapp.snapshot_dependencies.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.dagger.DaggerSnapshotDependenciesComponent;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.dagger.SnapshotDependenciesModule;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.presenter.SnapshotDependenciesPresenterImpl;

import javax.inject.Inject;

/**
 * Snapshot dependencies build lust fragment
 */
public class SnapshotDependenciesFragment extends Fragment {

    @Inject
    SnapshotDependenciesPresenterImpl presenter;

    public static SnapshotDependenciesFragment newInstance(String id) {
        SnapshotDependenciesFragment fragment = new SnapshotDependenciesFragment();
        Bundle args = new Bundle();
        args.putString(BundleExtractorValues.ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_snapshot_dependencies, container, false);

        // Injecting presenter
        DaggerSnapshotDependenciesComponent.builder()
                .snapshotDepsModule(new SnapshotDependenciesModule(view, (AppCompatActivity) this.getActivity(), getArguments()))
                .restApiComponent(((TeamCityApplication) getActivity().getApplication()).getRestApiInjector())
                .build()
                .inject(this);

        presenter.onViewsCreated();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.onViewsDestroyed();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }
}
