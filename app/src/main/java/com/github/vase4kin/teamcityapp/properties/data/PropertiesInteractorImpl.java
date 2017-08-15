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

package com.github.vase4kin.teamcityapp.properties.data;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.github.vase4kin.teamcityapp.overview.data.TextCopiedEvent;

import org.greenrobot.eventbus.EventBus;

public class PropertiesInteractorImpl implements PropertiesInteractor {

    private final EventBus mEventBus;
    private final Context mContext;

    public PropertiesInteractorImpl(EventBus eventBus, Context context) {
        this.mEventBus = eventBus;
        this.mContext = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyTextToClipBoard(String textToCopy) {
        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", textToCopy);
        clipboard.setPrimaryClip(clip);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postTextCopiedEvent() {
        mEventBus.post(new TextCopiedEvent());
    }
}
