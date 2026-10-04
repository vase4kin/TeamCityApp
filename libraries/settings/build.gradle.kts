plugins {
    id("teamcityapp.android.library")
    id("teamcityapp.android.hilt")
}

android { namespace = "teamcityapp.libraries.settings" }

dependencies {
    implementation(libs.datastore.preferences)
    api(libs.coroutines.core)
    implementation(libs.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}
