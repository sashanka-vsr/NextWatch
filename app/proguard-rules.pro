# ProGuard / R8 rules for NextWatch

# Ktor & SLF4J
-dontwarn org.slf4j.**
-dontwarn io.ktor.**

# Coroutines
-dontwarn kotlinx.coroutines.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# Keep MediaItem and models used in Database & Backup
-keep class com.nextwatch.app.data.** { *; }
-keep class com.nextwatch.app.data.backup.** { *; }
-keep class com.nextwatch.app.network.** { *; }
