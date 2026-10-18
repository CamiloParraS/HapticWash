# SPEC-APP changelog

Changes to [SPEC-APP.md](SPEC-APP.md) after M4, newest first. Each entry says what changed,
why, and where it is implemented. The Core spec is unaffected unless an entry says so.

## 2026-10-18 — Coaching milestone specified (branch `docs/m5-rescope`)

This entry changes the Core spec too (§1.4, §6 M6, section index). Decisions: D33, D34. Spec only,
nothing implemented. Full text: SPEC-APP "Coaching".

### C14 — §4.1.6: the time-reached tick is replaced by `MISSED` when a Step is undetected

- **Was:** three quick ticks at the 40 s target, always.
- **Now:** if any Step is `NOT_DETECTED` at 40 s (Target check, gated on M6), `Pulse.MISSED` plays
  instead and a card names the Steps; otherwise the ticks play as before.
- **Why:** name the skipped Step while hands are still soapy. One vibration event keeps the
  once-per-3 s rule.

### C15 — M4 Summary: tappable Step rows open a Step guide; Focus Step line

- **Was:** Summary rows are static; no illustrations or reasons.
- **Now:** each row opens a page with the Step's name, illustration and a sourced why line. A
  habitually undetected Step gets one estimate-language line on Summary.
- **Why:** knowing what a Step reaches supports the Steps people skip; evidence for post-Wash
  feedback is stronger than for live feedback.

### C16 — M4 banned-word list: claim words

- **Was:** health-result words only (D29).
- **Now:** claim words (EN + ES) join the list so why lines cannot drift into infection claims.

## 2026-10-17 — Twist start opens the app (branch `docs/m5-rescope`)

### C13 — §2.1: `USE_FULL_SCREEN_INTENT`

- **Was:** a Twist-started Wash ran in the service with the screen unchanged.
- **Now:** `WashService` posts a transient full-screen-intent notification that opens the app on the Wash screen. Adds `USE_FULL_SCREEN_INTENT`; the forbidden list is unchanged.
- **Why:** a background service can't start an activity (Android 10+). Android 14+ may require the user to grant this permission for non-call/alarm apps; unverified on a real watch, check at M6.
- **How:** `WashService.openApp()`, `MainActivity` navigates to `wash` when a Wash starts off the Start screen.

## 2026-10-11 — Background listening settled (branch `docs/m5-rescope`)

Decision: D32. The Core spec is unaffected.

### C12 — §2.1: the foreground service is type `health`, not `dataSync`

- **Was:** a `dataSync` foreground service; `HIGH_SAMPLING_RATE_SENSORS` not needed.
- **Now:** a `health` foreground service, for the Wash and for Twist start's listening.
  Permissions swap `FOREGROUND_SERVICE_DATA_SYNC` for `FOREGROUND_SERVICE_HEALTH` and declare
  `HIGH_SAMPLING_RATE_SENSORS`, which qualifies the type without a runtime prompt. The
  forbidden list is unchanged. Listening uses batched wake-up sensors with no wake lock.
- **Why:** with targetSdk 37, `dataSync` gets 6 h of background time a day and can't start at
  boot, so Twist start would stop most days and not survive a reboot. `health` has neither limit.
- **How:** not implemented yet. Ticket 04 (starts with a sensor probe on the watch) and 05.

## 2026-10-10 — M5 re-scoped (branch `docs/m5-rescope`)

This entry changes the Core spec too (§1.2, §6, §7, §11, new §13). Decision: D30.

### C11 — M5-App: Twist start and Auto-end replace always-on spotting

- **Was:** M5-App runs the M5-AI spotting model on-watch, duty-cycled, to start a Wash without a
  tap. It depends on M5-AI.
- **Now:** twisting the wrist three times starts a Wash (Twist start, toggle off by default, with
  the §4.1.8 battery warning). Every model-backed Wash ends itself on sustained `NULL` (Auto-end):
  cancelled if no washing is seen within 30 s, nudged to keep going if washing pauses before 40 s,
  ended normally if washing stops after 40 s. §4.1.6 gains the keep-going nudge. M5-AI moves to
  Future Work, so M5-App no longer depends on it.
- **Why:** a deliberate gesture is cheap, explainable and testable now. Spotting needs a 31.6 GB
  ingest, a second model, and duty-cycling, and published spotting F1 is low enough that false
  starts would annoy. Auto-end lets a hands-free start end hands-free too.
- **How:** not implemented yet. Tickets in `.scratch/m5-twist-start/issues/` (02 detector,
  03 Auto-end, 04 toggle and listening, 05 reboot, 06 report and audit).

## 2026-09-30 — Apple-style polish (branch `feat/state-machine`)

Accepted on the same rule as C1–C3.

### C4 — §4.1.6: missed step is three taps, not one long pulse

- **Was:** missed-step notice is "one long pulse" (600 ms).
- **Now:** three short taps, the last one falling (60 ms on, 160 ms off). Still at least 3 s after
  the end pulses, and still distinct from the time-reached ticks, which are lighter and twice as fast.
- **Why:** a long buzz reads as an alarm. A missed Step is a nudge to wash a bit longer next time,
  not an error, and Apple's own "failure" haptic is a short series of taps.
- **How:** `Pulse.MISSED` in `Haptics.kt`. `HapticsTest` checks the gap and that every pattern is
  distinct.

### C5 — §4.1.6: end pulses rise when all five Steps look covered

- **Was:** one end pattern for every Wash.
- **Now:** when Coverage shows all five Steps as "looks covered", the second end pulse rises
  instead of tapping. It is still two short pulses. The Summary ring gives one small spring pop as
  its last arc lands.
- **Why:** the best result deserves a moment of its own, felt on the wrist with eyes on the sink.
  It is rare, so it stays special; every other screen stays bounce-free.
- **How:** `Pulse.SUCCESS` and `Haptics.onEnd`; the pop is in `CoverageRing`. All patterns now play
  as haptic primitives (`VibrationEffect.Composition`) where the watch supports them, with the
  waveform timings as the fallback.

### C6 — M4 Summary: the live ring becomes the Coverage ring

- **Was:** In-progress → Summary is a plain screen change.
- **Now:** when a Wash ends with the progress ring on and Coverage shown, the live ring shrinks
  from the screen's edge into the Summary's Coverage ring and fades as that ring appears.
  Critically damped, no bounce. Opening a Summary from History has no transition.
- **Why:** things should come from somewhere. The ring the user watched while washing becomes the
  answer, so the Summary reads as the end of the same moment, not a new page.
- **How:** `SummaryScreen(fromWash = true)` on the `result/{id}` route, in `MainActivity.kt`.

### C7 — M4 In-progress: the ring blooms from the Start button

- **Was:** Start → In-progress is a plain screen change.
- **Now:** the ring and the countdown grow from where the Start button was. Critically damped, no
  bounce; with the ring setting off, the countdown fades in alone.
- **Why:** same as C6: things should come from somewhere. Start's button becomes the ring it started.
- **How:** `StartOrigin` (Start writes its button's place, `WashScreen` reads it) and the `enter`
  animation in `MainActivity.kt`.

### C8 — M4 In-progress and Summary: the big moments are seen as well as felt

- **Was:** the ring changed colour at the 40 s target; an all-covered Summary gave one ring pop.
- **Now:** at the target a thin ripple travels in from the screen's edge and the number pops, on the
  frame of C1's three ticks. When all five Steps look covered, droplets leave the Coverage ring as
  it pops (C5). Neither plays for a target that passed while the screen was away.
- **Why:** sight and touch should agree. The ripple and burst are the visual half of C1 and C5.
- **How:** `ripple`/`pop` in `WashScreen`; `burst` in `CoverageRing`. The Wash loop now wakes exactly
  at the target when the ring is off, so the ripple is not up to 250 ms late. The Summary burst
  follows the last arc landing (as the C5 pop does), so it trails the rising pulse by about 250 ms.

### C9 — M4 Summary: one big number, a glyph per Step, a warmer line

- **Was:** "2 of 5" in title type; ✓ ◐ ○ as the row's leading mark; "Washed for 1:07".
- **Now:** the count is one large rounded numeral with "of 5 look covered" under it. Each row leads
  with its Step's own glyph; the Status mark sits beside the Status words. All five covered reads
  "Nice work. Washed for 1:07". Language stays within §4.1.9 (`LanguageTest`).
- **Why:** the answer should be readable at arm's length, and rows should be found by shape.
- **How:** `CoverageRing`, `STEP_ICONS` and `res/drawable/ic_step_*.xml`, from Material Symbols (Apache 2.0): folded hands, back hand, handshake (closest to interlaced fingers), thumb up, fingerprint. The arcs carry no glyphs:
  at 124 dp they are too thin and the ring's centre is full.

### C10 — §4.1.7: an ambient display for the Wash (replaces M4's "no ambient UI")

- **Was:** "No ambient UI. The screen may turn off normally; the service keeps sensing."
- **Now:** while a Wash runs and the watch goes ambient, the app shows outlines on black: the ring's
  circle and "Washing". No time is shown: ambient redraws about once a minute, and a Wash is about
  that long. Waking returns to the live screen as it was. At any other time the app is not
  ambient-aware, so the system handles ambient as for any app.
- **Why:** mid-Wash the wrist is often down, and the first thing the screen should show on a glance is
  that the Wash is still on.
- **How:** `AmbientLifecycleObserver` (`androidx.wear:wear`), registered only while a Wash runs
  (`MainActivity.watchAmbient`); `AmbientWash`. Burn-in protection moves the outline each update.
  Not handled: low-bit ambient displays (no anti-aliasing change).

## 2026-09-30 — UX redesign (branch `feat/ui-haptics`)

Accepted on the rule "spec changes are welcome when they give better functionality and UX".

### C1 — §4.1.6: a fourth vibration, "time reached"

- **Was:** three patterns (start, end, missed step).
- **Now:** also three quick ticks (40 ms on, 80 ms off) when the Wash reaches the 40 s target.
  The 3 s minimum spacing is unchanged.
- **Why:** hands are wet and eyes are on the sink, so the wrist should say when enough time has
  passed, as Apple's Handwashing timer does. 40 s is the low end of WHO's 40–60 s.
- **How:** `Haptics.onStart` schedules the ticks at `TARGET_MS` with the start pulse. A Wash that
  ends earlier cancels them (`Buzzer.play` cancels pending buzzes). If a Wash ends within 3 s of
  the ticks, the end pulses wait for the gap. Replays and the Haptics setting behave as for the
  other patterns. `HapticsTest` checks the timing and that every pattern is distinct.

### C2 — M4 In-progress: count down to the target, then elapsed

- **Was:** "elapsed time + subtle progress".
- **Now:** time left to the 40 s target ("0:24 to go"), then the total ("0:43 · 40 s reached").
  The optional progress ring is unchanged and still turns `tertiary` when full.
- **Why:** a countdown gives a finish line, and the ring is off by default, so the number is
  the only goal most people see. Past the target the total stays visible, so nothing is lost.
- **How:** `WashScreen` in `MainActivity.kt`; the target is `Haptics.TARGET_MS`, shared with C1.

### C3 — M4 Settings: no always-on placeholder

- **Was:** Settings includes an "always-on toggle placeholder".
- **Now:** Settings has wrist side, haptics and progress ring. Always-on appears with M5, with its
  battery warning (§4.1.8).
- **Why:** a disabled switch that does nothing costs a scroll and a moment of thought on a small
  screen. It was removed in the polish pass (`UX_AUDIT.md`), which this entry makes official.
- **How:** already implemented in `fcf6652`.

## Coaching: why lines reworded (2026-10-18)

Why lines no longer cite a study ("about 1 in 4 missed it in one study"). They say why the Step matters
in terms of what hands do, with no figures, so the Škodová figure check is no longer an open gap. The Guide
also leads with a one-line technique. SPEC-APP §Coaching deliverable 2 and Open gaps, CONTEXT "Why line",
`step_whys` (EN, ES) and `StepGuideTest` updated.

## Coaching: why lines allow contact reasoning (2026-10-18)

SPEC-APP §Coaching deliverable 2 now allows contact reasoning ("pick up whatever you touch", "gets trapped
between fingers") so a why line can say why a Step is worth doing. Still banned: named germs or illnesses,
prevent/protect/safe, any outcome promise, statistics. `step_whys` (EN, ES) and `StepGuideTest` updated.

