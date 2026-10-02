# NothingWidget — Development Phases

Version 2.0 · Corrected 2026-10-01 against the actual code. Earlier revisions
marked phases "✅ DONE" from commit messages; several of those claims were not
true in code and have been corrected below.

> Forward planning now lives in [`roadmap.md`](./roadmap.md) (master) and
> [`backend-roadmap.md`](./backend-roadmap.md) (widget engine). This file records
> the **historical** phases and their *verified* status.

## Important correction

The original phases were written for the pre-migration "Repo 1" app (simple
4-widget, no Hilt/Room). The project later migrated to the "Repo 2" (Google AI
Studio) base — Hilt, Room, ~10 widget types, glyph/studio screens. That migration
record now lives in [`history/implementation_plan.md`](./history/implementation_plan.md).

## Historical phases — verified status

| Phase | Original claim | Verified status | Notes |
|---|---|---|---|
| 0 — Triage & blockers | done | ⚠ **Partial** | appId fixed, SDKs aligned (35), R8 *enabled* but **no keep rules**; namespace still `com.example`; dead nav removed |
| 1 — Architecture foundation | ✅ done | ✅ **Done** | Hilt + Room + MVVM + typed nav verified in code |
| 1b — Repo 2 migration | ✅ done | ✅ **Done** | Room/Coil/Retrofit/Moshi integrated; old Repo 1 UI removed |
| 2 — Widget engine (AlarmManager per-minute / midnight updates) | ✅ done | ❌ **NOT done** | **No AlarmManager exists anywhere.** Clock relies on `TextClock` XML; registered `ClockWidget` sets color only. Battery event-handling ✅ |
| 3 — Settings persistence | ✅ done | ⚠ **Partial** | Settings persist via Room/prefs; refresh-rate → scheduler wiring unverified (no scheduler exists yet) |
| 4 — Home/gallery live previews | ✅ done | ✅ **Mostly** | Gallery renders; previews present |
| 5 — Customization persistence & apply | ✅ done | ⚠ **Partial** | Config saves to Room, but keyed by string id (not `appWidgetId`); apply-to-placed-instance path needs verification |
| 6 — Onboarding | planned | ❌ **Not started** | no onboarding screen exists |
| 7 — Weather real data | planned | ⚠ **Partial** | `WeatherRepository` + `WeatherWorker` exist; real-data/offline/permission flow unverified |
| 8 — Accessibility & polish | planned | ❌ **Not started** | |
| 9 — Testing | planned | ❌ **Not started** | only placeholder tests |
| 10 — Play Store release | planned | ❌ **Not started** | no signing config / assets |

## Forward phases (see roadmaps for full detail)

These replace the stale "Phase 2–10" plans above. Ordered by dependency/risk.

**Milestone 1 — engine** (`backend-roadmap.md`)
- **B1** Widget system consolidation — one registered provider set, cover every type
- **B2** Reliable updates — AlarmManager + receiver; Doze + reboot
- **B3** Multi-instance configs — key on `appWidgetId` (+ Room migration)
- **B4** Release-build safety — `proguard-rules.pro` keep rules + `proguardFiles`

**Milestone 2 — v1.0 product** (`roadmap.md`)
- **P5** Customization apply end-to-end
- **P6** Weather real data (OpenMeteo + location + offline)
- **P7** Onboarding + permissions UX
- **P8** Settings drive the engine

**Milestone 3 — quality & release** (`roadmap.md`)
- **P9** Testing · **P10** Accessibility & polish · **P11** Play Store release

**Milestone 4 — expansion (v1.1+)**: world clock, step, audio, quick toggles,
quick note, Material You, tablet layouts, preset sharing.
