# R8 verification

Release builds use R8 full mode, obfuscation, and optimized resource shrinking
with AGP defaults. Application compilation uses the compile-time R class, and
resource fields are non-final. Use `if`/`when` comparisons rather than Java
`switch` labels or Kotlin `const val` definitions for resource IDs.

The OSS licenses SDK loads its layouts and generated license files by name.
`features/about/impl/src/androidMain/res/raw/teamcityapp_oss_licenses_keep.xml` preserves those dynamic
resources through optimization; the minified smoke suite checks their presence
and opens the external licenses screen.

Use JDK 17. Build the isolated, debug-signed verification app and instrumentation:

```sh
python3 scripts/prepare-r8-verification.py
./gradlew -Pr8Verification :app:assembleProdR8Verification :app:assembleProdR8VerificationAndroidTest
./gradlew -Pr8Verification :app:connectedProdR8VerificationAndroidTest
```

The opt-in `r8Verification` build type inherits release settings and uses release
library variants. Its application ID ends in `.r8`, so accounts and preferences
are isolated from installed production/debug apps. Its runner and test source root
are separate from the existing Hilt mock-debug suite. Omit `-Pr8Verification` for
normal builds/tests. `mockRelease` stays disabled. The connected task rejects
runs with fewer than five smoke tests, including runner startup failures.

The checks execute inside the optimized target APK, through a kept bridge available
only in this build type. Verification-only rules also preserve shared tracing and
Kotlin runtime APIs needed by the runner; production releases omit those rules.
This avoids instrumentation-generated rules keeping the
app APIs under test. They cover unannotated Gson fields and inherited/generic
models, account persistence with Keystore, Java serialization (including an installed
pre-migration filter payload and its original class name/locator), common Ktor response
decoding and response-cache disk restoration, Joda timezone
resources, login screen startup, and the external OSS licenses screen with its
generated raw resources. Ktor responses are supplied by its verification-only mock
engine; no TeamCity server is needed. The cache check creates a fresh client/cache
with an unavailable upstream and requires the saved response to be restored.

CI always builds the minified verification application. It also builds the R8
test APK and runs it on an ARM64 Android OS version 17 emulator when the full UI
suite is selected:
on default-branch pushes affecting application, Android test, or build inputs
unless every incoming commit is a valid chore (force pushes still run),
or manual dispatch with the suite override checked and
`suite: full`. Changed + smoke PR validation and sampled mock instrumentation runs omit
R8 test APK compilation and paid R8 execution. The full Marathon matrix runs R8
in parallel with mock instrumentation and requires `MARATHON_CLOUD_API_TOKEN`.
Each matrix entry invokes Marathon once with its matching application and test
APKs. R8 smoke tests do not collect coverage.
Mappings and R8 diagnostics are uploaded as `r8-diagnostics`.

Keep JSON field names stable for existing server payloads and saved accounts.
Installed launch payloads and historical cache files contain fully qualified DTO
class names, so those names remain stable. The retired RxCache engine is absent
from production runtimes; historical cache files remain untouched. Model modules
export their own consumer rules; application-specific model rules live in
`app/proguard-rules.pro`. New reflected models must receive equivalent rules.

Inspect `app/build/outputs/mapping/prodR8Verification/` for `mapping.txt`,
`configuration.txt`, `seeds.txt`, and `usage.txt` when changing keep rules. A
successful build alone does not verify reflective behavior; run the smoke tests.
