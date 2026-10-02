package teamcityapp.buildlogic

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidBaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        listOf("com.android.application", "com.android.library").forEach { pluginId ->
            pluginManager.withPlugin(pluginId) {
                extensions.configure(BaseExtension::class.java) {
                    compileSdkVersion(Config.compileSdk)
                    defaultConfig {
                        minSdk = Config.minSdk
                        targetSdk = Config.targetSdk
                        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                        testInstrumentationRunnerArguments["notAnnotation"] = "org.junit.Ignore"
                    }
                    compileOptions {
                        sourceCompatibility = Config.javaVersion
                        targetCompatibility = Config.javaVersion
                    }
                    lintOptions {
                        disable("InvalidPackage")
                        xmlReport = false
                        isAbortOnError = true
                        htmlOutput = layout.buildDirectory.file("reports/lint-report/lint-report.html").get().asFile
                    }
                }
            }
        }
        pluginManager.withPlugin("org.jetbrains.kotlin.android") {
            extensions.configure(KotlinAndroidProjectExtension::class.java) {
                compilerOptions.jvmTarget.set(JvmTarget.fromTarget(Config.KotlinOptions.jvmTarget))
            }
        }
    }
}
