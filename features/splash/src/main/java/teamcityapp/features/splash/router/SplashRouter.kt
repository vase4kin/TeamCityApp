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

package teamcityapp.features.splash.router

/**
 * Router to handle [teamcityapp.features.splash.view.SplashActivity] navigation
 */
interface SplashRouter {

    /**
     * Open login page
     */
    fun openLoginPage()

    /**
     * Open root projects activity
     */
    fun openProjectsRootPage()

    /**
     * Close activity
     */
    fun close()
}
