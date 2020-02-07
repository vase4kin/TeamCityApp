/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.about.dagger

import dagger.Module
import dagger.Provides
import teamcityapp.features.about.AboutFragment
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl

@Module
object AboutFragmentModule {

    @JvmStatic
    @Provides
    fun provideChromeTabs(fragment: AboutFragment): ChromeCustomTabs =
        ChromeCustomTabsImpl(fragment.requireActivity())
}
