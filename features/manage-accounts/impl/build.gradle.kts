/*
 * Copyright 2020 Andrey Tolpeev
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

import com.android.build.api.dsl.LibraryExtension

plugins {
    id("teamcityapp.android.library")
    id("teamcityapp.android.hilt")
    id("teamcityapp.android.compose")
    id("teamcityapp.android.screenshot")
}
extensions.configure<LibraryExtension> { namespace = "teamcityapp.features.manage_accounts.impl" }
dependencies {
    implementation(projects.features.manageAccounts.api)
    implementation(projects.libraries.theme)
    implementation(projects.libraries.resources)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.lifeCycleViewModel)
    implementation(libs.androidx.hilt.viewModelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.coroutines.android)
    implementation(platform(libs.google.firebaseBom))
    implementation(libs.google.analytics)
    testImplementation(libs.mockito.mockitoCore)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    kapt(libs.dagger.compiler)
}
