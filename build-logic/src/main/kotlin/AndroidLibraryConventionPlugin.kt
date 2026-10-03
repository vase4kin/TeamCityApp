package teamcityapp.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // Register shared defaults before applying Android.
        pluginManager.apply("teamcityapp.android.base")
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")
    }
}
