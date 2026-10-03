# R8 verification

Release builds use R8 full mode, obfuscation, and resource shrinking. Optimized
resource shrinking remains disabled because ButterKnife annotations require final
resource IDs. Remove that opt-out together with `android.nonFinalResIds=false`
after migrating the bindings.

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
models, account persistence with Keystore, Java serialization, Retrofit service
reflection/decoding, RxCache disk restoration, EventBus subscribers, Joda timezone
resources, and login screen startup. Retrofit responses are supplied by a local
interceptor; no TeamCity server is needed.

CI builds these APKs and runs them on Android OS version 17 in the R8 entry of
the Marathon Cloud matrix, in parallel with mock instrumentation tests and with
the same required `MARATHON_CLOUD_API_TOKEN` secret. Each matrix entry invokes
Marathon once with its matching application and test APKs. R8 smoke tests do
not collect coverage.
Mappings and R8 diagnostics are uploaded as `r8-diagnostics`.

Keep JSON field names stable for existing server payloads and saved accounts.
RxCache stores fully qualified DTO class names, so model class names must also
remain stable across updates. Model modules export their own consumer rules;
application-specific models, cache providers, and EventBus rules live in
`app/proguard-rules.pro`. New reflected models must receive equivalent rules.

Inspect `app/build/outputs/mapping/prodR8Verification/` for `mapping.txt`,
`configuration.txt`, `seeds.txt`, and `usage.txt` when changing keep rules. A
successful build alone does not verify reflective behavior; run the smoke tests.
