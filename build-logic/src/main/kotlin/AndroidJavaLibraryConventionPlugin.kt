package teamcityapp.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/** Android libraries containing Java sources or resources, without Kotlin processing. */
class AndroidJavaLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("teamcityapp.android.base")
        pluginManager.apply("com.android.library")
    }
}
