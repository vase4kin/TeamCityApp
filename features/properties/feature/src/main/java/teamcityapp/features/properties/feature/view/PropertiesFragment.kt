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

package teamcityapp.features.properties.feature.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import dagger.android.support.DaggerFragment
import teamcityapp.features.properties.feature.R
import teamcityapp.features.properties.feature.databinding.FragmentPropertiesBinding
import teamcityapp.features.properties.feature.model.InternalProperty
import teamcityapp.features.properties.feature.viewmodel.PropertiesViewModel
import teamcityapp.features.properties.repository.models.Properties
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class PropertiesFragment : DaggerFragment() {

    @Inject
    lateinit var viewModel: PropertiesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return DataBindingUtil.inflate<FragmentPropertiesBinding>(
            inflater,
            R.layout.fragment_properties,
            container,
            false
        ).apply {
            viewmodel = this@PropertiesFragment.viewModel
            lifecycle.addObserver(this@PropertiesFragment.viewModel)
        }.root
    }

    companion object {

        const val ARG_PROPERTIES = "arg_properties"

        fun create(properties: List<Properties.Property>): Fragment {
            val internalProperties = properties
                .map {
                    InternalProperty(
                        it.name,
                        it.value
                    )
                }
                .toTypedArray()
            val bundle = Bundle().apply {
                putParcelableArray(ARG_PROPERTIES, internalProperties)
            }
            return PropertiesFragment()
                .apply {
                    arguments = bundle
                }
        }
    }
}
