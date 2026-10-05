<!--
Copyright 2026 Andrey Tolpeev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Repository formatting

Run with JDK 17 from the repository root:

```bash
./gradlew spotlessApply  # Format only files with uncommitted changes.
./gradlew spotlessCheck  # Check those files without changing them.
./gradlew spotlessCheck -PspotlessBase=origin/dev  # Check a branch's changes.
```

Spotless uses a Git ratchet to leave untouched files alone. Local commands compare
against `HEAD`; set `spotlessBase` to a branch or commit to include committed changes.
For a stacked branch, use its immediate parent as the base. CI fetches full history
and supplies the PR's base SHA, the push's previous SHA, or `HEAD~1` for a manual
run. The formatting check fails on violations in changed files, including files
added in earlier commits of the PR. No repository-wide formatting baseline is needed.

The root configuration covers every module and the included `build-logic` build.
Kotlin in both `kotlin` and legacy `java` roots and Gradle Kotlin DSL scripts use
ktlint. Java uses Google Java Format's four-space AOSP style with a JDK 17 compatible
engine. Versions are pinned in the catalog and editor settings in `.editorconfig`.
Naming rules preserve existing APIs and packages. XML and repository configuration
files receive trailing-whitespace and final-newline checks; XML text and Data Binding
expressions are preserved. Markdown keeps intentional hard breaks. Generated output,
IDE metadata, and `local.properties` are excluded. The usual Android Gradle
configuration requirements still apply.
