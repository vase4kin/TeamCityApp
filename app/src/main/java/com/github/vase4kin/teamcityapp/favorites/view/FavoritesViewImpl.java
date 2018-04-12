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

package com.github.vase4kin.teamcityapp.favorites.view;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationAdapter;
import com.github.vase4kin.teamcityapp.navigation.view.OnNavigationItemClickListener;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class FavoritesViewImpl extends BaseListViewImpl<NavigationDataModel, SimpleSectionedRecyclerViewAdapter<NavigationAdapter>> implements FavoritesView {

    @BindView(R.id.floating_action_button)
    FloatingActionButton mFloatingActionButton;
    @BindView(R.id.container)
    View mContainer;

    private FavoritesView.ViewListener listener;

    public FavoritesViewImpl(View view,
                             Activity activity,
                             @StringRes int emptyMessage,
                             SimpleSectionedRecyclerViewAdapter<NavigationAdapter> adapter) {
        super(view, activity, emptyMessage, adapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(@NonNull BaseListView.ViewListener listener) {
        super.initViews(listener);
        mFloatingActionButton.setImageDrawable(new IconDrawable(mActivity, MaterialIcons.md_add).color(Color.WHITE));
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FavoritesViewImpl.this.listener != null) {
                    FavoritesViewImpl.this.listener.onFabClick();
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setViewListener(FavoritesView.ViewListener listener) {
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTitleCount(int count) {
        ActionBar actionBar = ((AppCompatActivity) mActivity).getSupportActionBar();
        if (actionBar != null) {
            String title = String.format(Locale.ENGLISH, "%s (%d)", mActivity.getString(R.string.favorites_drawer_item), count);
            actionBar.setTitle(title);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showInfoSnackbar() {
        Snackbar snackBar = Snackbar.make(
                mContainer,
                R.string.text_info_add,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.text_info_add_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null) {
                            listener.onSnackBarAction();
                        }
                    }
                });
        TextView textView = snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showAddFavPrompt(final OnboardingManager.OnPromptShownListener listener) {
        int color = getToolbarColor();
        new MaterialTapTargetPrompt.Builder(mActivity)
                .setTarget(mFloatingActionButton)
                .setPrimaryText(R.string.title_onboarding_add_fav)
                .setSecondaryText(R.string.text_onboarding_add_fav)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setBackgroundColour(color)
                .setCaptureTouchEventOutsidePrompt(true)
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                            listener.onPromptShown();
                        }
                    }
                })
                .show();
    }

    /**
     * @return color of toolbar
     */
    @ColorInt
    private int getToolbarColor() {
        return ((ColorDrawable) mActivity.findViewById(R.id.toolbar).getBackground()).getColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(final NavigationDataModel dataModel) {
        NavigationAdapter baseAdapter = mAdapter.getBaseAdapter();
        baseAdapter.setDataModel(dataModel);
        baseAdapter.setOnClickListener(new OnNavigationItemClickListener() {
            @Override
            public void onClick(NavigationItem navigationItem) {
                listener.onClick(navigationItem);
            }
        });

        List<SimpleSectionedRecyclerViewAdapter.Section> sections =
                new ArrayList<>();

        if (dataModel.getItemCount() != 0) {
            for (int i = 0; i < dataModel.getItemCount(); i++) {
                final String projectName = dataModel.getProjectName(i);
                if (sections.size() != 0) {
                    SimpleSectionedRecyclerViewAdapter.Section prevSection = sections.get(sections.size() - 1);
                    if (!prevSection.getTitle().equals(projectName)) {
                        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(i, projectName));
                    }
                } else {
                    sections.add(new SimpleSectionedRecyclerViewAdapter.Section(i, projectName));
                }
            }
            mAdapter.setListener(new SimpleSectionedRecyclerViewAdapter.OnSectionClickListener() {
                @Override
                public void onSectionClick(int position) {
                    final String projectName = dataModel.getProjectName(position);
                    final String projectId = dataModel.getProjectId(position);
                    NavigationActivity.start(projectName, projectId, mActivity);
                }
            });
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
    protected int recyclerViewId() {
        return R.id.favorites_recycler_view;
    }
}
