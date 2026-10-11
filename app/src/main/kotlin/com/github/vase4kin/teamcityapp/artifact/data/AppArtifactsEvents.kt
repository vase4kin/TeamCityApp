/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.artifact.data

import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import teamcityapp.features.artifacts.api.ArtifactAction
import teamcityapp.features.artifacts.api.ArtifactDownload
import teamcityapp.features.artifacts.api.ArtifactsEvents

/** Each visible/resumed feature route owns one subscription; nothing is retained between hosts. */
class AppArtifactsEvents @Inject constructor(private val eventBus: EventBus) : ArtifactsEvents {
    override val actions: Flow<ArtifactAction> = callbackFlow {
        val subscriber = Subscriber { trySend(it) }
        eventBus.register(subscriber)
        awaitClose { eventBus.unregister(subscriber) }
    }

    override fun downloadFailed() = eventBus.post(ArtifactErrorDownloadingEvent())

    // Public subscriber/methods keep EventBus reflection compatible with release builds.
    class Subscriber internal constructor(private val action: (ArtifactAction) -> Unit) {
        @Subscribe fun download(event: ArtifactDownloadEvent) = action(ArtifactAction.Download(ArtifactDownload(event.name, event.value)))

        @Subscribe fun open(event: ArtifactOpenEvent) = action(ArtifactAction.Open(event.fileName, event.href))

        @Subscribe fun browser(event: ArtifactOpenInBrowserEvent) = action(ArtifactAction.Browser(event.href))
    }
}
