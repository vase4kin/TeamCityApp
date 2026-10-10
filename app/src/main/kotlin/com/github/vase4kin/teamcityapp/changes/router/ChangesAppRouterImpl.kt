/*
 * Copyright 2026 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.changes.router

import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.base.tabs.data.OnTextTabChangeEvent
import com.github.vase4kin.teamcityapp.build_details.presenter.BuildDetailsPresenter
import javax.inject.Inject
import org.greenrobot.eventbus.EventBus
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangeDetailsNavigation
import teamcityapp.features.changes.api.ChangesAppRouter

/** Keeps outgoing change navigation and legacy tab-count events with the current UI owner. */
class ChangesAppRouterImpl @Inject constructor(
    private val fragment: Fragment,
    private val changeDetailsNavigation: ChangeDetailsNavigation,
    private val eventBus: EventBus
) : ChangesAppRouter {
    override fun openChange(change: ChangeDetails) = changeDetailsNavigation.open(fragment.requireActivity(), change)
    override fun updateTabCount(count: Int) {
        eventBus.post(OnTextTabChangeEvent(count, BuildDetailsPresenter.CHANGES_TAB))
    }
}
