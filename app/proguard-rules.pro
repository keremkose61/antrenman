# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep line number info for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ===== Room Database =====
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface *
-keepclassmembers @androidx.room.Dao interface * { *; }

# ===== WorkManager =====
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ===== AppWidget =====
-keep class * extends android.appwidget.AppWidgetProvider

# ===== Kotlin Coroutines =====
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ===== MPAndroidChart =====
-keep class com.github.mikephil.charting.** { *; }

# ===== Application Classes =====
-keep class com.pplantreman.app.** { *; }