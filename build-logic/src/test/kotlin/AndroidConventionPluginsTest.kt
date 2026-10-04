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
                    check(android.compileSdkVersion in setOf("android-37", "android-37.0")) { android.compileSdkVersion.orEmpty() }
                    check(android.defaultConfig.minSdkVersion?.apiLevel == 37)
                    check(android.compileOptions.sourceCompatibility == JavaVersion.VERSION_17)
                    check(android.defaultConfig.consumerProguardFiles.single().name == "consumer-rules.pro")
                    check(plugins.hasPlugin("jacoco"))
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
            "app/build/intermediates/javac/mockDebug/classes/OriginalApp.class",
            "app/build/intermediates/classes/mockDebug/transformMockDebugClassesWithAsm/dirs/com/github/vase4kin/teamcityapp/App.class",
            "library/build/tmp/kotlin-classes/debug/teamcityapp/sample/Library.class",
            "library/build/tmp/kotlin-classes/debug/R.class",
            "library/build/tmp/kotlin-classes/debug/dagger/Generated.class",
            "app/build/intermediates/classes/mockDebug/transformMockDebugClassesWithAsm/dirs/androidx/databinding/DataBindingComponent.class",
            "app/jacococli.jar",
            "app/src/main/java/example/App.java",
            "app/build/jacoco/sampleTest.exec",
            "library/build/coverage.ec",
            "library/build/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
            "library/build/outputs/unit_test_code_coverage/releaseUnitTest/testReleaseUnitTest.exec"
        ).forEach { path ->
            File(projectDir, path).apply {
                parentFile.mkdirs()
                writeText("fixture")
            }
        }
        java.util.zip.ZipOutputStream(File(appDir, "runtime-library.jar").outputStream()).use { archive ->
            archive.putNextEntry(java.util.zip.ZipEntry("teamcityapp/sample/Library.class"))
            archive.write("runtime bytecode".toByteArray())
            archive.closeEntry()
        }
        File(appDir, "build.gradle.kts").writeText(
            """
            import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
            import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
            import org.gradle.testing.jacoco.tasks.JacocoReport

            plugins {
                id("teamcityapp.android.coverage")
            }
            configurations.named("jacocoCli") {
                check(dependencies.single().version == "0.8.14")
                dependencies.clear()
                dependencies.add(project.dependencies.create(files("jacococli.jar")))
            }
            tasks.named<teamcityapp.buildlogic.InstrumentationCoverageInputs>("prepareInstrumentationCoverageInputs") {
                classJars.add(layout.projectDirectory.file("runtime-library.jar"))
                classDirectories.add(layout.projectDirectory.dir("build/intermediates/classes/mockDebug/transformMockDebugClassesWithAsm/dirs"))
            }
            val sampleTest = tasks.register<Test>("sampleTest")
            tasks.register("transformMockDebugClassesWithAsm")
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
                    check(orderedTasks.contains(":app:transformMockDebugClassesWithAsm"))
                    check(orderedTasks.contains(":library:compileDebugKotlin"))
                    check(!gradle.taskGraph.hasTask(":app:sampleTest"))
                    check(report.executionData.files.map { it.name }.toSet() == setOf("sampleTest.exec", "testDebugUnitTest.exec"))
                    val inputs = tasks.named<org.gradle.api.tasks.bundling.Zip>("prepareInstrumentationCoverageInputs").get()
                    check(!inputs.taskDependencies.getDependencies(inputs).contains(report))
                    check(inputs.source.files.map { it.name }.containsAll(listOf("App.class", "Library.class")))
                    check(inputs.source.files.none { it.extension in listOf("ec", "exec") })
                    check(inputs.archiveFileName.get() == "instrumentation-coverage-inputs.zip")
                    check(!gradle.taskGraph.hasTask(":app:generateCodeCoverageReport"))
                    java.util.zip.ZipFile(inputs.archiveFile.get().asFile).use { archive ->
                        check(archive.getEntry("classes/com/github/vase4kin/teamcityapp/App.class") != null)
                        check(archive.getEntry("classes/OriginalApp.class") == null)
                        check(archive.getEntry("tools/jacococli.jar") != null)
                        check(archive.getEntry("sources/example/App.java") != null)
                        val runtimeClass = archive.getEntry("classes/teamcityapp/sample/Library.class")
                        check(archive.getInputStream(runtimeClass).reader().readText() == "runtime bytecode")
                    }
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
        verify(projectDir, ":app:verifyConventions", ":app:prepareInstrumentationCoverageInputs")
    }

    @Test
    fun `instrumentation coverage is opt-in for debug and preserves release builds`() {
        val projectDir = createProject("instrumentation-coverage")
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("teamcityapp.android.application")
                id("teamcityapp.android.coverage")
            }
            android { namespace = "teamcityapp.conventiontest" }
            tasks.register("verifyConventions") {
                doLast {
                    check(android.buildTypes.getByName("debug").enableAndroidTestCoverage ==
                        providers.gradleProperty("instrumentationCoverage").isPresent)
                    check(!android.buildTypes.getByName("release").enableAndroidTestCoverage)
                    check(android.jacoco.version == "0.8.14")
                }
            }
            """.trimIndent()
        )
        verify(projectDir)
        verify(projectDir, "verifyConventions", "-PinstrumentationCoverage")
    }

    @Test
    fun `Compose convention enables compiler and shared BOM without data binding`() {
        val projectDir = createProject("compose-library")
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("teamcityapp.android.library")
                id("teamcityapp.android.compose")
            }
            android { namespace = "teamcityapp.conventiontest" }
            tasks.register("verifyConventions") {
                doLast {
                    check(android.buildFeatures.compose == true)
                    check(android.buildFeatures.dataBinding != true)
                    check(plugins.hasPlugin("org.jetbrains.kotlin.plugin.compose"))
                    check(configurations.getByName("implementation").dependencies.any {
                        it.group == "androidx.compose" && it.name == "compose-bom"
                    })
                    check(configurations.getByName("debugImplementation").dependencies.any {
                        it.group == "androidx.compose.ui" && it.name == "ui-tooling"
                    })
                }
            }
            """.trimIndent()
        )
        verify(projectDir)
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

            check(android.compileSdkVersion in setOf("android-37", "android-37.0")) { android.compileSdkVersion.orEmpty() }
            check(android.defaultConfig.minSdkVersion?.apiLevel == 37)
            check(android.defaultConfig.targetSdkVersion?.apiLevel == 37)

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
                    check(plugins.hasPlugin("jacoco"))
                    val agent = tasks.named<Test>("testDebugUnitTest").get()
                        .extensions.getByType<org.gradle.testing.jacoco.plugins.JacocoTaskExtension>()
                    check(agent.isIncludeNoLocationClasses)
                    check(agent.excludes == listOf("jdk.internal.*"))
                    check(project.extensions.getByType<org.gradle.testing.jacoco.plugins.JacocoPluginExtension>().toolVersion == "0.8.14")
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

    private fun verify(projectDir: File, task: String = "verifyConventions", vararg arguments: String) {
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(*arguments, task, "--stacktrace")
            .build()
        assertEquals(TaskOutcome.SUCCESS, result.task(if (task.startsWith(":")) task else ":$task")?.outcome)
    }
}
