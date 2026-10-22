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

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import teamcityapp.buildlogic.Config

plugins { id("teamcityapp.kmp.compose") }

compose.resources { packageOfResClass = "teamcityapp.shared.resources" }

kotlin {
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "TeamCityShared"
            isStatic = true
        }
    }
    sourceSets.named("commonMain") {
        dependencies {
            implementation(projects.features.about.presentation)
            implementation(libs.coroutines.core)
        }
    }
}

val generatedMetadata = layout.buildDirectory.dir("generated/appMetadata/commonMain/kotlin")
val generateAppMetadata = tasks.register("generateAppMetadata") {
    inputs.property("version", Config.versionName)
    outputs.dir(generatedMetadata)
    doLast {
        val output = generatedMetadata.get().file("teamcityapp/shared/AppMetadata.kt").asFile
        output.parentFile.mkdirs()
        output.writeText(
            """
            // Copyright 2026 Andrey Tolpeev. Licensed under the Apache License, Version 2.0.
            package teamcityapp.shared
            internal const val APP_VERSION = "${Config.versionName}"
            """.trimIndent() + "\n"
        )
    }
}
kotlin.sourceSets.named("commonMain") {
    kotlin.srcDir(generateAppMetadata.map { generatedMetadata.get() })
}
