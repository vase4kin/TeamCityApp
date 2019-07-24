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
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.google.android.material.snackbar.Snackbar;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * impl of {@link HomeView}
 */
public class HomeViewImpl extends DrawerViewImpl implements HomeView {

    private static final int TIME_NAVIGATION_DRAWER_PROMPT = 500;

    private View snackBarAnchor;
    @Nullable
    private ViewListener listener;

    public HomeViewImpl(AppCompatActivity activity, int drawerSelection, boolean isBackArrowEnabled) {
        super(activity, drawerSelection, isBackArrowEnabled);
    }

    @Override
    public void initViews(OnDrawerPresenterListener listener) {
        super.initViews(listener);
        snackBarAnchor = activity.findViewById(R.id.snackbar_anchor);
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
        Snackbar snackBar = Snackbar.make(
                snackBarAnchor,
                R.string.text_info_add,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.text_info_add_action, view -> {
                    if (listener != null) {
                        listener.onFavoritesSnackBarActionClicked();
                    }
                });
        snackBar.show();
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
        }, TIME_NAVIGATION_DRAWER_PROMPT);
    }
}
