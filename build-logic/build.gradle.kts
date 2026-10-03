plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.tools.gradleAndroid)
    implementation(libs.kotlin.tools.gradlePlugin)
    implementation(libs.hilt.gradlePlugin)
    testImplementation(gradleTestKit())
    testImplementation(libs.junit)
}

gradlePlugin {
    plugins {
        register("androidBase") {
            id = "teamcityapp.android.base"
            implementationClass = "teamcityapp.buildlogic.AndroidBaseConventionPlugin"
        }
        register("androidApplication") {
            id = "teamcityapp.android.application"
            implementationClass = "teamcityapp.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "teamcityapp.android.library"
            implementationClass = "teamcityapp.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidJavaLibrary") {
            id = "teamcityapp.android.library.java"
            implementationClass = "teamcityapp.buildlogic.AndroidJavaLibraryConventionPlugin"
        }
        register("androidCoverage") {
            id = "teamcityapp.android.coverage"
            implementationClass = "teamcityapp.buildlogic.AndroidCoverageConventionPlugin"
        }
        register("androidHilt") {
            id = "teamcityapp.android.hilt"
            implementationClass = "teamcityapp.buildlogic.AndroidHiltConventionPlugin"
        }
        register("androidDataBinding") {
            id = "teamcityapp.android.data-binding"
            implementationClass = "teamcityapp.buildlogic.AndroidDataBindingConventionPlugin"
        }
    }
}

tasks.test {
    inputs.file("../gradle/libs.versions.toml")
    systemProperty("teamcityapp.versionCatalog", file("../gradle/libs.versions.toml").absolutePath)
}
