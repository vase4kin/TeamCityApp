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

package com.github.vase4kin.teamcityapp.buildlist.presenter

import com.github.vase4kin.teamcityapp.base.list.presenter.BaseListPresenter
import com.github.vase4kin.teamcityapp.base.presenter.BaseMenuPresenter
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilter

/**
 * Presenter to handle logic of [com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity]
 */
interface BuildListPresenter : BaseListPresenter, BaseMenuPresenter {

    /**
     * On run build activity result
     *
     * @param queuedBuildHref - Queued build href
     */
    fun onRunBuildActivityResult(queuedBuildHref: String)

    /**
     * On filter builds activity result
     *
     * @param filter - filter to filter builds
     */
    fun onFilterBuildsActivityResult(filter: BuildListFilter)
}
