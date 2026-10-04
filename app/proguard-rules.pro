# Platform rules and modern library consumer rules come from AGP and dependencies.
# Gson uses field names as JSON keys. RxCache also persists DTO class names and
# restores them with Class.forName(), so keep names stable across app upgrades.
# Only data fields and no-argument constructors need reflective access.
-keep class com.github.vase4kin.teamcityapp.*.api.** {
    !static !transient <fields>;
    <init>();
}

# Gson reflects these models; preserve JSON keys and class names stored by RxCache.
-keep class teamcityapp.features.about.repository.models.** {
    !static !transient <fields>;
    <init>();
}

# RxCache creates this interface with a Proxy and reflects its annotated methods.
-keep interface com.github.vase4kin.teamcityapp.api.cache.CacheProviders { *; }
# Its own disk envelope is also serialized with Gson, including generic data.
-keep class io.rx_cache2.internal.Record {
    !static !transient <fields>;
    <init>();
}
-keepclassmembers enum io.rx_cache2.Source { *; }

# EventBus 3.0 discovers subscribers at runtime; no generated subscriber index.
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep,allowobfuscation class org.greenrobot.eventbus.ThreadMode { *; }

# Bundles still carry Java Serializable build models and filters.
-keep class com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl {
    !static !transient <fields>;
    <init>();
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Optional Joda Convert annotations are absent from this Android app.
-dontwarn org.joda.convert.**

# Gson reflects these models; preserve JSON keys and class names stored by RxCache.
-keep class teamcityapp.features.properties.repository.models.** {
    !static !transient <fields>;
    <init>();
}
