package com.github.vase4kin.teamcityapp.changes.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataManager
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataManagerImpl
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel
import com.github.vase4kin.teamcityapp.changes.extractor.ChangesValueExtractor
import com.github.vase4kin.teamcityapp.changes.extractor.ChangesValueExtractorImpl
import com.github.vase4kin.teamcityapp.changes.presenter.ChangesPresenterImpl
import com.github.vase4kin.teamcityapp.changes.view.ChangesAdapter
import com.github.vase4kin.teamcityapp.changes.view.ChangesFragment
import com.github.vase4kin.teamcityapp.changes.view.ChangesView
import com.github.vase4kin.teamcityapp.changes.view.ChangesViewHolderFactory
import com.github.vase4kin.teamcityapp.changes.view.ChangesViewImpl
import com.github.vase4kin.teamcityapp.changes.view.LoadMoreViewHolderFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import javax.inject.Named
import org.greenrobot.eventbus.EventBus

@Module
@InstallIn(FragmentComponent::class)
object ChangesModule {

    @Provides
    fun provideOwner(owner: Fragment): ChangesFragment = owner.requireScreenOwner<ChangesFragment>()

    @Provides
    fun providesChangesDataManager(repository: Repository, eventBus: EventBus): ChangesDataManager {
        return ChangesDataManagerImpl(repository, eventBus)
    }

    @Provides
    fun providesChangesView(
        fragment: ChangesFragment,
        changesAdapter: ChangesAdapter
    ): ChangesView {
        return ChangesViewImpl(fragment.requireView(), fragment.requireActivity(), R.string.empty_list_message_changes, changesAdapter)
    }

    @Provides
    fun providesChangesValueExtractor(fragment: ChangesFragment): ChangesValueExtractor {
        return ChangesValueExtractorImpl(fragment.arguments ?: Bundle.EMPTY)
    }

    @Provides
    @Named("ChangesFragment")
    fun providesViewTracker(): ViewTracker {
        return ViewTracker.STUB
    }

    @Provides
    fun providesChangesAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<ChangesDataModel>>): ChangesAdapter {
        return ChangesAdapter(viewHolderFactories)
    }

    @Provides
    @IntoMap
    @IntKey(BaseListView.TYPE_LOAD_MORE)
    fun providesLoadMoreViewHolderFactory(): ViewHolderFactory<ChangesDataModel> {
        return LoadMoreViewHolderFactory()
    }

    @Provides
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    fun providesChangesViewHolderFactory(): ViewHolderFactory<ChangesDataModel> {
        return ChangesViewHolderFactory()
    }

    @Provides
    fun provideChangesPresenterImpl(
        view: ChangesView,
        dataManager: ChangesDataManager,
        @Named("ChangesFragment") tracker: ViewTracker,
        valueExtractor: ChangesValueExtractor
    ): ChangesPresenterImpl = ChangesPresenterImpl(view, dataManager, tracker, valueExtractor)
}
