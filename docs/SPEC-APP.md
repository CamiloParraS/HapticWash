# HapticWash — Wear OS App Specification

**Repo:** `HapticWash - Main` (Kotlin, Wear OS app + phone module)
**Scope:** everything that runs on the device: sensing, the on-device preprocessing
mirror, LiteRT inference, coverage logic, UI, storage, and privacy guarantees. **No model
training happens here** — the model arrives as a release artifact from `HapticWash-AI`.

> Shared contracts (label set §1.3, CSV §8.1, `model_meta.json` §8.2, tensor §8.3,
> preprocessing §8.7, parity §9.2, budgets §4.3, agent rules §10) live in the
> [Core spec](../../HapticWash-AI/docs/HapticWash-SPEC.md), which wins on conflict. The model pipeline has its own
> spec (`HapticWash-AI/docs/SPEC-AI.md`). § numbers are global.

---

## 2. Tech Stack

### 2.1 `HapticWash` — Wear OS app

| Concern    | Choice                                                            | Notes                       |
| ---------- | ----------------------------------------------------------------- | --------------------------- |
| Language   | Kotlin                                                            | JVM target 17               |
| Min SDK    | 30 (Wear OS 3)                                                    | Target latest stable        |
| UI         | Jetpack Compose for Wear OS (`androidx.wear.compose`)             | Not phone Compose Material  |
| Lists      | `ScalingLazyColumn` / `TransformingLazyColumn`                    | Never plain `LazyColumn`    |
| Async      | Coroutines + `Flow`                                               |                             |
| Sensors    | `SensorManager`, `TYPE_ACCELEROMETER` + `TYPE_GYROSCOPE`          | 50 Hz, see §8.1             |
| Background | Foreground `Service`, type `health` (D32)                         | Notification required       |
| Inference  | LiteRT (TensorFlow Lite) Android runtime                          | CPU only, pin exact version |
| Storage    | Room (sessions + step coverage)                                   | Local only                  |
| Prefs      | DataStore (Preferences)                                           | wrist side, toggles         |
| DI         | Hilt (optional; manual DI acceptable if it keeps the graph small) |                             |
| Tests      | JUnit5 + Turbine (unit), AndroidX Test + Espresso (instrumented)  |                             |
**Permissions:** `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_HEALTH`, `HIGH_SAMPLING_RATE_SENSORS`,
`POST_NOTIFICATIONS`, `USE_FULL_SCREEN_INTENT`, `VIBRATE`, `WAKE_LOCK` (partial wake lock held only for the
duration of an active session, so sensor delivery continues with the screen off;
confirm it is needed on the emulator at M3 — D12).
Accelerometer and gyroscope at 50 Hz require **no** runtime permission.
`HIGH_SAMPLING_RATE_SENSORS` is declared only because it qualifies the `health` service type
(install-time, no prompt; D32); the app never samples above 200 Hz.
`USE_FULL_SCREEN_INTENT` is declared only so a Twist-started Wash can bring the app to the screen from the
background (a service can't start an activity); the notification it posts is transient and is shown only then.
Do **not** request `BODY_SENSORS`, `ACTIVITY_RECOGNITION`, `INTERNET`, or location.
`INTERNET` absence is a hard architectural guarantee — see §4.2.

## 3. Repository Layout

```
HapticWash/                          # Wear OS app
├── wear/src/main/java/com/hapticwash/
│   ├── sensing/                     # MotionSource interface, Real + Replay impls
│   ├── pipeline/                    # filtering, windowing, normalization
│   ├── inference/                   # LiteRT wrapper, model_meta parsing
│   ├── session/                     # coverage state machine, session lifecycle
│   ├── data/                        # Room entities, DAOs, repository
│   └── ui/                          # Compose screens
├── wear/src/main/assets/model/      # model.tflite + model_meta.json (from AI repo release)
├── wear/src/androidTest/            # instrumented: parity tests, replay E2E
├── wear/src/test/                   # unit: state machine, windowing, filters
```

Shared contracts: link to the [Core spec](../../HapticWash-AI/docs/HapticWash-SPEC.md), don't copy it.

---

## 4. Architectural Constraints

### 4.1 Wear OS UI/UX constraints

These are non-negotiable and apply to every screen.

1. **Glanceable.** Any screen must be comprehensible in under 5 seconds. If a screen
   needs a paragraph, it is the wrong screen.
2. **Round-safe.** Assume a circular display. No content in the corners. Use
   `ScalingLazyColumn` so first and last items scale toward the bezel. Always include
   `TimeText` at the top of a scrollable screen.
3. **Touch targets ≥ 48dp.** Primary action per screen: exactly one.
4. **One-tap start.** Starting a wash must be a single tap from the app's main screen.
5. **No text entry, no dialogs with more than two options, no nested navigation deeper
   than two levels.**
6. **Haptics over sound.** Feedback is vibration. Use short, distinct patterns:
   session start (one short pulse), session end (two short pulses, the second rising
   when all five Steps look covered), missed-step notice (three short taps), time reached
   at the 40 s target (three quick ticks; see [SPEC-CHANGELOG](SPEC-CHANGELOG.md) C1, C4, C5),
   keep going when washing pauses before the target (a gentle nudge, distinct from every
   other pattern; M5, C11). Never vibrate more than once per 3 seconds.
7. **Ambient mode.** During an active wash the app must handle ambient/always-on
   transitions without stopping sensor collection or crashing.
8. **Battery honesty.** Continuous 50 Hz accelerometer + gyroscope is expensive. Manual
   mode must stop the sensor listener the moment a session ends. Twist start (M5) listens
   in the background, so it must show an explicit battery warning at enable time.
9. **No result is presented as certainty.** Coverage UI language is "looks like",
   "may have been brief", "not detected". Never "you missed" / "you failed" / "clean".
10. **Accessibility.** All interactive elements carry `contentDescription`. Do not encode
    step status by colour alone; pair colour with an icon or text.

### 4.2 Offline / online AI execution policy

**Hard rules, enforceable by test.**

- **R1.** The Wear OS app declares no `INTERNET` permission. An instrumented test asserts
  this by reading the merged manifest. Any PR that adds it fails review automatically.
- **R2.** All inference required for the core feature runs on-watch, offline, in-process,
  on CPU. No dependency on a phone connection, network, or companion app.
- **R3.** Raw or windowed IMU samples never leave the watch. Not to a phone, not to a
  server, not to logs shipped off-device.
- **R4.** Tier 3 summaries (§2.3), if ever built, live in a **separate phone module**,
  are opt-in, transmit only aggregate counts and durations, and cache their last result
  so the feature is inert rather than broken when offline.
- **R5.** No model weights are downloaded at runtime. The model is bundled in `assets/`
  and versioned with the app build.
- **R6.** If model loading fails for any reason, the app degrades to a **timer-only
  mode** (count the wash, show duration, show no step coverage) and displays a single
  non-blocking notice. It must never crash and never show fabricated coverage.

---

## 6. Milestone Requirements

App-side milestones. M0 and M6 are in the Core spec; M1/M2 in the AI spec.

---

### M3 — App skeleton, replay harness, parity

**Objective.** Get the full production pipeline running end-to-end on an emulator against
real, labelled human data — and prove the model deployed on the device computes the same
thing as the model evaluated in Python.

This milestone is the answer to "I don't own a Wear OS device." The Wear OS emulator can
only simulate accelerometer and gyroscope through Extended Controls → Virtual Sensors →
Device Pose sliders, which cannot produce a realistic 50 Hz handwash trace. The replay
harness replaces it.

**Deliverables**

- `MotionSource` interface (§8.4) with `RealSensorSource` and `ReplaySource`.
  Everything downstream is source-agnostic.
- `ReplaySource` reads a canonical CSV pushed via `adb push` and emits samples honouring
  original inter-sample timing, with an optional speed multiplier for tests.
- Broadcast-intent trigger for replay (§8.5) so replay is demoable from the command line.
- Kotlin implementations of filtering, windowing, normalization, and feature extraction
  that mirror `HapticWash-AI` exactly.
- LiteRT inference wrapper reading `model_meta.json` at load time. Hardcoded shapes,
  label orders, or normalization constants in Kotlin are a **spec violation**.
- Foreground service collecting at 50 Hz.
- R8 keep rule for LiteRT in the release build (`-keep class org.tensorflow.** { *; }`);
  a release-build smoke test loads the model.
- **Golden-file parity test** (§9.2).
- Minimal UI: start / stop / raw predicted label. No polish yet.

**Acceptance criteria**

- `adb`-triggered replay of a held-out session from `zhang_who` runs end to end on the
  emulator and produces a per-window prediction stream.
- Parity: for ≥ 50 golden windows, on-device probability vectors match Python outputs
  within **1e-2 absolute** per class, and top-1 labels match on **≥ 98 %** of windows.
- Measured inference latency ≤ 15 ms/window (report the median and p95 from the emulator;
  treat as indicative, confirm on hardware at M6).
- Manifest test asserting `INTERNET` is absent (R1).
- Model-load-failure test: with a corrupt `model.tflite`, the app enters timer-only mode
  and does not crash (R6).

**Verification**

- `./gradlew :wear:testDebugUnitTest`
- `./gradlew :wear:connectedDebugAndroidTest` on a Wear OS AVD
- `scripts/replay_demo.sh <session.csv>` — documented, one command, works from a clean
  checkout.

---

### M4 — Coverage state machine, feedback, history

**Objective.** Turn a stream of window predictions into something a person can act on.

**Deliverables**

- `CoverageStateMachine` (§8.6): consumes smoothed predictions, accumulates seconds per
  step, emits per-step status `NOT_DETECTED | BRIEF | OK`.
- Thresholds in a single configuration object, not scattered constants. Defaults:
  `OK` ≥ 4 s cumulative, `BRIEF` ≥ 1.5 s, otherwise `NOT_DETECTED`; minimum session
  duration 10 s for coverage to be shown at all.
- Compose screens: Start, In-progress (time left to the 40 s target, then elapsed time;
  subtle progress), Summary (per-step status), History (last 20 washes), Settings (wrist
  side, haptics, progress ring). Twist start gets its setting with M5. See
  [SPEC-CHANGELOG](SPEC-CHANGELOG.md) C2, C3.
- Room persistence of sessions and per-step durations.
- Haptic patterns per §4.1 item 6.
- Wrist-side setting wired through to the mirroring transform.

**Acceptance criteria**

- State machine unit tests covering: all steps covered; one step entirely missing; a step
  detected in two non-contiguous bursts summing above threshold; a 4-second session
  (must show no coverage); a session with 40 % `WASH_OTHER`; an empty prediction stream.
- Replaying a labelled session yields step statuses consistent with its ground-truth step
  durations for ≥ 4 of 5 steps, on ≥ 8 of 10 held-out sessions.
- History survives process death and device reboot.
- UI audit against every item in §4.1, recorded as a checklist in `docs/UX_AUDIT.md`
  with a screenshot of each screen on a round AVD.
- No user-facing string asserts cleanliness or uses failure language. Enforced by a lint
  test over the strings resource against a banned-word list.

**Verification**

- `./gradlew :wear:testDebugUnitTest --tests '*CoverageStateMachine*'`
- `scripts/replay_batch.sh` over 10 held-out sessions, producing a comparison report.
- Manual UX audit checklist.

---

### M5-App — Twist start and Auto-end _(optional for v1)_

**Objective.** Start and end a Wash without touching the screen: twist the wrist three
times to start, and the Wash ends itself when washing stops. No second model: automatic
wash detection (spotting) is Future Work (Core §13, D30).

**Deliverables**

- **Twist detector.** Pure logic over the `MotionSample` stream: three forearm rotations
  (gyroscope roll peaks of alternating sign) inside a short window fire once, then a
  refractory period. Threshold, window and refractory period live in one configuration
  object; they are calibration knobs, tuned on hardware at M6.
- **Twist start setting.** "Start with a twist", off by default. Enabling it shows the
  §4.1 item 8 battery warning. While on, the watch listens in the background at a low
  sample rate, on batched wake-up sensors in the `health` service (D32); a Twist starts a Wash exactly as tapping Start does. Listening pauses during
  a Wash, resumes after it, and survives a reboot and a process kill.
- **Auto-end**, for every `sensor` Wash with the model loaded, driven by smoothed `NULL`:
  - no washing seen within a give-up window (30 s) from the start → the Wash is cancelled
    and not saved; before that it waits, so walking to the sink is fine;
  - a sustained `NULL` stretch before the 40 s target → the keep-going nudge (§4.1 item 6);
  - a sustained `NULL` stretch after the target → the Wash ends normally (Summary, end
    pattern, History). The Wash ends when washing stops, never at exactly 40 s.

  "Sustained" is a calibration knob. Manual Stop always works. Timer-only mode has no
  `NULL` signal, so it has no Auto-end. Replays follow the same rules.
- Battery-impact estimate for background listening.

**Acceptance criteria**

- Detector unit tests: three twists fire once; two twists, or three spread wider than the
  window, don't; one slow roll doesn't; held-out washing sessions produce no fires;
  mirrored right-wrist motion fires as left does.
- Auto-end unit tests: all-`NULL` for the give-up window → cancelled; idle then washing →
  not cancelled; a `NULL` gap before 40 s → one
  nudge and the Wash continues; sustained `NULL` past 40 s → ends with Coverage; a brief
  `NULL` blip past 40 s → no stop; Timer-only → no Auto-end.
- Toggle defaults **off**; with it off, the app holds no sensor connection and no wake lock
  outside a Wash.
- No new permission from the §2.1 forbidden list.

**Verification**

- Replay one held-out `ocdetect` participant-day through the detector and Auto-end; report
  spurious Twist starts and how many Auto-end cancelled. Any number is acceptable;
  omitting it is not.
- Emulator: a debug-only replay into the listener starts a Wash; repeat after a reboot.

---

### Coaching — Step guide, Focus Step, Target check _(post-M6)_

**Objective.** Help people stop skipping particular Steps, classically Thumbs and Fingertips, with
knowing the five Steps as support. Decisions: D33 (Target check), D34 (artwork); Core §1.4 amended.
Nothing here claims a Wash was good or hands are clean (§4.1.9); every output is an estimate.

**Evidence base (why it is shaped this way).** Showing missed Steps after a Wash helps (Lehotsky
2015: inadequate rubs 50 % → 15 %), so Summary stays. Live per-step feedback helps only while it
stays on (Lacey 2020), and nothing tests a single check at 40 s, so the Target check is a bet gated
on M6, not a proven effect. Thumbs, then Fingertips, are the most skipped Steps (Arias 2016). Full
notes: `research/feedback-timing-and-missed-steps.md` and `research/why-lines-and-who-licence.md`
(branches of the same names).

**Deliverables**

1. **Step guide.** A detail page behind each of the five Summary rows (Step name, illustration,
   why line, in that order), reached by tapping the row, and a pager (swipe sideways, page dots) walks the other four Steps in
   canonical order so one tap reaches every guide. All five rows are tappable whatever their
   status. History needs nothing new: its rows open the same Summary. No entry from Start or
   Settings (Start keeps its single action; Summary → page is two levels, §4.1.5). WHO's sixth
   motion ("backs of fingers to opposing palms") has no Step: it is left out, with no row and no
   claim that it is covered. Summary rows themselves carry no illustration (too small at watch size).
2. **Why lines.** One line per Step, ≤ ~60 characters, saying why that part of the hand needs its
   own attention in a Wash. Allowed: contact reasoning, meaning what that part of the hand does and
   picks up ("pick up whatever you touch", "gets trapped") and why the other rubbing misses it.
   Not allowed: named germs, bacteria, viruses or illnesses; "prevent", "protect", "safe" or any
   promise of an outcome ("clean hands", "kills"); study statistics (they need the paywalled figures
   checked, and a percentage does not say what to do differently); poster wording. A line says why
   the Step is worth doing, never that doing it makes hands clean or safe. Fingertips are never
   called "the most missed" (contested: Szilágyi 2013). The lines carry no figures, so they are
   unsourced; that is deliberate. The page leads with a one-line technique, then the why line.
   English:
   - Palms: "Palms handle the most, so they pick up the most"
   - Back of hands: "Backs of hands touch things too; palms never reach them"
   - Between fingers: "What you touch gets trapped between fingers and skipped"
   - Thumbs: "Thumbs grip everything, so they pick up whatever you touch"
   - Fingertips: "Fingertips touch first, and only get soap if aimed there"

   Strings are English and Spanish; `LanguageTest` scans both for the banned words. (Replaces the
   earlier "in one study" lines from Škodová 2015; see SPEC-CHANGELOG.)
3. **Step artwork.** The Hegasy sheet "Hand Disinfection according to DIN EN 1500" (Wikimedia
   Commons, CC BY-SA 4.0), cropped to one asset per Step and kept as separate files with a licence
   notice in the repo and in the app (credit, link, licence, note that the images were cropped).
   If the crops do not read on a round watch display, the guide falls back to the app's current
   per-Step icons (D34). WHO and CDC poster images are not used.
4. **Focus Step.** One Step shown on Summary only, as one estimate-language line under the Coverage
   rows (for example "Thumbs: often not detected lately"), opening the same Step guide page as that
   Step's row.
   - Window: the last 10 `sensor` Washes that have Coverage. Timer-only and too-short Washes are
     skipped (they neither count as misses nor fill the window). None until at least 5 qualify.
   - Candidate: `NOT_DETECTED` or `BRIEF` in ≥ 60 % of the window **and** `NOT_DETECTED` alone in
     ≥ 30 %. Both numbers are calibration knobs in the single configuration object (§M4).
   - Highest `NOT_DETECTED` rate wins; on equal rates the later Step in canonical order. Exactly one
     at a time; nothing shown if none qualifies (no praise, no streak, no "you improved").
   - Computed on demand from History: no stored state, no new permission. It disappears when the
     window stops qualifying it. Not shown on Start or the Tile.
5. **Target check.** At the 40 s target of a `sensor` Wash, if any Step is `NOT_DETECTED`:
   - `Pulse.MISSED` plays **instead of** the time-reached `TARGET` ticks (one vibration event, no
     new pattern). If every Step has some detection, `TARGET` plays as today.
   - The in-progress screen swaps to a card naming the undetected Steps (name + icon, not colour
     alone), in "not detected" language, never "you missed". It closes after 6 s or on tap; the
     Wash keeps running behind it.
   - Fires at most once per Wash. If the Wash ends while the card is up, the card closes and the
     end pulses play after the usual 3 s gap. A washing pause does not dismiss it. If a keep-going
     nudge moved the target, the check follows the rescheduled 40 s, as the ticks do, and respects
     the 3 s gap (§4.1.6).
   - No Focus Step emphasis: the card names only this Wash's undetected Steps. Timer-only Washes
     have no Coverage and get no check.
   - **Gated.** Ships enabled only if M6 meets the gate (Core M6, D33). Settings gets a "Target
     check" toggle, on by default once the gate passes. If the gate is missed, the code ships
     disabled behind a build flag with no UI mention, and `LIMITATIONS.md` records the miss and the
     numbers.
   - Every Wash with the check on logs the nudge timestamp, so its post-nudge windows stay
     separable from untouched ones.
6. **Language rules.** Claim words (the terms the Step guide could drift into when describing what
   a Step reaches) join the banned-word list of the M4 string lint (D29), English and Spanish. New
   strings go through that lint.

**Acceptance criteria**

- Focus Step unit tests: fewer than 5 qualifying Washes → none; Timer-only and too-short Washes
  neither count nor fill the window; a Step at 60 % but under 30 % `NOT_DETECTED` → none; two
  qualifying Steps → higher `NOT_DETECTED` rate wins; equal rates → later canonical Step; a window
  that stops qualifying → none again.
- Target check unit tests: nothing `NOT_DETECTED` → `TARGET` ticks, no card; any `NOT_DETECTED` →
  `MISSED` and no `TARGET`; fires once; Wash ending while the card is up closes it and orders the
  end pulses ≥ 3 s later; a washing pause leaves it up; a nudge-shifted target moves the check;
  Timer-only → nothing; toggle off or gate flag off → `TARGET` as today.
- Haptics test: still no two vibrations within 3 s and every pattern distinct.
- Navigation test: each of the five Summary rows (and the Focus Step line) opens its guide page;
  Start and Settings hold no route to it; no screen deeper than two levels.
- String lint passes over all new EN and ES strings. No new permission; the manifest still has no
  `INTERNET` (R1).
- UI audit entry in `docs/UX_AUDIT.md` with round-AVD screenshots of the guide page (all five
  Steps, one of them with a long Spanish line) and the Target check card, against every §4.1 item.
- Artwork: all five crops are checked on a round display, with particular attention to Back of hands
  against Between fingers, which are near-identical silhouettes. Record the outcome (use the crops,
  or fall back to icons).

**Verification**

- `./gradlew :wear:testDebugUnitTest`
- Replay a labelled session with a Step missing past 40 s: the card and `MISSED` appear once.
- M6 on `own_watch`: report the gate numbers in **Core M6**. Any number is acceptable; omitting it
  is not.

**Open gaps (not decided; do not invent answers while slicing)**

- **Spanish wording** of the why lines and every new string.
- **Where the artwork credit appears in the app** (the licence needs credit, link and licence
  notice; only "separate assets with a notice" is settled), and whether CC BY-SA ShareAlike reaches
  the app's own licence.
- **Panel-to-Step mapping** of the Hegasy sheet is the researcher's reading; confirm when cropping.
- **Gate numbers are calibration knobs** (Core M6): 10–20 Washes is a tiny sample, so report raw
  counts.

---

## 8. App Contracts

### 8.4 `MotionSource`

```kotlin
data class MotionSample(
    val timestampNs: Long,
    val ax: Float, val ay: Float, val az: Float,
    val gx: Float, val gy: Float, val gz: Float,
)

interface MotionSource {
    /** Cold flow. Collecting starts acquisition; cancelling stops it. */
    fun stream(): Flow<MotionSample>
    val nominalRateHz: Int
}
```

`RealSensorSource` registers accelerometer and gyroscope at `samplingPeriodUs = 20_000`
and pairs them by nearest timestamp, dropping unpaired samples. `ReplaySource` reads a
canonical CSV and emits with original inter-sample delays, scaled by `speed`.

No code outside `sensing/` may reference `SensorManager` or `SensorEvent`.

### 8.5 Replay trigger intent

```
adb shell am broadcast \
  -a com.hapticwash.REPLAY \
  --es path /sdcard/Download/session_042.csv \
  --ef speed 1.0 \
  com.hapticwash/.sensing.ReplayReceiver
```

Registered in debug builds only. Must not be present in the release manifest; a manifest
test asserts this.

### 8.6 Session and coverage contract

```kotlin
enum class StepStatus { NOT_DETECTED, BRIEF, OK }

data class StepCoverage(val label: Int, val seconds: Float, val status: StepStatus)

data class WashSession(
    val id: Long,
    val startedAtEpochMs: Long,
    val durationS: Float,
    val coverage: List<StepCoverage>,   // labels 1..5, always length 5, always in §1.3 order
    val washOtherS: Float,
    val modelVersion: String,           // from model_meta.json
    val source: String,                 // "sensor" | "replay"
)
```

`coverage` is always length 5 in fixed order, even when a step has zero seconds. The UI
never has to handle a missing entry. `source` is persisted so replay-generated history is
distinguishable from real washes.

---

## 9. Verification

### 9.3 Replay end-to-end test

For each of 10 held-out labelled sessions: replay through the full app pipeline, write
the resulting `WashSession` to a JSON report, and compare per-step seconds against ground
truth. Committed to `reports/` as a table. This is the primary demo artifact and should be
the GIF in the README.
