/*
 * Copyright 2019 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.runbuild.view

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.github.vase4kin.teamcityapp.R
import java.util.ArrayList

/**
 * Impl of [BranchesComponentView]
 */
class BranchesComponentViewImpl(private val activity: AppCompatActivity) : BranchesComponentView {

    @BindView(R.id.autocomplete_branches)
    lateinit var branchAutocomplete: AutoCompleteTextView
    @BindView(R.id.text_no_branches_available)
    lateinit var noBranchesAvailable: TextView
    @BindView(R.id.text_no_branches_available_to_filter)
    lateinit var noBranchesAvailableToFilter: TextView
    @BindView(R.id.progress_branches_loading)
    lateinit var branchesLoadingProgress: View
    lateinit var unbinder: Unbinder

    /**
     * {@inheritDoc}
     */
    override val branchName: String
        get() = branchAutocomplete.text.toString()

    /**
     * {@inheritDoc}
     */
    override fun initViews() {
        unbinder = ButterKnife.bind(this, activity)
    }

    /**
     * {@inheritDoc}
     */
    override fun unbindViews() {
        unbinder.unbind()
    }

    /**
     * {@inheritDoc}
     */
    override fun hideBranchesLoadingProgress() {
        branchesLoadingProgress.visibility = View.GONE
    }

    /**
     * {@inheritDoc}
     */
    override fun setupAutoComplete(branches: List<String>) {
        val adapter =
            BranchArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, branches)
        branchAutocomplete.setAdapter(adapter)
        branchAutocomplete.setOnItemClickListener { _, _, _, _ -> hideKeyboard() }
        branchAutocomplete.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
            }
            true
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun setupAutoCompleteForSingleBranch(branches: List<String>) {
        branchAutocomplete.setAdapter(
            ArrayAdapter(
                activity,
                android.R.layout.simple_dropdown_item_1line,
                branches
            )
        )
        branchAutocomplete.setText(branches[0], false)
        branchAutocomplete.isEnabled = false
    }

    private fun hideKeyboard() {
        val view = activity.currentFocus
        if (view != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun showNoBranchesAvailable() {
        noBranchesAvailable.visibility = View.VISIBLE
    }

    /**
     * {@inheritDoc}
     */
    override fun showNoBranchesAvailableToFilter() {
        noBranchesAvailableToFilter.visibility = View.VISIBLE
    }

    /**
     * {@inheritDoc}
     */
    override fun showBranchesAutoComplete() {
        branchAutocomplete.visibility = View.VISIBLE
    }

    /**
     * {@inheritDoc}
     */
    override fun setAutocompleteHintForFilter() {
        branchAutocomplete.setHint(R.string.hint_default_filter_branch)
    }

    /**
     * Branches adapter with custom branch filtering
     */
    private class BranchArrayAdapter
    /**
     * Constructor
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     * instantiating views.
     * @param objects The objects to represent in the ListView.
     */
    internal constructor(
        context: Context,
        resource: Int,
        objects: List<String>
    ) : ArrayAdapter<String>(context, resource, objects) {

        /**
         * Branches to show and filter
         */
        private val branches: List<String>
        /**
         * Branch filter
         */
        private var filter: BranchFilter? = null

        init {
            this.branches = ArrayList(objects)
        }

        /**
         * {@inheritDoc}
         */
        override fun getFilter(): Filter {
            val filter = this.filter ?: BranchFilter()
            this.filter = filter
            return filter
        }

        /**
         * Branch filter
         */
        private inner class BranchFilter : Filter() {

            /**
             * {@inheritDoc}
             */
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results = FilterResults()
                if (TextUtils.isEmpty(constraint)) {
                    results.values = branches
                    results.count = branches.size
                } else {
                    val newValues = ArrayList<String>()
                    for (branch in branches) {
                        if (branch.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            newValues.add(branch)
                        }
                    }
                    results.values = newValues
                    results.count = newValues.size
                }
                return results
            }

            /**
             * {@inheritDoc}
             */
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                if (results.count > 0) {
                    clear()
                    addAll(results.values as List<String>)
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}
