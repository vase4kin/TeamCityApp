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
    id("teamcityapp.android.data-binding")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "teamcityapp.features.change"
}

dependencies {
    implementation(projects.libraries.resources)
    implementation(projects.libraries.theme)
    implementation(projects.libraries.utils)
    implementation(projects.libraries.chromeTabs)
    implementation(projects.libraries.storage)
    implementation(projects.libraries.storageModels)
    implementation(projects.libraries.api)

    implementation(platform(libs.google.firebaseBom))
    implementation(libs.google.analytics)
    implementation(libs.google.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.dagger.dagger)
    implementation(libs.kotlin.stdlib)
    implementation(libs.groupie.groupie)
    implementation(libs.groupie.groupieDatabinding)

    kapt(libs.dagger.compiler)
}
