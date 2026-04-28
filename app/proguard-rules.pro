# Keep model classes for Gson
-keep class com.saiem.sportsapp.data.model.** { *; }
-keep class com.saiem.sportsapp.data.remote.api.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# ExoPlayer / Media3
-keep class androidx.media3.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }
