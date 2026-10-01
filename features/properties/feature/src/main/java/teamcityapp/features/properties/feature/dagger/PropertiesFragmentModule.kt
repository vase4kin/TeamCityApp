package teamcityapp.features.properties.feature.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import androidx.fragment.app.Fragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.android.scopes.FragmentScoped
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import javax.inject.Named
import teamcityapp.features.properties.feature.model.InternalProperty
import teamcityapp.features.properties.feature.router.PropertiesRouter
import teamcityapp.features.properties.feature.view.PropertiesFragment
import teamcityapp.features.properties.feature.view.PropertyItemFactory
import teamcityapp.features.properties.feature.view.PropertyItemFactoryImpl
import teamcityapp.features.properties.feature.stateholder.PropertiesStateHolder
import teamcityapp.libraries.utils.ResourcesManager
import teamcityapp.libraries.utils.ResourcesManagerImpl

@Module
@InstallIn(FragmentComponent::class)
object PropertiesFragmentModule {

    @Provides
    fun provideOwner(owner: Fragment): PropertiesFragment = owner.requireScreenOwner<PropertiesFragment>()

    @Provides
    @FragmentScoped
    fun providesStateHolder(
        fragment: PropertiesFragment,
        @Named("PropertiesFragment") adapter: GroupAdapter<GroupieViewHolder>,
        factory: PropertyItemFactory,
        resourcesManager: ResourcesManager
    ): PropertiesStateHolder {
        return PropertiesStateHolder(
            adapter = adapter,
            properties = fragment.arguments?.getParcelableArrayList<InternalProperty>(PropertiesFragment.ARG_PROPERTIES) ?: emptyList(),
            factory = factory,
            resourcesManager = resourcesManager
        )
    }

    @Provides
    fun provideFactory(router: PropertiesRouter): PropertyItemFactory {
        return PropertyItemFactoryImpl(router)
    }

    @Provides
    @Named("PropertiesFragment")
    fun providesAdapter(): GroupAdapter<GroupieViewHolder> = GroupAdapter<GroupieViewHolder>()

    @Provides
    fun provideResManager(fragment: PropertiesFragment): ResourcesManager {
        return ResourcesManagerImpl(fragment.requireContext())
    }
}
