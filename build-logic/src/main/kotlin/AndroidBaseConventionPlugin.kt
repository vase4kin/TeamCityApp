package teamcityapp.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidBaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        configureUnitTestCoverage()
        listOf("com.android.application", "com.android.library").forEach { pluginId ->
            pluginManager.withPlugin(pluginId) {
                extensions.configure(CommonExtension::class.java) {
                    compileSdk = Config.compileSdk
                    defaultConfig.apply {
                        minSdk = Config.minSdk
                        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                        testInstrumentationRunnerArguments["notAnnotation"] = "org.junit.Ignore"
                    }
                    compileOptions.apply {
                        sourceCompatibility = Config.javaVersion
                        targetCompatibility = Config.javaVersion
                    }
                    lint.apply {
                        disable.add("InvalidPackage")
                        abortOnError = true
                    }
                }
            }
        }
        pluginManager.withPlugin("com.android.library") {
            extensions.configure(CommonExtension::class.java) {
                testOptions.targetSdk = Config.targetSdk
            }
        }
        pluginManager.withPlugin("com.android.application") {
            extensions.configure(ApplicationExtension::class.java) {
                defaultConfig.targetSdk = Config.targetSdk
            }
        }
    }
}
