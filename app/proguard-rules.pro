-keepattributes *Annotation*, InnerClasses, Signature

# Room generated implementations are looked up reflectively at runtime.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**
