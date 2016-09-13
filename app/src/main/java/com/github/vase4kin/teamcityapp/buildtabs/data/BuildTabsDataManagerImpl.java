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

package com.github.vase4kin.teamcityapp.buildtabs.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.github.vase4kin.teamcityapp.base.tabs.data.BaseTabsDataManagerImpl;
import com.github.vase4kin.teamcityapp.overview.data.FloatButtonChangeVisibilityEvent;

import de.greenrobot.event.EventBus;

/**
 * Impl of {@link BuildTabsDataManager}
 */
public class BuildTabsDataManagerImpl extends BaseTabsDataManagerImpl implements BuildTabsDataManager {

    @Nullable
    private OnFloatButtonChangeVisibilityEventListener mListener;

    public BuildTabsDataManagerImpl(@NonNull EventBus mEventBus) {
        super(mEventBus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnFloatButtonChangeVisibilityEventListener(OnFloatButtonChangeVisibilityEventListener listener) {
        this.mListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postOnArtifactTabChangeEvent() {
        mEventBus.post(new OnArtifactTabChangeEvent());
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event FloatButtonChangeVisibilityEvent
     */
    @SuppressWarnings("unused")
    public void onEvent(FloatButtonChangeVisibilityEvent event) {
        if (mListener == null) return;
        switch (event.getVisibility()) {
            case View.VISIBLE:
                mListener.onShow();
                break;
            case View.GONE:
                mListener.onHide();
                break;
            default:
                break;
        }
    }
}
