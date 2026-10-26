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

import teamcityapp.buildlogic.Config

plugins {
    id("teamcityapp.kmp.compose")
    id("teamcityapp.kmp.hilt")
    id("teamcityapp.android.screenshot")
}

kotlin { android { namespace = "teamcityapp.features.about.impl" } }

compose.resources {
    packageOfResClass = "teamcityapp.features.about.impl.resources"
}

kotlin.sourceSets {
    named("commonMain") {
        dependencies {
            api(projects.features.about.api)
            api(projects.libraries.designSystem)
            implementation(libs.multiplatform.lifecycle.viewmodel)
            implementation(libs.multiplatform.lifecycle.runtime)
            implementation(libs.multiplatform.compose.windowSizeClass)
            implementation(libs.coroutines.core)
        }
    }
    named("commonTest") {
        dependencies {
            implementation(libs.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

kotlin.sourceSets.named("androidMain") {
    dependencies {
        implementation(projects.libraries.navigation)
        implementation(projects.features.about.api)
        implementation(projects.libraries.appRating)
        implementation(projects.libraries.resources)
        implementation(projects.libraries.theme)
        implementation(projects.libraries.chromeTabs)
        implementation(libs.compose.material3.windowSizeClass)
        implementation(libs.dagger.dagger)
        implementation(libs.androidx.appcompat)
        implementation(libs.google.openSourceLicensesLibrary)
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.lifecycle.lifeCycleViewModel)
        implementation(libs.androidx.hilt.viewModelCompose)
        implementation(libs.androidx.lifecycle.runtimeCompose)
        implementation(libs.coroutines.android)
        implementation(libs.kotlin.stdlib)
    }
}
kotlin.sourceSets.named("androidHostTest") {
    dependencies {
        implementation(libs.mockito.mockitoCore)
        implementation(libs.coroutines.test)
        implementation(libs.turbine)
    }
}

roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/test/screenshots/debug"))
    separateOutputDirs.set(false)
}

val generatedVersion = layout.buildDirectory.dir("generated/aboutVersion/commonMain/kotlin")
val generateAboutVersion = tasks.register("generateAboutVersion") {
    inputs.property("version", Config.versionName)
    outputs.dir(generatedVersion)
    doLast {
        generatedVersion.get().file("teamcityapp/features/about/impl/AboutVersion.kt").asFile.apply {
            parentFile.mkdirs()
            writeText("// Copyright 2026 Andrey Tolpeev. Licensed under the Apache License, Version 2.0.\npackage teamcityapp.features.about.impl\ninternal const val ABOUT_APP_VERSION = \"${Config.versionName}\"\n")
        }
    }
}
kotlin.sourceSets.named("commonMain") { kotlin.srcDir(generateAboutVersion.map { generatedVersion.get() }) }
