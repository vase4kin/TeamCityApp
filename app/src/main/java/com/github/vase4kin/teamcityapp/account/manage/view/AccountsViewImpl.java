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

package com.github.vase4kin.teamcityapp.account.manage.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity;
import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import tr.xip.errorview.ErrorView;

/**
 * View impl for account
 *
 * TODO: move all logic to presenter!
 */
public class AccountsViewImpl extends BaseListViewImpl<AccountDataModel> implements AccountsView, OnUserAccountClickListener {

    private SharedUserStorage mSharedUserStorage;

    @BindString(R.string.header_text_active_account)
    String mActiveAccountHeaderText;

    @BindString(R.string.header_text_not_active_account)
    String mNotActiveAccountHeaderText;

    @BindView(R.id.floating_action_button)
    FloatingActionButton mButtonFloat;

    private MaterialDialog mWarningDialog;
    private Snackbar mUndoSnackBar;
    private MultiSelector mMultiSelector = new MultiSelector();
    private ActionMode mActionMode;
    private boolean isSelectionDisabled = false;
    private AccountDataModel mDataModel;
    private SimpleSectionedRecyclerViewAdapter<AccountAdapter> mAdapter;

    // Select account logic
    private ModalMultiSelectorCallback mDeleteMode = new ModalMultiSelectorCallback(mMultiSelector) {

        private Map<Integer, UserAccount> backUpAccountMap = new HashMap<>();

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);
            mActivity.getMenuInflater().inflate(R.menu.menu_account_list_action_mode, menu);
            menu.findItem(R.id.delete).setIcon(
                    new IconDrawable(mActivity, MaterialIcons.md_delete)
                            .color(Color.WHITE)
                            .actionBarSize());
            mButtonFloat.hide();
            // Disable to prevent update of recycle view
            disableSwipeToRefresh();
            disableRecyclerView();
            return true;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode actionMode, final MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.delete) {
                backUpAccountMap.clear();
                for (int i = 0; i < mAdapter.getItemCount(); i++) {
                    int realPosition = mAdapter.positionToSectionedPosition(i);
                    if (mMultiSelector.isSelected(realPosition, 0)) {
                        final UserAccount userAccount = mDataModel.get(i);
                        backUpAccountMap.put(i, userAccount);
                    }
                }

                MaterialDialog mSureToDeleteDialog = new MaterialDialog.Builder(mActivity)
                        .content(R.string.dialog_remove_active_account_positive_content_text)
                        .positiveText(R.string.dialog_remove_active_account_positive_button_text)
                        .positiveColor(mActivity.getResources().getColor(R.color.md_blue_A100))
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                removeAccount(actionMode);
                            }
                        })
                        .negativeText(R.string.dialog_remove_active_account_positive_negative_text)
                        .negativeColor(mActivity.getResources().getColor(R.color.md_blue_A100))
                        .build();

                boolean isActiveAccountToDelete = false;

                for (final Integer userAccountPosition : backUpAccountMap.keySet()) {
                    if (backUpAccountMap.get(userAccountPosition).isActive()) {
                        isActiveAccountToDelete = true;
                        break;
                    }
                }

                if (isActiveAccountToDelete) {
                    mSureToDeleteDialog.show();
                } else {
                    removeAccount(actionMode);
                }

                return true;
            }
            return false;
        }

        private void removeAccount(ActionMode actionMode) {
            for (Integer userAccountPosition : backUpAccountMap.keySet()) {
                mDataModel.remove(backUpAccountMap.get(userAccountPosition));
            }

            mMultiSelector.clearSelections();
            actionMode.finish();

            String snackBarText =
                    backUpAccountMap.size() == 1
                            ? mActivity.getString(R.string.snack_bar_remove_account_single_account_removed_text)
                            : backUpAccountMap.size() + " " + mActivity.getString(R.string.snack_bar_remove_account_multiple_accounts_removed_text);

            mUndoSnackBar = Snackbar.make(mRecyclerView, snackBarText, Snackbar.LENGTH_LONG);
            TextView textView = (TextView) mUndoSnackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            mUndoSnackBar.setAction(R.string.snack_bar_remove_account_undo_button_text, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (Integer position : backUpAccountMap.keySet()) {
                        mDataModel.add(backUpAccountMap.get(position));
                        mRecyclerView.getAdapter().notifyItemInserted(position);
                    }
                    backUpAccountMap.clear();
                }
            });
            mUndoSnackBar.setCallback(new android.support.design.widget.Snackbar.Callback() {
                @Override
                public void onDismissed(android.support.design.widget.Snackbar snackbar, int event) {
                    if (event != android.support.design.widget.Snackbar.Callback.DISMISS_EVENT_ACTION) {
                        for (UserAccount account : backUpAccountMap.values()) {
                            mSharedUserStorage.removeUserAccount(account);
                            mOnAccountRemoveListener.onAccountRemove();
                        }
                        backUpAccountMap.clear();
                        isSelectionDisabled = false;
                        if (mDataModel != null && mRecyclerView != null) {
                            showData(mDataModel);
                        }
                        showWarningDialog();
                    }
                }
            });
            mUndoSnackBar.show();
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            super.onDestroyActionMode(actionMode);
            mActionMode = null;
            mMultiSelector.clearSelections();
            mButtonFloat.show();
            // Enable layout
            enableSwipeToRefresh();
            enableRecyclerView();
        }
    };
    private OnAccountRemoveListener mOnAccountRemoveListener;

    public AccountsViewImpl(View view,
                            Activity activity,
                            SharedUserStorage sharedUserStorage,
                            @StringRes int emptyMessage,
                            SimpleSectionedRecyclerViewAdapter<AccountAdapter> adapter) {
        super(view, activity, emptyMessage);
        this.mSharedUserStorage = sharedUserStorage;
        this.mAdapter = adapter;
    }

    @OnClick(R.id.floating_action_button)
    public void onClick() {
        showCreateNewAccountDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnAccountRemoveListener(OnAccountRemoveListener onAccountRemoveListener) {
        this.mOnAccountRemoveListener = onAccountRemoveListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(@NonNull ErrorView.RetryListener retryListener,
                          @NonNull SwipeRefreshLayout.OnRefreshListener refreshListener) {
        super.initViews(retryListener, refreshListener);

        //Setting float button icon
        mButtonFloat.setImageDrawable(new IconDrawable(mActivity, MaterialIcons.md_add).color(Color.WHITE));

        mWarningDialog =
                new MaterialDialog.Builder(mActivity)
                        .title(R.string.add_new_account_dialog_title)
                        .content(R.string.add_new_account_warning_dialog_content)
                        .positiveText(R.string.add_new_account_dialog_create_account_button_text)
                        .positiveColor(mActivity.getResources().getColor(R.color.snack_bar_action_color))
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                showCreateNewAccountDialog();
                            }
                        })
                        .build();
        mWarningDialog.setCancelable(false);
        mWarningDialog.setCanceledOnTouchOutside(false);

        showWarningDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(AccountDataModel dataModel) {
        mDataModel = dataModel;
        mDataModel.sort();

        AccountAdapter baseAdapter = mAdapter.getBaseAdapter();
        baseAdapter.setDataModel(dataModel);
        baseAdapter.setListener(this);
        baseAdapter.setMultiSelector(mMultiSelector);

        //This is the code to provide a sectioned list
        List<SimpleSectionedRecyclerViewAdapter.Section> sections =
                new ArrayList<>();

        //Sections
        // If there is one user
        if (mDataModel.getItemCount() > 0) {
            sections.add(new SimpleSectionedRecyclerViewAdapter.Section(0, mActiveAccountHeaderText));
        }

        //if there are more users
        if (mDataModel.getItemCount() > 1) {
            sections.add(new SimpleSectionedRecyclerViewAdapter.Section(1, mNotActiveAccountHeaderText));
        }

        SimpleSectionedRecyclerViewAdapter.Section[] userStates = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        mAdapter.setSections(sections.toArray(userStates));

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void click(int position) {
        if (!isSelectionDisabled) {
            mMultiSelector.setSelectable(true);
            int realPosition = mAdapter.positionToSectionedPosition(position);
            if (mMultiSelector.tapSelection(realPosition, position)) {
                if (mMultiSelector != null) {
                    if (mMultiSelector.isSelectable()) {
                        if (mDeleteMode != null) {
                            if (mActionMode == null) {
                                mDeleteMode.setClearOnPrepare(false);
                                mActionMode = ((AccountListActivity) mActivity).startSupportActionMode(mDeleteMode);
                            }
                            if (mMultiSelector.isSelected(realPosition, position)) {
                                mMultiSelector.setSelected(realPosition, position, true);
                            } else {
                                mMultiSelector.setSelected(realPosition, position, false);
                            }
                            if (mActionMode != null) {
                                mActionMode.setTitle(
                                        String.valueOf(mMultiSelector.getSelectedPositions().size()) +
                                                " " +
                                                mActivity.getString(R.string.action_mode_selected_text));
                            }
                        }
                    }
                }
            }
            if (mMultiSelector.getSelectedPositions().isEmpty()) {
                if (mActionMode != null) {
                    mActionMode.finish();
                }
            }
        }
    }

    /**
     * Show warning dialog that user needs to create account to use the app
     */
    private void showWarningDialog() {
        if (mSharedUserStorage.getUserAccounts().isEmpty()) {
            mWarningDialog.show();
        }
    }

    /**
     * Show create new account dialog
     */
    private void showCreateNewAccountDialog() {
        mActivity.startActivity(new Intent(mActivity, CreateAccountActivity.class));
        mActivity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.hold);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindViews() {
        if (mUndoSnackBar != null && mUndoSnackBar.isShown()) {
            mUndoSnackBar.dismiss();
        }
        super.unbindViews();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int recyclerViewId() {
        return R.id.account_recycler_view;
    }
}
