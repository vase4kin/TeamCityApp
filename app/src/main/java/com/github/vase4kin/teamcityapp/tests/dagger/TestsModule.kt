package com.github.vase4kin.teamcityapp.tests.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import teamcityapp.features.test_details.api.TestDetailsNavigation
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.tests.data.TestsDataManager
import com.github.vase4kin.teamcityapp.tests.data.TestsDataManagerImpl
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModel
import com.github.vase4kin.teamcityapp.tests.extractor.TestsValueExtractor
import com.github.vase4kin.teamcityapp.tests.extractor.TestsValueExtractorImpl
import com.github.vase4kin.teamcityapp.tests.presenter.TestsPresenterImpl
import com.github.vase4kin.teamcityapp.tests.router.TestsRouter
import com.github.vase4kin.teamcityapp.tests.router.TestsRouterImpl
import com.github.vase4kin.teamcityapp.tests.view.LoadMoreViewHolderFactory
import com.github.vase4kin.teamcityapp.tests.view.TestOccurrenceViewHolderFactory
import com.github.vase4kin.teamcityapp.tests.view.TestOccurrencesAdapter
import com.github.vase4kin.teamcityapp.tests.view.TestOccurrencesFragment
import com.github.vase4kin.teamcityapp.tests.view.TestsView
import com.github.vase4kin.teamcityapp.tests.view.TestsViewImpl
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
object TestsModule {

    @Provides
    fun provideOwner(owner: Fragment): TestOccurrencesFragment = owner.requireScreenOwner<TestOccurrencesFragment>()

    @Provides
    fun providesTestsDataManager(repository: Repository, eventBus: EventBus): TestsDataManager {
        return TestsDataManagerImpl(repository, eventBus)
    }

    @Provides
    fun providesTestsValueExtractor(fragment: TestOccurrencesFragment): TestsValueExtractor {
        return TestsValueExtractorImpl(fragment.arguments ?: Bundle.EMPTY)
    }

    @Provides
    fun providesTestsView(
        testsValueExtractor: TestsValueExtractor,
        adapter: SimpleSectionedRecyclerViewAdapter<TestOccurrencesAdapter>,
        fragment: TestOccurrencesFragment
    ): TestsView {
        return TestsViewImpl(
            fragment.requireView(),
            fragment.requireActivity(),
            testsValueExtractor,
            R.string.empty_passed_tests,
            adapter
        )
    }

    @Provides
    fun providesTestsRouter(fragment: TestOccurrencesFragment, navigation: TestDetailsNavigation): TestsRouter {
        return TestsRouterImpl(fragment.requireActivity(), navigation)
    }

    @Provides
    @Named("TestOccurrencesFragment")
    fun providesViewTracker(): ViewTracker {
        return ViewTracker.STUB
    }

    @Provides
    fun providesTestOccurrencesAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<TestsDataModel>>): TestOccurrencesAdapter {
        return TestOccurrencesAdapter(viewHolderFactories)
    }

    @Provides
    fun providesSimpleSectionedRecyclerViewAdapter(
        fragment: TestOccurrencesFragment,
        adapter: TestOccurrencesAdapter
    ): SimpleSectionedRecyclerViewAdapter<TestOccurrencesAdapter> {
        return SimpleSectionedRecyclerViewAdapter(fragment.requireContext(), adapter)
    }

    @Provides
    @IntoMap
    @IntKey(BaseListView.TYPE_LOAD_MORE)
    fun providesLoadMoreViewHolderFactory(): ViewHolderFactory<TestsDataModel> {
        return LoadMoreViewHolderFactory()
    }

    @Provides
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    fun providesTestsViewHolderFactory(): ViewHolderFactory<TestsDataModel> {
        return TestOccurrenceViewHolderFactory()
    }

    @Provides
    fun provideTestsPresenterImpl(
        view: TestsView,
        dataManager: TestsDataManager,
        @Named("TestOccurrencesFragment") tracker: ViewTracker,
        valueExtractor: TestsValueExtractor,
        router: TestsRouter
    ): TestsPresenterImpl = TestsPresenterImpl(view, dataManager, tracker, valueExtractor, router)
}
