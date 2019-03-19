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

package com.github.vase4kin.teamcityapp.login.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.RequiresApi;

/**
 * Custom edit text with disabled autofill
 */
public class EditTextNoAutofill extends androidx.appcompat.widget.AppCompatEditText {

    public EditTextNoAutofill(Context context) {
        super(context);
    }

    public EditTextNoAutofill(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextNoAutofill(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Override
    public int getAutofillType() {
        return View.AUTOFILL_TYPE_NONE;
    }
}
