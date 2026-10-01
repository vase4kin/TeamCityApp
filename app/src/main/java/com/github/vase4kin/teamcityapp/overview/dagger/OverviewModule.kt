package com.github.vase4kin.teamcityapp.overview.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.overview.data.OverViewInteractor
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModel
import com.github.vase4kin.teamcityapp.overview.data.OverviewInteractorImpl
import com.github.vase4kin.teamcityapp.overview.data.OverviewValueExtractor
import com.github.vase4kin.teamcityapp.overview.data.OverviewValueExtractorImpl
import com.github.vase4kin.teamcityapp.overview.presenter.OverviewPresenterImpl
import com.github.vase4kin.teamcityapp.overview.tracker.FirebaseOverviewTrackerImpl
import com.github.vase4kin.teamcityapp.overview.tracker.OverviewTracker
import com.github.vase4kin.teamcityapp.overview.view.OverviewAdapter
import com.github.vase4kin.teamcityapp.overview.view.OverviewFragment
import com.github.vase4kin.teamcityapp.overview.view.OverviewView
import com.github.vase4kin.teamcityapp.overview.view.OverviewViewHolderFactory
import com.github.vase4kin.teamcityapp.overview.view.OverviewViewImpl
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import org.greenrobot.eventbus.EventBus
import teamcityapp.libraries.onboarding.OnboardingManager

@Module
@InstallIn(FragmentComponent::class)
object OverviewModule {

    @Provides
    fun provideOwner(owner: Fragment): OverviewFragment = owner.requireScreenOwner<OverviewFragment>()

    @Provides
    fun providesOverViewDataManager(
        repository: Repository,
        eventBus: EventBus,
        valueExtractor: OverviewValueExtractor
    ): OverViewInteractor {
        return OverviewInteractorImpl(repository, eventBus, valueExtractor)
    }

    @Provides
    fun providesBaseValueExtractor(fragment: OverviewFragment): OverviewValueExtractor {
        return OverviewValueExtractorImpl(fragment.arguments ?: Bundle.EMPTY)
    }

    @Provides
    fun providesBaseListView(
        adapter: OverviewAdapter,
        fragment: OverviewFragment
    ): OverviewView {
        return OverviewViewImpl(fragment.requireView(), fragment.requireActivity().requireScreenOwner<AppCompatActivity>(), adapter)
    }

    @Provides
    fun providesOverviewAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<OverviewDataModel>>): OverviewAdapter {
        return OverviewAdapter(viewHolderFactories)
    }

    @Provides
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    fun providesOverviewViewHolderFactory(): ViewHolderFactory<OverviewDataModel> {
        return OverviewViewHolderFactory()
    }

    @Provides
    fun providesFirebaseViewTracker(firebaseAnalytics: FirebaseAnalytics): OverviewTracker {
        return FirebaseOverviewTrackerImpl(firebaseAnalytics)
    }
    @Provides
    fun presenter(
        view: OverviewView,
        interactor: OverViewInteractor,
        tracker: OverviewTracker,
        onboardingManager: OnboardingManager
    ): OverviewPresenterImpl =
        OverviewPresenterImpl(view, interactor, tracker, onboardingManager)
}
