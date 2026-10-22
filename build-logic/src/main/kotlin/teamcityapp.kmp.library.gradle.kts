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
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import teamcityapp.buildlogic.Config
import teamcityapp.buildlogic.configureUnitTestCoverage

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
configureUnitTestCoverage()

kotlin {
    jvmToolchain(17)
    android {
        namespace = "teamcityapp" + project.path.replace(':', '.').replace('-', '_')
        compileSdk = Config.compileSdk
        minSdk = Config.minSdk
        androidResources.enable = true
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        withHostTestBuilder {}.configure {}
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions.freeCompilerArgs.add("-Xoverride-konan-properties=osVersionMin.ios=${Config.minIos}")
            }
        }
    }
    sourceSets {
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.findLibrary("kotlin-test-junit").get())
        }
    }
}
