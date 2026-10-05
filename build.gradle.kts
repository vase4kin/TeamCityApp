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
    alias(libs.plugins.spotless)
    id("teamcityapp.android.application") apply false
    id("teamcityapp.android.library") apply false
    id("teamcityapp.android.library.java") apply false
    id("teamcityapp.android.hilt") apply false
    id("teamcityapp.android.data-binding") apply false
    id("teamcityapp.android.coverage") apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.performance) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.google.oss.licenses) apply false
}

tasks.named<Delete>("clean") {
    delete(layout.buildDirectory)
}

// A single repository-wide target also covers the separate build-logic build.
// Explicit source globs include Kotlin still living under legacy java roots.
val formattingExclusions = listOf(
    "**/build/**", "**/.gradle/**", "**/.kotlin/**", "**/.git/**", "**/.idea/**",
    "**/bin/**", "**/gen/**", "**/out/**", "**/__pycache__/**", "**/node_modules/**"
)

// Locally, format only uncommitted files. CI supplies the PR's base commit.
val formattingBase = providers.gradleProperty("spotlessBase").orElse("HEAD")

spotless {
    ratchetFrom(formattingBase.get())
    encoding("UTF-8")
    lineEndings = com.diffplug.spotless.LineEnding.UNIX
    kotlin {
        target(
            fileTree(rootDir) {
                include("**/src/**/*.kt")
                exclude(formattingExclusions)
            }
        )
        ktlint(libs.versions.ktlint.get())
    }
    kotlinGradle {
        target(
            fileTree(rootDir) {
                include("**/*.gradle.kts")
                exclude(formattingExclusions)
            }
        )
        ktlint(libs.versions.ktlint.get())
    }
    java {
        target(
            fileTree(rootDir) {
                include("**/src/**/*.java")
                exclude(formattingExclusions)
            }
        )
        // 1.24.0 runs on the project's JDK 17; newer engines require JDK 21.
        googleJavaFormat(libs.versions.google.java.format.get()).aosp()
    }
    format("xml") {
        target(
            fileTree(rootDir) {
                include("**/src/**/*.xml")
                exclude(formattingExclusions)
            }
        )
        // Preserve resource text and Data Binding expressions.
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("misc") {
        target(
            fileTree(rootDir) {
                include(
                    "**/*.yml", "**/*.yaml", "**/*.toml", "**/*.properties", "**/*.pro",
                    "**/*.py", "**/*.sh", "**/.gitignore", ".editorconfig", ".gitattributes"
                )
                exclude(formattingExclusions + "**/local.properties")
            }
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
}
