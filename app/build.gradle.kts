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

import com.android.build.api.dsl.ApplicationExtension
import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import teamcityapp.buildlogic.Config
import teamcityapp.buildlogic.verifyR8SmokeTestReports

plugins {
    id("teamcityapp.android.application")
    id("teamcityapp.android.app-tests")
    id("teamcityapp.android.compose")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("teamcityapp.android.hilt")
    id("teamcityapp.android.coverage")
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.performance)
    alias(libs.plugins.google.oss.licenses)
    alias(libs.plugins.google.services)
}

val r8VerificationEnabled = providers.gradleProperty("r8Verification").isPresent
val minimumAndroidInstrumentation = providers.gradleProperty("minimumAndroidInstrumentation").isPresent

extensions.configure<ApplicationExtension> {
    namespace = "com.github.vase4kin.teamcityapp"

    defaultConfig {
        applicationId = Config.applicationId
        versionCode = Config.versionCode
        versionName = Config.versionName
        vectorDrawables.useSupportLibrary = true
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
        testInstrumentationRunner = if (r8VerificationEnabled) {
            "androidx.test.runner.AndroidJUnitRunner"
        } else {
            "com.github.vase4kin.teamcityapp.helper.HiltTestRunner"
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(providers.gradleProperty("KEYSTORE_FILE").get())
            storePassword = providers.gradleProperty("KEYSTORE_PASSWORD").get()
            keyAlias = providers.gradleProperty("KEY_ALIAS").get()
            keyPassword = providers.gradleProperty("KEY_PASSWORD").get()
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isMinifyEnabled = true
            isShrinkResources = true
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        // Release-equivalent APK for R8 checks without release credentials.
        if (r8VerificationEnabled) {
            create("r8Verification") {
                initWith(getByName("release"))
                signingConfig = signingConfigs.getByName("debug")
                applicationIdSuffix = ".r8"
                matchingFallbacks += "release"
                proguardFile("src/r8Verification/proguard-rules.pro")
                configure<CrashlyticsExtension> {
                    mappingFileUploadEnabled = false
                }
            }
        }
    }

    if (r8VerificationEnabled) {
        testBuildType = "r8Verification"
        sourceSets.getByName("androidTest").apply {
            setRoot("src/r8VerificationTest")
            kotlin.directories.clear()
            kotlin.directories.add("src/r8VerificationTest/kotlin")
        }
    }

    flavorDimensions += "default"
    productFlavors {
        create("mock") {
            dimension = "default"
            applicationIdSuffix = ".mock"
        }
        create("prod") {
            dimension = "default"
        }
    }

    packaging {
        jniLibs {
            // Mockito Android needs native libraries extracted in the test APK.
            useLegacyPackaging = true
            // Preserve Mockito's test-only JVMTI agent rather than attempting to strip it.
            keepDebugSymbols += "**/libdexmakerjvmtiagent.so"
        }
        resources {
            excludes += listOf(
                "META-INF/*.version",
                "META-INF/proguard/*",
                "/*.properties",
                "fabric/*.properties",
                "META-INF/*.properties",
                "META-INF/*.kotlin_module"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
        unitTests.isIncludeAndroidResources = true
    }
}

androidComponents {
    beforeVariants(selector().withBuildType("release").withFlavor("default" to "mock")) {
        it.enable = false
    }
}

dependencies {
    if (r8VerificationEnabled) {
        "r8VerificationImplementation"(libs.ktor.client.mock)
    }
    implementation(libs.gson)
    "mockImplementation"(libs.ktor.client.mock)
    "mockImplementation"(libs.rxjava.rxJava)
    "mockImplementation"(libs.coroutines.rx2)
    "mockImplementation"(libs.okhttp.okhttp)
    implementation(projects.sharedApp)
    implementation(projects.libraries.sharedData)
    implementation(projects.features.home.api)
    implementation(projects.features.home.impl)
    implementation(projects.features.buildDetails.api)
    implementation(projects.features.buildDetails.impl)
    implementation(projects.libraries.navigation)
    implementation(libs.androidx.core.splashscreen)

    implementation(projects.libraries.coroutines)
    implementation(projects.libraries.api)
    implementation(projects.libraries.data)
    implementation(projects.libraries.session)
    implementation(libs.androidx.datastore.preferences)
    implementation(projects.libraries.resources)
    implementation(projects.libraries.theme)
    implementation(projects.libraries.chromeTabs)
    implementation(projects.libraries.storage)
    implementation(projects.libraries.storageModels)
    implementation(projects.libraries.onboarding)
    implementation(projects.libraries.security)
    implementation(projects.libraries.remote)

    implementation(projects.features.testDetails.api)
    implementation(projects.features.testDetails.impl)

    implementation(projects.features.splash.api)
    implementation(projects.features.splash.impl)

    implementation(projects.features.about.api)
    implementation(projects.features.about.impl)
    implementation(projects.libraries.appRating)

    implementation(projects.features.manageAccounts.api)
    implementation(projects.features.manageAccounts.impl)

    implementation(projects.features.drawer.api)
    implementation(projects.features.drawer.impl)

    implementation(projects.features.settings.api)
    implementation(projects.features.settings.impl)
    implementation(projects.libraries.appTheme)

    implementation(projects.features.changeDetails.api)
    implementation(projects.features.changeDetails.impl)

    implementation(projects.features.properties.api)
    implementation(projects.features.properties.impl)
    implementation(projects.features.agents.api)
    implementation(projects.features.agents.impl)
    implementation(projects.features.changes.api)
    implementation(projects.features.changes.impl)
    implementation(projects.features.tests.api)
    implementation(projects.features.tests.impl)
    implementation(projects.libraries.buildConfigurations)
    implementation(projects.features.navigation.api)
    implementation(projects.features.navigation.impl)
    implementation(projects.features.favorites.api)
    implementation(projects.features.favorites.impl)
    implementation(projects.libraries.builds)
    implementation(projects.features.buildHistory.api)
    implementation(projects.features.buildHistory.impl)
    implementation(projects.features.runningBuilds.api)
    implementation(projects.features.runningBuilds.impl)
    implementation(projects.features.buildQueue.api)
    implementation(projects.features.buildQueue.impl)
    implementation(projects.features.snapshotDependencies.api)
    implementation(projects.features.snapshotDependencies.impl)
    implementation(projects.features.buildArtifacts.api)
    implementation(projects.features.buildArtifacts.impl)
    implementation(projects.features.buildOverview.api)
    implementation(projects.features.buildOverview.impl)
    implementation(projects.features.bottomSheet.api)
    implementation(projects.features.bottomSheet.impl)

    implementation(projects.features.filterBottomSheet.api)
    implementation(projects.features.filterBottomSheet.impl)

    implementation(projects.features.filterBuilds.api)
    implementation(projects.features.filterBuilds.impl)

    implementation(projects.features.runBuild.api)
    implementation(projects.features.runBuild.impl)

    implementation(projects.libraries.authentication)
    implementation(projects.features.login.api)
    implementation(projects.features.login.impl)
    implementation(projects.features.createAccount.api)
    implementation(projects.features.createAccount.impl)

    // Android support libraries
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)

    // Event bus
    implementation(libs.eventBus)
    // Others
    implementation(libs.jodaTime)
    // Dagger
    implementation(libs.dagger.dagger)
    ksp(libs.dagger.compiler)

    implementation(libs.androidx.browser)
    implementation(libs.androidx.activity.compose)
    implementation(libs.coroutines.android)

    // Firebase
    implementation(platform(libs.google.firebaseBom))
    implementation(libs.google.analytics)
    implementation(libs.google.firebaseConfig)
    implementation(libs.google.firebasePerf)
    implementation(libs.google.openSourceLicensesLibrary)
    // Unit tests
    testImplementation(libs.robolectric)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.mockitoCore)
    testImplementation(libs.mockito.mockitoKotlin)
    testImplementation(libs.hamcrestJunit)
    debugImplementation(libs.compose.ui.testManifest)
    if (r8VerificationEnabled) {
        androidTestImplementation(libs.androidx.test.core)
        androidTestImplementation(libs.androidx.test.runner)
        androidTestImplementation(libs.androidx.test.rules)
        androidTestImplementation(libs.androidx.test.extJunit)
        androidTestUtil(libs.androidx.test.orchestrator)
    } else {
        // Ui tests
        // Core library
        androidTestImplementation(libs.androidx.test.core)
        // AndroidJUnitRunner and JUnit Rules
        androidTestImplementation(libs.androidx.test.runner)
        androidTestImplementation(libs.androidx.test.rules)
        androidTestUtil(libs.androidx.test.orchestrator)
        // Assertions
        androidTestImplementation(libs.androidx.test.extJunit)
        // Espresso dependenciesd
        androidTestImplementation(libs.androidx.test.espresso.core)
        androidTestImplementation(libs.androidx.test.espresso.intents)
        // Accessibility test dependencies use Hamcrest 2.2 at runtime.
        // Use its shim on both classpaths so Kotlin cannot call removed 1.3 overloads.
        androidTestImplementation(libs.hamcrestLibrary)
        // Mockito
        if (minimumAndroidInstrumentation) {
            // The modern inline engine requires API 28. Keep minimum-API runs
            // on the last subclass engine without changing normal CI/JVM tests.
            androidTestImplementation(libs.mockito.mockitoAndroidMinimum)
            androidTestImplementation(libs.mockito.mockitoKotlin) {
                exclude(group = "org.mockito", module = "mockito-core")
            }
        } else {
            androidTestImplementation(libs.mockito.mockitoAndroid)
            androidTestImplementation(libs.mockito.mockitoKotlin)
        }
        // Dagger mock
        androidTestImplementation(platform(libs.compose.bom))
        androidTestImplementation(libs.compose.ui.testJunit4)
        androidTestImplementation(libs.compose.ui.testAccessibility)
        androidTestImplementation(libs.hilt.testing)
        kspAndroidTest(libs.hilt.compiler)
        // Resolve conflits between apks
        androidTestImplementation(libs.google.material)
    }
    // Crashlytics
    implementation(libs.google.crashlytics)

    implementation(libs.kotlin.stdlib)
}

if (r8VerificationEnabled) {
    // Reject instrumentation startup failures that appear as successful zero-test runs.
    tasks.matching { it.name == "connectedProdR8VerificationAndroidTest" }.configureEach {
        doLast {
            val reports = fileTree(
                layout.buildDirectory.dir(
                    "outputs/androidTest-results/connected/r8verification/flavors/prod"
                )
            ) {
                include("**/TEST-*.xml")
            }
            verifyR8SmokeTestReports(reports.files)
        }
    }
}

val verifyProductionNetworking = tasks.register("verifyProductionNetworking") {
    group = "verification"
    description = "Rejects retired networking libraries in production Android runtimes."
    doLast {
        val retiredGroups = setOf(
            "com.squareup.retrofit2",
            "io.reactivex.rxjava2",
            "com.github.VictorAlbertos.RxCache",
            "com.github.VictorAlbertos.Jolyglot"
        )
        for (variant in listOf("prodDebug", "prodRelease")) {
            val retired = configurations.getByName("${variant}RuntimeClasspath")
                .incoming.resolutionResult.allComponents
                .mapNotNull { it.moduleVersion }
                .filter { it.group in retiredGroups }
            check(retired.isEmpty()) { "$variant includes retired networking dependencies: $retired" }
        }
    }
}

tasks.matching { it.name in setOf("check", "assembleProdDebug", "assembleProdRelease", "assembleProdR8Verification") }
    .configureEach { dependsOn(verifyProductionNetworking) }
