# Gson reflects these models; preserve JSON keys and class names stored by RxCache.
-keep class teamcityapp.libraries.api.** {
    !static !transient <fields>;
    <init>();
}
