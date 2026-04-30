import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

// Load local.properties if exists (for production keys)
val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localProps.load(localPropsFile.inputStream())
}

android {
    namespace = "com.nphstudio.appname"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nphstudio.appname"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // AdMob App ID — uses test ID by default, real ID from local.properties
        val admobAppId = localProps.getProperty(
            "NPH_ADMOB_APP_ID",
            "ca-app-pub-3940256099942544~3347511713"
        )
        manifestPlaceholders["admobAppId"] = admobAppId

        // Meta App ID — for Facebook SDK init (empty = SDK not initialized)
        val metaAppId = localProps.getProperty("NPH_META_APP_ID", "")
        val metaClientToken = localProps.getProperty("NPH_META_CLIENT_TOKEN", "")
        manifestPlaceholders["metaAppId"] = metaAppId
        manifestPlaceholders["metaClientToken"] = metaClientToken
        buildConfigField("String", "META_APP_ID", "\"$metaAppId\"")

        // TikTok App ID — for TikTok Business SDK init (empty = SDK not initialized)
        val tiktokAppId = localProps.getProperty("NPH_TIKTOK_APP_ID", "")
        buildConfigField("String", "TIKTOK_APP_ID", "\"$tiktokAppId\"")

        // Adjust token (empty = Adjust not initialized)
        val adjustToken = localProps.getProperty("NPH_ADJUST_TOKEN", "")
        buildConfigField("String", "ADJUST_TOKEN", "\"$adjustToken\"")

        // AppsFlyer key (empty = AppsFlyer not initialized)
        val appsFlyerKey = localProps.getProperty("NPH_APPSFLYER_KEY", "")
        buildConfigField("String", "APPSFLYER_KEY", "\"$appsFlyerKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // ══════════════════════════════════════════════════
    // NPH SDK — DO NOT MODIFY
    // ══════════════════════════════════════════════════
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

    // Transitive dependencies required by NPH SDK AARs
    // Google Mobile Ads
    implementation("com.google.android.gms:play-services-ads:24.0.0")
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    // Gson
    implementation("com.google.code.gson:gson:2.13.1")
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // ══════════════════════════════════════════════════
    // Tracking SDKs (optional — nph-track auto-detects at runtime)
    // Comment out any SDK you don't need for this project.
    // ══════════════════════════════════════════════════
    // Meta/Facebook SDK — for Meta Ads revenue tracking
    implementation("com.facebook.android:facebook-android-sdk:18.2.3")
    // TikTok Business SDK — for TikTok Ads revenue tracking (via JitPack)
    implementation("com.github.tiktok:tiktok-business-android-sdk:1.6.1")
    // Adjust SDK — for attribution & revenue tracking
    // implementation("com.adjust.sdk:adjust-android:5.0.1")
    // AppsFlyer SDK — for attribution & revenue tracking
    // implementation("com.appsflyer:af-android-sdk:6.15.0")

    // ══════════════════════════════════════════════════
    // App Dependencies — ADD YOUR OWN HERE
    // ══════════════════════════════════════════════════
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
