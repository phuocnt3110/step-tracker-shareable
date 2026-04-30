# NPH SDK — Keep public API
-keep class com.nphlab.sdk.ads.NphSdk { *; }
-keep class com.nphlab.sdk.ads.NphAds { *; }
-keep class com.nphlab.sdk.ads.AdError { *; }
-keep class com.nphlab.sdk.ads.AdError$* { *; }
-keep class com.nphlab.sdk.ads.listener.** { *; }
-keep class com.nphlab.sdk.config.ConfigSource { *; }

# Google Ads
-keep class com.google.android.gms.ads.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# App-specific rules — add below
