package com.github.vase4kin.teamcityapp.agents.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.agents.data.AgentDataModel
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManager
import com.github.vase4kin.teamcityapp.agents.data.AgentsDataManagerImpl
import com.github.vase4kin.teamcityapp.agents.extractor.AgentsValueExtractor
import com.github.vase4kin.teamcityapp.agents.extractor.AgentsValueExtractorImpl
import com.github.vase4kin.teamcityapp.agents.presenter.AgentPresenterImpl
import com.github.vase4kin.teamcityapp.agents.view.AgentListFragment
import com.github.vase4kin.teamcityapp.agents.view.AgentViewHolderFactory
import com.github.vase4kin.teamcityapp.agents.view.AgentViewImpl
import com.github.vase4kin.teamcityapp.agents.view.AgentsAdapter
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
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
object AgentModule {

    @Provides
    fun provideOwner(owner: Fragment): AgentListFragment = owner.requireScreenOwner<AgentListFragment>()

    @Provides
    fun providesAgentsDataManager(repository: Repository, eventBus: EventBus): AgentsDataManager {
        return AgentsDataManagerImpl(repository, eventBus)
    }

    @Provides
    fun providesBaseListView(
        fragment: AgentListFragment,
        adapter: AgentsAdapter,
        filterProvider: FilterProvider
    ): BaseListView<AgentDataModel> {
        return AgentViewImpl(
            fragment.requireView(),
            fragment.requireActivity(),
            R.string.empty_list_message_agents,
            adapter,
            filterProvider
        )
    }

    @Provides
    fun providesAgentsValueExtractor(fragment: AgentListFragment): AgentsValueExtractor {
        return AgentsValueExtractorImpl(fragment.arguments ?: Bundle.EMPTY)
    }

    @Provides
    @Named("AgentListFragment")
    fun providesViewTracker(): ViewTracker {
        return ViewTracker.STUB
    }

    @Provides
    fun providesAgentsAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<AgentDataModel>>): AgentsAdapter {
        return AgentsAdapter(viewHolderFactories)
    }

    @Provides
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    fun providesAgentViewHolderFactory(): ViewHolderFactory<AgentDataModel> {
        return AgentViewHolderFactory()
    }

    @Provides
    fun provideAgentPresenterImpl(
        view: BaseListView<AgentDataModel>,
        dataManager: AgentsDataManager,
        @Named("AgentListFragment") tracker: ViewTracker,
        valueExtractor: AgentsValueExtractor,
        filterProvider: FilterProvider,
        eventBus: EventBus
    ): AgentPresenterImpl = AgentPresenterImpl(view, dataManager, tracker, valueExtractor, filterProvider, eventBus)
}
