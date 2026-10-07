/*
 * Copyright 2020 Andrey Tolpeev
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

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "TeamCityApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
include(":libraries:api")
include(":libraries:utils")
include(":libraries:app-rating")
include(":libraries:coroutines")
include(":libraries:theme")
include(":libraries:resources")
include(":libraries:chrome-tabs")
include(":libraries:storage")
include(":libraries:storage-models")
include(":libraries:cache-manager")
include(":libraries:onboarding")
include(":libraries:security")
include(":libraries:remote")
include(":features:splash:api")
include(":features:splash:impl")
include(":features:test-details:api")
include(":features:test-details:impl")
include(":features:about:api")
include(":features:about:impl")
include(":features:manage-accounts:api")
include(":features:manage-accounts:impl")
include(":features:drawer:api")
include(":features:drawer:impl")
include(":features:settings:api")
include(":features:settings:impl")
include(":libraries:app-theme")
include(":features:change-details:api")
include(":features:change-details:impl")
include(":features:properties:api")
include(":features:properties:impl")

include(":libraries:authentication")
include(":features:login:api")
include(":features:login:impl")
include(":features:create-account:api")
include(":features:create-account:impl")
include(":features:run-build:api")
include(":features:run-build:impl")
