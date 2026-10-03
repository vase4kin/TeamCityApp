# Gson reflects these models; preserve JSON keys and class names stored by RxCache.
-keep class teamcityapp.libraries.storage.models.** {
    !static !transient <fields>;
    <init>();
}
