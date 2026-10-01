# SPEC-APP changelog

Changes to [SPEC-APP.md](SPEC-APP.md) after M4, newest first. Each entry says what changed,
why, and where it is implemented. The Core spec is unaffected unless an entry says so.

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
