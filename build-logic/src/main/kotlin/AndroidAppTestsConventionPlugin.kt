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

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Feature-owned integration sources executed by the application test host. */
class AndroidAppTestsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("teamcityapp.android.application")
        // R8 uses its own small runner and source root, without Hilt test suites.
        if (!providers.gradleProperty("r8Verification").isPresent) {
            val owners = rootDir.resolve("features").listFiles().orEmpty().map { it.resolve("impl") } +
                rootDir.resolve("libraries").listFiles().orEmpty().toList()
            val testRoots = owners.map { it.resolve("src/androidAppTest") }.filter { it.isDirectory }.sorted()
            extensions.configure<ApplicationExtension> {
                sourceSets.getByName("androidTest") {
                    kotlin.directories.addAll(testRoots.map { it.resolve("kotlin").path })
                    java.directories.addAll(testRoots.map { it.resolve("java").path })
                }
            }
        }
    }
}
