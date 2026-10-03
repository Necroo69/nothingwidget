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
