# Quy Tắc Đặt Tên nameSpace (NPH SDK)

> **Mục tiêu**: MO chỉ cần nhìn vào `nameSpace` là biết ads ở **màn hình nào, vị trí nào, hành động nào**.

## Format Chuẩn

```
nsp-<type>-<screen>-<position>-<action>
```

5 phần, **bắt buộc đủ 5 phần**, ngăn cách bằng dấu `-`. Tất cả lowercase, dùng **camelCase** cho từ ghép trong 1 phần.

| Phần | Ý nghĩa |
|------|---------|
| `nsp` | Tiền tố cố định = "namespace" |
| `<type>` | Loại quảng cáo |
| `<screen>` | Màn hình **sở hữu** ad (nơi ad được hiển thị/trigger) |
| `<position>` | Vị trí trong màn hình (banner/native) hoặc `fullscreen` |
| `<action>` | Hành động trigger ads (`auto` nếu tự hiển thị) |

---

## Bảng Giá Trị Hợp Lệ

### `<type>` — Loại quảng cáo

| Giá trị | Mô tả |
|---------|-------|
| `appopen` | App Open Ads (splash, resume) |
| `interstitial` | Quảng cáo toàn màn hình |
| `rewarded` | Quảng cáo có thưởng |
| `rewardedInter` | Rewarded Interstitial |
| `banner` | Banner |
| `native` | Native ads |

### `<screen>` — Màn hình sở hữu

Tên màn hình ngắn gọn, ví dụ: `splash`, `resume`, `language`, `onboarding`, `home`, `settings`, `detail`, `result`, `editor`, `gallery`, `premium`, `category`...

> **Quy tắc**: ad **thuộc về màn hình hiển thị nó**, không phải màn hình mà nó dẫn đến. Khi 1 ad có thể trigger từ nhiều luồng vào, phân biệt bằng `<action>`.

### `<position>` — Vị trí

**Cho `interstitial`, `rewarded`, `rewardedInter`, `appopen`**: luôn là `fullscreen`.

**Cho `banner` và `native`**: vị trí thực tế trong màn:

| Giá trị | Mô tả |
|---------|-------|
| `top` | Trên cùng |
| `bottom` | Dưới cùng |
| `center` | Giữa màn |
| `cardX` (camelCase) | Trong card cụ thể (`cardPremium`, `cardFeatured`) |
| `listN` | Vị trí trong list (`list1`, `list3`, `list5`) |
| `dialog` | Trong dialog/popup |

### `<action>` — Hành động trigger

| Giá trị | Khi nào dùng |
|---------|-------------|
| `auto` | Ad tự hiển thị (mọi banner/native + appopen + inter tự show khi vào màn) |
| `back` | User nhấn back |
| `complete` | Hoàn thành luồng (xong onboarding, xong save, xong scan) |
| `save` | Trigger từ nút Save |
| `cancel` | Trigger từ nút Cancel |
| `clickX` (camelCase) | Click vào nút/item cụ thể (`clickSettings`, `clickPremium`, `clickShare`) |
| `unlockX` (camelCase) | Reward để unlock tính năng (`unlockTheme`, `unlockHd`) |
| `submit` | Submit form |

---

## Ví Dụ Mẫu

```
# App Open
nsp-appopen-splash-fullscreen-auto
nsp-appopen-resume-fullscreen-auto

# Interstitial — đặt theo MÀN HÌNH SỞ HỮU + HÀNH ĐỘNG
nsp-interstitial-language-fullscreen-complete       # Sau khi chọn xong ngôn ngữ
nsp-interstitial-onboarding-fullscreen-complete     # Sau khi xong onboarding
nsp-interstitial-home-fullscreen-back               # Khi back từ Home
nsp-interstitial-home-fullscreen-clickSettings      # Khi click nút Settings ở Home
nsp-interstitial-home-fullscreen-clickPremium       # Khi click nút Premium ở Home
nsp-interstitial-result-fullscreen-back             # Khi back từ Result
nsp-interstitial-editor-fullscreen-save             # Khi save trong Editor

# Rewarded
nsp-rewarded-settings-fullscreen-unlockTheme        # Reward để unlock theme ở Settings
nsp-rewarded-result-fullscreen-saveHd               # Reward để save HD ở Result
nsp-rewarded-editor-fullscreen-unlockEffect         # Reward unlock effect

# Banner
nsp-banner-home-bottom-auto                         # Banner dưới Home
nsp-banner-home-top-auto                            # Banner trên Home
nsp-banner-settings-bottom-auto
nsp-banner-onboarding-bottom-auto

# Native
nsp-native-home-top-auto
nsp-native-home-cardPremium-auto                    # Native trong card Premium ở Home
nsp-native-language-list3-auto                      # Native ở vị trí thứ 3 của list ngôn ngữ
nsp-native-onboarding-bottom-auto
```

---

## Anti-Patterns (Tên KHÔNG nên dùng)

| Tên xấu | Lý do | Sửa thành |
|---------|-------|-----------|
| `nsp_inter_settings` | Vào hay ra Settings? Trigger từ đâu? | `nsp-interstitial-home-fullscreen-clickSettings` |
| `nsp_inter_language` | Khi mở màn hay khi rời màn? | `nsp-interstitial-language-fullscreen-complete` |
| `nsp_bn_home` | Top hay bottom? | `nsp-banner-home-bottom-auto` |
| `nsp_native_1` | Native gì? Ở đâu? | `nsp-native-home-list1-auto` |
| `nsp-inter-home-back` | Thiếu position `fullscreen` | `nsp-interstitial-home-fullscreen-back` |
| `nsp-banner-home-bottom` | Thiếu action `auto` | `nsp-banner-home-bottom-auto` |
| `nsp-Interstitial-Home-...` | Không lowercase | `nsp-interstitial-home-...` |
| `nsp-interstitial-home-fullscreen-click_settings` | Dùng `_` thay vì camelCase | `nsp-interstitial-home-fullscreen-clickSettings` |

---

## Quy Trình Đặt Tên

1. **Xác định loại ads** → chọn `<type>`
2. **Xác định màn hình SỞ HỮU** (nơi ad sẽ hiển thị/trigger) → chọn `<screen>`
3. **Xác định vị trí**:
   - Inter/Reward/AppOpen → `fullscreen`
   - Banner/Native → vị trí thực (`top`, `bottom`, `cardX`, `listN`, ...)
4. **Xác định hành động trigger**:
   - Tự hiển thị → `auto`
   - Click nút → `clickX` (X = tên nút, camelCase)
   - Hoàn thành luồng → `complete`
   - Action khác → từ vựng phù hợp (`back`, `save`, `cancel`, `submit`, `unlockX`...)
5. **Ghép lại**: `nsp-<type>-<screen>-<position>-<action>`

---

## Validation

Có thể validate bằng regex:

```regex
^nsp-(appopen|interstitial|rewarded|rewardedInter|banner|native)-[a-z][a-zA-Z0-9]*-[a-z][a-zA-Z0-9]*-[a-z][a-zA-Z0-9]*$
```

> **Lưu ý**: Phải khớp giữa `nameSpace` trong `ads_config.json`, code (`NphAds.show*()`), và bảng MO.md. Không khớp → SDK trả `PlacementNotFound`.
