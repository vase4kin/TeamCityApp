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

package teamcityapp.features.drawer.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import teamcityapp.features.drawer.R
import teamcityapp.features.drawer.viewmodel.DrawerViewModel
import javax.inject.Inject

/**
 * Bottom sheet dialog
 */
class DrawerBottomSheetDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var drawerAdapter: DrawerAdapter

    @Inject
    lateinit var viewModel: DrawerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_bottom_sheet_drawer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onDestroyView() {
        viewModel.onDestroyView()
        super.onDestroyView()
    }

    fun setAdapter(list: List<BaseDrawerItem>) {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.bottom_sheet_drawer_recycler_view) ?: return
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = drawerAdapter
        drawerAdapter.list.clear()
        drawerAdapter.list.addAll(list)
        recyclerView.invalidate()
    }

    companion object {
        fun createInstance(): DrawerBottomSheetDialogFragment =
            DrawerBottomSheetDialogFragment()
    }
}
