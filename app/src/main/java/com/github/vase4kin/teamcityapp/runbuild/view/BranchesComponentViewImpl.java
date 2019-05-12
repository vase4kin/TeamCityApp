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
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.vase4kin.teamcityapp.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Impl of {@link BranchesComponentView}
 */
public class BranchesComponentViewImpl implements BranchesComponentView {

    @BindView(R.id.autocomplete_branches)
    AutoCompleteTextView mBranchAutocomplete;
    @BindView(R.id.text_no_branches_available)
    TextView mNoBranchesAvailable;
    @BindView(R.id.text_no_branches_available_to_filter)
    TextView mNoBranchesAvailableToFilter;
    @BindView(R.id.progress_branches_loading)
    View mBranchesLoadingProgress;

    private AppCompatActivity mActivity;
    private Unbinder mUnbinder;

    public BranchesComponentViewImpl(AppCompatActivity activity) {
        this.mActivity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews() {
        mUnbinder = ButterKnife.bind(this, mActivity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindViews() {
        mUnbinder.unbind();
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
        BranchArrayAdapter adapter = new BranchArrayAdapter(mActivity, android.R.layout.simple_dropdown_item_1line, branches);
        mBranchAutocomplete.setAdapter(adapter);
        mBranchAutocomplete.setOnItemClickListener((parent, view, position, id) -> hideKeyboard());
        mBranchAutocomplete.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupAutoCompleteForSingleBranch(List<String> branches) {
        mBranchAutocomplete.setAdapter(new ArrayAdapter<>(mActivity, android.R.layout.simple_dropdown_item_1line, branches));
        mBranchAutocomplete.setText(branches.get(0), false);
        mBranchAutocomplete.setEnabled(false);
    }

    private void hideKeyboard() {
        View view = mActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
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
    public void showNoBranchesAvailableToFilter() {
        mNoBranchesAvailableToFilter.setVisibility(View.VISIBLE);
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
    public String getBranchName() {
        return mBranchAutocomplete.getText().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAutocompleteHintForFilter() {
        mBranchAutocomplete.setHint(R.string.hint_default_filter_branch);
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
