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

package com.github.vase4kin.teamcityapp.tests.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues;
import com.github.vase4kin.teamcityapp.tests.dagger.DaggerTestsComponent;
import com.github.vase4kin.teamcityapp.tests.dagger.TestsModule;
import com.github.vase4kin.teamcityapp.tests.presenter.TestsPresenterImpl;

import javax.inject.Inject;

/**
 * Fragment to manage build tests
 */
public class TestOccurrencesFragment extends Fragment {

    @Inject
    TestsPresenterImpl mPresenter;

    public static TestOccurrencesFragment newInstance(@NonNull String url, int passed, int failed, int ignored) {
        TestOccurrencesFragment fragment = new TestOccurrencesFragment();
        Bundle args = new Bundle();
        args.putString(BundleExtractorValues.URL, url);
        args.putInt(BundleExtractorValues.PASSED_COUNT_PARAM, passed);
        args.putInt(BundleExtractorValues.FAILED_COUNT_PARAM, failed);
        args.putInt(BundleExtractorValues.IGNORED_COUNT_PARAM, ignored);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        setHasOptionsMenu(true);

        // Injecting presenter
        DaggerTestsComponent.builder()
                .testsModule(new TestsModule(view, this))
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mPresenter.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mPresenter.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mPresenter.onOptionsItemSelected(item);
    }
}
