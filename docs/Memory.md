NothingWidget — Project Memory File
Last updated: Based on complete code analysis of 
sritampattanaik/nothingwidget1
 (27 commits, main branch) This file is the single source of truth for any AI session picking up this project.
1. Project Identity
Key
Value
Repo
https://github.com/sritampattanaik/nothingwidget1
Package ID
com.example.nothingwidget
 ← MUST CHANGE before Play Store
App Name
nothingwidget (in strings.xml — lowercase, fix this)
Version
1.0 (versionCode=1, versionName="1.0")
Min SDK
26 (Android 8.0)
Compile SDK
37 ← keep at 37: core 1.19 / lifecycle 2.11 require it (lowering to 35 breaks assembleDebug — see 2026-10-02 notes)
Target SDK
35 ← does NOT need to match compileSdk
AGP
9.3.1
Kotlin
2.2.10
Compose BOM
2026.02.01
2. What This App Is
A home screen widget app for Android, inspired by Nothing OS aesthetics:
Space Mono
 monospace font (dot-matrix look)
Monochrome palette
 — 
#141313
 background, 
#FFFFFF
 text, 
#FF5540
 red accent
Sharp geometry
 — 
RectangleShape
 everywhere (no rounded corners)
4 widgets
: Clock, Date, Battery, Weather
The app has 3 screens: Home (widget gallery), Customize (per-widget settings), Settings (app preferences). Navigation: Compose Navigation with string routes. UI: Jetpack Compose + Material 3.
3. File Map (Every Source File)
app/src/main/
├── AndroidManifest.xml           — declares 4 widget receivers, 1 activity
├── java/com/example/nothingwidget/
│   ├── MainActivity.kt           — ComponentActivity, NavHost with 3 routes
│   ├── ui/
│   │   ├── components/
│   │   │   ├── BottomNavBar.kt   — 4 tabs: Home, Dashboard, Code(dead), Settings
│   │   │   ├── TopAppBar.kt      — Box-based, centered title, 56dp tall
│   │   │   └── WidgetCard.kt     — Border + title row + content slot
│   │   ├── screens/
│   │   │   ├── HomeScreen.kt     — LazyVerticalGrid of 4 widget cards
│   │   │   ├── CustomizeScreen.kt — BottomSheetScaffold, live preview, option selectors
│   │   │   └── SettingsScreen.kt — Sectioned list with SettingsRow + SharpToggle
│   │   └── theme/
│   │       ├── Color.kt          — 6 color constants
│   │       ├── Theme.kt          — Dark-only MaterialTheme
│   │       └── Type.kt           — SpaceMono + 3 text styles
│   └── widgets/
│       ├── ClockWidget.kt        — AppWidgetProvider, only sets textColor
│       ├── DateWidget.kt         — AppWidgetProvider, only sets textColor
│       ├── BatteryWidget.kt      — AppWidgetProvider, reads battery level ✅
│       └── WeatherWidget.kt      — AppWidgetProvider, hardcoded "24°"
├── res/
│   ├── layout/
│   │   ├── widget_clock.xml      — TextClock in LinearLayout, Space Mono Bold 48sp
│   │   ├── widget_date.xml       — TextClock in FrameLayout, 28sp, white bottom line
│   │   ├── widget_battery.xml    — TextView + ProgressBar, 36sp
│   │   └── widget_weather.xml    — 2 TextViews (temp + desc), 36sp + 12sp
│   ├── xml/
│   │   ├── widget_clock_info.xml  — minWidth=250dp, updatePeriodMillis=60000 ❌
│   │   ├── widget_date_info.xml   — minWidth=250dp, updatePeriodMillis=86400000
│   │   ├── widget_battery_info.xml — minWidth=130dp, updatePeriodMillis=1800000
│   │   └── widget_weather_info.xml — minWidth=130dp, updatePeriodMillis=1800000
│   ├── font/
│   │   ├── space_mono_regular.ttf
│   │   └── space_mono_bold.ttf
│   └── values/
│       ├── strings.xml           — only app_name
│       ├── colors.xml            — default Android colors (purple_200 etc.) NOT USED
│       └── themes.xml            — Theme.Material.Light.NoActionBar ❌ (wrong for dark app)
4. Completed Features (What Actually Works)
Feature
Status
Notes
Clock widget displays time
✅
Via 
TextClock
 XML, OS-driven
Clock widget color change
✅
setTextColor()
 called in onUpdate
Date widget displays date
✅
Via 
TextClock
 XML
Date widget color change
✅
setTextColor()
 called in onUpdate
Battery widget shows %
✅
Reads from 
ACTION_BATTERY_CHANGED
 intent
Battery widget updates on charge change
✅
onReceive
 handles 
ACTION_BATTERY_CHANGED
Battery widget color change
✅
setTextColor()
 called
Home screen widget gallery
✅
4 cards, correct visual style
Live clock preview in Home screen
✅
LaunchedEffect + delay(500)
 coroutine
Customize screen UI
✅
BottomSheetScaffold, good visual design
Option selectors (Size/Style/Color/Font/Background)
✅
UI complete, local state
Live customize preview
✅
Reacts to option selection in real-time
Navigate Home → Customize (with widgetType)
✅
Route 
customize/{widgetType}
Navigate to Settings
✅
Route 
settings
App icon (Nothing-style N + red dot)
✅
Adaptive icon implemented
Space Mono font bundled
✅
Both regular and bold TTF present
Material 3 theme (dark)
✅
Correct color scheme tokens
5. Known Bugs (From Code Reading)
#
Bug
Severity
File
Root Cause
BUG-01
Clock widget updates at most every 30 min, not every minute
🔴 Critical
widget_clock_info.xml
updatePeriodMillis=60000
 capped by Android to 1,800,000ms
BUG-02
Widgets do NOT survive device reboot
🔴 Critical
Missing
No 
BOOT_COMPLETED
 receiver, no AlarmManager rescheduling
BUG-03
Weather widget shows hardcoded "24° / CLEAR SKY" always
🔴 Critical
WeatherWidget.kt
No real data source
BUG-04
Customization settings NOT persisted after app kill
🟠 High
CustomizeScreen.kt
Local 
remember {}
 state, not saved to DataStore
BUG-05
Settings screen changes NOT persisted
🟠 High
SettingsScreen.kt
Local 
remember {}
 state only
BUG-06
"ADD" button fails silently on Samsung/MIUI launchers
🟠 High
CustomizeScreen.kt
requestPinAppWidget
 not supported everywhere, no fallback
BUG-07
Date widget shows hardcoded "MON 28 JULY" in HomeScreen preview
🟡 Medium
HomeScreen.kt
Static 
Text("28")
 + 
Text("MON")
 + 
Text("JULY")
BUG-08
Battery shows hardcoded "87%" in HomeScreen preview
🟡 Medium
HomeScreen.kt
Static 
Text("87%")
 not reading real battery
BUG-09
Dead "Terminal" tab in BottomNavBar
🟡 Medium
BottomNavBar.kt
onClick = { }
 — empty, navigates nowhere
BUG-10
Icons.Default.ArrowBack
 is deprecated
🟢 Low
TopAppBar.kt
Should use 
Icons.AutoMirrored.Filled.ArrowBack
BUG-11
Package ID is 
com.example.nothingwidget
🔴 Critical
app/build.gradle.kts
Play Store rejects com.example.*
BUG-12
R8 disabled in release
🔴 Critical
app/build.gradle.kts
optimization { enable = false }
BUG-13
targetSdk = 34
, 
compileSdk = 37
 — mismatch
🟠 High
app/build.gradle.kts
WRONG — do not lower compileSdk. Keep compileSdk = 37 and targetSdk = 35 (see 2026-10-02 notes)
BUG-14
Theme XML uses 
Theme.Material.Light.NoActionBar
🟡 Medium
themes.xml
Inconsistent with the dark Compose theme
BUG-15
colors.xml
 has unused Android default colors
🟢 Low
colors.xml
purple_200
, 
purple_500
 etc — dead weight
BUG-16
savedStyle
 in widget providers is read but never applied
🟡 Medium
ClockWidget.kt
, 
DateWidget.kt
Style read from SharedPrefs but nothing is done with it
BUG-17
material-icons-extended
 dependency has no version pinned
🟠 High
app/build.gradle.kts
implementation("androidx.compose.material:material-icons-extended")
 — no version
6. Technical Debt
Area
Debt
Effort to Fix
State management
No ViewModel, no StateFlow anywhere
Medium (Phase 1)
Persistence
SharedPreferences in Composables
Medium (Phase 1)
DI
No Hilt, no dependency injection
Medium (Phase 1)
Widget updates
No AlarmManager, no WorkManager
Medium (Phase 2)
Widget content
Style preference read but never applied to widgets
Low
Navigation
String literals, no type safety
Low (Phase 1)
Strings
Hardcoded UI strings throughout
Low (Phase 8)
Testing
2 autogenerated placeholder tests
Medium (Phase 9)
Package ID
com.example.*
Low-effort, immediate fix
R8
Disabled
Trivial fix
7. Architecture Decisions Made
Decision
Choice
Reason
UI framework
Jetpack Compose ✅
Modern, correct
Navigation
Navigation Compose ✅
Already in place
Widget rendering
RemoteViews XML ✅
Required for AppWidget
Font
Space Mono ✅
Nothing OS aesthetic
Clock update mechanism
Currently updatePeriodMillis ❌ → AlarmManager
Android caps min at 30 min
Persistence
SharedPreferences ❌ → DataStore
DataStore is the modern replacement
DI
None ❌ → Hilt
Standard Android DI
State
Local remember ❌ → StateFlow + ViewModel
Lifecycle-safe
Theme
Dark only → add Light + dynamic color
Accessibility, user preference
8. Coding Conventions (as found in repo)
Convention
Current Practice
File organization
ui/screens/
, 
ui/components/
, 
ui/theme/
, 
widgets/
 — clean ✅
Color naming
Named constants in 
Color.kt
 ✅ but inline hex also used
Typography
Typography.labelSmall
 etc. used ✅
Commit messages
Conventional Commits format (
feat:
, 
fix:
) ✅
Shape
androidx.compose.ui.graphics.RectangleShape
 inline — needs extraction
Imports
Wildcard 
import androidx.compose.foundation.layout.*
 — acceptable for Compose
9. Current Milestone
Phase 0: Triage
 (Not started — this analysis IS the prerequisite to Phase 0)
Next immediate actions (in order):
Change package ID in 
build.gradle.kts
Set targetSdk = 35 (done). Keep compileSdk = 37; do NOT lower it to match.
Enable R8 (
isMinifyEnabled = true
, remove 
optimization { enable = false }
)
Add 
BootReceiver.kt
 + register in Manifest
Add AlarmManager scheduling for ClockWidget (set 
updatePeriodMillis = 0
)
Remove dead Terminal nav tab
Fix 
Icons.Default.ArrowBack
 → 
AutoMirrored
10. Next Milestone
Phase 1: Architecture Foundation
Add Hilt
Add DataStore
Create ViewModels for all 3 screens
Migrate CustomizeScreen from SharedPreferences to DataStore-backed ViewModel
Type-safe navigation
11. Important Context for AI Sessions
What the AI MUST know before suggesting any code change:
This is a pure Compose project
 — no XML layouts in the app (only in widget RemoteViews)
No Hilt yet
 — don't assume DI is set up
No DataStore yet
 — don't assume any persistence infrastructure
Widget providers are in 
.widgets
 package
 — 4 classes: ClockWidget, DateWidget, BatteryWidget, WeatherWidget
Bottom sheet in CustomizeScreen uses 
BottomSheetScaffold
 (experimental API, 
@OptIn(ExperimentalMaterial3Api::class)
)
SharpToggle is a custom composable
 defined at the bottom of SettingsScreen.kt — not a Material component
WidgetCard takes a content slot
 (
BoxScope.() -> Unit
) — this is the pattern used for all gallery items
The Canvas dot-pattern background
 in CustomizeScreen is in the scaffold's 
content
 slot, not the sheet
AGP version is 9.3.1
 — very new, may have API differences from typical tutorials
Kotlin version is 2.2.10
 — very new, use Kotlin 2.x compatible syntax
Common mistakes to avoid:
Don't suggest 
SharedPreferences.Editor.commit()
 — this project should move to DataStore
Don't add 
@Composable
 to ViewModel methods
Don't suggest 
lifecycleOwner
 patterns — use 
collectAsStateWithLifecycle()
Don't use 
rememberCoroutineScope
 for ViewModel operations — use 
viewModelScope
Don't add 
INTERNET
 permission for clock/date/battery widgets — they must work offline
12. Future Improvements (Parking Lot)
Multiple instances of same widget with independent configs (key by 
appWidgetId
)
Lock screen widgets (Android 13+, API 33)
Music Now Playing widget (MediaSession)
Steps widget (HealthConnect)
Widget preset system (save/load named configs)
Setup export (share home screen layout)
Material You dynamic color (opt-in)
Tablet responsive layout (WindowSizeClass)
App widget preview image generation for Play Store screenshots
CI/CD with GitHub Actions (
.github/workflows/android.yml
)
Firebase Crashlytics for crash reporting
In-app review (Google Play In-App Review API)
Localization (app is English only right now)
13. Brutally Honest Scores
Category
Score
Reason
Architecture
2/10
No ViewModel, no Repository, no DI, SharedPreferences in Composables
UI Design
8/10
Genuinely beautiful, faithful to Nothing OS, Space Mono is perfect
UX
4/10
Dead nav tab, no onboarding, "ADD" fails silently, hardcoded data in previews
Performance
3/10
No goAsync, clock updates broken, no Doze handling
Maintainability
3/10
573-line CustomizeScreen, all state local, no tests
Scalability
2/10
Adding a 5th widget means copy-pasting the same patterns everywhere
Play Store Readiness
1/10
com.example package, R8 off, no onboarding, no privacy policy
Code Quality
4/10
Commits are clean, structure is logical, but logic is entirely in UI layer
Security
6/10
No network calls (yet), no sensitive data — clean by omission
Accessibility
2/10
No content descriptions, no TalkBack testing, no light mode
OVERALL
3.5/10
Strong visual foundation, broken as a product
What this project IS:
A beautiful, well-designed UI prototype that demonstrates strong aesthetic sense and Compose fundamentals.
What this project IS NOT:
A working Android widget app ready for production.
The gap:
The gap between "looks good in screenshots" and "works reliably on a stranger's phone" is where all the missing architecture lives. The good news: the hardest part (design decisions and visual execution) is done well. The missing parts (DataStore, AlarmManager, ViewModel) are well-documented patterns with clear implementation paths.
## 6. Maintenance Notes (2026-08-09)

### Fixed: WeatherWorker refresh broadcast could be ignored
- **Bug:** `WeatherWorker` sent `ACTION_APPWIDGET_UPDATE` to `WeatherWidget` without `AppWidgetManager.EXTRA_APPWIDGET_IDS`. `AppWidgetProvider` update broadcasts are ID-driven, so a broadcast without active widget IDs can be ignored and the periodic WorkManager job may complete without refreshing any placed weather widgets.
- **Fix:** `WeatherWorker` now resolves active weather widget IDs with `AppWidgetManager.getAppWidgetIds(ComponentName(...))` and calls `WeatherWidget.onUpdate(...)` directly only when at least one weather widget is placed. **Superseded on 2026-10-02:** calling `onUpdate` on a hand-made receiver crashed the app; see below.
- **Current limitation:** This does not add live weather networking. Until the Phase 7 weather data source is implemented, the widget intentionally renders a neutral unavailable state (`--°` / `WEATHER UNAVAILABLE`) instead of stale fake weather.

### Fixed: compile SDK drifted past target SDK
- **Bug:** `compileSdk` was set to `37` while the project target was `35` and the project memory already noted SDK 35 as the intended baseline. That can break local builds on machines with only the intended Android 35 platform installed.
- **Fix:** `compileSdk` is now aligned to `35` while keeping `targetSdk = 35`. **Reverted on 2026-10-02:** this change broke `assembleDebug`; see below.

## 7. Maintenance Notes (2026-10-02)

### Fixed: APK could not be built (compileSdk 35)
- **Bug:** The 2026-08-09 change lowered `compileSdk` from 37 to 35, following an outdated note in section 1. `androidx.core 1.19.0` and `lifecycle 2.11.0` require compileSdk 37, and `activity 1.13.0` requires 36. `:app:checkDebugAarMetadata` therefore failed with 9 issues and `assembleDebug` produced no APK. `compileDebugKotlin` alone still passed, which hid the failure.
- **Fix:** `compileSdk = 37` restored (`android-37.0` platform). `targetSdk` stays 35. compileSdk only chooses which APIs you compile against; targetSdk opts into runtime behaviour, and the two do not need to match.
- **Verify builds with `assembleDebug`, not just `compileDebugKotlin`.**

### Fixed: WeatherWorker crashed the app process
- **Bug:** The 2026-08-09 fix called `WeatherWidget().onUpdate(...)` on a receiver the worker created itself. `onUpdate` calls `goAsync()`, which returns `null` when no broadcast is being delivered (AOSP returns `mPendingResult`, set only by the system). `pendingResult.finish()` then threw an NPE inside an unhandled `CoroutineScope(Dispatchers.IO)` coroutine. That killed the process on every worker run while a weather widget was placed, and the worker's `try/catch` could not catch it.
- **Fix:** The rendering logic moved to `WeatherWidget.updateWidgets(...)`, a suspend function in the companion object. `onUpdate` wraps it in `goAsync()`, and `WeatherWorker` calls it directly.
- **Rule:** Never call an `AppWidgetProvider` callback that uses `goAsync()` from outside a real broadcast.

### Fixed: Battery widget never refreshed after placement
- **Bug:** The battery widget relied on manifest-registered `ACTION_POWER_CONNECTED`/`ACTION_POWER_DISCONNECTED`. These are not on the implicit-broadcast exemption list, so they are never delivered to manifest receivers on API 26+ (our `minSdk`). With `updatePeriodMillis="0"`, the widget only redrew on placement, on boot, or after a customizer save. The "Battery widget updates on charge change ✅" claim in section 4 was false.
- **Fix:** `widget_battery_info.xml` now uses `updatePeriodMillis="1800000"` (30 min, the platform minimum). The undeliverable intent filters and the `onReceive` branch were removed.
- **Still open:** Instant updates on plug/unplug need a live process (for example a context-registered receiver), which is out of scope for now.

### Fixed: Gallery pinning silently failed or pinned the wrong widget
- **Bug:** `requestPinWidget` mapped `QUICK_TOGGLES`, `STEP_TRACKER` and `AUDIO_PLAYER` to the `widget/Nothing*` providers. Those are not registered in `AndroidManifest.xml`, so no pin dialog appeared. The `false` return of `requestPinAppWidget` was ignored. `QUICK_NOTE` pinned a **Clock** widget as a "fallback".
- **Fix:** The new `widgets/WidgetProviders.kt` (`providerClassFor(type)`) is the single type→provider mapping. It is used by both gallery pinning and customizer refresh, and it uses class references instead of `Class.forName` strings. Types without a registered provider return `null`, and the gallery shows "… isn't available as a home screen widget yet". A `false` pin result now shows a toast.
- **Update (2026-10-08, B1):** those four stay in-app only; their cards now show "IN-APP ONLY" instead of an "ADD TO HOME" button.

### Fixed: "AIR" quick toggle in the in-app preview did nothing
- **Bug:** The tile displays `isAirplaneModeOn`, but `WidgetGalleryViewModel` routed `"airplane"` to `toggleDnd()`.
- **Fix:** Added `QuickSettingsRepository.toggleAirplaneMode()` and routed the tile to it. The preview state is still mock data, not real system toggles.

### Fixed: In-app battery previews showed a stale or fake %
- **Bug:** The gallery card read the level once, when `BatteryRepository` was constructed, and nothing ever called `updateBatteryState()`. The customizer and Studio previews passed no battery data at all, so they showed the hardcoded `BatteryInfo()` defaults (84% / 18h). On an emulator, the customizer showed 84% while the device was at 42–77%.
- **Fix:** `BatteryRepository.batteryState` is now a `callbackFlow` backed by a context-registered `ACTION_BATTERY_CHANGED` receiver. The sticky broadcast delivers the current value on registration, and `distinctUntilChanged()` drops voltage/temperature-only updates. The gallery, customizer and Studio ViewModels collect it with `stateIn(WhileSubscribed(5000))`, seeded from `currentBatteryInfo()` so the fake 84% never flashes.
- **Verified (emulator):** all three previews follow `dumpsys battery set level` live. One receiver is registered while a preview is visible, and none 8 s after going home. The home-screen `BatteryWidget` still uses `getBatteryInfoSync()`.

### Done: B1 widget package consolidation (2026-10-08)
- **Problem:** Two provider packages existed. `widget/` (six `Nothing*WidgetProvider`s) was never registered or referenced, and showed placeholder data (battery hardcoded "88%"). `widgets/` was the registered set.
- **Decision:** Keep `widgets/` (the roadmap's older advice to keep `widget/` predated the 2026-10-02 fixes). Quick Toggles, Step Tracker, Audio Player and Quick Note stay in-app only.
- **Change:** Deleted `widget/`, its six `nothing_*_widget_info.xml`, its five layouts and the unreferenced `widget_clock.xml`. Gallery cards whose type has no provider (`providerClassFor() == null`) show a muted "IN-APP ONLY" label instead of the red "ADD TO HOME" button.
- **Verified (emulator):** the system lists exactly 4 providers (Clock, Date, Battery, Weather). Already-placed Weather and Battery widgets survived the update. The six pinnable cards show ADD TO HOME and the four others show IN-APP ONLY. Pinning Date opens the system pin dialog.

### Known, not fixed here (needs a design decision)
- The customizer saves by preset id, but providers read hard-coded ids (`clock_digital_default`, `date_default`, `battery_default`, `weather_default`). Customizing any other preset (analog/world clock, Studio-built widgets) saves fine but never changes a placed widget. This is backend-roadmap **B3** (key configs on `appWidgetId`).
- `WeatherWorker` is enqueued only from `BootReceiver`, so after a fresh install it never runs until the first reboot. It currently has no data to fetch, so the user sees no difference. Schedule it from `WeatherWidget.onEnabled`/`onDisabled` when Phase 7 adds real weather.

## 8. Maintenance Notes (2026-10-10)

### Done: B2 reliable updates — verified, no AlarmManager
- **Finding:** Earlier docs said the clock "relies on `TextClock` XML" as if that were a gap, and planned a per-minute `setExactAndAllowWhileIdle` alarm. Testing showed `TextClock` already meets every B2 acceptance criterion. The launcher re-formats it on each minute tick and on time, timezone and date changes. The app process does not need to run.
- **Verified (Pixel_10_Pro emulator, API 37, no code changes):** minute tick 23:00 → 23:01; midnight rollover via `cmd alarm set-time` (SAT · 10 → SUN · 11 on both clock and date widgets); `cmd alarm set-timezone UTC` applied immediately; correct time after 2 min of `deviceidle force-idle`; all four widgets correct, with accent colors, after `adb reboot` without opening the app; content kept after `adb install -r` (the system sends `APPWIDGET_UPDATE` to `ClockWidget` after a package update).
- **Decision:** no alarm scheduler. Exact per-minute alarms cost battery, `SCHEDULE_EXACT_ALARM` is denied by default on Android 14+, and Play restricts `USE_EXACT_ALARM` to alarm/calendar apps. `Rules.md` RULE-W2 now requires `TextClock` instead.
- **Change:** removed the unused `SCHEDULE_EXACT_ALARM` permission from the manifest. Added comments to `ClockWidget`/`DateWidget`.

### Fixed: the 24-HOUR FORMAT setting did nothing
- **Bug:** Settings saved `is_24_hour_clock`, but nothing read it. The widget layout and the `DigitalClockWidget` preview both hard-coded `HH:mm`.
- **Fix:** `ClockWidget` reads the setting and sets both `format12Hour` and `format24Hour` (via `RemoteViews.setCharSequence`) to `ClockWidget.timePattern()`. Setting both is required, because TextClock otherwise picks a format from the *system* 12/24h setting. `SettingsViewModel.toggle24h()` calls `requestWidgetUpdate(…, DIGITAL_CLOCK)` after the DataStore write. `MainActivity` provides `LocalIs24HourClock` to the previews. 12-hour is `h:mm` with no AM/PM (user decision).
- **Verified (emulator):** toggle off → placed widget shows 11:54 and the gallery preview 11:55; toggle on → both show 23:55.

### Changed: battery widget refreshes when the app opens
- User decision: keep the 30-min `updatePeriodMillis` (no extra background work). `MainActivity.onStart` now calls `requestWidgetUpdate(this, BATTERY_CIRCLE)`.
- **Verified (emulator):** `dumpsys battery set level 30` → widget still showed 62% → open app → 30%.
- **Testing tip:** after `cmd alarm set-time`, the next `TIME_TICK` can lag by up to ~30 s because the tick was scheduled against the old wall clock. Wait a full minute before calling a rollover failed. Re-enable `settings put global auto_time 1` afterwards.
