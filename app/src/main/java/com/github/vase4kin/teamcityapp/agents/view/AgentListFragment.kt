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

package com.github.vase4kin.teamcityapp.agents.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.agents.presenter.AgentPresenterImpl
import com.github.vase4kin.teamcityapp.home.router.HomeRouter
import dagger.android.support.AndroidSupportInjection
import teamcityapp.libraries.utils.initDrawer
import javax.inject.Inject

/**
 * Manages single agents tab
 */
class AgentListFragment : Fragment() {

    @Inject
    lateinit var presenter: AgentPresenterImpl

    @Inject
    lateinit var homeRouter: HomeRouter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agents_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AndroidSupportInjection.inject(this)
        presenter.onViewsCreated()
        view.initDrawer {
            homeRouter.openDrawer()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }

    override fun onDestroyView() {
        presenter.onViewsDestroyed()
        super.onDestroyView()
    }
}
