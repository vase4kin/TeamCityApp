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

package teamcityapp.features.bottom_sheet.impl.navigation

import android.os.Bundle
import androidx.compose.runtime.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import kotlinx.serialization.modules.*
import teamcityapp.features.bottom_sheet.api.SheetAction
import teamcityapp.features.bottom_sheet.api.navigation.*
import teamcityapp.features.bottom_sheet.impl.BottomSheetRoute
import teamcityapp.features.bottom_sheet.impl.router.BottomSheetRouter
import teamcityapp.libraries.clipboard.ClipboardWriter
import teamcityapp.libraries.navigation.*

class ActionSheetDestinationRegistration @Inject constructor(private val clipboard: ClipboardWriter) : FeatureDestination<ActionSheetDestination>(ActionSheetDestination::class, ActionSheetDestination.serializer()) {
    override val presentation = DestinationPresentation.BottomSheet
    override fun registerSerializers(builder: SerializersModuleBuilder) {
        super.registerSerializers(builder)
        builder.polymorphic(NavigationResult::class) { subclass(ActionSheetResult::class, ActionSheetResult.serializer()) }
    }

    @Composable override fun Content(destination: ActionSheetDestination, navigator: Navigator, entryId: String) {
        val arguments = remember(destination) {
            Bundle().apply {
                putString("arg_title", destination.title)
                putStringArray("arg_description", destination.descriptions.toTypedArray())
                putInt("arg_bottom_sheet_type", destination.menu.ordinal)
            }
        }
        val router = remember(destination, navigator) {
            object : BottomSheetRouter {
                override fun perform(item: teamcityapp.features.bottom_sheet.api.SheetItem) {
                    if (item.action == SheetAction.Copy) clipboard.copy("", item.description)
                    navigator.finish(ActionSheetResult(destination.consumerId, item), destination.recipientId)
                }
            }
        }
        DestinationArguments(arguments) { BottomSheetRoute(router) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ActionSheetDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: ActionSheetDestinationRegistration): DestinationRegistration
}
