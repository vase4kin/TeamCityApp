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

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.account.create.view.OnToolBarNavigationListenerImpl;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Impl of {@link RunBuildView}
 */
public class RunBuildViewImpl implements RunBuildView {

    @BindView(R.id.fab_queue_build)
    FloatingActionButton mQueueBuildFab;
    @BindView(R.id.switcher_is_personal)
    Switch mPersonalBuildSwitch;
    @BindView(R.id.switcher_queueAtTop)
    Switch mQueueToTheTopSwitch;
    @BindView(R.id.switcher_clean_all_files)
    Switch mCleanAllFilesSwitch;
    @BindView(R.id.container)
    View mContainer;
    @BindView(R.id.chooser_agent)
    View mAgentChooserView;
    @BindView(R.id.selected_agent)
    TextView mSelectedAgent;
    @BindView(R.id.text_no_agents_available)
    View mNoAgentsAvailable;
    @BindView(R.id.progress_agents_loading)
    View mLoadingAgentsProgress;
    @BindView(R.id.parameters_none)
    View mParametersNoneView;
    @BindView(R.id.button_clear_parameters)
    Button mClearAllParametersButton;
    @BindView(R.id.container_parameters)
    ViewGroup mParametersContainer;

    @OnClick(R.id.chooser_agent)
    public void onAgentSelect() {
        mAgentSelectionDialog.show();
    }

    @OnClick(R.id.button_add_parameter)
    public void onAddParameterButtonClick() {
        mListener.onAddParameterButtonClick();
    }

    @OnClick(R.id.button_clear_parameters)
    public void onClearAllParametersButtonClick() {
        mListener.onClearAllParametersButtonClick();
    }

    private MaterialDialog mProgressDialog;
    private MaterialDialog mAgentSelectionDialog;
    private MaterialDialog mAddParameterDialog;

    private RunBuildActivity mActivity;
    private Unbinder mUnbinder;

    private ViewListener mListener;

    public RunBuildViewImpl(RunBuildActivity activity) {
        this.mActivity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(final ViewListener listener) {
        mUnbinder = ButterKnife.bind(this, mActivity);
        mListener = listener;

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
                listener.onBuildQueue(
                        mPersonalBuildSwitch.isChecked(),
                        mQueueToTheTopSwitch.isChecked(),
                        mCleanAllFilesSwitch.isChecked());
            }
        });

        mProgressDialog = new MaterialDialog.Builder(mActivity)
                .content(R.string.text_queueing_build)
                .progress(true, 0)
                .autoDismiss(false)
                .build();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        mAddParameterDialog = new MaterialDialog.Builder(mActivity)
                .autoDismiss(false)
                .title(R.string.title_add_parameter)
                .customView(R.layout.layout_dialog_add_parameter, true)
                .positiveText(R.string.text_add_parameter_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        View view = dialog.getCustomView();
                        if (view != null) {
                            // TODO: Move logic presenter
                            EditText parameterNameEditText = (EditText)view.findViewById(R.id.parameter_name);
                            EditText parameterValueEditText = (EditText)view.findViewById(R.id.parameter_value);
                            TextInputLayout parameterNameWrapper = (TextInputLayout) view.findViewById(R.id.parameter_name_wrapper);
                            String parameterName = parameterNameEditText.getText().toString();
                            if (TextUtils.isEmpty(parameterName)) {
                                String errorMessage = view.getResources().getString(R.string.text_error_parameter_name);
                                parameterNameWrapper.setError(errorMessage);
                                return;
                            }
                            mListener.onParameterAdded(
                                    parameterNameEditText.getText().toString(),
                                    parameterValueEditText.getText().toString());
                            parameterNameWrapper.setError(null);
                            parameterNameEditText.setText("");
                            parameterValueEditText.setText("");
                            parameterNameEditText.requestFocus();
                            dialog.dismiss();
                        }
                    }
                })
                .negativeText(R.string.text_cancel_button)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .build();
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
     * {@inheritDoc}
     */
    @Override
    public void disableAgentSelectionControl() {
        mAgentChooserView.setEnabled(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enableAgentSelectionControl() {
        mAgentChooserView.setEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showSelectedAgentView() {
        mSelectedAgent.setVisibility(View.VISIBLE);
    }

    @Override
    public void setAgentListDialogWithAgentsList(List<String> agents) {
        mAgentSelectionDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.title_agent_chooser_dialog)
                .items(agents)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        mListener.onAgentSelected(position);
                        mSelectedAgent.setText(text);
                    }
                })
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideLoadingAgentsProgress() {
        mLoadingAgentsProgress.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showNoAgentsAvailable() {
        mNoAgentsAvailable.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showAddParameterDialog() {
        mAddParameterDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideNoneParametersView() {
        mParametersNoneView.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showNoneParametersView() {
        mParametersNoneView.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enableClearAllParametersButton() {
        mClearAllParametersButton.setEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disableClearAllParametersButton() {
        mClearAllParametersButton.setEnabled(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addParameterView(String name, String value) {
        View parameterLayout = LayoutInflater.from(mActivity).inflate(R.layout.layout_parameter_view, null);
        ((TextView)parameterLayout.findViewById(R.id.parameter_name)).setText(name);
        ((TextView)parameterLayout.findViewById(R.id.parameter_value)).setText(value);
        mParametersContainer.addView(parameterLayout);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllParameterViews() {
        mParametersContainer.removeAllViews();
    }
}
