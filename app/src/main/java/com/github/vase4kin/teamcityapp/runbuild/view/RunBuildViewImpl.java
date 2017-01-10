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

package com.github.vase4kin.teamcityapp.runbuild.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListenerImpl;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Impl of {@link RunBuildView}
 */
public class RunBuildViewImpl implements RunBuildView {

    @BindView(R.id.autocomplete_branches)
    AutoCompleteTextView mBranchAutocomplete;
    @BindView(R.id.text_no_branches_available)
    TextView mNoBranchesAvailable;
    @BindView(R.id.progress_branches_loading)
    View mBranchesLoadingProgress;
    @BindView(R.id.fab_queue_build)
    FloatingActionButton mQueueBuildFab;
    @BindView(R.id.container)
    View mContainer;

    private MaterialDialog mProgressDialog;

    private RunBuildActivity mActivity;
    private Unbinder mUnbinder;

    public RunBuildViewImpl(RunBuildActivity activity) {
        this.mActivity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(final ViewListener listener) {
        mUnbinder = ButterKnife.bind(this, mActivity);

        Toolbar mToolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);
        mActivity.setSupportActionBar(mToolbar);

        ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_run_build);
        }

        // For ui testing purpose
        mToolbar.setNavigationContentDescription(R.string.navigate_up);
        mToolbar.setNavigationIcon(new IconDrawable(mActivity, MaterialIcons.md_close).color(Color.WHITE).actionBarSize());
        mToolbar.setNavigationOnClickListener(new OnToolBarNavigationListenerImpl(listener));

        mQueueBuildFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBuildQueue(mBranchAutocomplete.getText().toString());
            }
        });

        mProgressDialog = new MaterialDialog.Builder(mActivity)
                .content(R.string.text_queueing_build)
                .progress(true, 0)
                .autoDismiss(false)
                .build();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideBranchesLoadingProgress() {
        mBranchesLoadingProgress.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupAutoComplete(final List<String> branches) {
        mBranchAutocomplete.setAdapter(new BranchArrayAdapter(mActivity, android.R.layout.simple_dropdown_item_1line, branches));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupAutoCompleteForSingleBranch(List<String> branches) {
        mBranchAutocomplete.setAdapter(new ArrayAdapter<>(mActivity, android.R.layout.simple_dropdown_item_1line, branches));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mBranchAutocomplete.setText(branches.get(0), false);
        }
        mBranchAutocomplete.setEnabled(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showNoBranchesAvailable() {
        mNoBranchesAvailable.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showBranchesAutoComplete() {
        mBranchAutocomplete.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showQueuingBuildProgress() {
        mProgressDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideQueuingBuildProgress() {
        mProgressDialog.dismiss();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showForbiddenErrorSnackbar() {
        Snackbar snackBar = Snackbar.make(
                mContainer,
                R.string.error_forbidden_error,
                Snackbar.LENGTH_LONG);
        TextView textView = (TextView) snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showErrorSnackbar() {
        Snackbar snackBar = Snackbar.make(
                mContainer,
                R.string.error_base_error,
                Snackbar.LENGTH_LONG);
        TextView textView = (TextView) snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindViews() {
        mUnbinder.unbind();
    }

    /**
     * Branches adapter with custom branch filtering
     */
    private static class BranchArrayAdapter extends ArrayAdapter<String> {

        /**
         * Branches to show and filter
         */
        private final List<String> branches;
        /**
         * Branch filter
         */
        private BranchFilter mFilter;

        /**
         * Constructor
         *
         * @param context  The current context.
         * @param resource The resource ID for a layout file containing a TextView to use when
         *                 instantiating views.
         * @param objects  The objects to represent in the ListView.
         */
        BranchArrayAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            this.branches = new ArrayList<>(objects);
        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new BranchFilter();
            }
            return mFilter;
        }

        /**
         * Branch filter
         */
        private class BranchFilter extends Filter {

            /**
             * {@inheritDoc}
             */
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults results = new FilterResults();
                if (TextUtils.isEmpty(constraint)) {
                    results.values = branches;
                    results.count = branches.size();
                } else {
                    List<String> newValues = new ArrayList<>();
                    for (String branch : branches) {
                        if (branch.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            newValues.add(branch);
                        }
                    }
                    results.values = newValues;
                    results.count = newValues.size();
                }
                return results;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count > 0) {
                    clear();
                    addAll((List<String>) results.values);
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    }
}
