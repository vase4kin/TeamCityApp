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

import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("teamcityapp.kmp.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin.sourceSets.named("commonMain") {
    dependencies {
        api(libs.findLibrary("multiplatform-compose-runtime").get())
        api(libs.findLibrary("multiplatform-compose-ui").get())
        api(libs.findLibrary("multiplatform-compose-foundation").get())
        api(libs.findLibrary("multiplatform-compose-material3").get().get().toString()) {
            // Android owns its Material dependency below. Native restores the full
            // port dependency graph explicitly in nativeMain.
            exclude(group = "androidx.graphics", module = "graphics-shapes")
        }
        implementation(libs.findLibrary("multiplatform-compose-resources").get())
        implementation(libs.findLibrary("multiplatform-compose-preview").get())
    }
}

// Compile shared Android code against the same Material/Compose APIs as existing
// Android consumers. The iOS Material port has an independent release version.
kotlin.sourceSets.named("androidMain") {
    dependencies {
        api(project.dependencies.platform(libs.findLibrary("compose-bom").get()))
        api(libs.findLibrary("compose-material3").get())
    }
}

// graphics-shapes is itself multiplatform. The common exclusion above must not
// remove its native implementation: native consumers keep the complete port graph.
kotlin.sourceSets.matching { it.name == "nativeMain" }.configureEach {
    dependencies {
        api(libs.findLibrary("multiplatform-compose-material3").get())
    }
}
