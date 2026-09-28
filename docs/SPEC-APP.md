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
| Background | Foreground `Service`, type `dataSync`                             | Notification required       |
| Inference  | LiteRT (TensorFlow Lite) Android runtime                          | CPU only, pin exact version |
| Storage    | Room (sessions + step coverage)                                   | Local only                  |
| Prefs      | DataStore (Preferences)                                           | wrist side, toggles         |
| DI         | Hilt (optional; manual DI acceptable if it keeps the graph small) |                             |
| Tests      | JUnit5 + Turbine (unit), AndroidX Test + Espresso (instrumented)  |                             |
**Permissions:** `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC`,
`POST_NOTIFICATIONS`, `VIBRATE`, `WAKE_LOCK` (partial wake lock held only for the
duration of an active session, so sensor delivery continues with the screen off;
confirm it is needed on the emulator at M3 — D12).
Accelerometer and gyroscope at 50 Hz require **no** runtime permission.
`HIGH_SAMPLING_RATE_SENSORS` is **not** needed (only applies above 200 Hz).
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
   session start (one short pulse), session end (two short pulses), missed-step
   notice (one long pulse). Never vibrate more than once per 3 seconds.
7. **Ambient mode.** During an active wash the app must handle ambient/always-on
   transitions without stopping sensor collection or crashing.
8. **Battery honesty.** Continuous 50 Hz accelerometer + gyroscope is expensive. Manual
   mode must stop the sensor listener the moment a session ends. Always-on mode (M5)
   must show an explicit battery warning at enable time.
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
- Compose screens: Start, In-progress (elapsed time + subtle progress), Summary
  (per-step status), History (last 20 washes), Settings (wrist side, haptics, always-on
  toggle placeholder).
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

### M5-App — Always-on runtime _(optional for v1)_

**Objective.** Run the M5-AI spotting model on-watch to start sessions without a tap,
within battery limits.

**Deliverables**

- Second model in the app assets; gated inference (spotting model runs continuously at
  low duty cycle; step model runs only after spotting fires).
- Duty-cycling strategy documented and implemented (e.g. spotting inference on 1 of every
  N windows until a positive, then dense).
- Battery-impact estimate.

**Acceptance criteria**

- Feature toggle defaults **off**, with the §4.1 item 8 battery warning at enable time.

**Verification**

- Replay of a full held-out `ocdetect` day through the app; count spurious session starts.

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
