# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# NPH SDK - Keep all classes
-keep class com.nphlab.sdk.** { *; }
-keep class com.nphlab.sdk.ads.** { *; }
-keep class com.nphlab.sdk.config.** { *; }
-keep class com.google.android.gms.ads.** { *; }

# Keep AdMob classes
-keep class com.google.android.gms.ads.AdRequest { *; }
-keep class com.google.android.gms.ads.LoadAdError { *; }
-keep class com.google.android.gms.ads.FullScreenContentCallback { *; }
-keep class com.google.android.gms.ads.OnUserEarnedRewardListener { *; }

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
