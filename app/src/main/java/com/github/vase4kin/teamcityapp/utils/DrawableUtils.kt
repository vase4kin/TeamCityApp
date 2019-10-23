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

package com.github.vase4kin.teamcityapp.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

fun getTintedDrawable(
    context: Context,
    @DrawableRes intRes: Int,
    @ColorRes colorRes: Int
): Drawable {
    val drawable = DrawableCompat.wrap(
        ContextCompat.getDrawable(context, intRes) ?: getEmptyDrawable()
    ).mutate()
    val color = ContextCompat.getColor(context, colorRes)
    drawable.setTint(color)
    return drawable
}

private fun getEmptyDrawable(): Drawable = ColorDrawable(
    Color.TRANSPARENT
)