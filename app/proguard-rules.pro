# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


#=============================================================================
# Global Optimization Attributes & Crash Log Debug Safeties
#=============================================================================
# FIXED: Uncommented to preserve line number information for readable crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Global attributes required by serialization frameworks (GSON, Retrofit)
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses, RuntimeVisibleStringAnnotations

#=============================================================================
# Android System, Architecture Baseline & ViewBinding Rules
#=============================================================================
# Keep ViewBinding implementations safe from reflection breakages
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** inflate(...);
    public static *** bind(...);
}

#=============================================================================
# Dependency: Gson & Data Serialization Models
#=============================================================================
-dontwarn com.google.gson.**
-keep class com.google.gson.reflect.TypeToken
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.TypeAdapter

# Safeguard any class member utilizing Gson SerializedName annotation fields
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Explicitly keep your data structure packages safe from renaming optimizations
-keep class com.futurecode.recoverdeletedmessages.notification.NotificationModel { *; }
-keep class com.futurecode.recoverdeletedmessages.model.** { *; }

#=============================================================================
# Dependency: Retrofit 2 & OkHttp 3
#=============================================================================
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp platform checks warnings suppression
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.**

#=============================================================================
# Dependency: Google Play Mobile Ads SDK (AdMob) & Mediation
#=============================================================================
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**

#=============================================================================
# Dependency: Meta Audience Network (Facebook Ads)
#=============================================================================
-keep class com.facebook.ads.** { *; }
-dontwarn com.facebook.ads.**

#=============================================================================
# Dependency: Google Play Billing Client (In-App Purchases)
#=============================================================================
-keep class com.android.billingclient.api.** { *; }
-dontwarn com.android.billingclient.api.**

#=============================================================================
# Dependency: Glide (Image Loading framework)
#=============================================================================
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule
-dontwarn com.bumptech.glide.GeneratedAppGlideModuleImpl
-dontwarn com.github.bumptech.glide.**

#=============================================================================
# Dependency: Lottie Animations
#=============================================================================
-keep class com.airbnb.lottie.** { *; }

#=============================================================================
# Dependency: Intuit Responsive Dimens (SDP / SSP)
#=============================================================================
-keep class com.intuit.sdp.** { *; }
-keep class com.intuit.ssp.** { *; }

#=============================================================================
# Dependency: Guava & Coroutines Optimization Safeties
#=============================================================================
-dontwarn com.google.common.**
-dontwarn kotlinx.coroutines.**