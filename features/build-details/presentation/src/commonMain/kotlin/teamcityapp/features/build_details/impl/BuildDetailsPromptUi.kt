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

package teamcityapp.features.build_details.impl
import org.jetbrains.compose.resources.StringResource
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_details.presentation.resources.*
data class BuildDetailsPromptUi(
    val prompt: BuildDetailsPrompt,
    val saving: Boolean = false,
    val saveFailed: Boolean = false
) {
    val action: BuildDetailsAction get() = when (prompt) {
        BuildDetailsPrompt.Stop, BuildDetailsPrompt.Remove -> BuildDetailsAction.Cancel
        BuildDetailsPrompt.Restart -> BuildDetailsAction.Restart
        BuildDetailsPrompt.Share -> BuildDetailsAction.Share
        BuildDetailsPrompt.Browser -> BuildDetailsAction.Browser
    }

    val title: StringResource get() = when (prompt) {
        BuildDetailsPrompt.Stop -> Res.string.title_onboarding_stop_build
        BuildDetailsPrompt.Remove -> Res.string.title_onboarding_remove_build
        BuildDetailsPrompt.Restart -> Res.string.title_onboarding_restart_build
        BuildDetailsPrompt.Share -> Res.string.title_onboarding_share_build
        BuildDetailsPrompt.Browser -> Res.string.title_onboarding_browser
    }

    val description: StringResource get() = when (prompt) {
        BuildDetailsPrompt.Stop -> Res.string.text_onboarding_stop_build
        BuildDetailsPrompt.Remove -> Res.string.text_onboarding_remove_build_from_queue
        BuildDetailsPrompt.Restart -> Res.string.text_onboarding_restart_build
        BuildDetailsPrompt.Share -> Res.string.text_onboarding_share_build
        BuildDetailsPrompt.Browser -> Res.string.text_onboarding_browser
    }
}
