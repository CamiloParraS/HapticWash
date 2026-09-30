# SPEC-APP changelog

Changes to [SPEC-APP.md](SPEC-APP.md) after M4, newest first. Each entry says what changed,
why, and where it is implemented. The Core spec is unaffected unless an entry says so.

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
