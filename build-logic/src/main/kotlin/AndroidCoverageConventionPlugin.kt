package teamcityapp.buildlogic

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

/** Preserves the app's aggregate debug coverage report and CI output locations. */
class AndroidCoverageConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureUnitTestCoverage()
            val jacocoVersion = extensions.getByType(VersionCatalogsExtension::class.java)
                .named("libs").findVersion("jacoco").get().requiredVersion
            pluginManager.withPlugin("com.android.application") {
                extensions.configure(BaseExtension::class.java) {
                    jacoco.version = jacocoVersion
                    buildTypes.getByName("debug").enableAndroidTestCoverage =
                        providers.gradleProperty("instrumentationCoverage").isPresent
                }
            }
            val coverageReport = tasks.register("generateCodeCoverageReport", JacocoReport::class.java) {
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
                        it is Test || it.name.matches(Regex("(compile(Debug|MockDebug)(JavaWithJavac|Kotlin)|transform(Debug|MockDebug)ClassesWithAsm)"))
                    })
                }
                val filters = listOf(
                    "**/R.class", "**/R\$*.class", "**/BR.class", "**/BR\$*.class",
                    "**/BuildConfig.*", "**/Manifest*.*", "android/**/*.*",
                    "androidx/databinding/**", "**/dagger/**"
                )
                sourceDirectories.setFrom(rootProject.subprojects.flatMap { module ->
                    listOf(module.file("src/main/java"), module.file("src/main/kotlin"))
                })
                // Hilt/Firebase transform bytecode before unit tests run. Analyse the
                // same classes so JaCoCo IDs match, falling back for untransformed modules.
                classDirectories.setFrom(provider {
                    rootProject.subprojects.flatMap { module ->
                        listOf("debug", "mockDebug").flatMap { variant ->
                            val capitalizedVariant = variant.replaceFirstChar { it.uppercase() }
                            val transformed = module.layout.buildDirectory.dir(
                                "intermediates/classes/$variant/transform${capitalizedVariant}ClassesWithAsm/dirs"
                            ).get().asFile
                            val paths = if (transformed.isDirectory) {
                                listOf(transformed)
                            } else {
                                listOf(
                                    module.layout.buildDirectory.dir("intermediates/javac/$variant").get().asFile,
                                    module.layout.buildDirectory.dir("tmp/kotlin-classes/$variant").get().asFile
                                )
                            }
                            paths.map { directory ->
                                module.fileTree(directory) { exclude(filters) }
                            }
                        }
                    }
                })
                executionData.setFrom(rootProject.subprojects.map { module ->
                    module.fileTree(module.layout.buildDirectory) {
                        include(
                            "jacoco/*.exec",
                            "outputs/unit_test_code_coverage/debugUnitTest/*.exec",
                            "outputs/unit_test_code_coverage/mockDebugUnitTest/*.exec"
                        )
                    }
                })
                doLast {
                    logger.lifecycle("file://${reports.html.outputLocation.get().asFile}/index.html")
                }
            }
            val jacocoCli = configurations.create("jacocoCli") {
                isCanBeConsumed = false
                isCanBeResolved = true
            }
            dependencies.add(jacocoCli.name, "org.jacoco:org.jacoco.cli:$jacocoVersion:nodeps")
            tasks.register("prepareInstrumentationCoverageInputs", Zip::class.java) {
                group = "code quality"
                description = "Export matching classes, sources and JaCoCo CLI for Marathon coverage"
                archiveFileName.set("instrumentation-coverage-inputs.zip")
                destinationDirectory.set(layout.buildDirectory.dir("coverage/instrumentation-inputs"))
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                // Read outputs after the APK producers without running tests or a report.
                mustRunAfter(tasks.matching { it.name == "assembleMockDebugAndroidTest" })
                from(provider { coverageReport.get().classDirectories }) {
                    into("classes")
                    include("**/*.class")
                }
                from(provider { coverageReport.get().sourceDirectories }) { into("sources") }
                from(jacocoCli) {
                    into("tools")
                    rename { "jacococli.jar" }
                }
            }
        }
    }
}

/** Collect execution data from every Android module, without instrumenting app APKs. */
internal fun Project.configureUnitTestCoverage() {
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
}
