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
│   ├── {Clock,Date,Battery,Weather}Widget.kt   # all REGISTERED; clock/date ticked by TextClock
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
| Registered clock widget shows **time content** | ✅ `TextClock` in the layout; provider applies color |
| Per-minute / midnight updates | ✅ launcher-driven `TextClock`, no AlarmManager needed (B2, verified 2026-10-10) |
| Widgets survive reboot, Doze, app update with correct data | ✅ verified 2026-10-10 |
| One provider set, all registered | ✅ B1 done; Quick Toggles / Steps / Audio / Quick Note are in-app only |
| Multi-instance independent configs | ✅ `widget_instances` keyed by `appWidgetId` (B3, 2026-10-11) |
| Minified release build runs | ❌ R8 on, no keep rules / `proguardFiles` |

## 2. Target architecture (to-be)

The target keeps the current MVVM/Hilt/Room shape and fixes the engine. See
[`backend-roadmap.md`](./backend-roadmap.md) for phase-level detail.

```
Presentation (Compose) → ViewModel (StateFlow) → Repository (Room / system / HTTP)
                                                → Widget engine (TextClock, WorkManager, RemoteViews)
```

Key changes:
1. ~~**One provider set**~~ — done (2026-10-08): `widgets/` kept, `widget/` deleted.
   Types without a provider are labelled in-app only. *(B1)*
2. ~~**Update engine**~~ — resolved (2026-10-10) without AlarmManager: clock and
   date are `TextClock`s that the launcher ticks itself. Verified for minute tick,
   midnight rollover, timezone change, forced Doze, reboot and app update. The
   unused `SCHEDULE_EXACT_ALARM` permission was removed. *(B2)*
3. ~~**Instance-scoped config**~~ — done (2026-10-11): `widget_instances` table,
   tap-to-edit, gallery edits flow to uncustomized widgets. *(B3)*
4. **Release safety:** `proguard-rules.pro` keep rules + `proguardFiles`. *(B4)*

## 3. Widget update architecture

```
Clock  → TextClock in RemoteViews; the launcher re-formats it on TIME_TICK, time/timezone/date
         change. The provider only sets the accent color (onUpdate / customizer save).
Date   → same TextClock mechanism; rolls over at local midnight by itself
Battery→ updatePeriodMillis 30 min (current). POWER_(DIS)CONNECTED / BATTERY_CHANGED are NOT
         delivered to manifest receivers on API 26+; instant updates need a live-process receiver
Weather→ WorkManager PeriodicWorkRequest (≥15 min) → OpenMeteo → Room → updateAppWidget
Boot   → the system re-sends APPWIDGET_UPDATE after boot and after an app update; BootReceiver
         also pings all providers and enqueues WeatherWorker
```

**Why no AlarmManager (decided 2026-10-10):** a per-minute exact alarm would wake
the device 1,440 times a day to redraw a widget nobody is looking at, and on
Android 14+ `SCHEDULE_EXACT_ALARM` is denied by default while `USE_EXACT_ALARM`
is restricted by Play policy to alarm/calendar apps. `TextClock` costs nothing
while the screen is off and is what AOSP DeskClock uses. Use AlarmManager only if
a widget must show time-derived content that `TextClock` cannot format (for
example a "next alarm in 3h" label); prefer an inexact alarm then.

## 4. Persistence (current)

**Room** `nothing_widgets.db`, schema version 2.

- `widget_configs` — gallery **templates**, primary key `id: String` (e.g.
  `clock_digital_default`). `WidgetRepository` merges built-in presets with saved
  rows via `associateBy { it.id }`. `updateIntervalMinutes` exists but nothing
  reads it.
- `widget_instances` (v2, B3) — one row per **placed** widget, primary key
  `appWidgetId`: `presetId` (the template it came from), `isCustomized`, and its
  own style columns.

How a placed widget gets its config (`WidgetRepository.getConfigForWidget`):

```
row = widget_instances[appWidgetId]
template = widget_configs[row.presetId] ?: provider's DEFAULT_PRESET_ID
config = row.isCustomized ? template + row's style : template
```

- **Pinned from the gallery:** `requestPinAppWidget`'s success callback
  (`WidgetPinnedReceiver`) inserts the row with `isCustomized = false`.
- **Added from the launcher picker, or placed before B3:** no row, so the provider's
  default template is used.
- **Tap a placed widget:** opens `MainActivity` with `ACTION_EDIT_WIDGET` and the
  customizer edits that widget only. Saving sets `isCustomized = true`.
- **Gallery EDIT:** saves the template. Every widget with `isCustomized = false`
  follows it.
- **Removed / restored:** `NothingWidgetProvider.onDeleted` deletes the row, and
  `onRestored` remaps ids after a backup restore.
- Migration 1→2 only creates the table (no data loss).
  `fallbackToDestructiveMigration()` is still set for unknown version jumps;
  remove it before launch so a missing migration fails loudly.

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
done via `widget_instances` (B3). Tablets → `WindowSizeClass`. Localization → move
decorative strings to `strings.xml`.
