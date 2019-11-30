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

package teamcityapp.features.test_details.data

interface TestDetailsDataManager {

    /**
     * Load test details
     *
     * @param loadingListener - Listener to receive server callbacks
     * @param url - Test details url
     */
    fun loadData(
        onSuccess: (testDetails: String) -> Unit,
        onError: (errorMessage: String) -> Unit, //No error needed here
        url: String
    )

    /**
     * Unsubscribe all server subscriptions
     */
    fun unsubscribe()
}
