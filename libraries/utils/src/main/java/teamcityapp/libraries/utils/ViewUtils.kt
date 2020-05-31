/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.libraries.utils

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

fun AppCompatActivity.initToolbar() {
    val toolBar: Toolbar? = findViewById(R.id.toolbar)
    if (toolBar != null) {
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_black_24dp)
        toolBar.setNavigationOnClickListener {
            finish()
        }
    }
}

@ColorInt
fun Context.getThemeColor(@AttrRes attribute: Int) =
    TypedValue().let { theme.resolveAttribute(attribute, it, true); it.data }

fun View.initDrawer(onDrawerClick: () -> Unit) {
    val toolBar: Toolbar? = findViewById(R.id.toolbar)
    if (toolBar != null) {
        toolBar.setNavigationContentDescription(R.string.content_navigation_content_description)
        toolBar.setNavigationOnClickListener {
            onDrawerClick()
        }
        toolBar.setNavigationIcon(R.drawable.ic_dehaze_black_24dp)
    }
}
