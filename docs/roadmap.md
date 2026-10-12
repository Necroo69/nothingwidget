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
| App UI / screens | ~90% | Gallery, customizer, settings, onboarding, glyph studio, widget studio |
| Architecture (Hilt / Room / MVVM) | ~80% | Real DI, Room DB, repositories, per-screen VMs, typed nav |
| Widget engine (functioning widgets) | ~95% | M1 + M2 done: 4 widgets render live content and every style option |
| Reliability (updates, reboot, Doze) | ~80% | Verified on one emulator; no OEM device matrix or 72 h soak yet (P9) |
| Release readiness | ~35% | Minified release verified; no signing, store assets, privacy policy or real tests |
| **Overall for release** | **~72%** | Product-complete (M1 + M2), package renamed; Milestone 3 (quality + store) remains |

*Updated 2026-10-12 after Milestone 2. See "Path to production" below.*

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
| ~~**B1 Consolidation**~~ ✅ 2026-10-08 | One provider set, all registered in the manifest, a provider per shipped widget type | B2, B3 |
| ~~**B2 Reliable updates**~~ ✅ 2026-10-10 | Clock/date tick on schedule and survive Doze + reboot. Met by `TextClock`, no AlarmManager (see backend-roadmap B2) | — |
| ~~**B3 Multi-instance**~~ ✅ 2026-10-11 | Each placed widget has its own config (`widget_instances` table, lossless v1→v2 migration); tap a widget to edit it | — |
| ~~**B4 Release safety**~~ ✅ 2026-10-11 | Minified release APK (5.4 MB) verified on device; AGP 9 already applies default + library rules; project rules add Retrofit 2.9 suspend fixes | release |

**Milestone 1 exit:** every v1.0 widget pins, renders live content, updates on
schedule, survives reboot/Doze, and a minified release APK runs without crashing.
✅ **Met 2026-10-11.** Weather still has no real data; that is P6.

---

## Milestone 2 — v1.0 product completeness (4 core widgets)

### ~~Phase P5 — Customization apply end-to-end~~ ✅ 2026-10-12
**Done:** the widget card is a bitmap drawn from the config (corner radius,
dot grid, glyph border), and Clock/Date show the subtitle. Verified on a placed
clock via tap-to-edit. On Android 12+ the launcher's corner radius is the
minimum. Details: Memory §11.

**Goal:** Editing a widget in the customizer visibly changes that placed instance.
- **Done in B3 (2026-10-11):** saving in the customizer redraws the placed
  widget (tap-to-edit for one widget, gallery EDIT for its template), and
  reopening pre-fills the saved values. Verified for accent color.
- **Tasks:** confirm the other style options (corner radius, dot grid, glyph
  border, subtitle) are rendered by the home-screen layouts at all. Today the
  providers apply only the accent color, so those options change the in-app
  preview but not the placed widget.
- **Files:** `ui/screens/customizer/WidgetCustomizerViewModel.kt` +
  `WidgetCustomizerScreen.kt` (read/write path only, no redesign), canonical
  providers, `WidgetRepository.kt`.
- **Acceptance:** change accent color → ADD → the placed widget shows the new
  color within ~3s; reopen customizer → previous selection pre-filled.
- **Dependencies:** B1, B3.

### ~~Phase P6 — Weather real data (OpenMeteo)~~ ✅ 2026-10-12
**Done:** Open-Meteo forecast + city search; device location (foreground only,
no background permission) or a chosen city; cached forecast with its "Updated"
time when offline; °C/°F applies instantly. Verified in debug and minified
release, which also exposed and fixed a WorkManager R8 bug. Details: Memory §11.

**Goal:** Weather widget shows real temperature/condition, degrades gracefully.
- **Tasks:** verify/repair `WeatherRepository` + `WeatherWorker` + Retrofit/Moshi
  path; request `ACCESS_COARSE_LOCATION`; cache last-known weather; map WMO code →
  icon; °C/°F toggle; offline "last updated X min ago" state; "grant location"
  prompt when permission absent.
- **Files:** `data/repository/WeatherRepository.kt`, `worker/WeatherWorker.kt`,
  weather provider + layout, manifest permission.
- **Acceptance:** fresh install shows real temp within ~2 min of placement;
  offline shows cached value with timestamp; unit toggle applies immediately.
- **Dependencies:** B1, B4 (done). Use Moshi codegen (`@JsonClass(generateAdapter = true)`)
  for the API models, and re-run the release check in `backend-roadmap.md` "B4 done"
  once the network call exists.

### ~~Phase P7 — Onboarding + permissions UX~~ ✅ 2026-10-12
**Done:** 4-page first-run onboarding (replayable from Settings), location
permission with a graceful denied state, "Tap to set a location" widget state.

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

### ~~Phase P8 — Settings drive the engine~~ ✅ 2026-10-12
**Done:** weather refresh rate (15 min–3 h) re-schedules the worker; refresh-all
action; dead "haptic" toggle and fake city chips removed; per-template
`updateIntervalMinutes` deliberately not honored.

**Goal:** Settings values actually affect behavior.
- **Done in B2 (2026-10-10):** the 24-HOUR FORMAT toggle drives the clock widget
  and previews.
- **Tasks:** confirm refresh-rate selection feeds the `WeatherWorker` interval
  (B2 added no app scheduler; clock/date need none); decide whether per-widget
  `updateIntervalMinutes` (currently unused) is honored or removed; "force refresh
  all widgets" action; remove any vestigial controls.
- **Files:** `ui/screens/settings/SettingsViewModel.kt`,
  `data/repository/AppPreferencesRepository.kt`, `worker/WeatherWorker.kt`.
- **Acceptance:** change refresh rate → kill/relaunch → value persists and the
  next scheduled interval matches.
- **Dependencies:** P6 (weather worker scheduling).

**Milestone 2 exit:** the 4 core widgets are fully functional, customizable,
persistent, onboarded, and permission-safe. ✅ **Met 2026-10-12.**

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
- **Tasks:** ~~decide namespace~~ (done 2026-10-12: `com.necroo69.nothingwidget`);
  release keystore + `signingConfigs` from `local.properties`
  (never committed); build signed AAB; verify size <8MB; store listing (name,
  descriptions), feature graphic (1024×500), icon (512×512), ≥4 screenshots;
  privacy policy URL; data-safety + content-rating forms; submit.
- **Acceptance:** signed AAB builds; app approved and live as v1.0 (versionCode 1).
- **Dependencies:** P9, P10, B4.

**Milestone 3 exit:** v1.0 live on the Play Store.

---

## Path to production (what is left before v1.0 ships)

Milestones 1 and 2 are done, so the app does everything v1.0 promises. What
remains is proving it works on real devices and meeting Play Store
requirements. Estimates assume one developer.

| # | Work | Phase | Size | Blocks launch? |
|---|---|---|---|---|
| 1 | Unit tests for ViewModels and repositories (≥50% VM/repo coverage); 8 weather tests exist | P9 | M | Yes |
| 2 | Instrumented tests: gallery renders, customizer save, onboarding | P9 | M | Yes |
| 3 | Device matrix: Pixel, Samsung One UI, Xiaomi/aggressive-battery OEM, Android 8 (minSdk 26) | P9 | M | Yes |
| 4 | 72 h Doze + reboot soak; 1 h zero-crash/ANR run | P9 | S | Yes |
| 5 | Accessibility: content descriptions, 48 dp targets, contrast (red on dark), TalkBack pass, lint clean | P10 | M | Yes |
| 6 | Widget font: Space Mono isn't applied inside RemoteViews | P10 | S | No (polish) |
| 7 | Remove `fallbackToDestructiveMigration()`; export Room schemas | P11 | S | Yes |
| 8 | ~~Namespace rename~~ — done 2026-10-12: `com.necroo69.nothingwidget` | P11 | S | ✅ Done |
| 9 | Release keystore + `signingConfigs` from `local.properties`; signed AAB; keep `mapping.txt` | P11 | S | Yes |
| 10 | Privacy policy URL. Now required: the app sends approximate location to Open-Meteo | P11 | S | Yes |
| 11 | Play Data safety form (approximate location, shared with Open-Meteo for app functionality, not stored on a server) and content rating | P11 | S | Yes |
| 12 | Store listing: name, descriptions, 512 px icon, 1024×500 feature graphic, ≥4 screenshots | P11 | M | Yes |
| 13 | Open-Meteo licence: free v1.0 fits the non-commercial tier. Buy a subscription (or switch provider) before the first paid feature | post-1.0 | Decision | No (v1.0 is free) |
| 14 | ~~Monetization~~ — decided 2026-10-12: v1.0 free, paid features later (PRD §9) | — | — | ✅ Decided |
| 15 | Instant battery updates on plug/unplug (needs a live receiver) | post-1.0 | S | No |

**Estimate:** roughly 2–3 weeks of focused work: P9 about 1–1.5 weeks, P10
3–4 days, P11 3–5 days, plus Play review time (often 1–7 days for a new
developer account). Items 8 and 14 are settled; v1.0 is free, so 13 waits for
the first paid feature.

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

1. ~~**Which provider set survives B1**~~ — decided 2026-10-08: keep `widgets/`
   (the registered, tested set) and delete `widget/` (unregistered, placeholder
   data). Quick Toggles / Steps / Audio / Quick Note stay in-app only for now.
2. ~~**Exact-alarm policy on Android 12+**~~ — decided 2026-10-10: no exact
   alarms. `TextClock` covers clock/date, and `SCHEDULE_EXACT_ALARM` was removed.
3. ~~**Room migration shape for `appWidgetId`**~~ — decided 2026-10-11: separate
   `widget_instances` table with a real (non-destructive) v1→v2 migration.
   Edit a placed widget by tapping it; gallery edits still reach widgets not
   customized on their own.
4. ~~**Namespace rename**~~ — done 2026-10-12: namespace and applicationId are
   `com.necroo69.nothingwidget` (GitHub username, not a personal name).
5. **v1.0 scope** — confirm the 4-widget cut (Clock/Date/Battery/Weather).
6. ~~**Weather location source**~~ — decided 2026-10-12 (M2): approximate device
   location, read only while the app is open, or a searched city. No background
   location permission.
7. ~~**Monetization**~~ — decided 2026-10-12: v1.0 free (no ads, no purchases);
   paid features later. An Open-Meteo subscription is needed before the first
   paid feature ships (see "Path to production" #13).
