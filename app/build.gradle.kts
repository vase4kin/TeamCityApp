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

import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import teamcityapp.buildlogic.Config
import teamcityapp.buildlogic.verifyR8SmokeTestReports

plugins {
    id("teamcityapp.android.application")
    id("teamcityapp.android.compose")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("teamcityapp.android.hilt")
    id("teamcityapp.android.data-binding")
    id("teamcityapp.android.coverage")
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.performance)
    alias(libs.plugins.google.oss.licenses)
    alias(libs.plugins.google.services)
}

val r8VerificationEnabled = providers.gradleProperty("r8Verification").isPresent

android {
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
        sourceSets.getByName("androidTest").setRoot("src/r8VerificationTest")
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

    variantFilter {
        if (buildType.name == "release" && flavors.first().name == "mock") {
            ignore = true
        }
    }

    packaging {
        jniLibs {
            // Mockito Android needs native libraries extracted in the test APK.
            useLegacyPackaging = true
        }
        resources {
            excludes += listOf(
                "META-INF/*.version", "META-INF/proguard/*", "/*.properties",
                "fabric/*.properties", "META-INF/*.properties", "META-INF/*.kotlin_module"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
    }
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })

    implementation(projects.libraries.utils)
    implementation(projects.libraries.coroutines)
    implementation(projects.libraries.api)
    implementation(projects.libraries.resources)
    implementation(projects.libraries.theme)
    implementation(projects.libraries.chromeTabs)
    implementation(projects.libraries.storage)
    implementation(projects.libraries.storageModels)
    implementation(projects.libraries.cacheManager)
    implementation(projects.libraries.onboarding)
    implementation(projects.libraries.security)
    implementation(projects.libraries.remote)

    implementation(projects.features.testDetails.models)
    implementation(projects.features.testDetails.repository)
    implementation(projects.features.testDetails.feature)

    implementation(projects.features.splash)

    implementation(projects.features.about.api)
    implementation(projects.features.about.impl)
    implementation(projects.libraries.appRating)

    implementation(projects.features.manageAccounts)

    implementation(projects.features.drawer)

    implementation(projects.features.settings)

    implementation(projects.features.changeDetails)

    implementation(projects.features.properties.api)
    implementation(projects.features.properties.impl)

    // Android support libraries
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardView)
    implementation(libs.androidx.legacySupport)
    implementation(libs.androidx.recyclerView)
    implementation(libs.androidx.preference)
    implementation(libs.google.material)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.lifecycle.lifeCycle)
    implementation(libs.androidx.lifecycle.lifeCycleLiveData)
    implementation(libs.androidx.lifecycle.lifeCycleViewModel)
    implementation(libs.androidx.lifecycle.lifeCycleCommonJava8)
    kapt(libs.androidx.lifecycle.lifeCycleCompiler)
    // Api third party libraries
    implementation(libs.okhttp.okhttp)
    implementation(libs.okhttp.loggingInterceptor)
    implementation(libs.retrofit.retrofit)
    implementation(libs.retrofit.gsonConverter)
    implementation(libs.retrofit.retrofitRxjavaAdapter)
    // Dialogs
    // Views
    implementation(libs.errorView)
    implementation(libs.groupie.groupie)
    // View injection
    implementation(libs.butterknife.butterKnife)
    kapt(libs.butterknife.butterKnifeCompiler)
    // Event bus
    implementation(libs.eventBus)
    // Others
    implementation(libs.jodaTime)
    implementation(libs.mugen)
    implementation(libs.shimmerlayout)
    // Onboarding
    implementation(libs.materialTapTargetPrompt)
    // Dagger
    implementation(libs.dagger.dagger)
    kapt(libs.dagger.compiler)

    // Rx
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.rx2)
    implementation(libs.rxjava.rxJava)
    implementation(libs.rxjava.rxAndroid)
    implementation(libs.rxjava.rxjava.kotlin)
    // Rx cache
    implementation(libs.rxcache.rxCache)
    implementation(libs.rxcache.jolyglot)
    // Firebase
    implementation(platform(libs.google.firebaseBom))
    implementation(libs.google.analytics)
    implementation(libs.google.firebaseConfig)
    implementation(libs.google.firebasePerf)
    implementation(libs.google.openSourceLicensesLibrary)
    // Bottom nav libries
    implementation(libs.fragNav)
    // Unit tests
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
        androidTestImplementation(libs.androidx.test.espresso.contrib) {
            // Its accessibility dependency brings protobuf-lite 3.0.1, which shadows Firebase's runtime.
            exclude(group = "com.google.protobuf", module = "protobuf-lite")
        }
        androidTestImplementation(libs.androidx.test.espresso.web)
        // Mockito
        androidTestImplementation(libs.mockito.mockitoAndroid)
        androidTestImplementation(libs.mockito.mockitoKotlin)
        // Dagger mock
        androidTestImplementation(platform(libs.compose.bom))
        androidTestImplementation(libs.compose.ui.testJunit4)
        androidTestImplementation(libs.hilt.testing)
        kaptAndroidTest(libs.hilt.compiler)
        // Resolve conflits between apks
        androidTestImplementation(libs.google.material)
        androidTestImplementation(libs.androidx.recyclerView)
    }
    // Crashlytics
    implementation(libs.google.crashlytics)

    implementation(libs.kotlin.stdlib)
}

// setRoot redirects Android's Java folders; external Kotlin has its own source set.
if (r8VerificationEnabled) {
    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
        sourceSets.getByName("androidTest").kotlin.setSrcDirs(
            listOf("src/r8VerificationTest/java", "src/r8VerificationTest/kotlin")
        )
    }
}

// Kapt processes both Kotlin and Java sources. Avoid loading the processors again.
tasks.withType<JavaCompile>().configureEach {
    if (name.endsWith("JavaWithJavac")) {
        doFirst {
            options.annotationProcessorPath = files()
        }
    } else if (name.startsWith("hiltJavaCompile")) {
        doFirst {
            // Hilt does not process view bindings; ButterKnife's scanner uses JDK internals.
            options.annotationProcessorPath = files(options.annotationProcessorPath?.filter {
                !it.name.contains("butterknife-compiler")
            })
        }
    }
}

if (r8VerificationEnabled) {
    // Reject instrumentation startup failures that appear as successful zero-test runs.
    tasks.matching { it.name == "connectedProdR8VerificationAndroidTest" }.configureEach {
        doLast {
            val reports = fileTree(layout.buildDirectory.dir(
                "outputs/androidTest-results/connected/r8verification/flavors/prod"
            )) {
                include("**/TEST-*.xml")
            }
            verifyR8SmokeTestReports(reports.files)
        }
    }
}
