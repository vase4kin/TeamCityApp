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

package teamcityapp.features.properties.impl.dagger

import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import teamcityapp.features.properties.impl.PropertiesFragment
import teamcityapp.features.properties.impl.router.PropertiesRouter
import teamcityapp.features.properties.impl.router.PropertiesRouterImpl
import teamcityapp.libraries.clipboard.ClipboardWriter
import teamcityapp.libraries.utils.requireScreenOwner

@Module
@InstallIn(FragmentComponent::class)
object PropertiesRouterModule {
    @Provides
    fun router(owner: Fragment, clipboard: ClipboardWriter): PropertiesRouter {
        owner.requireScreenOwner<PropertiesFragment>()
        return PropertiesRouterImpl(clipboard)
    }
}
