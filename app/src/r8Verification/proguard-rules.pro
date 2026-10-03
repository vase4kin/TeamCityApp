# These dependencies are shared with the test APK. AGP omits shared classes from
# instrumentation, so retain their test-only API in this verification APK.
# These rules never apply to production releases or TeamCity implementation code.
-keep class androidx.tracing.** { *; }
-keep class androidx.lifecycle.Lifecycle$State { *; }
# AndroidJUnitRunner/core are Kotlin consumers; their calls are invisible to the
# target shrinker, and their copy of the shared stdlib is omitted from the test APK.
-keep class kotlin.** { *; }
