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

package teamcityapp.features.build_history.impl
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import teamcityapp.features.build_history.api.*
import teamcityapp.features.build_history.impl.tracker.BuildHistoryTracker
@HiltViewModel
class AndroidBuildHistoryViewModel @Inject constructor(handle: SavedStateHandle, repository: BuildHistoryRepository, onboarding: BuildHistoryOnboardingRepository, tracker: BuildHistoryTracker) : BuildHistoryViewModel(handle, repository, onboarding, tracker)
