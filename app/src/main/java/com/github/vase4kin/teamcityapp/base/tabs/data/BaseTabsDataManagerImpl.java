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

package com.github.vase4kin.teamcityapp.base.tabs.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class BaseTabsDataManagerImpl implements BaseTabsDataManager {

    @NonNull
    protected EventBus mEventBus;
    @Nullable
    private OnTextTabChangeEventListener mListener;

    public BaseTabsDataManagerImpl(@NonNull EventBus mEventBus) {
        this.mEventBus = mEventBus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerEventBus() {
        mEventBus.register(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterEventBus() {
        mEventBus.unregister(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setListener(OnTextTabChangeEventListener listener) {
        this.mListener = listener;
    }

    /**
     * Handle {@link OnTextTabChangeEvent}
     */
    @Subscribe
    public void onEvent(OnTextTabChangeEvent event) {
        if (mListener != null) {
            mListener.onUpdateTabTitle(event.getTabPosition(), String.valueOf(event.getCount()));
        }
    }

}
