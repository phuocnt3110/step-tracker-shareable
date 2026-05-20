# Monetization Spec - Step Tracker

## App Structure

| Screen | Type | Description |
|--------|------|-------------|
| SplashActivity | Launcher | App startup with logo, auto navigate |
| MainActivity | Container | Bottom navigation with 4 tabs |
| StepsFragment (Home) | Main | Daily steps, charts, recent activity |
| ActivityFragment | Tab | Activity tracking, live map |
| AchievementFragment | Tab | Achievements list with unlock |
| ActivityListActivity | Child | Full list of activities |
| ActivityDetailActivity | Child | Activity detail view |
| SettingsActivity | Child | App settings |

## Ad Placements

| nameSpace | Màn hình / Trigger | Loại | interval | stepCount | Status |
|---|---|---|---|---|---|
| nsp_ao_splash | App khởi động (Splash) | App Open | - | - | ✅ Implemented |
| nsp_ao_resume | Quay lại từ background | App Open Resume | - | - | ✅ Auto by SDK |
| nsp_inter_main | Tab switching in MainActivity | Interstitial | 25 | 1 | ✅ Implemented |
| nsp_inter_activity_detail | Back from ActivityDetail | Interstitial | 25 | 1 | ✅ Implemented |
| nsp_inter_settings | Back from Settings | Interstitial | 25 | 1 | ✅ Implemented |
| nsp_reward_achievement | Unlock achievement | Rewarded | - | - | ✅ Implemented |
| nsp_bn_home_bottom | StepsFragment - bottom | Banner | - | - | ✅ Implemented |
| nsp_native_activity_list | ActivityListActivity | Native | - | - | ✅ Implemented |

## Notes
- App Open Resume (nsp_ao_resume) tự động bởi SDK khi `resumeAds` được cấu hình
- Banner ở Home (StepsFragment) sử dụng banner_adaptive
- Native ở ActivityList sử dụng native_medium
- Rewarded chỉ hiển thị khi user click để unlock achievement
