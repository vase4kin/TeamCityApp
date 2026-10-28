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

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package teamcityapp.shared

import kotlin.test.*
import platform.Foundation.*
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UISceneDidEnterBackgroundNotification

class IosPlatformStorageTest {
    @Test fun backgroundListenerRunsAndUnsubscribesWithItsScreen() {
        val platform = IosNativePlatform({ error("Background callbacks must not create UI") }, "background-test")
        var cancellations = 0
        val unsubscribe = platform.onBackground { cancellations++ }
        NSNotificationCenter.defaultCenter.postNotificationName(UIApplicationDidEnterBackgroundNotification, null)
        assertEquals(1, cancellations)
        NSNotificationCenter.defaultCenter.postNotificationName(UISceneDidEnterBackgroundNotification, null)
        assertEquals(2, cancellations)
        unsubscribe()
        NSNotificationCenter.defaultCenter.postNotificationName(UIApplicationDidEnterBackgroundNotification, null)
        NSNotificationCenter.defaultCenter.postNotificationName(UISceneDidEnterBackgroundNotification, null)
        assertEquals(2, cancellations)
    }

    @Test fun missingFileAndRejectedSystemActionsExposeDismissibleFailures() {
        val platform = IosNativePlatform({ error("Missing files must not create UI") }, "test-" + NSUUID().UUIDString)
        assertFalse(platform.openFile("/outside/app/report.txt"))
        assertNull(platform.failure.value)
        platform.clearFailure()
        assertNull(platform.failure.value)
        for (action in NativePlatformFailure.entries) {
            platform.rejected(action)
            assertEquals(action, platform.failure.value)
            platform.clearFailure()
        }
        platform.browser("file:///outside/report.txt")
        assertEquals(NativePlatformFailure.Browser, platform.failure.value)
    }
}
