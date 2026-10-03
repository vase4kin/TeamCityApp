package teamcityapp.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

/** Preserves the app's aggregate debug coverage report and CI output locations. */
class AndroidCoverageConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("jacoco")
            val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
            extensions.configure(JacocoPluginExtension::class.java) {
                toolVersion = libs.findVersion("jacoco").get().requiredVersion
            }
            tasks.withType(Test::class.java).configureEach {
                extensions.configure(JacocoTaskExtension::class.java) {
                    isIncludeNoLocationClasses = true
                    excludes = listOf("jdk.internal.*")
                }
            }
            tasks.register("generateCodeCoverageReport", JacocoReport::class.java) {
                group = "code quality"
                description = "Generate Jacoco coverage reports"
                reports {
                    xml.required.set(true)
                    xml.outputLocation.set(layout.buildDirectory.file(
                        "coverage/generateCodeCoverageReport/generateCodeCoverageReport.xml"
                    ))
                    html.required.set(true)
                    html.outputLocation.set(rootProject.layout.buildDirectory.dir("coverage-report"))
                }
                // CI can restore coverage files without recompiling. When producers are
                // requested in the same build, read their outputs only after they finish.
                rootProject.subprojects.forEach { module ->
                    mustRunAfter(module.tasks.matching {
                        it is Test || it.name.matches(Regex("compile(Debug|MockDebug)(JavaWithJavac|Kotlin)"))
                    })
                }
                val filters = listOf(
                    "**/R.class", "**/R\$*.class", "**/BR.class", "**/BR\$*.class",
                    "**/BuildConfig.*", "**/Manifest*.*", "android/**/*.*", "**/dagger/**"
                )
                sourceDirectories.setFrom(rootProject.subprojects.flatMap { module ->
                    listOf(module.file("src/main/java"), module.file("src/main/kotlin"))
                })
                classDirectories.setFrom(rootProject.subprojects.flatMap { module ->
                    listOf("debug", "mockDebug").flatMap { variant ->
                        listOf("intermediates/javac/$variant", "tmp/kotlin-classes/$variant").map { path ->
                            module.fileTree(module.layout.buildDirectory.dir(path)) {
                                exclude(filters)
                            }
                        }
                    }
                })
                executionData.setFrom(rootProject.subprojects.map { module ->
                    module.fileTree(module.layout.buildDirectory) {
                        include("jacoco/*.exec", "coverage.ec")
                    }
                })
                doLast {
                    logger.lifecycle("file://${reports.html.outputLocation.get().asFile}/index.html")
                }
            }
        }
    }
}
