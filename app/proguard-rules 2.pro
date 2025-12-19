# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep source file and line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Keep generic signature for Firebase/Firestore serialization
-keepattributes Signature

# Keep annotations
-keepattributes *Annotation*

# Firebase/Firestore data model classes
# Keep all data classes in the model.data package
-keep class ch.eureka.eurekapp.model.data.** { *; }

# Keep Kotlin data classes used with Firebase
-keep @kotlinx.serialization.Serializable class ** { *; }

# Keep Parcelable classes
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Retrofit/OkHttp (if used)
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Compose
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }