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

package com.github.vase4kin.teamcityapp.artifact.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.artifact.dagger.ArtifactsModule;
import com.github.vase4kin.teamcityapp.artifact.dagger.DaggerArtifactsComponent;
import com.github.vase4kin.teamcityapp.artifact.presenter.ArtifactPresenterImpl;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;

import javax.inject.Inject;

public class ArtifactListFragment extends Fragment {

    @Inject
    ArtifactPresenterImpl mPresenter;

    public static ArtifactListFragment newInstance(Build build, String url) {
        ArtifactListFragment fragment = new ArtifactListFragment();
        Bundle args = new Bundle();
        args.putSerializable(BundleExtractorValues.BUILD, build);
        args.putString(BundleExtractorValues.URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // Injecting presenter to fragment
        DaggerArtifactsComponent.builder()
                .artifactsModule(new ArtifactsModule(view, this))
                .restApiComponent(((TeamCityApplication) getActivity().getApplication()).getRestApiInjector())
                .build()
                .inject(this);

        mPresenter.onViewsCreated();

        // Setting the main container different id to determine correct view to replace
        view.findViewById(R.id.fragment_list).setId(R.id.artifact_fragment_list);

        return view;
    }

    @Override
    public void onDestroyView() {
        mPresenter.onViewsDestroyed();
        super.onDestroyView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
