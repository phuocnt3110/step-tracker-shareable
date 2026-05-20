# Preload Map — Step Tracker

## Preload Flow Diagram

```
┌──────────────────┐   preload: INTER_MAIN, INTER_SETTINGS,
│  SplashActivity  │            INTER_ACTIVITY_DETAIL, NATIVE_ACTIVITY_LIST
└────────┬─────────┘
         │ showSplash → navigateToMain
         ▼
┌──────────────────┐   preload all interstitials + native in onCreate()
│  MainActivity    │──────────────────────────────────────────────────────┐
│  (StepsFragment) │                                                      │
└────────┬─────────┘                                                      ▼
         │                                                    ┌───────────────────────┐
         │ tab switch (Activity tab)                          │ Pre-cached ads:       │
         │ → show INTER_MAIN                                  │ • INTER_MAIN          │
         │                                                    │ • INTER_SETTINGS      │
         ├──→ click Settings                                  │ • INTER_ACTIVITY_DETAIL│
         │    → startActivity(SettingsActivity)               │ • NATIVE_ACTIVITY_LIST│
         │    → back: show INTER_SETTINGS                     └───────────────────────┘
         │
         ├──→ click View All Activities
         │    → startActivity(ActivityListActivity)
         │    → loadNativeInto (uses preloaded NATIVE_ACTIVITY_LIST)
         │    → back: show INTER_MAIN
         │
         └──→ click View Activity Detail
              → startActivity(ActivityDetailActivity)
              → back: show INTER_ACTIVITY_DETAIL
```

## Preload Table

| Preload Location | nameSpace | Show Location | Ad Type |
|---|---|---|---|
| MainActivity.onCreate | `nsp_inter_main` | Tab switch (Activity tab), ActivityListActivity back | Interstitial |
| MainActivity.onCreate | `nsp_inter_settings` | SettingsActivity back | Interstitial |
| MainActivity.onCreate | `nsp_inter_activity_detail` | ActivityDetailActivity back | Interstitial |
| MainActivity.onCreate | `nsp_native_activity_list` | ActivityListActivity (inline) | Native |

## Notes

- Banner (`nsp_bn_home_bottom`) — load inline via `loadBannerInto()`, no preload needed
- Splash (`nsp_ao_splash`) — SDK handles internally
- Resume (`nsp_ao_resume`) — SDK handles internally
- Rewarded (`nsp_reward_achievement`) — shown on user action (unlock achievement), no preload (low frequency)
- All preloads happen in `MainActivity.onCreate()` since it's the container for all navigation
