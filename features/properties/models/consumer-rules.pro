# Gson reflects these models; preserve JSON keys and class names stored by RxCache.
-keep class teamcityapp.features.properties.repository.models.** {
    !static !transient <fields>;
    <init>();
}
