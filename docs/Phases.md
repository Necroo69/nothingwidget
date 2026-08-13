NothingWidget — Development Phases
Each phase is independently buildable and shippable. Based on actual codebase state.
Phase 0 — Triage & Critical Fixes
Objective: Make the existing app not broken. Fix all Play Store blockers. 
Est. time: 2–3 days
Problems being fixed
These were found by reading every source file in the repository:
Bug
File
Fix
Package ID is 
com.example.nothingwidget
app/build.gradle.kts
Change to 
com.sritam.nothingwidget
 (or chosen ID)
R8 disabled in release
app/build.gradle.kts
isMinifyEnabled = true
, remove 
optimization { enable = false }
targetSdk = 34
 vs 
compileSdk = 37
app/build.gradle.kts
Set both to 35
updatePeriodMillis=60000
 silently capped
widget_clock_info.xml
Set to 0; add AlarmManager
No BOOT_COMPLETED receiver
AndroidManifest.xml
Add 
BootReceiver.kt
 + register
Dead "Terminal" nav tab
BottomNavBar.kt
Remove or implement
Weather widget hardcodes "24°"
WeatherWidget.kt
Add placeholder state or real data
Theme is 
Theme.Material.Light.NoActionBar
 in XML
themes.xml
Change to 
Theme.Material3
Icons.Default.ArrowBack
 deprecated
TopAppBar.kt
Use 
Icons.AutoMirrored.Filled.ArrowBack
com.example
 in 
colors.xml
 (purple_200 etc.)
colors.xml
Remove unused default colors
Deliverables
app/build.gradle.kts
 — fixed package, R8 on, SDKs aligned
BootReceiver.kt
 — new file, schedules AlarmManager on boot
ClockUpdater.kt
 — AlarmManager scheduling logic extracted
widget_clock_info.xml
 — updatePeriodMillis=0, targetCellWidth/Height added
themes.xml
 — fixed parent theme
BottomNavBar.kt
 — dead tab removed
TopAppBar.kt
 — ArrowBack → AutoMirrored
Definition of Done
App builds release APK with R8 enabled, < 8MB
App package ID is not 
com.example.*
Clock widget updates every minute after boot
No compiler warnings in the fixed files
NavGraph.kt
 — extracted from 
MainActivity
Update all 
navController.navigate(...)
 calls
Files Created/Modified
app/src/main/java/com/sritam/nothingwidget/
├── NothingWidgetApp.kt             ← NEW
├── di/
│   └── AppModule.kt               ← NEW
├── data/
│   ├── datastore/
│   │   └── WidgetPreferencesDataStore.kt  ← NEW
│   ├── model/
│   │   └── WidgetConfig.kt        ← NEW
│   └── repository/
│       ├── WidgetRepository.kt    ← NEW
│       └── WidgetRepositoryImpl.kt  ← NEW
├── ui/
│   ├── navigation/
│   │   ├── Screen.kt              ← NEW
│   │   └── NavGraph.kt            ← NEW (extracted from MainActivity)
│   └── screens/
│       ├── home/HomeViewModel.kt  ← NEW
│       ├── customize/CustomizeViewModel.kt  ← NEW
│       └── settings/SettingsViewModel.kt    ← NEW
└── MainActivity.kt                ← MODIFIED (add @AndroidEntryPoint, use NavGraph)
## Phase 1 — Architecture Foundation (✅ DONE)
**Objective**: Introduce MVVM, DataStore, and Hilt so all future development is clean.

### Post-Phase 1 Major Architectural Migration (Repo 2 Integration) (✅ DONE)
The app has successfully transitioned to the Google AI Studio design (Repo 2 UI). The following changes were implemented and successfully built:
1. **Removed Repo 1 UI Architecture**: Deleted the old UI components, screens, navigation, and theme files.
2. **Integrated Repo 2 Architecture**: Copied over the `data`, `domain`, `ui`, and `widget` layers from Repo 2.
3. **Strict Visual Parity Sync**: Addressed and reversed minor local UI deviations to strictly enforce 100% visual and layout parity with Repo 2 (e.g. `enableEdgeToEdge()` in `MainActivity.kt`, and matched padding rules).
4. **Database Migration**: Replaced the legacy `DataStore` based repository with the new `Room` database implementation (`AppDatabase`, `WidgetConfigDao`, etc.).
5. **Dependency Resolution**: Added `Room`, `Coil`, `Retrofit`, and `Moshi` dependencies. Switched from KSP to KAPT for Room compiler to fix JVM signature conflicts. Resolved metadata version conflicts by updating Room to `2.7.0`.
6. **Cleaned Leftover Artifacts**: Purged old Repo 1 files like `WidgetRepositoryImpl.kt` and `AppModule.kt` that were causing compilation issues.

### Post-Phase 1 Bug Fixes (Automated Codebase Scan)
The following issues were identified and immediately fixed:
1. **Deprecated Logout Icon**: `SettingsScreen.kt` used `Icons.Filled.Logout`. Fixed by removing the entire vestigial `ACCOUNT` section as requested by Phase 3 design goals.
2. **Deprecated API usage**: `Theme.kt` used `window.statusBarColor` which is deprecated in Java. Added `@Suppress("DEPRECATION")` to safely suppress it since edge-to-edge migration isn't necessary yet.
3. **Unresolved TODO**: `HomeScreen.kt` had a `TODO` for the FAB `onClick`. Fixed by pointing it to `Screen.Customize("clock").createRoute()` as a placeholder.
4. **Dead UI Code**: `HomeScreen.kt` contained a dead decorative "FIRMWARE" row. Removed to match Phase 4 design goals.
5. **XML TODO**: Removed an auto-generated Android Studio `TODO` inside `data_extraction_rules.xml`.

### Post-Phase 1 State Persistence Bug Fixes
The following critical state management and persistence bugs were identified and fixed:
1. **Widget Overwrite Bug (Bug 1)**: Fixed an issue in `WidgetRepository.allConfigs` where saving a single customized widget to the Room database would cause all other unedited preset widgets to disappear from the gallery. The repository now correctly merges default presets with saved entities using `associateBy { it.id }`.
2. **Missing Widgets on App Restart (Bug 2)**: Resolved by the same `WidgetRepository` fix. Since the initial database is empty on a fresh install or restart (if no edits were made), the repository now correctly falls back to generating the default preset list instead of returning an empty list.
3. **Step Tracker Flow Bug**: Fixed an issue in `StepTrackerRepository` where calling `incrementSteps()` attempted to synchronously evaluate a Room database `Flow`, resulting in broken step increment logic. Introduced `getStepsForDateSync()` in `StepDao` to correctly read the current step state.
4. **Direct Widget Pinning Fix**: Updated the pinning logic in `WidgetGalleryScreen.kt` (`requestPinWidget`) to pass the explicit `Class<*>` via `Class.forName()` to `ComponentName`, fixing a bug where Android's native pinning flow was failing to trigger or bypassing the direct pin overlay.

### Definition of Done
App compiles with Hilt, no SharedPreferences anywhere
Each screen observes ViewModel StateFlow
Navigation uses typed routes
No business logic inside any Composable
## Phase 2 — Widget Engine (✅ DONE)
Objective: Make widgets actually work reliably — correct data, correct update timing, survive reboot. 
Est. time: 3–5 days
Tasks
2.1 Clock Widget — true per-minute updates
ClockUpdateReceiver.kt
 — BroadcastReceiver triggered by AlarmManager
ClockUpdater.kt
 — schedules 
setExactAndAllowWhileIdle
 at next minute boundary
ClockWidget.onUpdate()
 — reads config from DataStore via coroutine, builds RemoteViews with correct time string AND color
2.2 Date Widget — midnight updates
DateUpdateReceiver.kt
 — fires at midnight, reschedules for next midnight
DateWidget.onUpdate()
 — formats date per user's chosen format, sets text
2.3 Battery Widget — polish existing
Extract duplicated battery-reading logic into 
BatteryRepository
Add 
ACTION_POWER_CONNECTED
 / 
DISCONNECTED
 handling for charging indicator
Remove 
savedStyle
 variable that's read but never used
2.4 Boot Receiver
BootReceiver.kt
 — on 
BOOT_COMPLETED
:
ClockUpdater.schedule(context)
DateUpdater.schedule(context)
Enqueue WeatherWorker if weather widget is placed
2.5 Widget Info XML updates
All 4 widgets: add 
targetCellWidth
, 
targetCellHeight
, 
previewLayout
, 
description
Clock/Date: 
updatePeriodMillis="0"
Battery: 
updatePeriodMillis="0"
 (event-driven)
Weather: 
updatePeriodMillis="0"
 (WorkManager-driven)
2.6 goAsync() in all onUpdate() methods
Wrap DataStore reads in 
goAsync()
 + coroutine scope
Definition of Done
Clock widget shows correct time within 1 second of opening home screen
Clock widget updates at :00 of every minute (verified by watching for 3 minutes)
After 
adb shell reboot
, all widgets are present and showing correct data within 60s
Battery widget shows correct % matching Android status bar
## Phase 3 — Settings Persistence (✅ DONE)
Objective: Make the Settings screen actually save and apply settings. 
Est. time: 1–2 days
Tasks
SettingsViewModel reads/writes AppPreferencesDataStore
"WIDGET REFRESH RATE" selection actually persists and is read by WorkManager interval
"DOT-MATRIX INTENSITY" slider persists and affects the Canvas in CustomizeScreen
"THEME" selection persists (dark only for now, extended in Phase 5)
"LOG OUT" — remove (there's no account system; this is vestigial UI)
"SYNC DATA" — remove or implement as "Force refresh all widgets"
Settings screen no longer shows "ONLINE" / "DEVICE STATUS" hardcoded hero — replace with real device/app info (version name, widget count, etc.)
Definition of Done
Killing and relaunching the app shows the same Settings values
Refresh rate change reflects in actual WorkManager periodic interval
## Phase 4 — Home Screen Polish (✅ DONE)
Objective: Make the Home/Gallery screen show real live data previews, not hardcoded values. 
Est. time: 2 days
Tasks
Clock card preview: already uses `LaunchedEffect + delay(500)` ✅ — keep this
Date card preview: hardcoded "MON 28 JULY" -> replace with `LocalDate.now()` formatted
Battery card preview: hardcoded "87%" -> replace with real battery level via `BatteryRepository`
Weather card preview: hardcoded "CLEAR SKY" -> show placeholder "-- °" until data available
FAB ("+" button): implement — navigate to a widget picker or show a bottom sheet for choosing which widget to add
"FIRMWARE_V2.0.4 / DOWNLOAD" row at bottom — remove (decorative, confusing to users)
Definition of Done
Every widget card on Home screen shows live data
No hardcoded date/percentage visible to user
FAB does something useful
## Phase 5 — Customization Persistence & Apply (✅ DONE)
Objective: Make the Customize screen actually save settings and apply them to real widgets. 
Est. time: 2–3 days
Tasks
CustomizeViewModel.loadConfig(widgetType) reads from DataStore on screen entry
All option selectors (size, style, color, font, background) update ViewModel state
Live preview reacts to ViewModel state changes (already visually OK — just needs to read from VM)
"ADD" button -> `viewModel.applyToWidget()`:
Saves to DataStore
Calls `AppWidgetManager.updateAppWidget()` with new RemoteViews built from saved config
If `requestPinAppWidget` succeeds -> toast "Widget added to home screen"
If `requestPinAppWidget` not supported -> show a dialog: "Long-press your home screen -> Widgets -> NothingWidget -> [widget name]"
Settings per widget must be keyed by 
appWidgetId
 (Int), not just 
widgetType
 (String) — allows multiple instances with different configs
Definition of Done
Change color to red, tap ADD → widget on home screen shows red text within 3 seconds
Place same widget type twice → each instance can have different colors independently
Kill app, reopen Customize → previous selections are pre-filled
## Phase 6 — Onboarding (✅ DONE)
Objective: New users understand how to use the app in < 30 seconds. 
Est. time: 1 day
Tasks
OnboardingScreen.kt
 — shown on first launch only (tracked via DataStore boolean)
3 steps: (1) See your widgets → (2) Customize them → (3) Add to home screen
Step 3 shows illustrated instruction: long-press home → Widgets → NothingWidget
"GET STARTED" button marks onboarding complete and navigates to Home
Definition of Done
First launch shows onboarding
Subsequent launches skip onboarding
User can re-trigger onboarding from Settings → "How to add widgets"
## Phase 7 — Weather Widget (Real Data) (✅ DONE)
Objective: WeatherWidget shows actual weather using OpenMeteo (no API key required). 
Est. time: 3 days
Tasks
Add Retrofit + Kotlin Serialization to dependencies
OpenMeteoApi.kt
 — Retrofit interface for current weather endpoint
WeatherRepository.kt
 — fetches, caches last known weather to DataStore
WeatherWorker.kt
 — WorkManager PeriodicWorkRequest (15 min interval minimum)
Request 
ACCESS_COARSE_LOCATION
 permission in app for location
Show "Grant Location" prompt in WeatherWidget customize screen if not granted
WeatherWidget shows real temperature + WMO weather code → icon mapping
Graceful offline state: show last known weather with timestamp
Definition of Done
Weather widget shows real temperature within 2 minutes of placement on fresh install
Widget shows cached data when offline, with "last updated X min ago" label
Unit toggle (°C/°F) immediately applies to displayed temperature
## Phase 8: Design Polish & Accessibility (✅ DONE)
Objective: Every pixel is intentional. Every element is accessible. 
Est. time: 2–3 days
Tasks
Move all inline 
Color(0xFF1A1A1A)
 references to theme/Color.kt named constants
Extract all 9+ 
RectangleShape
 references to a named 
SharpShape
 constant
Add 
contentDescription
 to all icons and widget TextViews
Verify color contrast ratios (AccentRed on dark background)
Add 
semantics { }
 blocks to custom interactive components (SharpToggle, BottomNavItem)
Add touch target sizing (
minimumInteractiveComponentSize
)
Test with TalkBack enabled on a physical device
Light theme implementation (light color scheme in 
Theme.kt
)
In-app theme toggle (DARK / LIGHT / SYSTEM) in Settings
Definition of Done
Zero accessibility warnings in Android Studio
App is fully navigable with TalkBack
Light mode looks as good as dark mode
All touch targets ≥ 48dp
## Phase 9 — Testing (🚧 IN PROGRESS — unit tests done)
Objective: Build confidence for Play Store release. 
Est. time: 3–4 days

### Unit tests (✅ DONE)
Replaced the placeholder `ExampleUnitTest.kt` with 25 host-side JVM unit tests
(run via `./gradlew testDebugUnitTest`, all passing):
- `WidgetCustomizerViewModelTest` — load/update accent color, toggle monochrome,
  save persists to repo + fires callback, no-op guards before a config is loaded
  (actual class is `WidgetCustomizerViewModel`, not `CustomizeViewModel`)
- `WidgetRepositoryTest` — defaults returned on empty DB, saved config overrides
  matching default without dropping others (BUG-01 regression guard), custom
  studio widgets appended, `getConfigById` save/fallback/null paths
  (actual class is `WidgetRepository`, not `WidgetRepositoryImpl`)
- `WeatherCodeMapperTest` — WMO code → `WeatherCondition` for every branch
  (replaces the planned `DateFormatterTest`; date is rendered by `TextClock`,
  so there is no formatter class to unit test)
- `BatteryCalculationTest` — level/scale → percentage edge cases incl.
  non-100 scale, unavailable values, divide-by-zero guard

Supporting refactors (remove duplication, make logic testable):
- Extracted `WeatherCondition.fromWmoCode()` — single source of truth previously
  duplicated in `WeatherRepository` and `WeatherWidget`
- Extracted `BatteryRepository.batteryPercentage(level, scale)` pure function
- Added `MainDispatcherRule` + `FakeWidgetConfigDao` test helpers
- Added `kotlinx-coroutines-test` test dependency

### Instrumented tests (⬜ TODO — require a device/emulator)
Add instrumented tests:
HomeScreenTest
 — all 4 widget cards visible
CustomizeScreenTest
 — bottom sheet opens, size selection updates preview
NavigationTest
 — back navigation works correctly
Manual device testing matrix:
Pixel 6 (Android 13)
Samsung Galaxy (One UI — different launcher)
Any device with MIUI/Xiaomi (aggressive battery management)
Android 8.0 (minSdk = 26 compliance check)
Definition of Done
Unit test coverage ≥ 50% on ViewModel and Repository classes
All instrumented tests pass
Zero ANR in 1-hour soak test
Widget verified updating on Samsung, Pixel launchers
Phase 10 — Play Store Release
Objective: Submit to Play Store successfully. 
Est. time: 1–2 days
Tasks
Release keystore generation + storage (NEVER commit to git)
signingConfigs
 in 
build.gradle.kts
 reading from 
local.properties
Build signed release AAB (
./gradlew bundleRelease
)
Verify APK size < 8MB
Write Play Store listing:
App name: "NothingWidget — Minimal Widgets"
Short description (80 chars)
Full description
Create assets:
Feature graphic (1024×500)
App icon (512×512, PNG, no alpha)
Minimum 4 phone screenshots
Complete Data Safety form (no data collected for core)
Complete content rating questionnaire
Add privacy policy URL to listing
Submit for review (expect 3–7 days)
Definition of Done
App approved and live on Play Store
Version 1.0, versionCode 1
Rating shown (any rating counts as success)