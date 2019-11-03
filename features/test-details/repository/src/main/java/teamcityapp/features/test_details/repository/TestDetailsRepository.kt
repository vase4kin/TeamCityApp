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

package teamcityapp.features.test_details.repository

import io.reactivex.Single
import teamcityapp.features.test_details.api.modelsTestOccurrence

interface TestDetailsRepository {

    /**
     * Get single test info by url (cache's supported)
     *
     * @param url - Test url
     * @return [Single] with [TestOccurrence]
     */
    fun testOccurrence(url: String): Single<TestOccurrence>
}
