# MO Suggest — Chiến lược Monetization tối ưu cho Step Tracker

> Tài liệu đề xuất vị trí ads tối ưu, cân bằng giữa **doanh thu** (hoàn vốn trong 1-3 giờ sử dụng) và **trải nghiệm người dùng** (Health/Fitness app cần UX mượt hơn Entertainment).
>
> **Naming convention**: `nsp_<type>_<screen>_<position>`

---

## 1. Phân tích User Flow

```
App Launch → Splash (AppOpen) → [First Launch] Language → Home (Steps tab)
                                                              ↓
Home ←→ Activity Tab ←→ Report Tab ←→ Achievement Tab
  ↓          ↓
  ├→ Settings  ├→ Start/Stop Activity Tracking
  │            └→ Activity List → Activity Detail (BottomSheet / Full)
  └→ View Detail
```

**Thời gian trung bình mỗi session** (ước tính):
- Splash: 3-5s
- Home (Steps tab): 30-60s (check progress, view stats)
- Activity tracking: 15-60 phút (chạy bộ/đi bộ)
- Report tab: 20-40s (xem biểu đồ)
- Achievement tab: 30-60s (browse badges, xem rewarded)
- Settings: 15-30s
- **Tổng browsing session (không tracking)**: ~3-5 phút
- **Active tracking session**: 15-60+ phút (app background)

**Đặc điểm Health/Fitness app**:
- Sessions ngắn nhưng TẦN SUẤT cao (2-5 lần/ngày check steps)
- Retention tốt (daily habit)
- User quan tâm data → UX phải clean, ads KHÔNG được che data

---

## 2. Chiến lược: Balanced Revenue cho Health App

### Nguyên tắc

| Giai đoạn | Mục tiêu | Mật độ ads |
|-----------|----------|------------|
| **0-30 phút** (sessions 1-2) | Hoàn vốn CPI nhanh | **Trung bình-Cao** — interstitial tại transition, banner persistent |
| **30-180 phút** (sessions 3-10) | Tăng ARPU | **Trung bình** — giảm inter frequency, giữ banner, native blend |
| **>3 giờ** (retained users) | Giữ chân, LTV | **Thấp** — chủ yếu banner, rewarded tự nguyện |

### Nguyên tắc UX cho Health App

1. **KHÔNG BAO GIỜ** show interstitial khi user đang tracking activity (phá vỡ workout)
2. **KHÔNG** show ads che mất step counter/progress ring trên Home
3. **Banner PHẢI** ở vị trí KHÔNG overlap bottom navigation hoặc action buttons
4. **Native** nên blend vào content (giữa list items, không đè lên cards)
5. **Interstitial** chỉ tại **natural breaks** (chuyển tab, back từ detail, hoàn thành action)

---

## 3. Đề xuất Ad Placements

### 3.1 High-Value Touchpoints (Hoàn vốn nhanh)

| # | nameSpace | Type | Màn hình | Trigger | Lý do |
|---|-----------|------|----------|---------|-------|
| 1 | `nsp_ao_splash` | App Open | Splash | Cold start | **100% impression**. eCPM $5-15 |
| 2 | `nsp_ao_resume` | App Open | Resume | Quay lại app (>30s) | Multi-session revenue. Health app mở nhiều lần/ngày |
| 3 | `nsp_inter_language` | Interstitial | Language | Done (first launch) | **First-launch monetize** — chỉ 1 lần |
| 4 | `nsp_inter_main` | Interstitial | Home→Activity Tab | Chuyển tab Activity | **Natural break** — user đang browse, chưa tracking |
| 5 | `nsp_inter_activity_detail` | Interstitial | Activity Detail | Back | **Post-view** — user đã xem xong detail |
| 6 | `nsp_inter_settings` | Interstitial | Settings | Back | **Post-action** — user đã cấu hình xong |
| 7 | `nsp_reward_achievement` | Rewarded | Achievement | Tap unlocked badge | **Value exchange** — xem ad để claim reward. eCPM $15-30 |

### 3.2 Supporting Placements (Doanh thu bền vững)

| # | nameSpace | Type | Màn hình | Ghi chú |
|---|-----------|------|----------|---------|
| 8 | `nsp_bn_home_bottom` | Collapsible Banner | Home (Steps) | **Scroll cuối page** — không che step counter. Collapsible = user có thể đóng |
| 9 | `nsp_bn_report_bottom` | Banner (Adaptive) | Report Tab | **NEW** — dưới chart cards, không che data |
| 10 | `nsp_bn_settings_bottom` | Banner (Adaptive) | Settings | **NEW** — cuối page settings, không che buttons |
| 11 | `nsp_native_activity_list` | Native (Medium) | Activity List | **Inline** giữa items — blend tự nhiên |
| 12 | `nsp_native_language` | Native (Medium) | Language (first launch) | Giữa toolbar và list — đã triển khai |

### 3.3 Đề xuất mới (Tăng revenue)

| # | nameSpace | Type | Màn hình | Trigger | Lý do |
|---|-----------|------|----------|---------|-------|
| 13 | `nsp_inter_activity_complete` | Interstitial | Activity Tab | Stop tracking | **Post-workout** — natural completion moment |
| 14 | `nsp_reward_double_steps` | Rewarded | Home | Button "2x Steps Bonus" | **Opt-in** — user muốn bonus, sẵn lòng xem |
| 15 | `nsp_native_report` | Native (Small) | Report Tab | Inline dưới stats | **Blend** vào content khi user xem báo cáo |
| 16 | `nsp_inter_achievement_back` | Interstitial | Achievement | Back | **Post-browse** — user đã xem xong badges |

---

## 4. Vấn đề UX hiện tại cần sửa

### 4.1 Banner Home che BottomNavigation

**Vấn đề**: `bannerAdContainer` trong `fragment_steps.xml` đặt ở cuối LinearLayout trong NestedScrollView — khi banner load (collapsible_bottom), nó có thể bị BottomNavigation che phủ hoặc ngược lại banner che mất content.

**Fix**: Di chuyển banner ra ngoài ScrollView, đặt **TRÊN** BottomNavigationView với margin bottom phù hợp. Hoặc đảm bảo banner cuối cùng trong scroll content với `paddingBottom` đủ cho BottomNav.

### 4.2 Native Ad trong Language screen che list

**Vấn đề**: `native_ad_container` đặt giữa toolbar và RecyclerView. Khi native_medium load (~250dp), nó chiếm quá nhiều space → user thấy ít languages hơn, phải scroll nhiều.

**Fix**: Dùng `native_small` thay vì `native_medium` cho Language screen. Hoặc đặt native **sau** RecyclerView (cuối list, user scroll xuống mới thấy).

### 4.3 Native trong ActivityList chiếm quá nhiều space

**Vấn đề**: `nativeAdContainer` có `minHeight="200dp"` — luôn chiếm 200dp dù ad chưa load, đẩy list xuống.

**Fix**: Bỏ `minHeight`, dùng `visibility="gone"` mặc định. SDK sẽ set VISIBLE khi ad loaded.

### 4.4 Interstitial khi chuyển tab Activity

**Vấn đề**: User nhấn tab Activity → interstitial hiện → phải đóng ad → mới thấy tab. Nếu user muốn start tracking nhanh (đang chuẩn bị chạy), ad gây khó chịu.

**Fix**: Chỉ show interstitial mỗi `stepCount: 2` (mỗi 2 lần nhấn tab, không phải mỗi lần). Thêm `interval: 30` để tránh spam.

---

## 5. Bảng Ads Placement + Config tần suất

### 5.1 Toàn bộ placements — Config khuyến nghị

> **Chú thích trạng thái**: ✅ = đã triển khai, ⚠️ = cần thêm/sửa, 🆕 = chưa có

| # | nameSpace | Type | interval | stepCount | forceShow | isShowLoading | Status | Ghi chú |
|---|-----------|------|----------|-----------|-----------|---------------|--------|---------|
| 1 | `nsp_ao_splash` | AppOpen | 0 | 1 | false | true | ✅ | Cold start, 100% show |
| 2 | `nsp_ao_resume` | AppOpen | 30 | 1 | false | true | ✅ | 30s cooldown (đã có trong SDK) |
| 3 | `nsp_inter_language` | Interstitial | 0 | 1 | true | true | ✅ | First launch only |
| 4 | `nsp_inter_main` | Interstitial | **30** | **2** | false | true | ⚠️ FIX | Hiện: interval=25, stepCount=1 → quá aggresive. Đổi interval=30, stepCount=2 |
| 5 | `nsp_inter_activity_detail` | Interstitial | **30** | **2** | false | true | ⚠️ FIX | Tương tự #4, giảm spam |
| 6 | `nsp_inter_settings` | Interstitial | **30** | **2** | false | true | ⚠️ FIX | Tương tự |
| 7 | `nsp_reward_achievement` | Rewarded | 0 | 1 | false | true | ✅ | Opt-in, user tự chọn |
| 8 | `nsp_bn_home_bottom` | Collapsible | - | - | - | - | ✅ | Scroll cuối, collapsible |
| 9 | `nsp_bn_report_bottom` | Adaptive | - | - | - | - | 🆕 NEW | Dưới charts |
| 10 | `nsp_bn_settings_bottom` | Adaptive | - | - | - | - | 🆕 NEW | Cuối settings |
| 11 | `nsp_native_activity_list` | Native Medium | - | - | - | - | ✅ | Inline giữa list |
| 12 | `nsp_native_language` | Native Small | - | - | - | - | ⚠️ FIX | Đổi native_medium → native_small |
| 13 | `nsp_inter_activity_complete` | Interstitial | 0 | 1 | false | true | 🆕 NEW | Sau stop tracking |
| 14 | `nsp_reward_double_steps` | Rewarded | 0 | 1 | false | true | 🆕 NEW | Opt-in bonus |
| 15 | `nsp_native_report` | Native Small | - | - | - | - | 🆕 NEW | Report tab inline |
| 16 | `nsp_inter_achievement_back` | Interstitial | 30 | 2 | false | true | 🆕 NEW | Back từ Achievement |

### 5.2 Chiến lược Preload — Vị trí & Thời điểm

| # | Placement | Preload? | Vị trí gọi `preload()` | Lý do |
|---|-----------|----------|------------------------|-------|
| 1 | Splash AppOpen | ✅ | SDK auto (cold start) | Luôn có sẵn |
| 2 | Resume AppOpen | ✅ | SDK auto (onResume) | Luôn có sẵn |
| 3 | Language Inter | ✅ | `LanguageActivity.onCreate` | Preload while browsing languages |
| 4 | Main Inter | ✅ | `MainActivity.onCreate` | Ready khi user chuyển tab |
| 5 | Activity Detail Inter | ✅ | `MainActivity.onCreate` | Ready khi user mở detail |
| 6 | Settings Inter | ✅ | `MainActivity.onCreate` | Ready khi user vào settings |
| 7 | Reward Achievement | ❌ | — | Rewarded auto-fetches khi show |
| 8 | Home Banner | ❌ | — | Auto-load khi container visible |
| 9-10 | Report/Settings Banner | ❌ | — | Auto-load |
| 11-12 | Native ads | ✅ | `MainActivity.onCreate` | Preload native, bind instant khi screen mở |
| 13 | Activity Complete Inter | ✅ | `ActivityFragment.startTracking()` | Preload khi bắt đầu track → ready khi stop |
| 14 | Reward Double Steps | ❌ | — | Rewarded auto-fetches |
| 15 | Native Report | ✅ | `MainActivity.onCreate` | Ready instant khi tab Report mở |
| 16 | Achievement Back Inter | ✅ | Tab Achievement chọn | Ready khi user back |

---

## 6. Ước tính Revenue per User Session

### Giả định

| Metric | Giá trị | Ghi chú |
|--------|---------|---------|
| eCPM App Open | $8 | Health/Fitness, average tier |
| eCPM Interstitial | $5 | Average Tier 1+2 |
| eCPM Rewarded | $20 | Opt-in = premium eCPM |
| eCPM Banner | $1.5 | Health app = higher engagement |
| eCPM Native | $3 | Blend vào content |

### Session trung bình (5 phút browsing, 2-3 sessions/ngày)

| Placement | Imp/session | Revenue |
|-----------|-------------|---------|
| App Open Splash | 1 | $0.008 |
| App Open Resume | 1 | $0.008 |
| Interstitial (main + detail + settings + achievement) | ~2 | $0.010 |
| Interstitial Activity Complete | 0.5 | $0.003 |
| Rewarded (achievement) | 0.5 | $0.010 |
| Rewarded (double steps) | 0.3 | $0.006 |
| Banner (home + report + settings) | ~5 min view | $0.004 |
| Native (activity list + report) | ~2 | $0.006 |
| **Tổng session** | | **~$0.055** |

### Day 1 (5 sessions check-in)

| | Revenue |
|---|---------|
| Session 1 | $0.055 |
| Session 2 | $0.050 |
| Session 3 | $0.045 |
| Session 4 | $0.040 |
| Session 5 | $0.035 |
| **Day 1 ARPU** | **~$0.225** |

### Break-even

| Thị trường | CPI | Day 1 ARPU | Break-even | D7 LTV (50% ret.) |
|------------|-----|------------|------------|---------------------|
| VN | $0.10-0.20 | $0.225 | **D1** | $0.60 |
| SEA (PH, ID, TH) | $0.20-0.50 | $0.225 | **D1-D2** | $0.60 |
| LATAM | $0.40-0.80 | $0.225 | **D2-D4** | $0.60 |
| Tier 1 | $1.00-2.00 | $0.45* | **D3-D7** | $1.20* |

> *Tier 1 eCPM cao hơn ~2x → ARPU thực tế cao hơn.
> Health app có **retention rất tốt** (40-50% D7) → LTV dài hạn cao.

---

## 7. A/B Testing Roadmap

| Test | Biến | Metric | Thời gian |
|------|------|--------|-----------|
| **T1**: Main tab interstitial | `stepCount: 1` vs `2` vs `3` | D1 retention, sessions/day | 2 tuần |
| **T2**: Resume interval | `interval: 30` vs `60` | Sessions/day, ARPU | 2 tuần |
| **T3**: Double Steps rewarded | Button visible vs hidden | Rewarded engagement rate | 2 tuần |
| **T4**: Activity Complete inter | `isEnable: true` vs `false` | Tracking sessions/day | 2 tuần |
| **T5**: Native placement | Activity List vs Report vs Both | CTR, eCPM | 3 tuần |

---

## 8. Tổng hợp Priority

| Priority | Action | Impact | Effort |
|----------|--------|--------|--------|
| **P0** | Fix UX: banner/native không che UI | Giảm uninstall | Low (layout changes) |
| **P0** | Fix config: `interval=30, stepCount=2` cho back interstitials | Giảm churn | Config-only |
| **P1** | Thêm `nsp_bn_report_bottom` + `nsp_bn_settings_bottom` | +$0.003/session passive | Layout + code |
| **P1** | Thêm `nsp_inter_activity_complete` | +$0.003/session | Code trigger |
| **P1** | Thêm `nsp_reward_double_steps` | +$0.006/session (opt-in) | UI + code |
| **P2** | Thêm `nsp_native_report` | +$0.003/session | Layout + code |
| **P2** | Thêm `nsp_inter_achievement_back` | +$0.003/session | Code trigger |
| **P3** | Đổi Language native → `native_small` | UX improvement | Config change |
