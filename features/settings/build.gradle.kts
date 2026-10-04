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
    id("teamcityapp.android.library")
    id("teamcityapp.android.hilt")
    id("teamcityapp.android.compose")
}

android {
    namespace = "teamcityapp.features.settings"
}

dependencies {
    implementation(projects.libraries.theme)
    implementation(projects.libraries.settings)
    implementation(projects.libraries.utils)
    implementation(projects.libraries.resources)

    implementation(libs.google.material)
    implementation(platform(libs.google.firebaseBom))
    implementation(libs.google.analytics)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.lifeCycleViewModel)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.mockitoCore)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    implementation(libs.kotlin.stdlib)
    implementation(libs.dagger.dagger)

    kapt(libs.dagger.compiler)
}
