package teamcityapp.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidDataBindingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        listOf("com.android.application", "com.android.library").forEach { pluginId ->
            pluginManager.withPlugin(pluginId) {
                extensions.configure(CommonExtension::class.java) {
                    dataBinding.enable = true
                }
            }
        }
    }
}
