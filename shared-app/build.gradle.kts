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

import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import teamcityapp.buildlogic.Config

plugins {
    id("teamcityapp.kmp.compose")
    id("teamcityapp.android.screenshot")
    alias(libs.plugins.kotlin.serialization)
}

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
            implementation(projects.features.buildArtifacts.presentation)
            implementation(projects.features.settings.presentation)
            implementation(projects.features.runBuild.presentation)
            implementation(projects.features.filterBuilds.presentation)
            implementation(projects.features.filterBottomSheet.presentation)
            implementation(projects.features.bottomSheet.presentation)
            implementation(projects.features.buildDetails.presentation)
            implementation(projects.features.home.presentation)
            implementation(projects.features.favorites.presentation)
            implementation(projects.features.runningBuilds.presentation)
            implementation(projects.features.buildQueue.presentation)
            implementation(projects.features.agents.presentation)
            implementation(projects.features.changes.presentation)
            implementation(projects.features.tests.presentation)
            implementation(projects.features.testDetails.presentation)
            implementation(projects.features.changeDetails.presentation)
            implementation(projects.features.properties.presentation)
            implementation(projects.features.snapshotDependencies.presentation)
            implementation(projects.features.about.presentation)
            implementation(projects.features.login.presentation)
            implementation(projects.features.navigation.presentation)
            implementation(projects.features.buildHistory.presentation)
            implementation(projects.features.buildOverview.presentation)
            implementation(projects.libraries.sharedData)
            implementation(projects.libraries.designSystem)
            implementation(libs.multiplatform.compose.backhandler)
            implementation(libs.multiplatform.lifecycle.viewmodel)
            implementation(libs.multiplatform.lifecycle.runtime)
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

kotlin.sourceSets.named("commonTest") { dependencies { implementation(libs.coroutines.test) } }

// Capture the actual device link inputs rather than inferring native dependencies
// from the Android catalog. Release review consumes this reproducible inventory.
tasks.register("exportIosReleaseDependencies") {
    val output = layout.buildDirectory.file("reports/ios-release-dependencies.tsv")
    outputs.file(output)
    outputs.upToDateWhen { false }
    doLast {
        val configuration = configurations.getByName("iosArm64CompileKlibraries")
        val artifacts = configuration.incoming.artifacts.artifacts
        output.get().asFile.apply {
            parentFile.mkdirs()
            writeText(
                artifacts.mapNotNull { artifact ->
                    val id = artifact.id.componentIdentifier as? ModuleComponentIdentifier ?: return@mapNotNull null
                    "${id.group}:${id.module}:${id.version}\t${artifact.file.absolutePath}"
                }.distinct().sorted().joinToString("\n", postfix = "\n")
            )
        }
    }
}
