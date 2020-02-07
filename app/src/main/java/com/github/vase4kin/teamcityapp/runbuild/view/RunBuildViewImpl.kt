/*
 * Copyright 2020 Andrey Tolpeev
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

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.afollestad.materialdialogs.MaterialDialog
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListenerImpl
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

/**
 * Impl of [RunBuildView]
 */
class RunBuildViewImpl(private val activity: RunBuildActivity) : RunBuildView {

    @BindView(R.id.fab_queue_build)
    lateinit var queueBuildFab: ExtendedFloatingActionButton
    @BindView(R.id.switcher_is_personal)
    lateinit var personalBuildSwitch: Switch
    @BindView(R.id.switcher_queueAtTop)
    lateinit var queueToTheTopSwitch: Switch
    @BindView(R.id.switcher_clean_all_files)
    lateinit var cleanAllFilesSwitch: Switch
    @BindView(R.id.container)
    lateinit var container: View
    @BindView(R.id.chooser_agent)
    lateinit var agentChooserView: View
    @BindView(R.id.selected_agent)
    lateinit var selectedAgent: TextView
    @BindView(R.id.text_no_agents_available)
    lateinit var noAgentsAvailable: View
    @BindView(R.id.progress_agents_loading)
    lateinit var loadingAgentsProgress: View
    @BindView(R.id.parameters_none)
    lateinit var parametersNoneView: View
    @BindView(R.id.button_clear_parameters)
    lateinit var clearAllParametersButton: Button
    @BindView(R.id.container_parameters)
    lateinit var parametersContainer: ViewGroup

    private lateinit var progressDialog: MaterialDialog
    private lateinit var agentSelectionDialog: MaterialDialog
    private lateinit var addParameterDialog: MaterialDialog
    private lateinit var unbinder: Unbinder

    private var listener: RunBuildView.ViewListener? = null

    @OnClick(R.id.chooser_agent)
    fun onAgentSelect() {
        agentSelectionDialog.show()
    }

    @OnClick(R.id.button_add_parameter)
    fun onAddParameterButtonClick() {
        listener?.onAddParameterButtonClick()
    }

    @OnClick(R.id.button_clear_parameters)
    fun onClearAllParametersButtonClick() {
        listener?.onClearAllParametersButtonClick()
    }

    /**
     * {@inheritDoc}
     */
    override fun initViews(listener: RunBuildView.ViewListener) {
        unbinder = ButterKnife.bind(this, activity)
        this.listener = listener

        val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        activity.setSupportActionBar(toolbar)

        val actionBar = activity.supportActionBar
        actionBar?.setTitle(R.string.title_run_build)

        // For ui testing purpose
        toolbar.setNavigationContentDescription(R.string.navigate_up)
        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        toolbar.setNavigationOnClickListener(OnToolBarNavigationListenerImpl(listener))

        queueBuildFab.setOnClickListener {
            listener.onBuildQueue(
                personalBuildSwitch.isChecked,
                queueToTheTopSwitch.isChecked,
                cleanAllFilesSwitch.isChecked
            )
        }

        progressDialog = MaterialDialog.Builder(activity)
            .content(R.string.text_queueing_build)
            .progress(true, 0)
            .autoDismiss(false)
            .build()
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)

        addParameterDialog = MaterialDialog.Builder(activity)
            .autoDismiss(false)
            .title(R.string.title_add_parameter)
            .customView(R.layout.layout_dialog_add_parameter, true)
            .positiveText(R.string.text_add_parameter_button)
            .onPositive { dialog, _ ->
                val view = dialog.customView
                if (view != null) {
                    // TODO: Move logic presenter
                    val parameterNameEditText = view.findViewById<EditText>(R.id.parameter_name)
                    val parameterValueEditText = view.findViewById<EditText>(R.id.parameter_value)
                    val parameterNameWrapper =
                        view.findViewById<TextInputLayout>(R.id.parameter_name_wrapper)
                    val parameterName = parameterNameEditText.text.toString()
                    if (TextUtils.isEmpty(parameterName)) {
                        val errorMessage =
                            view.resources.getString(R.string.text_error_parameter_name)
                        parameterNameWrapper.error = errorMessage
                        return@onPositive
                    }
                    listener.onParameterAdded(
                        parameterNameEditText.text.toString(),
                        parameterValueEditText.text.toString()
                    )
                    parameterNameWrapper.error = null
                    parameterNameEditText.setText("")
                    parameterValueEditText.setText("")
                    parameterNameEditText.requestFocus()
                    dialog.dismiss()
                }
            }
            .negativeText(R.string.text_cancel_button)
            .onNegative { dialog, _ -> dialog.dismiss() }
            .build()
    }

    /**
     * {@inheritDoc}
     */
    override fun showQueuingBuildProgress() {
        progressDialog.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun hideQueuingBuildProgress() {
        progressDialog.dismiss()
    }

    /**
     * {@inheritDoc}
     */
    override fun showForbiddenErrorSnackbar() {
        val snackBar = Snackbar.make(
            container,
            R.string.error_forbidden_error,
            Snackbar.LENGTH_LONG
        )
            .setAnchorView(queueBuildFab)
        snackBar.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showErrorSnackbar() {
        val snackBar = Snackbar.make(
            container,
            R.string.error_base_error,
            Snackbar.LENGTH_LONG
        )
            .setAnchorView(queueBuildFab)
        snackBar.show()
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
    override fun disableAgentSelectionControl() {
        agentChooserView.isEnabled = false
    }

    /**
     * {@inheritDoc}
     */
    override fun enableAgentSelectionControl() {
        agentChooserView.isEnabled = true
    }

    /**
     * {@inheritDoc}
     */
    override fun showSelectedAgentView() {
        selectedAgent.visibility = View.VISIBLE
    }

    override fun setAgentListDialogWithAgentsList(agents: List<String>) {
        agentSelectionDialog = MaterialDialog.Builder(activity)
            .title(R.string.title_agent_chooser_dialog)
            .items(agents)
            .itemsCallback { _, _, position, text ->
                listener?.onAgentSelected(position)
                selectedAgent.text = text
            }
            .build()
    }

    /**
     * {@inheritDoc}
     */
    override fun hideLoadingAgentsProgress() {
        loadingAgentsProgress.visibility = View.GONE
    }

    /**
     * {@inheritDoc}
     */
    override fun showNoAgentsAvailable() {
        noAgentsAvailable.visibility = View.VISIBLE
    }

    /**
     * {@inheritDoc}
     */
    override fun showAddParameterDialog() {
        addParameterDialog.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun hideNoneParametersView() {
        parametersNoneView.visibility = View.GONE
    }

    /**
     * {@inheritDoc}
     */
    override fun showNoneParametersView() {
        parametersNoneView.visibility = View.VISIBLE
    }

    /**
     * {@inheritDoc}
     */
    override fun enableClearAllParametersButton() {
        clearAllParametersButton.isEnabled = true
    }

    /**
     * {@inheritDoc}
     */
    override fun disableClearAllParametersButton() {
        clearAllParametersButton.isEnabled = false
    }

    /**
     * {@inheritDoc}
     */
    override fun addParameterView(name: String, value: String) {
        val parameterLayout =
            LayoutInflater.from(activity).inflate(R.layout.layout_parameter_view, null)
        (parameterLayout.findViewById<View>(R.id.parameter_name) as TextView).text = name
        (parameterLayout.findViewById<View>(R.id.parameter_value) as TextView).text = value
        parametersContainer.addView(parameterLayout)
    }

    /**
     * {@inheritDoc}
     */
    override fun removeAllParameterViews() {
        parametersContainer.removeAllViews()
    }
}
