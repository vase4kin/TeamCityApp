/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.libraries.chrome_tabs

/**
 * Chrome custom tabs manager
 */
interface ChromeCustomTabs {

    /**
     * Init chrome custom tabs service
     */
    fun initCustomsTabs()

    /**
     * Unbind chrome custom tabs service
     */
    fun unbindCustomsTabs()

    /**
     * Open browser with url
     *
     * @param url - Url to open
     */
    fun launchUrl(url: String)
}
