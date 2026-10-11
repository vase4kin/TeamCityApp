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

package teamcityapp.features.filter_bottom_sheet.impl.navigation

import android.os.Bundle
import androidx.compose.runtime.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.filter_bottom_sheet.api.navigation.QuickFilterDestination
import teamcityapp.features.filter_bottom_sheet.impl.FilterBottomSheetRoute
import teamcityapp.features.filter_bottom_sheet.impl.router.FilterBottomSheetRouter
import teamcityapp.libraries.navigation.*

class QuickFilterDestinationRegistration @Inject constructor() : FeatureDestination<QuickFilterDestination>(QuickFilterDestination::class, QuickFilterDestination.serializer()) {
    override val presentation = DestinationPresentation.BottomSheet

    @Composable override fun Content(destination: QuickFilterDestination, navigator: Navigator, entryId: String) {
        val arguments = remember(destination) { Bundle().apply { putInt("arg_code", destination.code) } }
        val router = remember(navigator, entryId) {
            object : FilterBottomSheetRouter {
                override fun close() {
                    navigator.pop()
                }
            }
        }
        DestinationArguments(arguments) { FilterBottomSheetRoute(router) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class QuickFilterDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: QuickFilterDestinationRegistration): DestinationRegistration
}
