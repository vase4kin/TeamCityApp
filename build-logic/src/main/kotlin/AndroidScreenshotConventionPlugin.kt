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

package teamcityapp.buildlogic

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidHostTestCompilation
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Opt-in local Compose golden tests for the module that owns the UI. */
@OptIn(ExperimentalRoborazziApi::class)
class AndroidScreenshotConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("io.github.takahirom.roborazzi")
        val multiplatform = pluginManager.hasPlugin("com.android.kotlin.multiplatform.library")
        if (multiplatform) {
            val android = extensions.getByType(KotlinMultiplatformExtension::class.java).targets.getByName("android") as KotlinMultiplatformAndroidLibraryTarget
            (android.compilations.getByName("hostTest") as KotlinMultiplatformAndroidHostTestCompilation).isIncludeAndroidResources = true
        } else {
            extensions.configure(CommonExtension::class.java) {
                testOptions.unitTests.isIncludeAndroidResources = true
            }
        }
        extensions.configure(RoborazziExtension::class.java) {
            outputDir.set(layout.projectDirectory.dir("src/test/screenshots"))
            separateOutputDirs.set(true)
            compare.outputDir.set(layout.buildDirectory.dir("outputs/roborazzi"))
        }
        val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
        val testConfiguration = if (multiplatform) "androidHostTestImplementation" else "testImplementation"
        dependencies.add(testConfiguration, dependencies.platform(libs.findLibrary("compose-bom").get()))
        listOf("junit", "robolectric", "roborazzi-core", "compose-ui-testJunit4").forEach {
            dependencies.add(testConfiguration, libs.findLibrary(it).get())
        }
        dependencies.add(if (multiplatform) testConfiguration else "debugImplementation", libs.findLibrary("compose-ui-testManifest").get())
        tasks.withType(Test::class.java).configureEach {
            // Stable resource loading without relying on Robolectric's repo1 default.
            systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
            jvmArgs(
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.util=ALL-UNNAMED",
                "--add-opens=java.base/java.io=ALL-UNNAMED",
                "--add-opens=java.base/java.net=ALL-UNNAMED",
                "--add-opens=java.base/java.security=ALL-UNNAMED",
                "--add-opens=java.base/java.text=ALL-UNNAMED",
                "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
                "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED",
                "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED"
            )
        }
    }
}
