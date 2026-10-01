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

import teamcityapp.libraries.utils.requireScreenOwner
import androidx.fragment.app.Fragment
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
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.presenter.BottomSheetPresenterImpl
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetAdapter
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetDialogFragment
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetDialogFragment.Companion.ARG_BOTTOM_SHEET_TYPE
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetDialogFragment.Companion.ARG_DESCRIPTION
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetDialogFragment.Companion.ARG_TITLE
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetItemViewHolderFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetView
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetViewImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import org.greenrobot.eventbus.EventBus

@Module
@InstallIn(FragmentComponent::class)
object BottomSheetModule {

    @Provides
    fun provideOwner(owner: Fragment): BottomSheetDialogFragment = owner.requireScreenOwner<BottomSheetDialogFragment>()

    @Provides
    fun providesBottomSheetDataModel(
        fragment: BottomSheetDialogFragment,
        menuItemsFactories: Map<Int, @JvmSuppressWildcards MenuItemsFactory>
    ): BottomSheetDataModel {
        val menuType = fragment.arguments?.getInt(ARG_BOTTOM_SHEET_TYPE) ?: 0
        return BottomSheetDataModelImpl(menuItemsFactories[menuType]?.createMenuItems() ?: emptyList())
    }

    @Provides
    fun providesInteractor(
        fragment: BottomSheetDialogFragment,
        model: BottomSheetDataModel,
        eventBus: EventBus
    ): BottomSheetInteractor = BottomSheetInteractorImpl(
        fragment.arguments?.getString(ARG_TITLE) ?: "",
        model,
        fragment.requireView().context,
        eventBus
    )

    @Provides
    fun providesBottomSheetView(
        fragment: BottomSheetDialogFragment,
        adapter: BottomSheetAdapter
    ): BottomSheetView = BottomSheetViewImpl(fragment.requireView(), fragment, adapter)

    @Provides
    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_DEFAULT)
    fun providesDefaultMenu(fragment: BottomSheetDialogFragment): MenuItemsFactory =
        DefaultMenuItemsFactory(fragment.requireView().context, fragment.descriptions())

    @Provides
    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_BRANCH)
    fun providesBranchMenu(fragment: BottomSheetDialogFragment): MenuItemsFactory =
        BranchMenuItemsFactory(fragment.requireView().context, fragment.descriptions())

    @Provides
    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_ARTIFACT_DEFAULT)
    fun providesArtifactDefaultMenu(fragment: BottomSheetDialogFragment): MenuItemsFactory =
        ArtifactDefaultMenuItemsFactory(fragment.requireView().context, fragment.descriptions())

    @Provides
    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_ARTIFACT_BROWSER)
    fun providesArtifactBrowserMenu(fragment: BottomSheetDialogFragment): MenuItemsFactory =
        ArtifactBrowserMenuItemsFactory(fragment.requireView().context, fragment.descriptions())

    @Provides
    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_ARTIFACT_FOLDER)
    fun providesArtifactFolderMenu(fragment: BottomSheetDialogFragment): MenuItemsFactory =
        ArtifactFolderMenuItemsFactory(fragment.requireView().context, fragment.descriptions())

    @Provides
    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_ARTIFACT_FULL)
    fun providesArtifactFullMenu(fragment: BottomSheetDialogFragment): MenuItemsFactory =
        ArtifactFullMenuItemsFactory(fragment.requireView().context, fragment.descriptions())

    @Provides
    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_BUILD_TYPE)
    fun providesBuildTypeMenu(fragment: BottomSheetDialogFragment): MenuItemsFactory =
        BuildTypeMenuItemsFactory(fragment.requireView().context, fragment.descriptions())

    @Provides
    @IntoMap
    @IntKey(MenuItemsFactory.TYPE_PROJECT)
    fun providesProjectMenu(fragment: BottomSheetDialogFragment): MenuItemsFactory =
        ProjectMenuItemsFactory(fragment.requireView().context, fragment.descriptions())

    @Provides
    fun providesAdapter(
        viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<BottomSheetDataModel>>
    ): BottomSheetAdapter = BottomSheetAdapter(viewHolderFactories)

    @Provides
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    fun providesViewHolderFactory(): ViewHolderFactory<BottomSheetDataModel> = BottomSheetItemViewHolderFactory()

    @Provides
    fun provideBottomSheetPresenterImpl(
        view: BottomSheetView,
        interactor: BottomSheetInteractor
    ): BottomSheetPresenterImpl = BottomSheetPresenterImpl(view, interactor)

    private fun BottomSheetDialogFragment.descriptions(): List<String> {
        val descriptions = arguments?.getStringArray(ARG_DESCRIPTION)?.toList() ?: emptyList()
        return if (descriptions.isNotEmpty()) descriptions else listOf("")
    }
}
