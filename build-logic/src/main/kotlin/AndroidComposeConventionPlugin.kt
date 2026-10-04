package teamcityapp.buildlogic

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        extensions.configure(BaseExtension::class.java) {
            buildFeatures.compose = true
        }
        val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
        dependencies.add("implementation", dependencies.platform(libs.findLibrary("compose-bom").get()))
        dependencies.add("implementation", libs.findLibrary("compose-material3").get())
        dependencies.add("implementation", libs.findLibrary("compose-ui-toolingPreview").get())
        dependencies.add("debugImplementation", libs.findLibrary("compose-ui-tooling").get())
        Unit
    }
}
