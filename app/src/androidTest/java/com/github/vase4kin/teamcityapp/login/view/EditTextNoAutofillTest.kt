/*
 * Copyright 2019 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.login.view

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class EditTextNoAutofillTest {

    private lateinit var editTextNoAutofill: EditTextNoAutofill

    @Before
    fun setUp() {
        editTextNoAutofill =
            EditTextNoAutofill(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Test
    fun testGetAutofillType() {
        assertThat(editTextNoAutofill.autofillType, `is`(equalTo(View.AUTOFILL_TYPE_NONE)))
    }
}
