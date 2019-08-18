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

package com.github.vase4kin.teamcityapp.account.create.view

import android.view.MenuItem
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.github.vase4kin.teamcityapp.R

class OnCreateMenuItemClickListenerImpl(
    private val onValidateListener: OnValidateListener,
    private val serverUrl: TextView,
    private val userName: TextView,
    private val password: TextView,
    private val guestUserSwitch: Switch,
    private val disableSslSwitch: Switch
) : Toolbar.OnMenuItemClickListener {

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_create) {
            if (guestUserSwitch.isChecked) {
                onValidateListener.validateGuestUserData(
                        serverUrl.text.toString().trim(),
                        disableSslSwitch.isChecked)
            } else {
                onValidateListener.validateUserData(
                        serverUrl.text.toString().trim(),
                        userName.text.toString().trim(),
                        password.text.toString().trim(),
                        disableSslSwitch.isChecked)
            }
            return true
        }
        return false
    }
}
