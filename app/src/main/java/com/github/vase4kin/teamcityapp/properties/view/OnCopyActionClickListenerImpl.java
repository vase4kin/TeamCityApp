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

package com.github.vase4kin.teamcityapp.properties.view;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.MenuItem;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.github.vase4kin.teamcityapp.R;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

/**
 * Impl of {@link OnCopyActionClickListener}
 */
public class OnCopyActionClickListenerImpl implements OnCopyActionClickListener {

    /**
     * Instance of {@link Activity}
     */
    private Activity mActivity;

    public OnCopyActionClickListenerImpl(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(String value) {
        copy(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLongClick(String title, String value) {
        buildBottomSheetDialog(title, value).show();
    }

    /**
     * Build bottom sheet
     *
     * @param title      - Bottom sheet title
     * @param textToCopy - Text to be copied
     * @return built bottom sheet
     */
    protected BottomSheet buildBottomSheetDialog(final String title, final String textToCopy) {
        BottomSheet bottomSheet = new BottomSheet.Builder(mActivity)
                .title(title)
                .sheet(R.menu.menu_build_element)
                .listener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.copy:
                                copy(textToCopy);
                                return true;
                            default:
                                return false;
                        }
                    }
                }).build();
        bottomSheet.getMenu().findItem(R.id.copy)
                .setIcon(new IconDrawable(mActivity, MaterialIcons.md_content_copy));

        return bottomSheet;
    }

    /**
     * Action to copy text to clipboard
     *
     * @param textToCopy - Text to be copied
     */
    private void copy(String textToCopy) {
        ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", textToCopy);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(mActivity, R.string.build_element_copy_text, Toast.LENGTH_SHORT).show();
    }
}
