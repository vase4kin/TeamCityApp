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

plugins {
    id("teamcityapp.android.application") apply false
    id("teamcityapp.android.library") apply false
    id("teamcityapp.android.library.java") apply false
    id("teamcityapp.android.hilt") apply false
    id("teamcityapp.android.data-binding") apply false
    id("teamcityapp.android.coverage") apply false
    id("teamcityapp.android.compose") apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.performance) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.google.oss.licenses) apply false
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
