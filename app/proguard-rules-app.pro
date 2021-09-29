# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/aditya/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in device.installedpackges.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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

# dagger
-dontwarn com.google.errorprone.annotations.**

# joda time
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

# retrolambda
-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*

# aws
-keep class com.amazonaws.** { *; }
-keepnames class com.amazonaws.** { *; }
-dontwarn com.amazonaws.**
-dontwarn com.fasterxml.**

# retrofit
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-dontwarn retrofit2.Platform$Java8
-dontwarn okhttp3.**
-keep class okhttp3.** { *;}


# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# Deeplink
-keep @interface com.airbnb.deeplinkdispatch.DeepLink
-keepclasseswithmembers class * {
    @com.airbnb.deeplinkdispatch.DeepLink <methods>;
}

# For CleverTap SDK
-dontwarn com.clevertap.android.sdk.**

-dontwarn androidx.work.impl.background.systemjob.SystemJobService

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

##################################################
# GUAVA
##################################################
# OFFICIAL
## Not yet defined: follow https://github.com/google/guava/issues/2117

# UNOFFICIAL
## https://github.com/google/guava/issues/2926#issuecomment-325455128
## https://stackoverflow.com/questions/9120338/proguard-configuration-for-guava-with-obfuscation-and-optimization
-dontwarn com.google.common.base.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn java.lang.ClassValue
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

-keepattributes Signature
-keepclassmembers class ApiMessages { <fields>; }
-keep class in.okcredit.backend._offline.server.internal.** { <fields>; }

#UCrop
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }


# Reactive Network
-dontwarn com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
-dontwarn io.reactivex.functions.Function
-dontwarn rx.internal.util.**
-dontwarn sun.misc.Unsafe

# SerializedName R8 Issue
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
  }
-keep,allowobfuscation @interface com.google.gson.annotations.SerializedName


#TODO: remove ignorewarnings
-ignorewarnings

-dontwarn android.support.constraint.**
-keep class android.support.constraint.** { *; }
-keep interface android.support.constraint.** { *; }
-keep public class android.support.constraint.R$* { *; }

#Rxdogtag
-keeppackagenames com.uber.rxdogtag**
-keeppackagenames io.reactivex**

# Facebook Enum
-keepclassmembers enum * {
public static **[] values();
public static ** valueOf(java.lang.String);
}

# Youtube Player
-keep public class com.pierfrancescosoffritti.androidyoutubeplayer.** {
public *;
}

-keepnames class com.pierfrancescosoffritti.androidyoutubeplayer.*

## Facebook Enum
#-keepclassmembers enum * {
#public static **[] values();
#public static ** valueOf(java.lang.String);
#}
#
## Youtube Player
#-keep public class com.pierfrancescosoffritti.androidyoutubeplayer.** {
#public *;
#}
#
#-keepnames class com.pierfrancescosoffritti.androidyoutubeplayer.*


# referral api (appsflyser)
-dontwarn com.android.installreferrer
-keep class com.appsflyer.** { *; }

-assumenosideeffects class com.appsflyer.AFLogger {
public void afInfoLog(...);
public void resetDeltaTime(...);
public void afRDLog(...);
public void afDebugLog(...);
public void afInfoLog(...);
public void afErrorLog(...);
public void afWarnLog(...);
void (...);
}

# uninstall measurement (appsflyser)
-dontwarn com.appsflyer.**
-keep public class com.google.firebase.messaging.FirebaseMessagingService {
  public *;
}

#-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient { com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context); }
#-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info { java.lang.String getId(); boolean isLimitAdTrackingEnabled(); }
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class in.okcredit.backend._offline.model.** {*; }

-keep class tech.okcredit.account_chat_sdk.models.** {*; }

-keepnames class androidx.navigation.fragment.NavHostFragment

-keep class com.github.anrwatchdog.** { *; }
