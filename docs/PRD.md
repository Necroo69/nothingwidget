# NothingWidget — Product Requirements Document (PRD)

Version 2.0 · Rewritten 2026-10-01 from a direct code inspection of branch
`claude/project-status-release-732b84`. Supersedes v1.0, which described an
earlier 4-widget design that no longer matches the codebase.

> Planning source of truth for sequencing is [`roadmap.md`](./roadmap.md) and
> [`backend-roadmap.md`](./backend-roadmap.md). This PRD defines *what* the
> product is and *why*; the roadmaps define *when*.

## 1. Vision

NothingWidget brings the Nothing OS aesthetic — dot-matrix typography, sharp
geometry, red accent — to the home screen of **any** Android device. Built in
Jetpack Compose with a Hilt + Room architecture.

One-line pitch: *"Your home screen, reinvented. Nothing-style. Any phone."*

## 2. Current state (what actually exists)

**Architecture — in place and working**
- Hilt dependency injection (`NothingWidgetApp`, `@AndroidEntryPoint`).
- **Room** database (`AppDatabase`, `WidgetConfigDao`, `StepDao`, `NoteDao`) —
  *not* DataStore. `AppPreferencesRepository` handles app settings.
- Per-screen ViewModels + StateFlow; typed navigation (`Screen`, `NavGraph`).
- Repositories: `WidgetRepository`, `BatteryRepository`, `WeatherRepository`,
  `StepTrackerRepository`, `AudioRepository`, `QuickSettingsRepository`.

**UI — largely built**
- Gallery, Customizer, Settings, Glyph Studio, Widget Studio screens.
- Nothing visual language (dot-matrix text, glass cards, red accent).

**Widget model — 10 types defined** (`WidgetType`): digital clock, analog clock,
world clock, weather, battery circle, step tracker, quick toggles, audio player,
quick note, date.

**Known broken / incomplete (verified in code)**
- **Two provider packages coexist.** `widget/` has 6 `Nothing*WidgetProvider`s
  that render real content but are **not registered** in the manifest. `widgets/`
  has 4 providers that **are** registered; its `ClockWidget` only sets text
  *color* and relies on `TextClock` XML for the time.
- **No AlarmManager / update scheduler exists.** `SCHEDULE_EXACT_ALARM` is
  declared but unused. Clock/date have no app-driven tick.
- **Release build unsafe.** `isMinifyEnabled = true` with no `proguard-rules.pro`
  and no `proguardFiles` — Moshi/Retrofit/Room/Hilt likely crash under R8.
- **Configs keyed by string id**, not `appWidgetId` — no independent
  multi-instance configs.
- **Coverage gaps.** `QUICK_NOTE` has no provider in either package; several types
  can't actually pin.
- **No onboarding, no real tests.** Namespace is still `com.example.nothingwidget`
  (applicationId is correctly `com.sritam.nothingwidget`).

## 3. Target users

- **The Aesthetic Android User** (18–30, Pixel/Samsung/OnePlus) who curates their
  home screen and loves Nothing's design without owning the hardware.
- **The Nothing fan on other hardware** wanting ecosystem visual consistency.
- **The Minimalist** who values reliability and no ads above all.

## 4. v1.0 scope (focused)

Ship **4 rock-solid widgets** first — Clock, Date, Battery, Weather — fully
functional, customizable, persistent, and reliable. The remaining six types
(world clock, step, quick toggles, audio, quick note, analog clock extras) are
**v1.1+** (see roadmap Milestone 4). Breadth is deferred; reliability is not.

| ID | Use case | Priority |
|---|---|---|
| UC-01 | Clock widget shows live time, updates every minute | P0 |
| UC-02 | Date widget shows today's date, updates at midnight | P0 |
| UC-03 | Battery widget shows live %, updates on change | P0 |
| UC-04 | Customize a widget and have it apply to the placed instance | P0 |
| UC-05 | Gallery of widget previews | P0 (done) |
| UC-06 | Widget survives reboot and 72h Doze | P0 |
| UC-07 | Onboarding explains how to add a widget | P1 |
| UC-08 | Weather widget shows real temperature | P1 |
| UC-09 | Each placed instance keeps its own config | P1 |

## 5. Functional requirements

- **FR-01 Updates:** clock via `AlarmManager.setExactAndAllowWhileIdle` (not
  `updatePeriodMillis`); date reschedules at midnight; battery event-driven;
  weather via WorkManager. All re-armed on `BOOT_COMPLETED`.
- **FR-02 Persistence:** Room; configs keyed by `appWidgetId`; survive process
  death and reboot.
- **FR-03 Content:** every placed widget renders real content (time/date/%/temp),
  not just color; graceful "no data / no permission" states.
- **FR-04 Architecture:** one registered provider set; no business logic in
  Composables; typed routes (already satisfied).
- **FR-05 Play Store compliance:** R8 with correct keep rules; signed release;
  privacy policy; `targetSdk == compileSdk` (already 35); decide namespace.

## 6. Non-functional requirements

- Widget update < 100ms; cold start < 1.5s.
- No continuous background service; AlarmManager + WorkManager only.
- Survive 72h Doze and reboot.
- minSdk 26; APK < 8MB.
- Content descriptions on all interactive elements and widgets.
- No hardcoded API keys; no `com.example` in the published applicationId.

## 7. Acceptance criteria (v1.0)

- **Clock:** correct time on placement; updates each minute; survives reboot;
  color customization reflects in the widget; 12/24h toggle works.
- **Date:** correct date; rolls over at midnight; format applies.
- **Battery:** correct %; updates on charge change; color applies.
- **Customization:** apply changes a placed instance within ~3s; two instances of
  a type can differ; selections pre-fill on reopen.
- **Quality:** zero launch crashes; minified release APK runs; R8 enabled with
  keep rules; no dead navigation.

## 8. Success metrics (90 days post-launch)

Downloads 5,000+ · Rating ≥4.4 · D1 retention ≥55% · D7 ≥25% · crash-free ≥99.5%
· ≥65% of users with ≥1 widget placed · ANR <0.5%.

## 9. Monetization

Free core (Clock/Date/Battery) + one-time premium unlock (~$1.49) for Weather,
full color/font options, and future widgets. **No ads.** Alternative: fully free
+ tip jar.

## 10. Future features

Light theme + Material You · world clock · step (Health Connect) · audio
visualizer · quick toggles · quick note · lock-screen widgets (API 33+) · tablet
layouts · preset export/sharing. Sequenced in [`roadmap.md`](./roadmap.md)
Milestone 4.
