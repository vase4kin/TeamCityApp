package teamcityapp.buildlogic

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidDataBindingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        listOf("com.android.application", "com.android.library").forEach { pluginId ->
            pluginManager.withPlugin(pluginId) {
                extensions.configure(BaseExtension::class.java) {
                    dataBinding.isEnabled = true
                }
            }
        }
    }
}
