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

package com.github.vase4kin.teamcityapp.overview.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.View;

import com.cocosw.bottomsheet.BottomSheet;
import com.github.vase4kin.teamcityapp.overview.data.FloatButtonChangeVisibilityEvent;
import com.github.vase4kin.teamcityapp.properties.view.OnCopyActionClickListenerImpl;

import de.greenrobot.event.EventBus;

/**
 * On custom copy action listener
 */
public class OnCustomCopyActionClickListenerImpl extends OnCopyActionClickListenerImpl {

    private static final int DELAY = 500;

    public OnCustomCopyActionClickListenerImpl(Activity activity) {
        super(activity);
    }

    /**
     * Build bottom sheet dialog with title and text to copy for an action
     *
     * @param title      - Sheet title
     * @param textToCopy - Text to copy
     * @return Bottom sheet dialog
     */
    @Override
    protected BottomSheet buildBottomSheetDialog(String title, String textToCopy) {
        BottomSheet bottomSheet = super.buildBottomSheetDialog(title, textToCopy);
        bottomSheet.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                EventBus.getDefault().post(new FloatButtonChangeVisibilityEvent(View.GONE));
            }
        });
        bottomSheet.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(new FloatButtonChangeVisibilityEvent(View.VISIBLE));
                    }
                }, DELAY);
            }
        });
        return bottomSheet;
    }

}
