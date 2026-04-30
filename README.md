# NPH App Template

Android Studio project template tích hợp sẵn **NPH SDK v1.0.0**.

## Quick Start

```bash
# 1. Clone
git clone https://github.com/nphlab/codebase.git <app-name>
cd <app-name>

# 2. Đổi package name
#    - app/build.gradle.kts → applicationId
#    - AndroidManifest.xml → package
#    - Rename folders: app/src/main/java/com/nphstudio/appname/ → your package

# 3. Add google-services.json (từ Firebase Console)
#    Copy vào app/google-services.json

# 4. Build
./gradlew assembleDebug
```

## Project Structure

```
├── app/
│   ├── libs/                    ← NPH SDK AARs (DO NOT MODIFY)
│   │   ├── nph-ads-1.0.0-release.aar
│   │   ├── nph-config-1.0.0-release.aar
│   │   └── nph-track-1.0.0-release.aar
│   ├── src/main/
│   │   ├── assets/ads_config.json   ← Test ad config (auto-loaded)
│   │   ├── java/.../
│   │   │   ├── MyApp.kt            ← SDK init
│   │   │   └── ui/                  ← Fragments (Splash, Language, Onboarding, Home, Settings)
│   │   ├── res/                     ← Layouts, navigation, strings
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── docs/
│   ├── product.md               ← App spec (NPH Lab fills)
│   └── MO.md                    ← Monetization spec (NPH Lab fills)
├── .github/workflows/
│   └── android-ci.yml           ← CI checks on push/PR
├── local.properties.example     ← Key format reference
├── .gitignore                   ← Blocks local.properties, *.jks, google-services.json
└── README.md
```

## Key Management

- **Dev:** Không cần tạo `local.properties`. App tự dùng Google test IDs.
- **NPH Lab (release):** Tạo `local.properties` với real keys trên máy build.
- Xem `local.properties.example` để biết format.

## SDK API (Quick Reference)

```kotlin
// Init (MyApp.kt — already configured)
NphSdk.init(context, ConfigSource.FIREBASE, enableDebug = BuildConfig.DEBUG)

// Interstitial
NphAds.showInterstitial(activity, "nsp-interstitial-home-fullscreen-clickScan", listener)

// Rewarded
NphAds.showRewarded(activity, "nsp-rewarded-result-fullscreen-unlockHd", rewardListener)

// Banner
NphAds.loadBannerInto(container, "nsp-banner-home-bottom-auto")

// Native
NphAds.loadNativeInto(container, "nsp-native-history-top-auto")

// Splash (already in SplashFragment)
NphAds.showSplash(activity) { navigateToNext() }

// Preload
NphAds.preload(activity, "nsp-interstitial-result-fullscreen-complete")

// Premium
NphAds.setPremium(true)

// Cleanup
NphAds.destroy(activity) // in onDestroy()
```

## CI/CD (GitHub Actions)

Mỗi push/PR sẽ tự động kiểm tra:

| Check | Mô tả |
|---|---|
| Build Debug APK | `assembleDebug` phải thành công |
| No direct AdMob imports | Source code không được import `com.google.android.gms.ads.*` |
| No hardcoded ad IDs | Không có `ca-app-pub-` trong `.kt`/`.java` files |
| No local.properties | File sensitive không được commit |
| No keystore files | `.jks`/`.keystore` không được commit |
| google-services.json not tracked | Firebase config không track trong git |
| SDK AARs present | 3 file AAR phải có trong `app/libs/` |
| Required docs | `README.md`, `docs/product.md`, `docs/MO.md` phải tồn tại |
| Android Lint | Chạy lint (warning only) |

## Tracking SDKs

nph-track tự động detect và gửi ad revenue cho các platform đã cài:

| SDK | Dependency | Dùng cho |
|---|---|---|
| Firebase Analytics | Luôn có (BOM 33.7.0) | Google Ads optimization |
| Meta/Facebook | `facebook-android-sdk:18.2.3` | Meta Ads campaigns |
| TikTok | `tiktok-business-android-sdk:1.6.1` | TikTok Ads campaigns |
| Adjust | `adjust-android:5.0.1` (commented) | Attribution MMP |
| AppsFlyer | `af-android-sdk:6.15.0` (commented) | Attribution MMP |

Comment/uncomment trong `app/build.gradle.kts` để bật/tắt per project.
Remote Config key `nph_tracker_config` cho phép NPH Lab tắt/bật runtime.

## Documentation

- **⭐ Dev Guide:** Xem [`docs/DEV_GUIDE.md`](docs/DEV_GUIDE.md) — hướng dẫn đầy đủ cho dev/AI
- **Nghiệm thu:** Xem [`docs/ACCEPTANCE.md`](docs/ACCEPTANCE.md) — yêu cầu + tiêu chí (mỗi app điền riêng)
- **Product spec:** Xem [`docs/product.md`](docs/product.md)
- **Monetization spec:** Xem [`docs/MO.md`](docs/MO.md) — dev hoàn thành sau khi nghiên cứu app mẫu

## Rules

- ❌ KHÔNG import trực tiếp AdMob/FAN SDK
- ❌ KHÔNG hardcode ad unit IDs
- ❌ KHÔNG sửa file trong `app/libs/`
- ❌ KHÔNG commit `local.properties`, `*.jks`, `google-services.json`
- ✅ Dùng `NphAds.*` cho tất cả ad operations
- ✅ Handle cả `onAdDismissed` + `onAdFailed` callbacks
- ✅ Gọi `NphAds.destroy(this)` trong `onDestroy()`
