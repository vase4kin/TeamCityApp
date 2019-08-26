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

package com.github.vase4kin.teamcityapp.base.tabs.data

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

open class BaseTabsDataManagerImpl(protected var eventBus: EventBus) : BaseTabsDataManager {

    private var listener: OnTextTabChangeEventListener? = null

    /**
     * {@inheritDoc}
     */
    override fun registerEventBus() {
        eventBus.register(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun unregisterEventBus() {
        eventBus.unregister(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun setListener(listener: OnTextTabChangeEventListener) {
        this.listener = listener
    }

    /**
     * Handle [OnTextTabChangeEvent]
     */
    @Subscribe
    fun onEvent(event: OnTextTabChangeEvent) {
        listener?.onUpdateTabTitle(event.tabPosition, event.count.toString())
    }
}
