# Namespace Registry — Step Tracker

## Ad Namespace Constants

File: `app/src/main/java/com/steptracker/nativeapp/ui/AdNamespaces.kt`

| Constant | Value | Type | Screen / Trigger |
|----------|-------|------|-----------------|
| `INTER_MAIN` | `nsp_inter_main` | Interstitial | Tab switch to Activity, back from ActivityList |
| `INTER_ACTIVITY_DETAIL` | `nsp_inter_activity_detail` | Interstitial | Back from ActivityDetailActivity |
| `INTER_SETTINGS` | `nsp_inter_settings` | Interstitial | Back from SettingsActivity |
| `REWARD_ACHIEVEMENT` | `nsp_reward_achievement` | Rewarded | Unlock achievement (user-initiated) |
| `BANNER_HOME_BOTTOM` | `nsp_bn_home_bottom` | Banner | StepsFragment bottom (collapsible) |
| `NATIVE_ACTIVITY_LIST` | `nsp_native_activity_list` | Native | ActivityListActivity top |

## System-managed (no code needed)

| nameSpace | Type | Trigger |
|-----------|------|---------|
| `nsp_ao_splash` | App Open | App cold start (SplashActivity) |
| `nsp_ao_resume` | App Open Resume | Return from background |

## Usage Locations

| File | Namespaces Used |
|------|----------------|
| `SplashActivity.kt` | (splash — SDK managed) |
| `MainActivity.kt` | `INTER_MAIN` (show on tab switch), preload all |
| `StepsFragment.kt` | `BANNER_HOME_BOTTOM` (load banner) |
| `ActivityListActivity.kt` | `NATIVE_ACTIVITY_LIST` (load native), `INTER_MAIN` (back) |
| `ActivityDetailActivity.kt` | `INTER_ACTIVITY_DETAIL` (back) |
| `SettingsActivity.kt` | `INTER_SETTINGS` (back) |
| `AchievementFragment.kt` | `REWARD_ACHIEVEMENT` (unlock) |

## Rules

1. All namespace strings MUST be declared in `AdNamespaces.kt`
2. Code MUST use `AdNamespaces.CONSTANT` — never hardcoded strings
3. Every namespace in this registry MUST exist in `ads_config.json`
4. Every ad call in code MUST use a namespace from this registry
