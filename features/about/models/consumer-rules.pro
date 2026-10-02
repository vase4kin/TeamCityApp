# Gson reflects these models; preserve JSON keys and class names stored by RxCache.
-keep class teamcityapp.features.about.repository.models.** {
    !static !transient <fields>;
    <init>();
}
