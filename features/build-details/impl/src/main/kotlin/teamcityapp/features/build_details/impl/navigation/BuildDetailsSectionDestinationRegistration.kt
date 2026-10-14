/*
 * Copyright 2026 Andrey Tolpeev
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

package teamcityapp.features.build_details.impl.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.serialization.modules.*
import teamcityapp.features.artifacts.api.navigation.ArtifactsDestination
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_details.impl.BuildDetailsSectionScreen
import teamcityapp.features.changes.api.navigation.ChangesDestination
import teamcityapp.features.properties.api.Property
import teamcityapp.features.properties.api.navigation.PropertiesDestination
import teamcityapp.features.snapshot_dependencies.api.navigation.SnapshotDependenciesDestination
import teamcityapp.features.tests.api.navigation.TestsDestination
import teamcityapp.libraries.navigation.*

internal fun BuildDetailsSectionDestination.contentDestination() = when (section) {
    BuildDetailsSection.Changes -> ChangesDestination(checkNotNull(build.changes?.href))
    BuildDetailsSection.Tests -> checkNotNull(build.tests).let { TestsDestination(checkNotNull(it.href), it.passed, it.failed, it.ignored) }
    BuildDetailsSection.Parameters -> PropertiesDestination(build.properties?.items.orEmpty().map { Property(it.name.orEmpty(), it.value.orEmpty()) })
    BuildDetailsSection.Artifacts -> ArtifactsDestination(build, checkNotNull(build.artifacts?.href), showToolbar = false)
    BuildDetailsSection.Snapshots -> SnapshotDependenciesDestination(build.id)
    else -> error("Not a build section")
}

class BuildDetailsSectionDestinationRegistration @Inject constructor(private val registry: Provider<DestinationRegistry>) : FeatureDestination<BuildDetailsSectionDestination>(BuildDetailsSectionDestination::class, BuildDetailsSectionDestination.serializer()) {
    override fun registerSerializers(builder: SerializersModuleBuilder) {
        super.registerSerializers(builder)
        builder.polymorphic(NavigationResult::class) { subclass(BuildDetailsSectionCountResult::class, BuildDetailsSectionCountResult.serializer()) }
    }

    @Composable override fun Content(destination: BuildDetailsSectionDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val content = remember(destination, entryId) { NavigationEntry("$entryId/content", destination.contentDestination()) }
        var count by rememberSaveable(destination, entryId) { mutableStateOf<Int?>(null) }
        val countHost = remember(destination, entryId) { BuildDetailsSectionCountHost { count = it } }
        val up = {
            val updated = count
            val origin = destination.originEntryId
            val finished = updated != null && origin != null && navigator.finish(BuildDetailsSectionCountResult(destination.build.id, destination.section, updated), origin)
            if (!finished && !navigator.pop()) activity.finish()
        }
        BackHandler(enabled = count != null && destination.originEntryId != null && navigator.current.id == entryId, onBack = up)
        BuildDetailsSectionScreen(destination.section, destination.buildNumber, destination.configurationName ?: destination.build.configuration?.name.orEmpty(), up) {
            CompositionLocalProvider(LocalBuildDetailsSectionCountHost provides countHost) {
                RetainedNavigationPane(listOf(content), content.id, navigator, registry.get(), resultRecipientId = entryId)
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BuildDetailsSectionDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: BuildDetailsSectionDestinationRegistration): DestinationRegistration
}
