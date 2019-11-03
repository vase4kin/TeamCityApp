/*
 * Copyright 2019 Andrey Tolpeev
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

import org.gradle.api.JavaVersion

object Config {
    val minSdk = 21
    val compileSdk = 29
    val targetSdk = 29
    val javaVersion = JavaVersion.VERSION_1_8
    val buildTools = "29.0.2"

    val versionCode = 72
    val versionName = "1.4.5.0"
    val applicationId = "com.github.vase4kin.teamcityapp"
}

object Versions {
    // AndroidX
    val androidx_appcompat = "1.0.2"
    val androidx_annotation = "1.0.2"
    val androidx_core = "1.0.1"
    val androidx_recyclerview = "1.0.0"
    val androidx_navigation = "2.0.0"
    val androidx_constraintLayout = "1.1.3"
    val material = "1.1.0-alpha04"

    //Retrofit
    val okhttp = "3.9.0"
    val gson = "2.8.2"
    val retrofit = "2.4.0"

    //Rxjava
    val rxjava = "2.2.6"

    // Testing
    val junit = "4.12"
    val androidx_espresso = "3.1.0"
    val androidx_testing = "1.1.1"
    val mockitoCore = "1.10.19"
    val powerMock = "1.6.4"

    // Gradle plugins
    val gradleandroid = "3.4.2"
    val kotlin = "1.3.41"
    val gradleversions = "0.21.0"
    val androidAppt = "1.8"
    val googleServices = "4.3.0"
    val jacoco = "0.8.1"
    val firebasePerf = "1.3.1"
    val kotlinter = "2.1.1"
    val fabric = "1.+"

    // Kotlin
    val kotlin_stdlibJdk7 = "1.3.41"

    val dexMaker = "1.4"
}

object Deps {
    val androidx_appcompat = "androidx.appcompat:appcompat:${Versions.androidx_appcompat}"
    val androidx_annotation = "androidx.annotation:annotation:${Versions.androidx_annotation}"
    val androidx_core = "androidx.core:core-ktx:${Versions.androidx_core}"
    val androidx_constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.androidx_constraintLayout}"
    val androidx_material = "com.google.android.material:material:${Versions.material}"
    val androidx_navigation_fragment = "androidx.navigation:navigation-fragment-ktx:${Versions.androidx_navigation}"
    val androidx_navigation_ui = "androidx.navigation:navigation-ui-ktx:${Versions.androidx_navigation}"
    val androidx_recyclerview = "androidx.recyclerview:recyclerview:${Versions.androidx_recyclerview}"

    val api_okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    val api_okhttpLogginInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    val api_gson = "com.google.code.gson:gson:${Versions.gson}"
    val api_retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    val api_retrofitConverterGson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    val api_retrofitAdapterRxJava2 = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"

    val rxjava = "io.reactivex.rxjava2:rxjava:${Versions.rxjava}"

    val test_junit = "junit:junit:${Versions.junit}"
    val test_mockito = "org.mockito:mockito-core:${Versions.mockitoCore}"

    val test_powerMockJunit4 = "org.powermock:powermock-module-junit4:${Versions.powerMock}"
    val test_powerMockJunitRule = "org.powermock:powermock-module-junit4-rule:${Versions.powerMock}"
    val test_powerMockApiMockito = "org.powermock:powermock-api-mockito:${Versions.powerMock}"
    val test_powerMockClassLoadingXstream = "org.powermock:powermock-classloading-xstream:${Versions.powerMock}"

    val test_androidx_rules = "androidx.test:rules:${Versions.androidx_testing}"
    val test_androidx_runner = "androidx.test:runner:${Versions.androidx_testing}"
    val test_androidx_espressocore = "androidx.test.espresso:espresso-core:${Versions.androidx_espresso}"

    val dexmaker = "com.crittercism.dexmaker:dexmaker:${Versions.dexMaker}"
    val dexmakerDx = "com.crittercism.dexmaker:dexmaker-dx:${Versions.dexMaker}"
    val dexmakerMockito = "com.crittercism.dexmaker:dexmaker-mockito:${Versions.dexMaker}"

    val kotlin_stdlibJdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin_stdlibJdk7}"

    val tools_gradleandroid = "com.android.tools.build:gradle:${Versions.gradleandroid}"
    val tools_kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val tools_gradleversions = "com.github.ben-manes:gradle-versions-plugin:${Versions.gradleversions}"
    val tools_androidAapt = "com.neenbedankt.gradle.plugins:android-apt:${Versions.androidAppt}"
    val tools_googleServices = "com.google.gms:google-services:${Versions.googleServices}"
    val tools_jacoco = "org.jacoco:org.jacoco.core:${Versions.jacoco}"
    val tools_firebasePerf = "com.google.firebase:perf-plugin:${Versions.firebasePerf}"
    val tools_kotlinter = "org.jmailen.gradle:kotlinter-gradle:${Versions.kotlinter}"
    val tools_fabric = "io.fabric.tools:gradle:${Versions.fabric}"
}