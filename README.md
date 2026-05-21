# Step Tracker

A native Android step tracking and fitness app built with Kotlin and modern Android components.

## Features

- Real-time step counting using device sensors
- Activity tracking (walking, running, cycling, hiking) with GPS
- Daily, weekly, monthly, and yearly reports with charts
- Achievement system with progress tracking
- Weight tracking with line charts
- Multi-language support (13 languages)
- Customizable daily step goals
- Push notifications for milestones
- Dark/light theme support

## Tech Stack

- **Language:** Kotlin
- **Min SDK:** 26
- **Architecture:** Single Activity + Fragments
- **Database:** Room
- **Sensors:** Step Counter, Accelerometer, GPS
- **Charts:** MPAndroidChart
- **Location:** Google Play Services Location
- **Coroutines:** Kotlin Coroutines + Flow
- **UI:** Material Design 3, ViewBinding

## Getting Started

```bash
git clone https://github.com/phuocnt3110/step-tracker-shareable.git
cd step-tracker-shareable
./gradlew assembleDebug
```

## Project Structure

```
app/src/main/java/com/steptracker/nativeapp/
├── StepTrackerApplication.kt      # Application class
├── data/
│   ├── AppDatabase.kt             # Room database
│   ├── DataRepository.kt          # Data repository
│   ├── DailyData.kt               # Daily step data model
│   ├── ActivityRecord.kt          # Activity tracking record
│   ├── Achievement.kt             # Achievement model
│   └── UserSettings.kt            # User preferences
├── sensor/
│   ├── StepCounterManager.kt      # Step sensor manager
│   ├── ActivityTrackingService.kt  # Foreground service for GPS tracking
│   ├── LocationTracker.kt         # GPS location tracking
│   └── DailyResetReceiver.kt      # Daily midnight reset
├── ui/
│   ├── SplashActivity.kt          # Splash screen
│   ├── MainActivity.kt            # Main screen with bottom navigation
│   ├── StepsFragment.kt           # Home - step counter & charts
│   ├── ActivityFragment.kt        # Activity tracking (start/stop)
│   ├── ReportFragment.kt          # Statistics & reports
│   ├── AchievementFragment.kt     # Achievements & badges
│   ├── SettingsActivity.kt        # User settings
│   ├── ActivityListActivity.kt    # Activity history list
│   ├── ActivityDetailActivity.kt  # Activity detail view
│   └── language/                   # Language selection
└── util/
    └── LanguageUtil.kt            # Locale management
```

## Permissions

- `ACTIVITY_RECOGNITION` — Step counting
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` — GPS activity tracking
- `FOREGROUND_SERVICE` — Background activity tracking
- `POST_NOTIFICATIONS` — Milestone notifications

## License

This project is provided for educational and demonstration purposes.
