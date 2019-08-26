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

package com.github.vase4kin.teamcityapp.bottomsheet_dialog.dagger

import android.view.View
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.ArtifactBrowserMenuItemsFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.ArtifactDefaultMenuItemsFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.ArtifactFolderMenuItemsFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.ArtifactFullMenuItemsFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.BranchMenuItemsFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.BuildTypeMenuItemsFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.DefaultMenuItemsFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.MenuItemsFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.ProjectMenuItemsFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModel
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetDataModelImpl
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetInteractor
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.model.BottomSheetInteractorImpl
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetAdapter
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetItemViewHolderFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetView
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetViewImpl
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import org.greenrobot.eventbus.EventBus

/**
 * Bottom sheet dialog dependencies
 */
@Module
class BottomSheetModule(private val view: View, private val fragment: BottomSheetDialogFragment) {
    private val menuType: Int
    private val title: String
    private val descriptions: List<String>

    init {
        this.menuType = fragment.arguments?.getInt(ARG_BOTTOM_SHEET_TYPE) ?: 0
        this.title = fragment.arguments?.getString(ARG_TITLE) ?: ""
        val descriptions = fragment.arguments?.getStringArray(ARG_DESCRIPTION)?.toList() ?: emptyList()
        this.descriptions = if (descriptions.isNotEmpty()) descriptions else listOf("")
    }

    @Provides
    fun providesBottomSheetDataModel(menuItemsFactories: Map<Int, @JvmSuppressWildcards MenuItemsFactory>): BottomSheetDataModel {
        return BottomSheetDataModelImpl(
            menuItemsFactories[menuType]?.createMenuItems() ?: emptyList()
        )
    }

    @Provides
    fun providesInteractor(
        model: BottomSheetDataModel,
        eventBus: EventBus
    ): BottomSheetInteractor {
        return BottomSheetInteractorImpl(title, model, view.context, eventBus)
    }

    @Provides
    fun providesBottomSheetView(adapter: BottomSheetAdapter): BottomSheetView {
        return BottomSheetViewImpl(view, fragment, adapter)
    }

    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_DEFAULT)
    @Provides
    fun providesDefaultMenu(): MenuItemsFactory {
        return DefaultMenuItemsFactory(view.context, descriptions)
    }

    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_BRANCH)
    @Provides
    fun providesBranchMenu(): MenuItemsFactory {
        return BranchMenuItemsFactory(view.context, descriptions)
    }

    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_ARTIFACT_DEFAULT)
    @Provides
    fun providesArtifactDefaultMenu(): MenuItemsFactory {
        return ArtifactDefaultMenuItemsFactory(view.context, descriptions)
    }

    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_ARTIFACT_BROWSER)
    @Provides
    fun providesArtifactBrowserMenu(): MenuItemsFactory {
        return ArtifactBrowserMenuItemsFactory(view.context, descriptions)
    }

    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_ARTIFACT_FOLDER)
    @Provides
    fun providesArtifactFolderMenu(): MenuItemsFactory {
        return ArtifactFolderMenuItemsFactory(view.context, descriptions)
    }

    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_ARTIFACT_FULL)
    @Provides
    fun providesArtifactFullMenu(): MenuItemsFactory {
        return ArtifactFullMenuItemsFactory(view.context, descriptions)
    }

    @Provides
    fun providesAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<BottomSheetDataModel>>): BottomSheetAdapter {
        return BottomSheetAdapter(viewHolderFactories)
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    fun providesViewHolderFactory(): ViewHolderFactory<BottomSheetDataModel> {
        return BottomSheetItemViewHolderFactory()
    }

    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_BUILD_TYPE)
    @Provides
    fun providesBuildTypeMenu(): MenuItemsFactory {
        return BuildTypeMenuItemsFactory(view.context, descriptions)
    }

    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_PROJECT)
    @Provides
    fun providesProjectMenu(): MenuItemsFactory {
        return ProjectMenuItemsFactory(view.context, descriptions)
    }

    companion object {

        const val ARG_TITLE = "arg_title"
        const val ARG_DESCRIPTION = "arg_description"
        const val ARG_BOTTOM_SHEET_TYPE = "arg_bottom_sheet_type"
    }
}
