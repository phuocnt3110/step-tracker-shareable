# Monetization Specification (MO)

> **TODO:** NPH Lab điền spec monetization trước khi giao cho dev.
> Dev đọc file này để biết đặt ads ở đâu với nameSpace nào.
>
> **Quy tắc đặt tên `nameSpace`**: xem [`ads-naming-convention.md`](ads-naming-convention.md).
> Format: `nsp-<type>-<screen>-<position>-<action>`

## Ad Placements

### App Open Ads
| nameSpace | Vị trí | Ghi chú |
|---|---|---|
| `nsp-appopen-splash-fullscreen-auto` | Cold start — Splash screen | Show 1 lần khi mở app |
| `nsp-appopen-resume-fullscreen-auto` | Warm start — quay lại từ background | Exclude: Premium, Splash |

### Interstitial Ads
| nameSpace | Trigger | interval | stepCount | Ghi chú |
|---|---|---|---|---|
| `nsp-interstitial-language-fullscreen-complete` | Sau khi chọn xong ngôn ngữ | 25s | 1 | |
| `nsp-interstitial-onboarding-fullscreen-complete` | Sau khi xong onboarding | 25s | 1 | |
| `nsp-interstitial-home-fullscreen-clickSettings` | Click nút Settings ở Home | 25s | 2 | Skip lần đầu |

### Rewarded Ads
| nameSpace | Trigger | Reward | Ghi chú |
|---|---|---|---|
| <!-- nsp-rewarded-<screen>-fullscreen-<action> --> | <!-- Mô tả --> | <!-- Reward gì --> | <!-- Ghi chú --> |

### Rewarded Interstitial Ads
| nameSpace | Trigger | Reward | Ghi chú |
|---|---|---|---|
| <!-- nsp-rewardedInter-<screen>-fullscreen-<action> --> | <!-- Mô tả --> | <!-- Reward gì --> | <!-- Opt-in không bắt buộc --> |

### Banner Ads

Types: `banner_adaptive`, `collapsible_top`, `collapsible_bottom`

| nameSpace | Vị trí | Type | Ghi chú |
|---|---|---|---|
| `nsp-banner-onboarding-bottom-auto` | Onboarding — bottom | banner_adaptive | |
| `nsp-banner-home-bottom-auto` | Home — bottom | collapsible_bottom | |
| `nsp-banner-settings-bottom-auto` | Settings — bottom | banner_adaptive | |

### Native Ads
| nameSpace | Vị trí | Type | Ghi chú |
|---|---|---|---|
| `nsp-native-home-top-auto` | Home — top | native_medium | |

## Premium Policy
- Premium users: ALL ads disabled via `NphAds.setPremium(true)`
- IAP product ID: <!-- com.nphstudio.appname.premium -->

## Notes
<!-- Ghi chú thêm về chiến lược monetization -->
