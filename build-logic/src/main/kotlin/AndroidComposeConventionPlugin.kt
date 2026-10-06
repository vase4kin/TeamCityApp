/*
 * Copyright 2026 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamcityapp.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        extensions.configure(CommonExtension::class.java) {
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
