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

package teamcityapp.buildsrc

import org.gradle.api.JavaVersion

object Config {
    const val minSdk = 21
    const val compileSdk = 29
    const val targetSdk = 29
    const val buildTools = "29.0.2"
    const val versionCode = 72
    const val versionName = "1.4.5.0"
    const val applicationId = "com.github.vase4kin.teamcityapp"
    val javaVersion = JavaVersion.VERSION_1_8
}

object Libs {

    const val errorView = "tr.xip.errorview:library:3.0.0"
    const val aboutLibrary = "com.github.daniel-stoneuk:material-about-library:2.2.5"
    const val dialogs = "com.afollestad.material-dialogs:core:0.9.6.0"

    const val junit = "junit:junit:4.12"

    object AndroidX {
        const val appcompat = "androidx.appcompat:appcompat:1.0.2"
        const val annotationn = "androidx.annotation:annotation:1.0.2"
        const val browser = "androidx.browser:browser:1.0.0"
        const val constraintLayout = "com.android.support.constraint:constraint-layout:1.1.3"

        object LifeCycle {
            private const val version = "2.2.0-rc02"
            const val lifeCycle = "androidx.lifecycle:lifecycle-runtime:$version"
            const val lifeCycleCompiler = "androidx.lifecycle:lifecycle-compiler:$version"
        }
    }

    object Google {
        const val material = "com.google.android.material:material:1.0.0"

        const val firebaseCore = "com.google.firebase:firebase-core:17.2.1"
        const val firebaseConfig = "com.google.firebase:firebase-config:19.0.3"
        const val firebasePerf = "com.google.firebase:firebase-perf:19.0.1"

        const val openSourceLicensesLibrary =
            "com.google.android.gms:play-services-oss-licenses:17.0.0"

        object Tools {
            const val openSourceLicensesPlugin = "com.google.android.gms:oss-licenses-plugin:0.9.5"
        }
    }

    object OkHttp {
        private const val version = "3.9.0"
        const val okhttp = "com.squareup.okhttp3:okhttp:$version"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$version"
    }

    object Retrofit {
        private const val version = "2.4.0"
        const val retrofit = "com.squareup.retrofit2:retrofit:$version"
        const val retrofitRxjavaAdapter = "com.squareup.retrofit2:adapter-rxjava2:$version"
        const val gsonConverter = "com.squareup.retrofit2:converter-gson:$version"
    }

    object RxJava {
        const val rxJava = "io.reactivex.rxjava2:rxjava:2.2.14"
        const val rxAndroid = "io.reactivex.rxjava2:rxandroid:2.1.1"
        const val rxjava_kotlin = "io.reactivex.rxjava2:rxkotlin:2.4.0"
    }

    object Dagger {
        private const val version = "2.25.2"
        const val dagger = "com.google.dagger:dagger:$version"
        const val androidSupport = "com.google.dagger:dagger-android-support:$version"
        const val compiler = "com.google.dagger:dagger-compiler:$version"
        const val androidProcessor = "com.google.dagger:dagger-android-processor:$version"
    }

    object ButterKnife {
        private const val version = "10.1.0"
        const val butterKnife = "com.jakewharton:butterknife:$version"
        const val butterKnifeCompiler = "com.jakewharton:butterknife-compiler:$version"
    }

    object DexMaker {
        private const val version = "1.4"
        const val dexmaker = "com.crittercism.dexmaker:dexmaker:$version"
        const val dexmakerDx = "com.crittercism.dexmaker:dexmaker-dx:$version"
        const val dexmakerMockito = "com.crittercism.dexmaker:dexmaker-mockito:$version"
    }

    object Kotlin {
        private const val version = "1.3.50"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$version"

        object Tools {
            const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
            const val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"
        }
    }

    object Tools {
        const val gradleAndroid = "com.android.tools.build:gradle:3.5.1"
        const val gradleversions = "com.github.ben-manes:gradle-versions-plugin:0.21.0"
        const val androidAapt = "com.neenbedankt.gradle.plugins:android-apt:1.8"
        const val googleServices = "com.google.gms:google-services:4.3.0"
        const val jacoco = "org.jacoco:org.jacoco.core:0.8.1"
        const val firebasePerf = "com.google.firebase:perf-plugin:1.3.1"
        const val kotlinter = "org.jmailen.gradle:kotlinter-gradle:2.1.1"
        const val fabric = "io.fabric.tools:gradle:1.+"
    }

    object Mockito {
        const val mockito = "org.mockito:mockito-core:1.10.19"
        const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0"
        const val mockito3 = "org.mockito:mockito-core:3.1.0"
    }

    object PowerMock {
        private const val version = "1.6.4"
        const val junit4 = "org.powermock:powermock-module-junit4:$version"
        const val junitRule = "org.powermock:powermock-module-junit4-rule:$version"
        const val apiMockito = "org.powermock:powermock-api-mockito:$version"
        const val classLoadingXstream = "org.powermock:powermock-classloading-xstream:$version"
    }

    object Groupie {
        private const val version = "2.7.0"
        const val groupie = "com.xwray:groupie:$version"
        const val groupieDatabinding = "com.xwray:groupie-databinding:$version"
    }
}