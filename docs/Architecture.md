# NothingWidget — Architecture Document

Version 2.0 · Rewritten 2026-10-01 from a direct code inspection of branch
`claude/project-status-release-732b84`. Supersedes v1.0, which described a
pre-migration design (no ViewModel/Hilt/DataStore, 4 widgets) that no longer
reflects the code.

## 1. Current architecture (as-is, verified)

```
com.example.nothingwidget/              # namespace still com.example; appId = com.sritam.nothingwidget
├── NothingWidgetApp.kt                 # @HiltAndroidApp
├── MainActivity.kt                     # @AndroidEntryPoint, hosts NavGraph, edge-to-edge
├── BootReceiver.kt                     # BOOT_COMPLETED: pings widgets once, enqueues WeatherWorker
├── data/
│   ├── local/                          # ROOM (not DataStore)
│   │   ├── AppDatabase.kt
│   │   ├── WidgetConfigDao.kt / WidgetConfigEntity.kt
│   │   ├── StepDao.kt / StepEntryEntity.kt
│   │   └── NoteDao.kt / SavedNoteEntity.kt
│   └── repository/
│       ├── WidgetRepository.kt         # presets merged with saved rows, keyed by STRING id
│       ├── AppPreferencesRepository.kt
│       ├── BatteryRepository.kt / WeatherRepository.kt
│       ├── StepTrackerRepository.kt / AudioRepository.kt / QuickSettingsRepository.kt
├── domain/model/                       # NothingWidgetConfig, WidgetType (10), WidgetSize, etc.
├── ui/
│   ├── navigation/ (Screen.kt, NavGraph.kt)   # typed routes
│   ├── components/ (DotMatrixText, GlyphVisualizerCanvas, NothingGlassCard, …)
│   ├── theme/ (Color, Theme, Type)
│   └── screens/ (gallery, customizer, settings, glyph, studio)  # each with a ViewModel
├── widgets/                            # the only provider package (B1 done 2026-10-08)
│   ├── {Clock,Date,Battery,Weather}Widget.kt   # all REGISTERED; ClockWidget time via TextClock XML
│   └── WidgetProviders.kt              # providerClassFor(type): WidgetType → provider, or null
└── worker/WeatherWorker.kt             # WorkManager periodic (15 min)
```

**Pattern:** MVVM + Repository + Hilt DI. Room for persistence. This foundation is
real and solid — the gaps are in the widget engine, not the app architecture.

### What works vs. what doesn't (verified)

| Capability | Status |
|---|---|
| Hilt DI, Room, per-screen ViewModels, typed nav | ✅ |
| Gallery / customizer / settings / glyph / studio screens render | ✅ |
| Battery widget shows % | ✅ refreshes every 30 min; no instant plug/unplug update |
| Registered clock widget shows **time content** | ❌ sets color only; time via `TextClock` XML |
| App-driven per-minute / midnight updates (AlarmManager) | ❌ none exists |
| Widgets survive reboot with correct data | ⚠ BootReceiver pings once; no alarm re-arm |
| One provider set, all registered | ✅ B1 done; Quick Toggles / Steps / Audio / Quick Note are in-app only |
| Multi-instance independent configs | ❌ keyed by string id, not `appWidgetId` |
| Minified release build runs | ❌ R8 on, no keep rules / `proguardFiles` |

## 2. Target architecture (to-be)

The target keeps the current MVVM/Hilt/Room shape and fixes the engine. See
[`backend-roadmap.md`](./backend-roadmap.md) for phase-level detail.

```
Presentation (Compose) → ViewModel (StateFlow) → Repository (Room / system / HTTP)
                                                → Widget engine (AlarmManager, WorkManager, RemoteViews)
```

Key changes:
1. ~~**One provider set**~~ — done (2026-10-08): `widgets/` kept, `widget/` deleted.
   Types without a provider are labelled in-app only. *(B1)*
2. **Update engine:** `WidgetUpdateScheduler` + `WidgetUpdateReceiver` using
   `AlarmManager.setExactAndAllowWhileIdle`, wired into `onEnabled`/`onUpdate`/
   `BootReceiver`, re-armed after reboot, Doze-tolerant. *(B2)*
3. **Instance-scoped config:** add `appWidgetId` keying (+ Room migration). *(B3)*
4. **Release safety:** `proguard-rules.pro` keep rules + `proguardFiles`. *(B4)*

## 3. Widget update architecture (target)

```
Clock  → AlarmManager.setExactAndAllowWhileIdle(next minute) → WidgetUpdateReceiver → updateAppWidget → reschedule
Date   → AlarmManager at next local midnight → receiver → updateAppWidget → reschedule
Battery→ updatePeriodMillis 30 min (current). POWER_(DIS)CONNECTED / BATTERY_CHANGED are NOT
         delivered to manifest receivers on API 26+; instant updates need a live-process receiver
Weather→ WorkManager PeriodicWorkRequest (≥15 min) → OpenMeteo → Room → updateAppWidget
Boot   → BootReceiver re-arms all alarms + enqueues WeatherWorker
```

Android 12+ exact-alarm policy: use exact for the clock
(`USE_EXACT_ALARM`/`SCHEDULE_EXACT_ALARM`, check `canScheduleExactAlarms()`),
inexact fallback elsewhere.

## 4. Persistence (current)

**Room**, table `widget_configs`, primary key `id: String` (e.g.
`clock_digital_default`). `WidgetRepository` merges built-in presets with saved
rows via `associateBy { it.id }`. `updateIntervalMinutes` exists on the entity but
no scheduler reads it. **Target:** add `appWidgetId` so instances are independent.

## 5. Testing strategy

Current: placeholder `ExampleUnitTest` / `ExampleInstrumentedTest` only. Target:
unit tests for ViewModels, repositories, date formatting, battery %, config
round-trip; instrumented tests for gallery + customizer; manual device matrix
(Pixel, Samsung One UI, Xiaomi/aggressive-battery, Android 8). Detail in
[`roadmap.md`](./roadmap.md) Phase P9.

## 6. Scalability

New widget type → add a provider in the canonical package, register in the
manifest, add a widget-info XML + layout, extend `WidgetType` and presets. More
options → extend `NothingWidgetConfig` + the customizer. Multiple instances →
`appWidgetId` keying (B3). Tablets → `WindowSizeClass`. Localization → move
decorative strings to `strings.xml`.
