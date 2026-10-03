NothingWidget — Product Requirements Document (PRD)
Version 1.0 | Based on actual codebase analysis of 
sritampattanaik/nothingwidget1
 | Status: Draft → Production
1. Vision
NothingWidget is the definitive home screen widget app for Android users who want the Nothing OS aesthetic — dot-matrix typography, sharp geometry, red accent language — on 
any
 Android device. Built in Jetpack Compose, it brings beautiful, battery-efficient, deeply customizable widgets beyond Nothing hardware boundaries.
One-line pitch:
 
"Your home screen, reinvented. Nothing-style. Any phone."
2. Problem Statement
What exists today
The current codebase (
nothingwidget1
) is a proof-of-concept app with:
A visually strong Compose-based gallery and customization UI (Space Mono font, black/red palette, sharp geometry)
4 widget providers: Clock, Date, Battery, Weather — all registered in the Manifest
A bottom sheet customization system with Size / Style / Color / Font / Background selectors
A live preview in the Customize screen
What is broken or missing (discovered by code analysis)
Widget providers are hollow.
 ClockWidget, DateWidget, WeatherWidget only set text color from SharedPreferences — they do 
not
 set actual time/date text. 
ClockWidget
 never calls 
setTextViewText
 for time; it relies entirely on 
TextClock
 in XML (which auto-updates itself but can't receive RemoteViews-set fonts/colors reliably on all launchers).
No persistence architecture.
 
SharedPreferences
 is used directly in Compose composables via 
LocalContext
, violating MVVM. There is zero ViewModel, zero Repository, zero DataStore.
Clock widget updates every 60,000ms
 via 
updatePeriodMillis
 — the Android minimum is actually 1,800,000ms (30 min). The 60s value is silently capped by the OS; the widget will NOT update every minute via this mechanism.
No WorkManager or AlarmManager
 for widget refresh — no BOOT_COMPLETED receiver — widgets die after reboot.
Weather widget shows hardcoded "24°"
 — no real data, no API, no placeholder even.
Date widget shows hardcoded "MON 28 JULY"
 in HomeScreen preview — not live.
optimization { enable = false }
 in release build — R8/ProGuard disabled, APK will be enormous and unobfuscated.
Package ID is 
com.example.nothingwidget
 — an example namespace, not publishable to Play Store.
No onboarding
 — user has no guidance on how to add widgets to home screen.
Bottom nav has a "Terminal" tab
 (Code icon) that navigates nowhere — dead UI.
"ADD" button in CustomizeScreen
 calls 
requestPinAppWidget
 which only works on supported launchers (Pixel/stock); on Samsung it silently fails with no feedback.
FIRMWARE_V2.0.4 / PRECISION_QUARTZ_ENGINE / LON: -0.1273 | LAT: 51.6974
 are decorative hardcoded strings — left over as UI chrome, not real data.
No accessibility
 — no content descriptions on any icon or widget.
No dark/light mode toggle
 — theme is hardcoded dark only, no 
lightColorScheme
.
targetSdk = 34
 while 
compileSdk = 37
 — targetSdk is outdated (now 35). compileSdk does not need to match targetSdk, and must stay 37 for current AndroidX dependencies.
Theme.Material.Light.NoActionBar
 in themes.xml — a Light theme for a dark app, inconsistent with the dark Compose theme.
3. Target Users
Primary — "The Aesthetic Android User"
Age 18–30, premium mid-range to flagship device (Pixel, Samsung Galaxy, OnePlus)
Curates their home screen obsessively (r/androidthemes, YouTube setups)
Knows Nothing OS exists and loves the design even without owning a Nothing Phone
Secondary — "The Nothing Phone Fan on Other Hardware"
Owns a Pixel/Samsung, uses Nothing earbuds, follows Nothing's brand
Wants visual consistency with Nothing's ecosystem
Tertiary — "The Minimalist Productivity User"
Wants clean, distraction-free widgets — hates bloated widget apps with ads
Values reliability above all — tired of widgets that stop updating
4. Use Cases
ID
Use Case
Priority
UC-01
Add Clock widget that shows live time (updates every minute)
P0
UC-02
Add Date widget showing today's date, updating at midnight
P0
UC-03
Add Battery widget showing live %, updating on change
P0
UC-04
Customize widget font color and see a live preview
P0
UC-05
Launch app and see a gallery of widget previews
P0
UC-06
Navigate to Settings and toggle app preferences
P1
UC-07
Widget survives device reboot without user action
P0
UC-08
Widget works after 72h with screen off (Doze)
P0
UC-09
User understands how to add a widget (onboarding)
P1
UC-10
Add Weather widget showing real temperature
P2
5. Competitive Analysis
App
Aesthetic
Reliability
Customization
Price
Android-native
Widgetsmith
⭐⭐⭐
⭐⭐⭐⭐⭐
⭐⭐⭐⭐
Freemium
❌ iOS only
KWGT
⭐⭐⭐⭐⭐
⭐⭐⭐
⭐⭐⭐⭐⭐
$3.99
✅
Nothing OS Widgets
⭐⭐⭐⭐⭐
⭐⭐⭐⭐⭐
⭐⭐
Free (bundled)
✅ Hardware-locked
Color Widgets
⭐⭐
⭐⭐⭐
⭐⭐⭐
Freemium
✅
NothingWidget (target)
⭐⭐⭐⭐⭐
⭐⭐⭐⭐⭐
⭐⭐⭐⭐
Free/One-time
✅
Differentiator:
 Authentic Nothing OS dot-matrix aesthetic + Compose architecture + production reliability on any Android.
6. Core Features (v1.0 — Production Target)
Widgets (all must actually function)
Clock Widget
 — Live time via AlarmManager, dot-matrix Space Mono font, 12/24h toggle, 3 size variants
Date Widget
 — Live date via midnight AlarmManager, 3 format options
Battery Widget
 — Live battery via BroadcastReceiver, percentage + progress bar, charging state
Weather Widget
 
(stretch)
 — OpenMeteo API via WorkManager, temperature + condition
App Screens
Home/Gallery
 — Live previews of all 4 widgets (clock preview must use a running coroutine, not hardcoded "MON 28 JULY")
Customize
 — Bottom sheet with Size/Style/Color/Font/Background; settings persisted to DataStore (not SharedPreferences); "ADD" button with proper UX fallback for launchers that don't support pin
Settings
 — Functional refresh rate, dark/light theme toggle, app info
Onboarding
 — One-screen explainer showing how to long-press home screen → Widgets → NothingWidget
Core Systems
DataStore Preferences (replacing SharedPreferences)
ViewModel + StateFlow for each screen
AlarmManager for clock minute-tick updates
WorkManager for periodic widget refresh (battery, weather)
BOOT_COMPLETED BroadcastReceiver
Proper package ID (not 
com.example.*
)
7. Future Features
Feature
Version
Light mode theme
v1.1
Weather widget (OpenMeteo)
v1.1
Music Now Playing widget
v1.2
Steps widget (Health Connect)
v1.2
Lock screen widgets (API 33+)
v1.3
Setup export / preset sharing
v2.0
Material You / Dynamic Color
v1.1
Tablet adaptive layout
v1.2
8. User Stories
As a user, I want the clock widget to show the correct time and update every minute,
so that I don't have to open the app to check the time.
As a user, I want my customizations to be saved even after I reboot my phone,
so that I don't have to reconfigure widgets after every restart.
As a first-time user, I want the app to show me how to add a widget to my home screen,
so that I don't get confused when I open the app.
As a user, I want to preview exactly what my widget will look like before placing it,
so that I know what I'm getting without trial and error.
As a developer/power user, I want the app to work fully offline,
so that core widget functionality never depends on internet connectivity.
9. Functional Requirements
FR-01: Widget Update Mechanism
Clock must use 
AlarmManager.setExactAndAllowWhileIdle
 for per-minute updates (not 
updatePeriodMillis
)
Battery must use 
BroadcastReceiver
 for 
ACTION_BATTERY_CHANGED
 (✅ partially done, but duplicated logic)
Date must reschedule at midnight via AlarmManager
All widgets must register a 
BOOT_COMPLETED
 receiver to reschedule after reboot
updatePeriodMillis
 must be set to 
0
 for clock/date (they use their own scheduling)
FR-02: Data Persistence
All widget settings must use DataStore Preferences (replace 
SharedPreferences
)
Settings must survive app process death and device reboot
Each widget type gets its own DataStore key namespace
FR-03: Widget Content
ClockWidget must call 
setTextViewText
 with formatted time string (don't rely solely on 
TextClock
 for customized colors — or handle it properly)
DateWidget must call 
setTextViewText
 with formatted date string
BatteryWidget must show actual battery % — ✅ mostly done
WeatherWidget must show real data or show a clear "no location/permission" state
FR-04: App Architecture
Every screen must have a ViewModel
No business logic inside Composables
Navigation routes must be type-safe (use sealed class or 
@Serializable
)
No raw 
Context.getSharedPreferences
 inside Composables
FR-05: Play Store Compliance
Package ID must not be 
com.example.*
R8 must be enabled in release builds
No debug code in production
Privacy policy URL must be provided
targetSdk
 must meet the current Play Store minimum (compileSdk may be higher)
10. Non-Functional Requirements
Category
Requirement
Performance
Widget update < 100ms; App cold start < 1.5s
Battery
No continuous background services; AlarmManager only for clock
Reliability
Widgets survive 72h Doze; survive reboot
Compatibility
minSdk 26 (✅ already set); targetSdk 35; compileSdk 37 (≥ targetSdk, as required by dependencies)
Accessibility
Content descriptions on all interactive elements and widgets
Security
No 
com.example
 package; R8 enabled; no hardcoded API keys
APK Size
< 8MB (currently no bloat, should stay lean)
11. Acceptance Criteria
AC-01: Clock Widget (currently FAILING)
Shows current time on placement
Updates every 60 seconds on-screen
Survives device reboot
Color customization reflects in actual widget
12h/24h format toggle works
AC-02: Date Widget (currently FAILING — no live date in widget)
Shows today's date on placement
Updates at midnight
Date format from customization applies
AC-03: Battery Widget (currently PARTIALLY PASSING)
Shows correct battery %
Updates on charge state change
Color customization reflects in widget
AC-04: Customization Persistence
Settings persist after app kill 
(currently uses SharedPreferences — fragile)
Settings persist after device reboot 
(currently FAILING)
"ADD" button gives feedback on unsupported launchers
AC-05: App Quality
Zero crashes on launch
Package ID is not 
com.example.*
R8 enabled in release
No dead navigation items
12. Success Metrics
Metric
Target (90 days post-launch)
Downloads
5,000+
Play Store Rating
≥ 4.4 stars
Day 1 Retention
≥ 55%
Day 7 Retention
≥ 25%
Crash-free sessions
≥ 99.5%
Users with ≥ 1 widget placed
≥ 65%
ANR rate
< 0.5%
13. Monetization
Recommended: Open Source + One-Time Premium Unlock ($1.49)
Free: Clock, Date, Battery widgets with basic customization
Premium: Weather widget, all color options, all font options, future widgets
No ads — contradicts the aesthetic philosophy entirely
Alternative: Completely free + tip jar
Builds community loyalty faster
Good for open-source reputation
14. Play Store Launch Checklist
Immediate Blockers (must fix before submission)
Change package ID from 
com.example.nothingwidget
 to production namespace
Enable R8 in release build (remove 
optimization { enable = false }
)
Raise targetSdk to 35 (done; keep compileSdk at 37, do not lower it)
Fix clock widget to actually update every minute
Add BOOT_COMPLETED receiver
Remove dead "Terminal" nav tab
Metadata
App name, short description, full description
Feature graphic (1024×500)
Icon (512×512)
Screenshots (minimum 4 phone)
Privacy policy URL
Technical
Release keystore signed APK/AAB
No 
com.example
 anywhere
Data safety form completed
Content rating completed
15. Roadmap
Month 1 — v1.0: Fix and Ship
  Fix all critical bugs (clock update, persistence, package ID, R8)
  Add BOOT_COMPLETED receiver
  Add onboarding screen
  Play Store submission
Month 2 — v1.1: Polish
  DataStore migration from SharedPreferences
  ViewModel + StateFlow architecture
  Light mode support
  Weather widget with OpenMeteo
Month 3 — v1.2: Expand
  Music Now Playing widget
  Material You Dynamic Color
  Lock screen widgets (API 33+)
  Health Connect step widget