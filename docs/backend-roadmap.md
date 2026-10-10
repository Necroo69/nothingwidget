# NothingWidget — Backend / Widget-Engine Roadmap

Scope: **widget engine and reliability only.** No UI work is planned here. This
roadmap is based on a direct inspection of the code on branch
`claude/project-status-release-732b84` (not on prior notes or the older docs).

## Verified starting state (2026-09-25)

- **Two provider packages coexist.**
  - `widget/` — `NothingAudioWidgetProvider`, `NothingBatteryWidgetProvider`,
    `NothingClockWidgetProvider`, `NothingQuickSettingsWidgetProvider`,
    `NothingStepWidgetProvider`, `NothingWeatherWidgetProvider`.
    **None are registered in `AndroidManifest.xml`.** These actually build
    `RemoteViews` content (e.g. `NothingClockWidgetProvider` sets the time text).
  - `widgets/` — `ClockWidget`, `DateWidget`, `BatteryWidget`, `WeatherWidget`.
    **All four are registered.** `ClockWidget` only sets text *color*, never the
    time text; it depends on `TextClock` in XML for the actual time.
- **No AlarmManager / update scheduler exists.** No `ClockUpdater`/`DateUpdater`,
  no update `BroadcastReceiver`. `SCHEDULE_EXACT_ALARM` is declared but unused.
  `updatePeriodMillis="0"` in the widget-info XML, so there is no OS-driven or
  app-driven tick — only `TextClock` self-refresh in `widget_clock_layout.xml` /
  `widget_date.xml`.
- **`BootReceiver`** pings the four registered `widgets/` providers once and
  enqueues a 15-min periodic `WeatherWorker`. It does not (re)schedule any alarm.
- **Release build is unsafe.** `isMinifyEnabled = true` with **no `proguardFiles`
  line and no `proguard-rules.pro`**. Moshi/Retrofit/Room/Hilt rely on
  reflection/codegen and will likely fail under default R8.
- **Configs are keyed by string `id`** (e.g. `"clock_digital_default"`), not by
  `appWidgetId`. `WidgetConfigEntity` has no `appWidgetId` column, so two
  instances of the same widget type cannot hold independent configs.
- **Provider coverage is incomplete in both sets.** `WidgetType` has 10 values;
  `DATE` exists only in `widgets/`, `STEP_TRACKER`/`QUICK_TOGGLES`/`AUDIO_PLAYER`
  only in `widget/`, and `QUICK_NOTE` has no provider in either (gallery falls
  back to `ClockWidget`).
- **Namespace** is still `com.example.nothingwidget`; `applicationId` is
  `com.sritam.nothingwidget`.

### Fixed since (2026-10-02) — see `docs/Memory.md` §7
- `compileSdk` restored to 37 (35 broke `assembleDebug`).
- `WeatherWorker` no longer calls `WeatherWidget().onUpdate()` (its null
  `goAsync()` crashed the process); it calls `WeatherWidget.updateWidgets()`.
- Gallery pinning / customizer refresh now share `widgets/providerClassFor()`.
  The `QUICK_NOTE` → `ClockWidget` fallback is gone. Types with no registered
  provider (`QUICK_TOGGLES`, `STEP_TRACKER`, `AUDIO_PLAYER`, `QUICK_NOTE`) show
  a "not available yet" toast instead of failing silently. B1 still decides
  their real providers.
- Battery widget refreshes every 30 min (`updatePeriodMillis`). The power-
  connected filters never fired on API 26+ and were removed.

### B1 done (2026-10-08)
- Decision: keep `widgets/`, not `widget/` as recommended below. By 2026-10-08
  `widget/` was the dead set: unregistered, never referenced, with placeholder
  content (its battery provider hardcoded "88%"). `widgets/` was the registered
  set that the 2026-10-02 fixes had repaired and verified.
- Deleted the `widget/` package, its six `nothing_*_widget_info.xml` files, its
  five layouts, and the unreferenced duplicate `widget_clock.xml`. One layout and
  one info XML per widget remain.
- `QUICK_TOGGLES`, `STEP_TRACKER`, `AUDIO_PLAYER` and `QUICK_NOTE` stay
  in-app only. `providerClassFor()` returns `null` for them, and their gallery
  cards show an "IN-APP ONLY" label instead of an "ADD TO HOME" button. Building
  their widgets is future work (Steps/Audio need real data sources first).
- B2 and B3 are unblocked.

### B2 done (2026-10-10) — without AlarmManager
- The "verified starting state" above treated `TextClock` as a gap. It is not.
  `TextClock` inside `RemoteViews` is re-formatted by the launcher on every minute
  tick and on time, timezone and date changes. It needs no app process and costs
  nothing while the screen is off. AOSP DeskClock's widget works the same way.
- **Tested on the Pixel_10_Pro emulator (API 37), widgets placed from the gallery,
  with no code changes:**

  | B2 acceptance check | Result |
  |---|---|
  | Clock advances at the minute | ✅ 23:00 → 23:01 |
  | Date rolls over at local midnight (`cmd alarm set-time` to 23:59:40) | ✅ SAT · 10 → SUN · 11, both widgets |
  | Timezone change (`cmd alarm set-timezone UTC`) | ✅ 23:07 → 17:37 immediately |
  | `dumpsys deviceidle force-idle` for 2 min, then wake | ✅ correct time on wake |
  | `adb reboot`, no app launch | ✅ all 4 widgets correct, accent colors kept |
  | App update (`adb install -r`) | ✅ system re-sends `APPWIDGET_UPDATE`; content and colors kept |

- **Decision:** no `WidgetUpdateScheduler`/`WidgetUpdateReceiver`. A per-minute
  exact alarm would wake the device 1,440 times a day for a widget nobody is
  looking at. On Android 14+ `SCHEDULE_EXACT_ALARM` is denied by default, and Play
  allows `USE_EXACT_ALARM` only for alarm/calendar apps.
- **Changes:**
  - Removed the unused `SCHEDULE_EXACT_ALARM` permission. Added comments to
    `ClockWidget`/`DateWidget` saying why there is no alarm.
  - **12/24-hour (decided: follow the app setting).** The Settings "24-HOUR
    FORMAT" toggle used to do nothing. Now `ClockWidget` sets both TextClock
    formats to `ClockWidget.timePattern()` (`HH:mm` or `h:mm`, no AM/PM).
    Toggling redraws placed clocks, and in-app previews read it through
    `LocalIs24HourClock`.
  - **Battery (decided: keep the 30-min `updatePeriodMillis`).** It now also
    refreshes whenever the app is opened (`MainActivity.onStart`).
  - `triggerWidgetUpdate` moved out of the customizer screen into
    `widgets/WidgetProviders.kt` as `requestWidgetUpdate(context, type)`.
- **Still open, outside B2:** `WeatherWorker` is enqueued only from
  `BootReceiver` (Memory §7, fix in P6). Instant battery updates on plug/unplug
  are not possible without a running process.
- The original B2 plan below is kept for history but **superseded**.

### B3 done (2026-10-11)
- **Decisions (user):**
  - **Storage:** a separate `widget_instances` table, not a column on
    `widget_configs`, so gallery templates and placed widgets stay apart.
  - **Editing one widget:** tap the placed widget.
  - **Gallery EDIT:** changes the template and every placed widget not
    customized on its own.
- **Data:** `WidgetInstanceEntity(appWidgetId PK, presetId, isCustomized, style
  columns)`. Room v2 with `MIGRATION_1_2`, which only creates the table, so no
  data is lost. `WidgetRepository.getConfigForWidget(appWidgetId,
  fallbackPresetId)` resolves the config: instance row → its template → the
  provider's `DEFAULT_PRESET_ID`.
- **Lifecycle:**
  - Gallery pinning passes a success callback (`WidgetPinnedReceiver`) that
    links the new id to its template.
  - All four providers extend `NothingWidgetProvider`. `onDeleted` removes rows
    and `onRestored` remaps ids.
  - Every provider renders per id and sets a tap → `MainActivity`
    (`ACTION_EDIT_WIDGET`) → customizer in "this widget only" mode.
- **Verified on the emulator (API 37):**

  | B3 acceptance check | Result |
  |---|---|
  | v1 DB with saved templates → install v2 | ✅ `user_version` 2, table added, 2 saved templates kept, no crash |
  | Pin digital + analog clock from gallery | ✅ rows `(13, clock_digital_default)`, `(12, clock_analog_default)`; red vs white |
  | Tap one clock → yellow → save | ✅ only that clock turns yellow |
  | Gallery EDIT digital template → green | ✅ both uncustomized digital clocks (incl. pre-B3 widget with no row) turn green; yellow one unchanged |
  | Reboot | ✅ each widget keeps its own color |
  | Remove the yellow widget | ✅ its row is deleted |
  | Pin Date from gallery | ✅ renders immediately, linked to `date_default` |

- **Known limits:**
  - Providers apply only the **accent color**. Corner radius, dot grid, glyph
    border and subtitle change the in-app preview but not the home-screen
    layouts (roadmap P5).
  - Clock, analog and world clock all render the same digital `TextClock`
    layout; the analog/world templates only differ in color on the home screen.
  - `fallbackToDestructiveMigration()` is still enabled. Remove it before launch.
- **Testing tip:** `am force-stop` puts the app's widgets in the launcher's
  "loading" placeholder until the app runs again. That is platform behaviour,
  not a bug, but it can look like a failed render.

### B4 done (2026-10-11)
- **Finding:** the premise "R8 is on with no `proguardFiles`, so the release build
  is unsafe" was out of date. AGP 9.3.1 already applies
  `proguard-android-optimize.txt` by default and merges `src/main/keepRules/*.keep`.
  Every library's consumer rules were included (Room, WorkManager, Hilt/Dagger,
  DataStore, coroutines, Compose, Navigation, Moshi, Retrofit, OkHttp), as were
  AAPT2's rules for manifest components. Check this in
  `app/build/outputs/mapping/release/configuration.txt`.
- **Real gap found:** Retrofit **2.9** does not bundle the R8 full-mode rules for
  `suspend` service methods (they arrived in 2.10). P6's weather call would have
  crashed in release with `ClassCastException … ParameterizedType`. Those rules
  are now in `app/src/main/keepRules/rules.keep`. They are conditional and do
  nothing until a Retrofit interface exists. Delete them after upgrading Retrofit.
- **No `proguard-rules.pro` / `proguardFiles` added:** with AGP 9 the `keepRules`
  folder is the supported place, and adding `proguardFiles` would duplicate the
  default file.
- **Verified on the emulator (API 37), release APK signed with the debug key:**

  | B4 acceptance check | Result |
  |---|---|
  | `./gradlew assembleRelease` | ✅ succeeds |
  | APK size (target < 8 MB) | ✅ 5.4 MB (debug: 27.8 MB) |
  | Launch + every screen (gallery, customizer, card builder, glyph studio, settings) | ✅ no crashes |
  | Room v2 DB and saved templates read in release | ✅ |
  | 24h toggle (DataStore) → widget | ✅ `1:18` ↔ `01:18` |
  | Tap widget → per-widget customizer → save | ✅ only that clock changed |
  | Pin analog clock (pin callback receiver) | ✅ rendered in the analog template's white |
  | WeatherWorker via WorkManager (`cmd jobscheduler run -f`) | ✅ ran and was re-enqueued, no crash |
  | Reboot | ✅ all widgets correct, colors kept |
  | Weather network path | ⏭ not applicable: no network call exists yet (P6). Re-run this table after P6 |

- **How to repeat:** see Memory §10.

## Phase ordering rationale

Ordered by dependency, then risk:

1. **B1 – Consolidation** is foundational: every later phase edits providers, so
   the provider set must be settled first.
2. **B2 – Reliable updates** builds directly on the consolidated providers.
3. **B3 – Multi-instance configs** depends on the final providers and interacts
   with B2's scheduling (schedule per `appWidgetId`).
4. **B4 – Release-build safety** is independent of the others, but its keep-rules
   must be verified against the *final* class set, so it is validated last. Its
   rules can be drafted at any time — pull it earlier if you want to test release
   builds sooner.

---

## Phase B1 — Widget system consolidation

**Goal:** One canonical provider set, registered correctly, with a provider for
every `WidgetType` the gallery can pin.

**Decision required before starting (flagged, not guessed):**
Recommended path is to **standardize on the `widget/` (`Nothing*`) set** because
those providers actually render content, and **delete the `widgets/` set** — but
the `widget/` set is missing `DATE` and `QUICK_NOTE` providers, so those must be
ported/created. Confirm this direction, or state the alternative, before B1 code
work begins.

**Tasks:**
- Choose the canonical package (recommend `widget/`).
- Port a `NothingDateWidgetProvider` (logic from `widgets/DateWidget`) into the
  canonical package; create a `NothingQuickNoteWidgetProvider` (or explicitly
  drop `QUICK_NOTE` from the gallery for v1 — decision point).
- Delete the non-canonical package once every mapping is migrated.
- Register **every** canonical provider in `AndroidManifest.xml` with its
  `<intent-filter>` + `<meta-data>` pointing at a widget-info XML.
- Update `requestPinWidget` in `WidgetGalleryScreen.kt` so every `WidgetType`
  maps to a registered provider class (remove the `ClockWidget` fallbacks for
  `QUICK_NOTE`, `DIGITAL_CLOCK`/`ANALOG_CLOCK`/`WORLD_CLOCK`, etc.).
- Reconcile the duplicate widget-info XML and layout files (`widget_clock.xml`
  vs `widget_clock_layout.xml`, etc.) — keep one per widget.
- Update `BootReceiver` to reference the canonical providers.

**Files touched:**
`app/src/main/AndroidManifest.xml`,
`app/src/main/java/.../widget/*` (canonical set, + new Date/QuickNote),
`app/src/main/java/.../widgets/*` (deleted),
`app/src/main/java/.../ui/screens/gallery/WidgetGalleryScreen.kt`,
`app/src/main/java/.../BootReceiver.kt`,
`app/src/main/res/xml/*_info.xml`, `app/src/main/res/layout/widget_*`.

**Acceptance criteria:**
- Only one widget package remains in the source tree.
- Every `WidgetType` offered in the gallery maps to a provider that is registered
  in the manifest (`Class.forName` targets all resolve; no fallbacks).
- Pinning each widget type from the gallery succeeds on a stock launcher (the
  system pin dialog appears — no silent failure/exception).
- Placed clock widget shows the correct current time on placement (content, not
  just color).

**Dependencies:** none. Blocks B2, B3.

---

## Phase B2 — Reliable live updates (AlarmManager + Doze + reboot)

> **Superseded 2026-10-10.** Every acceptance criterion below already passes with
> `TextClock`, and no alarm code was added. See "B2 done" above.

**Goal:** Time-sensitive widgets update on schedule without relying on
`TextClock` XML or the capped `updatePeriodMillis`, and survive Doze and reboot.

**Tasks:**
- Add a `WidgetUpdateScheduler` (or `ClockUpdater`/`DateUpdater`) that uses
  `AlarmManager.setExactAndAllowWhileIdle` scheduled to the next minute boundary
  (clock) and next local midnight (date).
- Add a `WidgetUpdateReceiver` (`BroadcastReceiver`) that the alarm fires; it
  rebuilds `RemoteViews` for the relevant widget ids, calls
  `AppWidgetManager.updateAppWidget`, then reschedules the next alarm.
- Wire scheduling into `onEnabled` (first instance placed), `onUpdate`, and
  cancel in `onDisabled` (last instance removed).
- Update `BootReceiver` to re-arm all alarms on `ACTION_BOOT_COMPLETED` (and
  `ACTION_MY_PACKAGE_REPLACED`).
- **Android 12+ exact-alarm policy (flagged):** `setExactAndAllowWhileIdle`
  requires `SCHEDULE_EXACT_ALARM` (revocable) or the `USE_EXACT_ALARM` permission
  (allowed only for clock/alarm-class apps). Decide per widget: use exact for the
  clock, and check `AlarmManager.canScheduleExactAlarms()` with a graceful
  fallback to `setAndAllowWhileIdle` (inexact) where exact isn't granted. This is
  a policy decision, not an implementation detail — confirm the approach.
- Keep `WeatherWorker` (WorkManager) for network-backed widgets; do not put
  network on the alarm path.

**Files touched:**
new `widget/WidgetUpdateScheduler.kt` + `widget/WidgetUpdateReceiver.kt`,
`app/src/main/AndroidManifest.xml` (register the receiver),
canonical clock/date providers, `BootReceiver.kt`,
`app/src/main/res/xml/*_info.xml` (`updatePeriodMillis="0"` confirmed).

**Acceptance criteria:**
- Clock widget visibly updates at the top of each minute, observed for 3+ minutes
  with the screen on.
- Date widget rolls over at local midnight (verifiable by setting device clock to
  23:59 and waiting).
- After `adb shell reboot`, all placed widgets show correct data within ~60s
  without opening the app.
- After forcing Doze (`adb shell dumpsys deviceidle force-idle`), the next
  scheduled update still fires.
- No persistent foreground service and no wakelock beyond the alarm.

**Dependencies:** B1 (needs the final provider set).

---

## Phase B3 — Multi-instance configs (key on `appWidgetId`)

> **Done 2026-10-11** with shape (b), a separate table. See "B3 done" above.

**Goal:** Each placed widget instance carries its own config, so two clocks (or
two of any type) can differ independently.

**Tasks:**
- Add an `appWidgetId` → config mapping. Two viable shapes (flag the choice):
  (a) add an `appWidgetId: Int` column to `WidgetConfigEntity` and store one row
  per placed instance seeded from the chosen preset; or (b) a separate
  `WidgetInstanceEntity(appWidgetId PK, presetId, overrides…)`. Recommend (a) for
  simplicity unless presets must stay immutable.
- Room **schema migration** required (schema version bump + `Migration`, or a
  documented destructive migration for pre-release). Flag: destructive wipe is
  acceptable only because the app is not yet published.
- On pin/placement (`onUpdate` for a new id), copy the selected preset into an
  instance-scoped config row keyed by `appWidgetId`.
- Providers read config by `appWidgetId` (not by preset string id).
- On `onDeleted`, remove the instance row for those ids.
- Customizer "apply" writes to the specific `appWidgetId` being edited.

**Files touched:**
`WidgetConfigEntity.kt`, `WidgetConfigDao.kt`, `AppDatabase.kt` (version +
migration), `WidgetRepository.kt`, canonical providers,
customizer VM (`ui/screens/customizer/WidgetCustomizerViewModel.kt`) — read-path
only; no UI redesign.

**Acceptance criteria:**
- Place the same widget type twice; change one instance's accent color; only that
  instance changes on the home screen.
- Kill and relaunch the app; each instance retains its own config.
- Removing one instance does not affect the other's config.
- Room migration runs without crash from the previous schema (or destructive
  migration is explicitly documented and applied).

**Dependencies:** B1. Interacts with B2 (schedule/rebuild per `appWidgetId`).

---

## Phase B4 — Release-build safety (R8 / ProGuard)

> **Done 2026-10-11.** See "B4 done" above. The plan below assumed an older AGP;
> no `proguard-rules.pro` was needed.

**Goal:** A minified release build runs without reflection/codegen crashes and is
verified on-device.

**Tasks:**
- Create `app/proguard-rules.pro` with keep rules for:
  - **Moshi** — generated `*JsonAdapter` classes, `@JsonClass` models,
    `-keepclasseswithmembers` for adapters; keep `kotlin.Metadata`.
  - **Retrofit** — keep interface method signatures / annotations
    (`-keepattributes Signature,RuntimeVisibleAnnotations`), OkHttp platform
    warnings suppressed.
  - **Room** — generated `*_Impl` classes and entities.
  - **Hilt/Dagger** — generated components (usually handled by the plugin;
    verify) and `@Inject` members.
  - **Widget providers** — keep all `AppWidgetProvider` subclasses and any class
    referenced by name via `Class.forName` in `requestPinWidget`.
  - Data model / enum classes serialized by Moshi.
- Wire `proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
  "proguard-rules.pro")` into the `release` block; consider
  `isShrinkResources = true`.
- Build `assembleRelease`, install the release APK on a device, and exercise
  every widget + the weather network path.

**Files touched:**
new `app/proguard-rules.pro`, `app/build.gradle.kts` (release block).

**Acceptance criteria:**
- `./gradlew assembleRelease` succeeds.
- Release APK installs and launches with zero crashes.
- Weather fetch (Moshi/Retrofit) returns and renders in release (not just debug).
- Every widget pins, renders content, and updates in the release build.
- APK size recorded (target < 8 MB).

**Dependencies:** independent, but verify against the final code from B1–B3.
Rules can be drafted early; final verification is the last step.

---

## Cross-cutting / deferred (tracked, not scheduled here)

- Namespace is still `com.example.nothingwidget`; renaming is a larger refactor —
  track separately, not part of the widget engine.
- `updateIntervalMinutes` on the config exists but is unused; B2 should decide
  whether the scheduler honors it (per-widget refresh cadence) or ignores it.
- Runtime permissions for step (`ACTIVITY_RECOGNITION`), audio
  (notification-listener), and location (weather) are UI/permission work —
  out of scope for this backend pass, but B2/B3 providers should degrade
  gracefully when a permission is absent.
