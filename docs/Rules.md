NothingWidget — Development Rules
Strict rules for production-quality development. Every rule has a reason derived from actual codebase findings.
1. Architecture Rules
RULE-A1: Every screen must have a ViewModel
Violation found:
 HomeScreen, CustomizeScreen, SettingsScreen — all use 
remember {}
 local state only.
// ❌ WRONG — business logic and state in a Composable
@Composable
fun CustomizeScreen(navController: NavController, widgetType: String) {
    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    var selectedColor by remember { mutableStateOf("#FFFFFF") }
    ...
}
// ✅ CORRECT
@Composable
fun CustomizeScreen(
    navController: NavController,
    widgetType: String,
    viewModel: CustomizeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ...
}
RULE-A2: No SharedPreferences. Ever. Use DataStore.
Violation found:
 CustomizeScreen and all 4 widget providers use 
getSharedPreferences
.
// ❌ WRONG
val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
prefs.edit().putString("clock_color", color).apply()
// ✅ CORRECT
widgetRepository.saveColor(WidgetType.CLOCK, color)  // internally uses DataStore
RULE-A3: No business logic inside Composables
Business logic includes: SharedPreferences reads/writes, broadcast sends, AppWidgetManager calls. Composables are for 
rendering only
.
RULE-A4: Use StateFlow, not LiveData or local remember state for shared state
// ❌ WRONG (if state needs to survive configuration change or be shared)
var selectedColor by remember { mutableStateOf("#FFFFFF") }
// ✅ CORRECT
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
RULE-A5: Navigation routes must be type-safe
Violation found:
 Route strings like 
"customize/{widgetType}"
 are raw strings.
// ❌ WRONG
navController.navigate("customize/clock")
// ✅ CORRECT
navController.navigate(Screen.Customize("clock").route)
// or with Navigation 2.8+ @Serializable objects
RULE-A6: Dependency injection via Hilt only. No singleton abuse.
No companion object singletons. No 
object
 holding application state. No 
Application.instance
.
2. Widget Rules
RULE-W1: Never rely on updatePeriodMillis for frequent updates
Violation found:
 
widget_clock_info.xml
 sets 
updatePeriodMillis="60000"
. Android silently caps this at 1,800,000ms (30 min).
<!-- ❌ WRONG — silently ignored by Android for values < 1,800,000 -->
android:updatePeriodMillis="60000"
<!-- ✅ CORRECT — use AlarmManager instead; set this to 0 for clock -->
android:updatePeriodMillis="0"
RULE-W2: Clock widgets must use AlarmManager with setExactAndAllowWhileIdle
// ✅ CORRECT — schedule next minute update
fun scheduleNextMinuteUpdate(context: Context) {
    val intent = Intent(context, ClockUpdateReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val nextMinute = Calendar.getInstance().apply {
        add(Calendar.MINUTE, 1)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
        .setExactAndAllowWhileIdle(AlarmManager.RTC, nextMinute.timeInMillis, pendingIntent)
}
RULE-W3: All widget providers must handle BOOT_COMPLETED
A standalone 
BootReceiver
 must reschedule AlarmManager and enqueue WorkManager tasks.
<receiver android:name=".receiver.BootReceiver" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
RULE-W4: Widget XML layouts must include targetCellWidth/targetCellHeight (Android 12+)
<appwidget-provider
    android:targetCellWidth="4"
    android:targetCellHeight="2"
    android:minResizeWidth="110dp"
    android:maxResizeWidth="320dp"
    android:previewLayout="@layout/widget_clock_preview"
    android:description="@string/widget_clock_desc" />
RULE-W5: Widget update logic must run off the main thread
All SharedPreferences (or DataStore) reads inside 
onUpdate()
 must use a coroutine or background thread.
// ✅ CORRECT
override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
    ids.forEach { id ->
        goAsync().let { result ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val config = widgetRepository.getConfig(WidgetType.CLOCK)
                    val views = buildClockViews(context, config)
                    manager.updateAppWidget(id, views)
                } finally {
                    result.finish()
                }
            }
        }
    }
}
RULE-W6: Never hardcode content in widget RemoteViews
Violation found:
 WeatherWidget hardcodes 
"24°"
 / 
"CLEAR SKY"
. Battery widget shows 
"87%"
 as static 
android:text
 in XML. All dynamic content must be set via 
setTextViewText()
 in the provider's 
onUpdate
.
RULE-W7: Widgets must have content descriptions for accessibility
<TextView
    android:contentDescription="@string/widget_clock_desc"
    ... />
3. UI / Compose Rules
RULE-UI1: Never use Material 2 components. Material 3 only.
No 
androidx.compose.material.*
 imports (except 
material-icons-extended
 for icons is OK). All components from 
androidx.compose.material3.*
.
RULE-UI2: Never use deprecated 
Icons.Default.ArrowBack
Violation found:
 
TopAppBar.kt
 uses 
Icons.Default.ArrowBack
.
// ❌ WRONG
Icon(Icons.Default.ArrowBack, ...)
// ✅ CORRECT
Icon(Icons.AutoMirrored.Filled.ArrowBack, ...)
RULE-UI3: Theme colors must come from MaterialTheme, not hardcoded hex values
Violation found:
 Multiple hardcoded 
Color(0xFF1A1A1A)
 scattered throughout CustomizeScreen.
// ❌ WRONG
.background(Color(0xFF1A1A1A))
// ✅ CORRECT
.background(MaterialTheme.colorScheme.surfaceContainer)
// or use the named constants from Color.kt, not inline hex
RULE-UI4: No raw 
RectangleShape
 references. Use a named constant.
Violation found:
 
androidx.compose.ui.graphics.RectangleShape
 is used 9+ times across files.
// ❌ WRONG
.border(1.dp, PrimaryText, androidx.compose.ui.graphics.RectangleShape)
// ✅ CORRECT (define once in theme)
val SharpShape = RectangleShape  // in Theme.kt
.border(1.dp, MaterialTheme.colorScheme.outline, SharpShape)
RULE-UI5: Every interactive element must have a contentDescription
// ❌ WRONG
Icon(Icons.Default.Settings, contentDescription = null)
// ✅ CORRECT
Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
RULE-UI6: Strings must be in strings.xml, not hardcoded in Kotlin
Violation found:
 "YOUR WIDGETS", "FIRMWARE_V2.0.4", "NothingWidget © 2024", "PRECISION_QUARTZ_ENGINE LON:..." etc. are all hardcoded. UI-visible strings → 
strings.xml
. Decorative/non-translatable strings → 
strings.xml
 with 
translatable="false"
.
RULE-UI7: No dead navigation destinations
Violation found:
 "Terminal" tab in BottomNavBar navigates nowhere (
onClick = {}
). Remove any navigation item that has no corresponding screen. Ship only what works.
RULE-UI8: Prefer 
collectAsStateWithLifecycle
 over 
collectAsState
// ❌ Less optimal
val uiState by viewModel.uiState.collectAsState()
// ✅ CORRECT — lifecycle-aware, stops collecting when UI is hidden
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
4. Build & Release Rules
RULE-B1: Package ID must never be com.example.*
Violation found:
 
applicationId = "com.example.nothingwidget"
 in 
app/build.gradle.kts
. Play Store rejects 
com.example.*
 package IDs.
RULE-B2: R8 must be enabled in release builds
Violation found:
 
optimization { enable = false }
 in the release build type.
// ❌ WRONG — current release config
buildTypes {
    release {
        optimization { enable = false }   // disables R8/ProGuard
    }
}
// ✅ CORRECT
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
RULE-B3: compileSdk must be >= targetSdk, and >= what dependencies require
compileSdk does not have to equal targetSdk. Never lower compileSdk below what the AndroidX dependencies declare in their AAR metadata. Lowering it from 37 to 35 broke assembleDebug on 2026-08-09 (core 1.19 and lifecycle 2.11 need 37). Raise targetSdk on its own schedule (Play policy), separately from compileSdk. Verify with ./gradlew assembleDebug, not just compileDebugKotlin.
RULE-B4: No debug flags in release builds
No 
Log.d/v/i
 in production. Use a logging wrapper that no-ops in release:
object NWLog {
    fun d(tag: String, msg: String) { if (BuildConfig.DEBUG) Log.d(tag, msg) }
}
RULE-B5: All Gradle dependency versions must be in libs.versions.toml
No inline version strings in 
build.gradle.kts
:
// ❌ WRONG
implementation("androidx.compose.material:material-icons-extended")  // no version!
// ✅ CORRECT — pin in libs.versions.toml
implementation(libs.androidx.compose.material.icons.extended)
5. Security Rules
RULE-S1: No API keys in source code
Weather API keys (if any) must come from 
local.properties
 → 
BuildConfig
, never from committed files.
RULE-S2: No internet permission for core widgets
Clock, Date, Battery widgets must work with zero network access. Only WeatherWidget needs 
INTERNET
 permission (and should degrade gracefully without it).
RULE-S3: SharedPreferences must not store sensitive data
Widget preferences (colors, sizes) are not sensitive, but establish the pattern now.
6. Naming Conventions
Kotlin files
ViewModels: 
{Screen}ViewModel.kt
 — e.g., 
CustomizeViewModel.kt
Screens: 
{Name}Screen.kt
 — e.g., 
HomeScreen.kt
Widget providers: 
{Name}Widget.kt
 — e.g., 
ClockWidget.kt
Receivers: 
{Name}Receiver.kt
 — e.g., 
BootReceiver.kt
Workers: 
{Name}Worker.kt
 — e.g., 
WeatherWorker.kt
Repositories: 
{Domain}Repository.kt
 (interface) + 
{Domain}RepositoryImpl.kt
Use cases: 
{Verb}{Noun}UseCase.kt
 — e.g., 
GetWidgetConfigUseCase.kt
Compose
Composable functions: PascalCase, no verb prefix — 
WidgetCard
, not 
BuildWidgetCard
Composable parameters: camelCase
State variables: 
uiState
, not 
state
 or 
s
Resources
Widget layouts: 
widget_{name}.xml
 ✅ (already done)
Widget info XML: 
widget_{name}_info.xml
 ✅ (already done)
String IDs: 
snake_case
 — 
widget_clock_desc
, 
settings_theme_label
Color names in 
colors.xml
: 
snake_case
 — current file has leftover 
purple_200
 etc. — remove them
Git Commits
Follow Conventional Commits (already doing this — 
feat:
, 
fix:
 ✅):
feat: add BOOT_COMPLETED receiver for widget persistence
fix: replace updatePeriodMillis with AlarmManager for clock widget
refactor: migrate SharedPreferences to DataStore
perf: defer widget updates to background thread using goAsync
chore: update compileSdk and targetSdk to 35
7. Performance Rules
RULE-P1: Widget update must use goAsync()
Widget providers run on the main thread. Any I/O (DataStore read) must use 
goAsync()
.
RULE-P2: No continuous background services
No 
Service
 running at all times. Use WorkManager for periodic tasks.
RULE-P3: Compose recomposition must be minimal
Hoist state to ViewModel
Use 
key()
 in LazyRow/LazyColumn where needed
Avoid reading DataStore directly in Composables — observe StateFlow
RULE-P4: Canvas drawing in Composables should be stable
The dot-matrix background Canvas in CustomizeScreen redraws on every recomposition. Extract to a 
remember
-stable object or use 
drawBehind
.
8. Accessibility Rules
RULE-ACC1: Every Icon must have a non-null contentDescription
No 
contentDescription = null
 on interactive icons.
RULE-ACC2: Widget must have android:contentDescription
All widget TextViews must have content descriptions set via 
setContentDescription()
 in the provider.
RULE-ACC3: Color contrast must meet WCAG AA
White (#FFFFFF) on dark (#141313) = contrast ratio ~15:1 ✅ AccentRed (#FF5540) on dark (#141313) = verify meets 4.5:1 for text.
RULE-ACC4: Touch targets must be at least 48×48dp
The 8dp red dot (
Box(modifier = Modifier.size(8.dp))
) used as a decorative element — OK. But BottomNavItem must be at least 48dp tall — current 64dp height ✅.
9. Code Review Checklist
Before merging any PR, verify:
No 
SharedPreferences
 usage (use DataStore)
No business logic in Composables
ViewModel exists for any new screen
StateFlow used for screen state
No 
com.example.*
 package references
No hardcoded strings visible to users
All icons have 
contentDescription
No 
Log.d/v
 in non-debug code
updatePeriodMillis = 0
 for clock/date widgets
Widget 
onUpdate()
 uses 
goAsync()
New widget registered in Manifest and has 
info.xml
New DataStore keys added to 
WidgetPreferencesKeys
 object
Conventional commit message format
Unit test added for any new ViewModel or Repository method