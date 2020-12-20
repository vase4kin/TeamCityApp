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

package teamcityapp.buildsrc

import org.gradle.api.JavaVersion

object Config {
    const val minSdk = 21
    const val compileSdk = 30
    const val targetSdk = 30
    const val buildTools = "30.0.2"
    const val versionCode = 111
    const val versionName = "1.52.3"
    const val applicationId = "com.github.vase4kin.teamcityapp"
    val javaVersion = JavaVersion.VERSION_1_8

    object KotlinOptions {
        const val jvmTarget = "1.8"
    }
}

object Libs {

    const val errorView = "tr.xip.errorview:library:3.0.0"
    const val aboutLibrary = "com.github.daniel-stoneuk:material-about-library:3.1.2"
    const val dialogs = "com.afollestad.material-dialogs:core:0.9.6.0"
    const val shimmerlayout = "io.supercharge:shimmerlayout:2.1.0"
    const val eventBus = "org.greenrobot:eventbus:3.0.0"
    const val materialTapTargetPrompt = "uk.co.samuelwall:material-tap-target-prompt:3.0.0"
    const val conceal = "com.facebook.conceal:conceal:1.1.3@aar"
    const val jodaTime = "joda-time:joda-time:2.8.1"
    const val mugen = "com.vinaysshenoy:mugen:1.0.3"
    const val fragNav = "com.ncapdevi:frag-nav:3.3.0"

    const val junit = "junit:junit:4.13"
    const val hamcrestJunit = "org.hamcrest:hamcrest-junit:2.0.0.0"
    const val daggerMock = "com.github.fabioCollini:DaggerMock:0.8.5"
    const val conditionwatcher = "com.azimolabs.conditionwatcher:conditionwatcher:0.2"

    const val crashlytics = "com.crashlytics.sdk.android:crashlytics:2.6.8@aar"

    object AndroidX {
        const val appcompat = "androidx.appcompat:appcompat:1.2.0"
        const val cardView = "androidx.cardview:cardview:1.0.0"
        const val legacySupport = "androidx.legacy:legacy-support-v13:1.0.0"
        const val recyclerView = "androidx.recyclerview:recyclerview:1.0.0"
        const val annotationn = "androidx.annotation:annotation:1.0.2"
        const val browser = "androidx.browser:browser:1.0.0"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.0.0"
        const val preference = "androidx.preference:preference:1.1.1"

        object LifeCycle {
            private const val version = "2.2.0"
            const val lifeCycle = "androidx.lifecycle:lifecycle-runtime:$version"
            const val lifeCycleCompiler = "androidx.lifecycle:lifecycle-compiler:$version"
            const val lifeCycleLiveData = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
            const val lifeCycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
            const val lifeCycleCommonJava8 = "androidx.lifecycle:lifecycle-common-java8:$version"
        }

        object Test {

            private const val version = "1.3.0"
            const val core = "androidx.test:core:$version"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"

            const val extJunit = "androidx.test.ext:junit:1.1.1"

            object Espresso {
                private const val version = "3.3.0"
                const val core = "androidx.test.espresso:espresso-core:$version"
                const val intents = "androidx.test.espresso:espresso-intents:$version"
                const val contrib = "androidx.test.espresso:espresso-contrib:$version"
                const val web = "androidx.test.espresso:espresso-web:$version"
            }
        }
    }

    object Google {
        const val material = "com.google.android.material:material:1.2.1"

        const val firebaseCore = "com.google.firebase:firebase-core:17.5.0"
        const val firebaseConfig = "com.google.firebase:firebase-config:19.2.0"
        const val firebasePerf = "com.google.firebase:firebase-perf:19.0.8"
        const val crashlytics =  "com.google.firebase:firebase-crashlytics:17.2.1"
        const val analytics =  "com.google.firebase:firebase-analytics:17.5.0"

        const val openSourceLicensesLibrary =
            "com.google.android.gms:play-services-oss-licenses:17.0.0"

        const val databindingCompiler = "com.android.databinding:compiler:3.1.4"

        object Tools {
            const val openSourceLicensesPlugin = "com.google.android.gms:oss-licenses-plugin:0.10.2"
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
        const val rxJava = "io.reactivex.rxjava2:rxjava:2.2.19"
        const val rxAndroid = "io.reactivex.rxjava2:rxandroid:2.1.1"
        const val rxjava_kotlin = "io.reactivex.rxjava2:rxkotlin:2.4.0"
    }

    object RxCache {
        const val rxCache = "com.github.VictorAlbertos.RxCache:runtime:1.8.3-2.x"
        const val jolyglot = "com.github.VictorAlbertos.Jolyglot:gson:0.0.4"
    }

    object Dagger {
        private const val version = "2.29.1"
        const val dagger = "com.google.dagger:dagger:$version"
        const val androidSupport = "com.google.dagger:dagger-android-support:$version"
        const val compiler = "com.google.dagger:dagger-compiler:$version"
        const val androidProcessor = "com.google.dagger:dagger-android-processor:$version"
    }

    object ButterKnife {
        private const val version = "10.2.1"
        const val butterKnife = "com.jakewharton:butterknife:$version"
        const val butterKnifeCompiler = "com.jakewharton:butterknife-compiler:$version"
    }

    object DexMaker {
        private const val version = "2.28.0"
        const val dexmaker = "com.linkedin.dexmaker:dexmaker:$version"
        const val dexmakerMockito = "com.linkedin.dexmaker:dexmaker-mockito:$version"
    }

    object Kotlin {
        private const val version = "1.4.10"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$version"

        object Tools {
            const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
            const val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"
        }
    }

    object Tools {
        const val gradleAndroid = "com.android.tools.build:gradle:4.1.1"
        const val gradleversions = "com.github.ben-manes:gradle-versions-plugin:0.33.0"
        const val androidAapt = "com.neenbedankt.gradle.plugins:android-apt:1.8"
        const val googleServices = "com.google.gms:google-services:4.3.0"
        const val jacoco = "org.jacoco:org.jacoco.core:0.8.1"
        const val firebasePerf = "com.google.firebase:perf-plugin:1.3.1"
        const val kotlinter = "org.jmailen.gradle:kotlinter-gradle:3.2.0"
        const val crashlytics = "com.google.firebase:firebase-crashlytics-gradle:2.3.0"
    }

    object Mockito {
        const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0"
        const val mockito3 = "org.mockito:mockito-core:3.1.0"
    }

    object Groupie {
        private const val version = "2.7.0"
        const val groupie = "com.xwray:groupie:$version"
        const val groupieDatabinding = "com.xwray:groupie-databinding:$version"
    }
}
