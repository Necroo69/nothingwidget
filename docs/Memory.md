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
37 ← should be 35
Target SDK
34 ← must match compileSdk, change to 35
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
Should both be 35
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
Fix 
targetSdk
 = 
compileSdk
 = 35
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
- **Fix:** `WeatherWorker` now resolves active weather widget IDs with `AppWidgetManager.getAppWidgetIds(ComponentName(...))` and calls `WeatherWidget.onUpdate(...)` directly only when at least one weather widget is placed.
- **Current limitation:** This does not add live weather networking. Until the Phase 7 weather data source is implemented, the widget intentionally renders a neutral unavailable state (`--°` / `WEATHER UNAVAILABLE`) instead of stale fake weather.

### Fixed: compile SDK drifted past target SDK
- **Bug:** `compileSdk` was set to `37` while the project target was `35` and the project memory already noted SDK 35 as the intended baseline. That can break local builds on machines with only the intended Android 35 platform installed.
- **Fix:** `compileSdk` is now aligned to `35` while keeping `targetSdk = 35`.
