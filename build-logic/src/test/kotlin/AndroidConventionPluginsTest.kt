package teamcityapp.buildlogic

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AndroidConventionPluginsTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `Kotlin library conventions allow module overrides`() {
        verifyConventions("library")
    }

    @Test
    fun `application conventions allow module overrides`() {
        verifyConventions("application")
    }

    @Test
    fun `Java library conventions do not introduce Kotlin processing`() {
        val projectDir = createProject("java-library")
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("teamcityapp.android.library.java")
            }
            android {
                namespace = "teamcityapp.conventiontest"
                defaultConfig {
                    consumerProguardFiles("consumer-rules.pro")
                }
            }
            tasks.register("verifyConventions") {
                doLast {
                    check(android.compileSdkVersion == "android-36")
                    check(android.defaultConfig.minSdkVersion?.apiLevel == 24)
                    check(android.compileOptions.sourceCompatibility == JavaVersion.VERSION_17)
                    check(android.defaultConfig.consumerProguardFiles.single().name == "consumer-rules.pro")
                    check(!plugins.hasPlugin("org.jetbrains.kotlin.android"))
                    check(!plugins.hasPlugin("org.jetbrains.kotlin.kapt"))
                }
            }
            """.trimIndent()
        )
        verify(projectDir)
    }

    @Test
    fun `coverage aggregates module outputs and preserves exclusions and report paths`() {
        val projectDir = createProject("coverage", "app", "library")
        val appDir = File(projectDir, "app").apply { mkdirs() }
        File(projectDir, "library").mkdirs()
        File(projectDir, "library/build.gradle.kts").writeText(
            "tasks.register(\"compileDebugKotlin\")"
        )
        listOf(
            "app/build/intermediates/javac/mockDebug/classes/App.class",
            "library/build/tmp/kotlin-classes/debug/Library.class",
            "library/build/tmp/kotlin-classes/debug/R.class",
            "library/build/tmp/kotlin-classes/debug/dagger/Generated.class",
            "app/build/jacoco/sampleTest.exec",
            "library/build/coverage.ec"
        ).forEach { path ->
            File(projectDir, path).apply {
                parentFile.mkdirs()
                writeText("fixture")
            }
        }
        File(appDir, "build.gradle.kts").writeText(
            """
            import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
            import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
            import org.gradle.testing.jacoco.tasks.JacocoReport

            plugins {
                id("teamcityapp.android.coverage")
            }
            val sampleTest = tasks.register<Test>("sampleTest")
            tasks.register("verifyConventions") {
                doLast {
                    val agent = sampleTest.get().extensions.getByType<JacocoTaskExtension>()
                    check(agent.isIncludeNoLocationClasses)
                    check(agent.excludes == listOf("jdk.internal.*"))
                    check(project.extensions.getByType<JacocoPluginExtension>().toolVersion == "0.8.14")
                    val report = tasks.named<JacocoReport>("generateCodeCoverageReport").get()
                    check(report.classDirectories.files.map { it.name }.toSet() == setOf("App.class", "Library.class"))
                    val orderedTasks = report.mustRunAfter.getDependencies(report).map { it.path }
                    check(orderedTasks.contains(":app:sampleTest"))
                    check(orderedTasks.contains(":library:compileDebugKotlin"))
                    check(!gradle.taskGraph.hasTask(":app:sampleTest"))
                    check(report.executionData.files.map { it.name }.toSet() == setOf("sampleTest.exec", "coverage.ec"))
                    check(report.sourceDirectories.files.contains(rootProject.file("library/src/main/java")))
                    check(report.reports.xml.outputLocation.get().asFile == layout.buildDirectory.file(
                        "coverage/generateCodeCoverageReport/generateCodeCoverageReport.xml"
                    ).get().asFile)
                    check(report.reports.html.outputLocation.get().asFile == rootProject.layout.buildDirectory.dir(
                        "coverage-report"
                    ).get().asFile)
                }
            }
            """.trimIndent()
        )
        verify(projectDir, ":app:verifyConventions")
    }

    private fun verifyConventions(kind: String) {
        val projectDir = createProject(kind)
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("teamcityapp.android.$kind")
                id("teamcityapp.android.hilt")
                id("teamcityapp.android.data-binding")
            }

            // Reapplying optional conventions must not duplicate dependencies.
            pluginManager.apply("teamcityapp.android.hilt")
            pluginManager.apply("teamcityapp.android.data-binding")

            check(android.compileSdkVersion == "android-36")
            check(android.defaultConfig.minSdkVersion?.apiLevel == 24)
            check(android.defaultConfig.targetSdkVersion?.apiLevel == 36)

            android {
                namespace = "teamcityapp.conventiontest"
                compileSdkVersion(35)
                defaultConfig {
                    minSdk = 26
                    testInstrumentationRunner = "teamcityapp.CustomRunner"
                }
            }

            tasks.register("verifyConventions") {
                doLast {
                    check(android.compileSdkVersion == "android-35")
                    check(android.defaultConfig.minSdkVersion?.apiLevel == 26)
                    check(android.defaultConfig.testInstrumentationRunner == "teamcityapp.CustomRunner")
                    check(android.defaultConfig.testInstrumentationRunnerArguments["notAnnotation"] == "org.junit.Ignore")
                    check(android.compileOptions.sourceCompatibility == JavaVersion.VERSION_17)
                    check(android.compileOptions.targetCompatibility == JavaVersion.VERSION_17)
                    check(kotlin.compilerOptions.jvmTarget.get().target == "17")
                    check(!android.lint.xmlReport)
                    check(android.lint.abortOnError)
                    check(android.lint.disable.contains("InvalidPackage"))
                    check(android.dataBinding.isEnabled)
                    check(plugins.hasPlugin("org.jetbrains.kotlin.kapt"))
                    check(plugins.hasPlugin("com.google.dagger.hilt.android"))
                    check(configurations.getByName("implementation").dependencies.count {
                        it.group == "com.google.dagger" && it.name == "hilt-android"
                    } == 1)
                    check(configurations.getByName("kapt").dependencies.count {
                        it.group == "com.google.dagger" && it.name == "hilt-compiler"
                    } == 1)
                }
            }
            """.trimIndent()
        )
        verify(projectDir)
    }

    private fun createProject(name: String, vararg modules: String): File {
        val projectDir = temporaryFolder.newFolder(name)
        File(projectDir, "libs.versions.toml").writeText(
            File(System.getProperty("teamcityapp.versionCatalog")).readText()
        )
        File(projectDir, "settings.gradle.kts").writeText(
            """
            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }
            rootProject.name = "convention-test"
            dependencyResolutionManagement {
                versionCatalogs {
                    create("libs") { from(files("libs.versions.toml")) }
                }
            }
            """.trimIndent() + modules.joinToString("") { "\ninclude(\"$it\")" }
        )
        File(projectDir, "gradle.properties").writeText(
            "android.builtInKotlin=false\nandroid.newDsl=false\n"
        )
        return projectDir
    }

    private fun verify(projectDir: File, task: String = "verifyConventions") {
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(task, "--stacktrace")
            .build()
        assertEquals(TaskOutcome.SUCCESS, result.task(if (task.startsWith(":")) task else ":$task")?.outcome)
    }
}
