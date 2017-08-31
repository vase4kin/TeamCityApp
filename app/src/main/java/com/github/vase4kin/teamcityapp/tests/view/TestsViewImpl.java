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

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModel;
import com.github.vase4kin.teamcityapp.tests.extractor.TestsValueExtractor;
import com.mugen.Mugen;
import com.mugen.MugenCallbacks;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;

/**
 * Impl of {@link TestsView}
 */
public class TestsViewImpl extends BaseListViewImpl<TestsDataModel, SimpleSectionedRecyclerViewAdapter<TestOccurrencesAdapter>> implements TestsView {

    @BindString(R.string.text_passed)
    String mPassedText;

    @BindString(R.string.text_failed)
    String mFailedText;

    @BindString(R.string.text_ignored)
    String mIgnoredText;

    private MugenCallbacks mLoadMoreCallbacks;

    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";
    private int mSelectedId = R.id.show_failed;
    private List<SimpleSectionedRecyclerViewAdapter.Section> mSections = new ArrayList<>();
    private TestsDataModel mTestsDataModel;
    private OnTestsPresenterListener mListener;

    private int mPassed = 0;
    private int mFailed = 0;
    private int mIgnored = 0;

    public TestsViewImpl(View mView,
                         Activity activity,
                         TestsValueExtractor valueExtractor,
                         @StringRes int emptyMessage,
                         SimpleSectionedRecyclerViewAdapter<TestOccurrencesAdapter> adapter) {
        super(mView, activity, emptyMessage, adapter);
        mPassed = valueExtractor.getPassedCount();
        mFailed = valueExtractor.getFailedCount();
        mIgnored = valueExtractor.getIgnoredCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setListener(OnTestsPresenterListener listener) {
        this.mListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(TestsDataModel dataModel) {
        mTestsDataModel = dataModel;
        TestOccurrencesAdapter baseAdapter = mAdapter.getBaseAdapter();
        baseAdapter.setDataModel(dataModel);
        baseAdapter.setOnClickListener(mListener);
        initSections();
        initSectionAdapter();
        Mugen.with(mRecyclerView, mLoadMoreCallbacks).start();
    }

    /**
     * Init sectionAdapter
     */
    private void initSectionAdapter() {
        setSections();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * Set sections for mSectionAdapter
     */
    private void setSections() {
        SimpleSectionedRecyclerViewAdapter.Section[] userStates = new SimpleSectionedRecyclerViewAdapter.Section[mSections.size()];
        mAdapter.setSections(mSections.toArray(userStates));
    }

    /**
     * Return sections for build data
     */
    private void initSections() {
        mSections.clear();
        if (mTestsDataModel.getItemCount() != 0) {
            for (int i = 0; i < mTestsDataModel.getItemCount(); i++) {
                String title = getFormattedTitle(mTestsDataModel.getStatus(i));
                if (mSections.size() != 0) {
                    SimpleSectionedRecyclerViewAdapter.Section prevSection = mSections.get(mSections.size() - 1);
                    if (!prevSection.getTitle().equals(title)) {
                        mSections.add(new SimpleSectionedRecyclerViewAdapter.Section(i, title));
                    }
                } else {
                    mSections.add(new SimpleSectionedRecyclerViewAdapter.Section(i, title));
                }
            }
        }
    }

    /**
     * Get formatted title
     *
     * @param status - Status of the test
     * @return Title
     */
    private String getFormattedTitle(String status) {
        switch (status) {
            case SUCCESS:
                return String.format(mPassedText, mPassed);
            case FAILURE:
                return String.format(mFailedText, mFailed);
            default:
                return String.format(mIgnoredText, mIgnored);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidateOptionsMenu() {
        mActivity.invalidateOptionsMenu();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_test_occurences_fragment, menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mIgnored == 0 &&
                mFailed == 0 &&
                mPassed == 0) {
            menu.clear();
        } else {
            menu.findItem(R.id.show_failed).setVisible(true);
            menu.findItem(R.id.show_ignored).setVisible(true);
            menu.findItem(R.id.show_passed).setVisible(true);
            if (mIgnored == 0) {
                menu.findItem(R.id.show_ignored).setVisible(false);
            }
            if (mFailed == 0 || mSelectedId == R.id.show_failed) {
                menu.findItem(R.id.show_failed).setVisible(false);
            }
            if (mPassed == 0) {
                menu.findItem(R.id.show_passed).setVisible(false);
            }
            if (mSelectedId != 0) {
                menu.findItem(mSelectedId).setVisible(false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mActivity.invalidateOptionsMenu();
        switch (item.getItemId()) {
            case R.id.show_ignored:
                mListener.loadIgnoredTests();
                mSelectedId = R.id.show_ignored;
                return true;
            case R.id.show_failed:
                mListener.loadFailedTests();
                mSelectedId = R.id.show_failed;
                return true;
            case R.id.show_passed:
                mListener.loadSuccessTests();
                mSelectedId = R.id.show_passed;
                return true;
            default:
                return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnLoadMoreListener(MugenCallbacks loadMoreCallbacks) {
        this.mLoadMoreCallbacks = loadMoreCallbacks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLoadMore() {
        TestOccurrencesAdapter baseAdapter = mAdapter.getBaseAdapter();
        baseAdapter.addLoadMore();
        baseAdapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadMore() {
        TestOccurrencesAdapter baseAdapter = mAdapter.getBaseAdapter();
        baseAdapter.removeLoadMore();
        baseAdapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMoreBuilds(TestsDataModel dataModel) {
        TestOccurrencesAdapter baseAdapter = mAdapter.getBaseAdapter();
        baseAdapter.addMoreBuilds(dataModel);
        baseAdapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRetryLoadMoreSnackBar() {
        Snackbar snackBar = Snackbar.make(
                mRecyclerView,
                R.string.load_more_retry_snack_bar_text,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.download_artifact_retry_snack_bar_retry_button, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLoadMoreCallbacks.onLoadMore();
                    }
                });
        TextView textView = (TextView) snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceSkeletonViewContent() {
        replaceSkeletonViewContent(R.layout.layout_skeleton_agent_list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int recyclerViewId() {
        return R.id.tests_recycler_view;
    }
}
