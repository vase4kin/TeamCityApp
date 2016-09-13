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

package com.github.vase4kin.teamcityapp.drawer.view;

import android.view.View;

import com.mikepenz.materialdrawer.Drawer;

/**
 * Drawer listener
 */
public class OnDrawerListenerImpl implements Drawer.OnDrawerListener {

    private OnDrawerPresenterListener mListener;

    public OnDrawerListenerImpl(OnDrawerPresenterListener mListener) {
        this.mListener = mListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawerOpened(View drawerView) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawerClosed(View drawerView) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        if (slideOffset > 0 && slideOffset < 0.3) {
            mListener.onDrawerSlide();
        }
    }
}
