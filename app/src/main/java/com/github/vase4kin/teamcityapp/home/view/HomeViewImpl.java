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

package com.github.vase4kin.teamcityapp.home.view;

import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.drawer.view.DrawerViewImpl;
import com.github.vase4kin.teamcityapp.drawer.view.OnDrawerPresenterListener;
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.Filter;
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.FilterBottomSheetDialogFragment;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * impl of {@link HomeView}
 */
public class HomeViewImpl extends DrawerViewImpl implements HomeView {

    private static final String TAG_BOTTOM_SHEET = "Tag filter bottom sheet";
    private static final int TIME_PROMPT_DELAY = 500;

    private View snackBarAnchor;
    @Nullable
    private ViewListener listener;
    @Nullable
    private Snackbar snackbar;
    @Nullable
    private FloatingActionButton fab;

    public HomeViewImpl(AppCompatActivity activity, int drawerSelection, boolean isBackArrowEnabled) {
        super(activity, drawerSelection, isBackArrowEnabled);
    }

    @Override
    public void initViews(OnDrawerPresenterListener listener) {
        super.initViews(listener);
        snackBarAnchor = activity.findViewById(R.id.snackbar_anchor);
        fab = activity.findViewById(R.id.floating_action_button);
    }

    @Override
    public void setListener(@Nullable ViewListener listener) {
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDrawerSelection(int selection) {
        drawerResult.setSelection(selection, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showFavoritesInfoSnackbar() {
        this.snackbar = Snackbar.make(
                snackBarAnchor,
                R.string.text_info_add,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.text_info_add_action, view -> {
                    if (listener != null) {
                        listener.onFavoritesSnackBarActionClicked();
                    }
                });
        snackbar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dimissSnackbar() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showFilterBottomSheet(@NotNull Filter filter) {
        FilterBottomSheetDialogFragment.Companion.createBottomSheetDialog(filter.getCode()).show(activity.getSupportFragmentManager(), TAG_BOTTOM_SHEET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showFilterAppliedSnackBar() {
        this.snackbar = Snackbar.make(
                snackBarAnchor,
                R.string.text_filters_applied,
                Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showNavigationDrawerPrompt(@NonNull final OnboardingManager.OnPromptShownListener listener) {
        // Creating prompt
        int color = ContextCompat.getColor(activity, mDefaultColor);
        final MaterialTapTargetPrompt.Builder navigationDrawerPrompt = new MaterialTapTargetPrompt.Builder(activity)
                .setPrimaryText(R.string.title_onboarding_navigation_drawer)
                .setSecondaryText(R.string.text_onboarding_navigation_drawer)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setIcon(R.drawable.ic_menu_black_24dp)
                .setIconDrawableTintList(ColorStateList.valueOf(color))
                .setBackgroundColour(color)
                .setCaptureTouchEventOutsidePrompt(true)
                .setPromptStateChangeListener((prompt, state) -> listener.onPromptShown());
        // Show prompt
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigationDrawerPrompt.setTarget(toolbar.getChildAt(1));
            navigationDrawerPrompt.show();
        }, TIME_PROMPT_DELAY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showAddFavPrompt(@NonNull final OnboardingManager.OnPromptShownListener listener) {
        int color = ContextCompat.getColor(activity, mDefaultColor);
        new MaterialTapTargetPrompt.Builder(activity)
                .setTarget(fab)
                .setPrimaryText(R.string.title_onboarding_add_fav)
                .setSecondaryText(R.string.text_onboarding_add_fav)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setBackgroundColour(color)
                .setCaptureTouchEventOutsidePrompt(true)
                .setPromptStateChangeListener((prompt, state) -> {
                    if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                        listener.onPromptShown();
                    }
                }).show();
    }
}
