# NothingWidget — Master Roadmap (to a shippable app)

Single source of truth for *what ships next and in what order*. Grounded in a
direct code inspection of branch `claude/project-status-release-732b84`
(2026-10-01), not the older design docs.

- Backend/widget-engine phases are summarized here and specified in full in
  [`backend-roadmap.md`](./backend-roadmap.md).
- Status language is evidence-based. "Done" means verified in code, not merely
  committed with a "Complete Phase N" message.

## Where the project actually is

| Area | Completion | Notes |
|---|---|---|
| App UI / screens | ~85% | Gallery, customizer, settings, glyph studio, widget studio all built |
| Architecture (Hilt / Room / MVVM) | ~80% | Real DI, Room DB, repositories, per-screen VMs, typed nav |
| Widget engine (functioning widgets) | ~35% | Registered clock sets color only; no live content tick |
| Reliability (updates, reboot, Doze) | ~25% | **No AlarmManager exists**; relies on `TextClock` XML |
| Release readiness | ~15% | R8 on with no keep rules; no signing, tests, onboarding |
| **Overall for commercial release** | **~45–50%** | UI-complete, engine not delivered |

## Strategy

Ship a **focused v1.0 of 4 rock-solid widgets** (Clock, Date, Battery, Weather)
before expanding. A widget app is judged on reliability, not breadth. The glyph /
audio / step / quick-toggles / quick-note widgets move to v1.1+.

Milestones are ordered by dependency and risk. Finish a milestone before starting
the next; within a milestone, phases are also ordered.

---

## Milestone 1 — Make the engine real (backend)

Full detail in [`backend-roadmap.md`](./backend-roadmap.md). Summary:

| Phase | Goal | Blocks |
|---|---|---|
| **B1 Consolidation** | One provider set, all registered in the manifest, a provider per shipped widget type | B2, B3 |
| **B2 Reliable updates** | `AlarmManager.setExactAndAllowWhileIdle` + update receiver wired into `onEnabled`/`onUpdate`/`BootReceiver`; survive Doze + reboot | — |
| **B3 Multi-instance** | Key config on `appWidgetId` (+ Room migration) so instances differ | — |
| **B4 Release safety** | `proguard-rules.pro` keep rules for Moshi/Retrofit/Room/Hilt + `proguardFiles`, verified on a real release build | release |

**Milestone 1 exit:** every v1.0 widget pins, renders live content, updates on
schedule, survives reboot/Doze, and a minified release APK runs without crashing.

---

## Milestone 2 — v1.0 product completeness (4 core widgets)

### Phase P5 — Customization apply end-to-end
**Goal:** Editing a widget in the customizer visibly changes that placed instance.
- **Tasks:** customizer "apply" writes the config for the specific `appWidgetId`
  (depends on B3), then calls the widget's update path; live preview reads from
  the same config; confirm color/size/style/format options all round-trip.
- **Files:** `ui/screens/customizer/WidgetCustomizerViewModel.kt` +
  `WidgetCustomizerScreen.kt` (read/write path only, no redesign), canonical
  providers, `WidgetRepository.kt`.
- **Acceptance:** change accent color → ADD → the placed widget shows the new
  color within ~3s; reopen customizer → previous selection pre-filled.
- **Dependencies:** B1, B3.

### Phase P6 — Weather real data (OpenMeteo)
**Goal:** Weather widget shows real temperature/condition, degrades gracefully.
- **Tasks:** verify/repair `WeatherRepository` + `WeatherWorker` + Retrofit/Moshi
  path; request `ACCESS_COARSE_LOCATION`; cache last-known weather; map WMO code →
  icon; °C/°F toggle; offline "last updated X min ago" state; "grant location"
  prompt when permission absent.
- **Files:** `data/repository/WeatherRepository.kt`, `worker/WeatherWorker.kt`,
  weather provider + layout, manifest permission.
- **Acceptance:** fresh install shows real temp within ~2 min of placement;
  offline shows cached value with timestamp; unit toggle applies immediately.
- **Dependencies:** B1, B4 (Moshi keep rules).

### Phase P7 — Onboarding + permissions UX
**Goal:** A new user understands the app and grants what each widget needs.
- **Tasks:** first-run onboarding (tracked via a DataStore/Room flag): see → customize
  → add-to-home instructions; "how to add widgets" re-entry from Settings;
  runtime permission prompts and graceful denied states for location (weather)
  and, later, activity-recognition/notification-listener (expansion widgets).
- **Files:** new `ui/screens/onboarding/*`, Settings screen entry, a first-run flag.
- **Acceptance:** first launch shows onboarding; later launches skip it;
  re-triggerable from Settings; denying a permission never crashes — widget shows
  a clear "permission needed" state.
- **Dependencies:** none hard; pairs with P6.

### Phase P8 — Settings drive the engine
**Goal:** Settings values actually affect behavior.
- **Tasks:** confirm refresh-rate selection feeds the scheduler/WorkManager
  interval; decide whether per-widget `updateIntervalMinutes` (currently unused)
  is honored; "force refresh all widgets" action; remove any vestigial controls.
- **Files:** `ui/screens/settings/SettingsViewModel.kt`,
  `data/repository/AppPreferencesRepository.kt`, scheduler from B2.
- **Acceptance:** change refresh rate → kill/relaunch → value persists and the
  next scheduled interval matches.
- **Dependencies:** B2.

**Milestone 2 exit:** the 4 core widgets are fully functional, customizable,
persistent, onboarded, and permission-safe.

---

## Milestone 3 — Quality & Play Store release

### Phase P9 — Testing
- **Tasks:** replace placeholder tests with real unit tests (ViewModels, repos,
  date formatting, battery % edge cases, config round-trip); instrumented tests
  (gallery renders, customizer apply); manual device matrix — Pixel (stock), Samsung
  One UI, a Xiaomi/aggressive-battery OEM, and an Android 8 (minSdk 26) check;
  reboot + 72h Doze soak.
- **Acceptance:** ≥50% coverage on VM/repo; instrumented tests pass; widgets
  verified updating on Pixel + Samsung; zero crashes/ANR in a 1-hour soak.
- **Dependencies:** M1–M2.

### Phase P10 — Accessibility & polish
- **Tasks:** content descriptions on icons and widget text; touch targets ≥48dp;
  contrast check (red on dark); extract inline colors/`RectangleShape` to named
  tokens; optional light theme + DARK/LIGHT/SYSTEM toggle.
- **Acceptance:** zero accessibility lint warnings; fully TalkBack-navigable.
- **Dependencies:** M1–M2.

### Phase P11 — Play Store release
- **Tasks:** decide namespace (`com.example.nothingwidget` → `com.sritam.nothingwidget`
  — a rename refactor) ; release keystore + `signingConfigs` from `local.properties`
  (never committed); build signed AAB; verify size <8MB; store listing (name,
  descriptions), feature graphic (1024×500), icon (512×512), ≥4 screenshots;
  privacy policy URL; data-safety + content-rating forms; submit.
- **Acceptance:** signed AAB builds; app approved and live as v1.0 (versionCode 1).
- **Dependencies:** P9, P10, B4.

**Milestone 3 exit:** v1.0 live on the Play Store.

---

## Milestone 4 — Post-launch expansion (v1.1+)

Re-introduce the deferred widgets one at a time, each with its own update path,
permission story, and graceful denied state:

- **World Clock** (timezone list) — v1.1
- **Weather polish** (hourly bar, Material You tint) — v1.1
- **Step Tracker** (`ACTIVITY_RECOGNITION` / Health Connect) — v1.2
- **Audio Visualizer** (notification-listener access) — v1.2
- **Quick Toggles** (Wi-Fi/BT/flashlight/DND — note OS limits on toggling from a widget) — v1.2
- **Quick Note** (needs a provider — none exists today) — v1.2
- **Material You / dynamic color, tablet adaptive layout, preset export/sharing** — v1.3+

Each expansion widget is "done" only when it pins, renders, updates on schedule,
survives reboot, and handles its permission being denied — the same bar as v1.0.

---

## Decisions still open (flagged, not guessed)

1. **Which provider set survives B1** — recommend the `widget/` (`Nothing*`) set
   (renders content), porting `DATE` and adding `QUICK_NOTE`.
2. **Exact-alarm policy on Android 12+** — exact for clock (needs
   `USE_EXACT_ALARM`/`SCHEDULE_EXACT_ALARM`), inexact fallback elsewhere.
3. **Room migration shape for `appWidgetId`** — column on `WidgetConfigEntity`
   vs a separate instance table; destructive migration acceptable pre-launch.
4. **Namespace rename** — do it before first publish or stay on `com.example`
   internally (applicationId is already correct).
5. **v1.0 scope** — confirm the 4-widget cut (Clock/Date/Battery/Weather).
