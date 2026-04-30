# Developer Guide

> Cho developer (hoặc AI model) sử dụng codebase để phát triển app Android.

## 1. Quy trình tổng quan

1. NPH Lab gửi cho dev **link app mẫu** + codebase repo + **package name** + **license key**
2. Dev clone codebase, đổi package name, app name, icon, điền license key
3. Dev **cài app mẫu**, dùng thử, ghi nhận vị trí ads, **hoàn thành `docs/MO.md`**
4. Dev phát triển tính năng theo app mẫu, đặt ads theo MO.md
5. Dev **build AAB release** trên local, test trên device thật
6. Dev nộp source code + AAB + APK cho NPH Lab nghiệm thu

## 2. Yêu cầu môi trường

| Yêu cầu | Giá trị |
| --- | --- |
| Android Studio | Ladybug 2024.2+ |
| JDK | 17 (bundled) |
| Gradle | 8.9+ (có sẵn trong wrapper) |
| compileSdk / targetSdk | 35 |
| minSdk | 24 |
| Kotlin | 2.0+ |

Khi dev không cần tạo `local.properties` - app tự dùng Google test ad IDs.

## 3. Clone và thiết lập

```bash
git clone <codebase-repo-url> my-app-name
cd my-app-name
rm -rf .git
git init && git add . && git commit -m "Init from codebase"
./gradlew assembleDebug
```

## 4. Cấu trúc project

```text
app/
  libs/                       <- 3 AAR NPH SDK (KHÔNG SỬA)
  src/main/
    assets/ads_config.json    <- Config ads (đã có sẵn Google test IDs)
    java/.../
      MyApp.kt                <- SDK init (ĐÃ CẤU HÌNH, KHÔNG SỬA)
      ui/
        MainActivity.kt       <- Single Activity + NavHost
        splash/SplashFragment.kt
        language/LanguageFragment.kt
        onboarding/OnboardingFragment.kt
        home/HomeFragment.kt
        settings/SettingsFragment.kt
    res/
      layout/
      navigation/nav_graph.xml
      values/
    AndroidManifest.xml
  build.gradle.kts
  proguard-rules.pro
docs/
  product.md                  <- App spec
  MO.md                       <- Ad placement spec (DEV HOÀN THÀNH)
```

### Phân quyền sửa file

| File | Sửa? | Ghi chú |
| --- | --- | --- |
| `app/src/main/java/...` | OK | Code logic, UI |
| `app/src/main/res/...` | OK | Layouts, strings, drawable |
| `AndroidManifest.xml` | OK | Giữ nguyên AdMob meta-data |
| `docs/MO.md` | OK | Dev hoàn thành |
| `docs/product.md` | OK | Dev điền nếu NPH Lab chưa điền |
| `ads_config.json` | Cẩn thận | Chỉ khi thêm ad placement test |
| `app/build.gradle.kts` | Cẩn thận | Chỉ section `App Dependencies` |
| `app/libs/*.aar` | KHÔNG | KHÔNG SỬA, KHÔNG XÓA |
| `MyApp.kt` | Cẩn thận | Chỉ điền `apiKey` do NPH Lab cung cấp |

## 5. Tùy biến cho app mới

### 5.1 Đổi Package Name

NPH Lab cung cấp package name cho mỗi project. Thay `com.nphstudio.appname` ở tất cả vị trí:

1. `app/build.gradle.kts` - `namespace` và `applicationId`
2. Tất cả `.kt` - dòng `package` và import nội bộ (`R`, `databinding`, `BuildConfig`)
3. `nav_graph.xml` - thuộc tính `android:name`
4. Rename folder `app/src/main/java/com/nphstudio/appname/`

Mẹo: find-and-replace toàn project.

### 5.2 Điền License Key

NPH Lab cung cấp license key cho mỗi project. Điền vào `MyApp.kt`:

```kotlin
NphSdk.init(
    context = this,
    apiKey = "nph_xxxxx...",   // <- điền license key NPH Lab cung cấp
    configSource = ConfigSource.FIREBASE,
    enableDebug = BuildConfig.DEBUG
)
```

### 5.3 Đổi tên App

```xml
<string name="app_name">Your App Name</string>
```

### 5.4 Đổi App Icon

Android Studio > New > Image Asset.

### 5.5 google-services.json

Tạo project Firebase, download, copy vào `app/`. Đã bị `.gitignore`.

### 5.6 Thêm dependency

Thêm ở section `App Dependencies` trong `app/build.gradle.kts`. KHÔNG sửa section `NPH SDK` và `Tracking SDKs`.

## 6. Nghiên cứu app mẫu và hoàn thành MO.md

### 6.1 Cài và dùng app mẫu

- Cài app mẫu lên device thật
- Dùng từ đầu đến cuối, đi qua mọi màn hình
- Ghi nhận chính xác vị trí quảng cáo

### 6.2 Phân loại ad

| Thấy gì | Loại | API |
| --- | --- | --- |
| Ad toàn màn hình khi chuyển màn | Interstitial | `showInterstitial()` |
| Ad toàn màn hình, xem để nhận thưởng | Rewarded | `showRewarded()` |
| Ad toàn màn hình tự hiện, có reward | Rewarded Interstitial | `showRewardedInterstitial()` |
| Dải ngang top/bottom | Banner | `loadBannerInto()` |
| Ad nhỏ trong UI (icon, title, body) | Native | `loadNativeInto()` |
| Ad khi mở app (splash) | App Open | `showSplash()` |
| Ad khi quay lại từ background | App Open Resume | Tự động (SDK) |

### 6.3 Điền docs/MO.md

Quy tắc nameSpace: `nsp-<type>-<screen>-<position>-<action>` — xem chi tiết trong [`ads-naming-convention.md`](ads-naming-convention.md)

- type: `appopen`, `interstitial`, `rewarded`, `rewardedInter`, `banner`, `native`
- position: `fullscreen` (inter/reward/appopen) hoặc vị trí thực (`top`, `bottom`, `cardX`, `listN`...)
- action: `auto` (tự hiển thị), `back`, `complete`, `clickX` (camelCase), `save`, `unlockX`...
- VD: `nsp-interstitial-home-fullscreen-clickSettings`, `nsp-banner-home-bottom-auto`

Mặc định: Interstitial `interval=25`, `stepCount=1`. Banner/Rewarded không cần.

### 6.4 Cập nhật ads_config.json

File `assets/ads_config.json` là config ads dùng cho dev/offline (fallback khi Firebase Remote Config chưa có).
Production sẽ dùng Firebase Remote Config do NPH Lab quản lý, cùng format JSON.

**Cấu trúc JSON chuẩn:**

```json
{
  "isEnableAll": true,
  "splashAds": {
    "nameSpace": "nsp-appopen-splash-fullscreen-auto",
    "isEnable": true,
    "unitIds": [
      { "unitId": "<app_open_unit_id>", "isEnable": true, "network": "admob" }
    ],
    "timeOut": 15000
  },
  "resumeAds": {
    "nameSpace": "nsp-appopen-resume-fullscreen-auto",
    "isEnable": true,
    "unitIds": [
      { "unitId": "<app_open_unit_id>", "isEnable": true, "network": "admob" }
    ]
  },
  "interstitialAds": [
    {
      "nameSpace": "nsp-interstitial-<screen>-fullscreen-<action>",
      "isEnable": true,
      "interval": 25,
      "stepCount": 1,
      "forceShow": false,
      "unitIds": [
        { "unitId": "<interstitial_unit_id>", "isEnable": true, "network": "admob" }
      ]
    }
  ],
  "rewardedAds": [
    {
      "nameSpace": "nsp-rewarded-<screen>-fullscreen-<action>",
      "isEnable": true,
      "unitIds": [
        { "unitId": "<rewarded_unit_id>", "isEnable": true, "network": "admob" }
      ]
    }
  ],
  "rewardedInterAds": [
    {
      "nameSpace": "nsp-rewardedInter-<screen>-fullscreen-<action>",
      "isEnable": true,
      "unitIds": [
        { "unitId": "<rewarded_inter_unit_id>", "isEnable": true, "network": "admob" }
      ]
    }
  ],
  "bannerAds": [
    {
      "nameSpace": "nsp-banner-<screen>-<position>-auto",
      "isEnable": true,
      "bannerType": "banner_adaptive",
      "unitIds": [
        { "unitId": "<banner_unit_id>", "isEnable": true, "network": "admob" }
      ]
    }
  ],
  "nativeAds": [
    {
      "nameSpace": "nsp-native-<screen>-<position>-auto",
      "isEnable": true,
      "nativeType": "native_medium",
      "unitIds": [
        { "unitId": "<native_unit_id>", "isEnable": true, "network": "admob" }
      ]
    }
  ]
}
```

**Các field quan trọng:**

| Field | Mô tả |
| --- | --- |
| `nameSpace` | Định danh placement, phải khớp với code gọi `NphAds.show*()` |
| `isEnable` | Bật/tắt placement (hoặc toàn bộ ads với `isEnableAll`) |
| `interval` | Khoảng cách tối thiểu giữa 2 lần show (giây), chỉ interstitial |
| `stepCount` | Số lần trigger trước khi thực sự show, chỉ interstitial |
| `forceShow` | Bỏ qua kiểm tra interval + stepCount |
| `bannerType` | `banner_adaptive` hoặc `collapsible_bottom` |
| `nativeType` | `native_small` hoặc `native_medium` |
| `network` | `admob` (mặc định) hoặc `fan` (Meta Audience Network) |

**Test unit IDs (dùng trong dev, đã có sẵn trong template):**

| Loại | Test Unit ID |
| --- | --- |
| App Open | `ca-app-pub-3940256099942544/9257395921` |
| Interstitial | `ca-app-pub-3940256099942544/1033173712` |
| Rewarded | `ca-app-pub-3940256099942544/5224354917` |
| Rewarded Inter | `ca-app-pub-3940256099942544/5354046379` |
| Banner | `ca-app-pub-3940256099942544/9214589741` |
| Native | `ca-app-pub-3940256099942544/2247696110` |

> **QUAN TRỌNG:** Key JSON phải đúng tên (`interstitialAds`, `rewardedAds`, `bannerAds`, `nativeAds`, v.v.). SDK parse bằng Gson — sai tên key sẽ bị bỏ qua và ads sẽ không hiển thị.

## 7. SDK Public API

### 7.1 Import

```kotlin
import com.nphlab.sdk.ads.NphAds
import com.nphlab.sdk.ads.AdError
import com.nphlab.sdk.ads.listener.NphAdListener
import com.nphlab.sdk.ads.listener.NphRewardListener
```

### 7.2 Interstitial

```kotlin
NphAds.showInterstitial(
    activity = requireActivity(),
    nameSpace = "nsp-interstitial-<screen>-fullscreen-<action>",
    listener = object : NphAdListener() {
        override fun onAdDismissed() {
            if (isAdded) navigateToNextScreen()
        }
        override fun onAdFailed(error: AdError) {
            if (isAdded) navigateToNextScreen()
        }
    }
)
```

LUÔN handle cả `onAdDismissed` + `onAdFailed`.

### 7.3 Rewarded

```kotlin
NphAds.showRewarded(
    activity = requireActivity(),
    nameSpace = "nsp-rewarded-<screen>-fullscreen-<action>",
    listener = object : NphRewardListener() {
        override fun onRewardEarned(rewardType: String, rewardAmount: Int) {
            grantReward(rewardType, rewardAmount)
        }
        override fun onAdDismissed() { }
        override fun onAdFailed(error: AdError) { showToast("Ad not available") }
    }
)
```

### 7.4 Rewarded Interstitial

```kotlin
NphAds.showRewardedInterstitial(
    activity = requireActivity(),
    nameSpace = "nsp-rewardedInter-<screen>-fullscreen-<action>",
    listener = object : NphRewardListener() {
        override fun onRewardEarned(rewardType: String, rewardAmount: Int) {
            grantReward(rewardType, rewardAmount)
        }
        override fun onAdDismissed() { }
        override fun onAdFailed(error: AdError) { }
    }
)
```

### 7.5 Banner

```xml
<FrameLayout
    android:id="@+id/ad_banner_container"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_gravity="bottom" />
```

```kotlin
NphAds.loadBannerInto(binding.adBannerContainer, "nsp-banner-<screen>-<position>-auto")
```

### 7.6 Native

```kotlin
NphAds.loadNativeInto(binding.adNativeContainer, "nsp-native-<screen>-<position>-auto")
```

### 7.7 Splash (đã cấu hình sẵn trong SplashFragment.kt)

```kotlin
NphAds.showSplash(requireActivity()) {
    if (isAdded) findNavController().navigate(R.id.action_splash_to_language)
}
```

### 7.8 Preload

```kotlin
NphAds.preload(requireActivity(), "nsp-interstitial-home-fullscreen-clickSettings")
```

Hỗ trợ interstitial, rewarded, rewarded interstitial, app open. Banner/native không cần (load inline).

### 7.9 Utility

```kotlin
NphAds.isPlacementEnabled("nsp-rewarded-<screen>-fullscreen-<action>")  // check placement bật/tắt
NphAds.setPremium(true)                       // tắt toàn bộ ads
NphAds.isPremium()
NphAds.pauseResumeAds()                       // tạm dừng resume ads
NphAds.resumeResumeAds()
NphAds.excludeResumeActivity(XxxActivity::class.java)
NphAds.destroy(activity)                      // BẮT BUỘC trong onDestroy()
```

### 7.10 Callbacks

**NphAdListener:**

| Callback | Khi nào |
| --- | --- |
| `onAdShowed()` | Ad hiển thị thành công |
| `onAdDismissed()` | User đóng ad |
| `onAdFailed(error: AdError)` | Ad không hiển được |
| `onAdClicked()` | User click ad |
| `onAdImpression()` | Impression ghi nhận |

**NphRewardListener** (kế thừa NphAdListener, thêm):

| Callback | Khi nào |
| --- | --- |
| `onRewardEarned(rewardType, rewardAmount)` | User xem xong, nhận thưởng |

**AdError types:**

| Error | Code | Ý nghĩa |
| --- | --- | --- |
| `NotInitialized` | 100 | Chưa gọi NphSdk.init() |
| `PlacementNotFound` | 101 | nameSpace không có trong config (không khớp với MO.md / `ads_config.json`) |
| `PlacementDisabled` | 102 | Placement bị tắt |
| `FrequencyLimited` | 103 | Bị chặn bởi interval/stepCount |
| `LoadFailed` | 200 | Load thất bại |
| `WaterfallExhausted` | 201 | Tất cả units đều fail |
| `NoInternet` | 300 | Không có internet |
| `ConsentRequired` | 400 | Cần consent |
| `PremiumUser` | 500 | Premium, ads tắt |

## 8. Đặt quảng cáo theo MO.md

1. Đọc `docs/MO.md` -> biết nameSpace nào đặt ở đâu
2. Gọi API tương ứng tại đúng trigger (mục 7)
3. SDK tự xử lý interval/stepCount
4. Preload ở màn trước nếu cần

Ví dụ: MO.md ghi `nsp-interstitial-home-fullscreen-clickSettings | Click nút Settings ở Home | 25s | 2`

```kotlin
// HomeFragment.onViewCreated()
NphAds.preload(requireActivity(), "nsp-interstitial-home-fullscreen-clickSettings")

binding.btnSettings.setOnClickListener {
    NphAds.showInterstitial(
        activity = requireActivity(),
        nameSpace = "nsp-interstitial-home-fullscreen-clickSettings",
        listener = object : NphAdListener() {
            override fun onAdDismissed() {
                if (isAdded) findNavController().navigate(R.id.action_home_to_settings)
            }
            override fun onAdFailed(error: AdError) {
                if (isAdded) findNavController().navigate(R.id.action_home_to_settings)
            }
        }
    )
}
```

## 9. Thêm màn hình

### Template Fragment

```kotlin
package com.nphstudio.yourappname.ui.xxx

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nphstudio.yourappname.databinding.FragmentXxxBinding

class XxxFragment : Fragment() {
    private var _binding: FragmentXxxBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentXxxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

### Pattern sẵn có

| Pattern | File | Mô tả |
| --- | --- | --- |
| Splash + App Open | `SplashFragment.kt` | showSplash > callback > navigate |
| Inter khi chuyển màn | `LanguageFragment.kt` | showInterstitial > onDismissed/onFailed > navigate |
| Banner | `HomeFragment.kt` | loadBannerInto trong onViewCreated |
| Preload | `HomeFragment.kt` | preload sớm, show sau |
| Premium | `SettingsFragment.kt` | setPremium(true) khi IAP thành công |

## 10. Navigation

Jetpack Navigation Component, single Activity.

```text
Splash > Language > Onboarding > Home > Settings
```

Thêm destination:

```xml
<fragment
    android:id="@+id/newFeatureFragment"
    android:name="com.nphstudio.yourappname.ui.xxx.XxxFragment"
    android:label="@string/xxx_title"
    tools:layout="@layout/fragment_xxx" />

<action
    android:id="@+id/action_home_to_xxx"
    app:destination="@id/newFeatureFragment" />
```

```kotlin
findNavController().navigate(R.id.action_home_to_xxx)
```

Giữ nguyên luồng Splash > Language > Onboarding. Thêm feature screens sau Home.

## 11. Build và đóng gói

### Debug

```bash
./gradlew assembleDebug
```

### Release AAB (bắt buộc trước khi nộp)

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

Hoặc dùng Android Studio > Build > Generate Signed Bundle.

### Release APK (test nhanh)

```bash
./gradlew assembleRelease
```

Lưu ý: `isMinifyEnabled = true` trong release. Nếu library mới bị strip, thêm keep rules vào `proguard-rules.pro`. Test kỹ trên device thật sau build release.

## 12. Quy tắc bắt buộc

### CẤM

- Import `com.google.android.gms.ads.*` (SDK đã wrap)
- Hardcode ad unit ID `ca-app-pub-...` trong code
- Sửa/xóa `app/libs/` hoặc `MyApp.kt`
- Commit `local.properties`, `*.jks`, `google-services.json`
- Block user khi ad fail
- Xóa `NphAds.destroy(this)` trong MainActivity

### BẮT BUỘC

- Dùng `NphAds.*` cho mọi ad operation
- Handle cả `onAdDismissed` + `onAdFailed`
- Check `isAdded` trước navigate trong Fragment
- `NphAds.destroy(this)` trong mọi Activity.onDestroy()
- nameSpace khớp với `docs/MO.md`
- Banner container: `FrameLayout`, `wrap_content` height
- Hoàn thành `docs/MO.md` trước khi bắt đầu code ads
- Build AAB release thành công trước khi nộp

## 13. Checklist trước khi nộp

```text
[ ] Build Debug thành công
[ ] Build Release AAB thành công (bundleRelease)
[ ] Test trên device thật
[ ] Package name đã đổi theo NPH Lab cung cấp
[ ] License key đã điền vào MyApp.kt
[ ] App name đã đổi
[ ] App icon đã thay
[ ] docs/MO.md hoàn thành
[ ] Ads đặt đúng nameSpace theo MO.md
[ ] Handle onAdDismissed + onAdFailed
[ ] NphAds.destroy() trong onDestroy()
[ ] Không import com.google.android.gms.ads.*
[ ] Không hardcode ca-app-pub-*
[ ] Không commit local.properties, jks, google-services.json
[ ] README.md cập nhật tên app
```

## 14. FAQ

**Ads không hiện?** Check Logcat tag `NphAds`. Đảm bảo internet + Google Play Services.

**Thêm ad placement ngoài MO.md?** Tự thêm vào MO.md, ghi lý do. NPH Lab review khi nghiệm thu.

**ads_config.json vs Firebase Remote Config?** ads_config.json là fallback dev/offline. Production dùng Firebase Remote Config do NPH Lab quản lý.

**Dùng Jetpack Compose?** Được. Thêm deps ở section App Dependencies. Banner wrap trong AndroidView.

**Build release crash?** Thêm keep rules vào `proguard-rules.pro`.

---

**Tóm tắt cho AI: Đọc product.md + MO.md > đổi package > thêm screens (copy pattern từ file sẵn có) > đặt ads (NphAds.* với nameSpace từ MO.md) > build AAB > chạy checklist. KHÔNG sửa libs/, MyApp.kt, section NPH SDK.