# Gson reflects these models; preserve JSON keys and class names stored by RxCache.
-keep class teamcityapp.features.test_details.repository.models.** {
    !static !transient <fields>;
    <init>();
}
