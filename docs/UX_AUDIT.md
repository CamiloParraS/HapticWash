# UX audit — M4

SPEC-APP §4.1 items 1–10, checked on every M4 screen. Done 2026-09-30 on the
`Wear_OS_Large_Round` AVD (454 × 454 px, 320 dpi, so 48 dp = 96 px; API 37, Wear SDK 7),
with a debug build of `feat/ui-haptics` unless a row says release. Evidence comes from
screenshots, `uiautomator dump` bounds, `dumpsys` (vibrator_manager, sensorservice,
power) and the `HapticWash` logcat tag.

## Screens

`screencap` saves the full square frame. The watch shows only the inscribed circle.

| Screen | Screenshot |
|---|---|
| Start | [start](ux/start.png) |
| Start, Timer-only | [start_timer_only](ux/start_timer_only.png) |
| In-progress, ring off | [in_progress](ux/in_progress.png) |
| In-progress, ring on | [in_progress_ring](ux/in_progress_ring.png) |
| In-progress, Timer-only | [in_progress_timer_only](ux/in_progress_timer_only.png) |
| Summary, mixed Statuses (zhang_who_6 replay, right wrist) | [summary_mixed](ux/summary_mixed.png), [scrolled](ux/summary_mixed_scrolled.png) |
| Summary, too short (5 s) | [summary_too_short](ux/summary_too_short.png) |
| Summary, Timer-only | [summary_timer_only](ux/summary_timer_only.png) |
| History, with items | [history](ux/history.png) |
| History, empty | [history_empty](ux/history_empty.png) |
| Settings | [settings](ux/settings.png), [scrolled](ux/settings_scrolled.png) |

## Checklist

Columns are the screens: **St** Start, **IP** In-progress, **Su** Summary, **Hi** History,
**Se** Settings. ✓ pass, ◐ pass with a caveat in the notes, — not applicable.

| § | Item | St | IP | Su | Hi | Se | Notes |
|---|---|---|---|---|---|---|---|
| 1 | Glanceable < 5 s | ✓ | ✓ | ✓ | ✓ | ✓ | Each screen has one header and short lines. Summary is five rows of a Step name and its Status. |
| 2 | Round-safe, scaling list, `TimeText` | ✓ | ✓ | ✓ | ✓ | ✓ | Lists use `TransformingLazyColumn`, the Wear Material 3 successor to `ScalingLazyColumn`. `TimeText` comes from `AppScaffold` on every screen. In-progress is centred and not scrollable. The ring leaves a 60° gap at the top for `TimeText` (fixed in this audit: it used to run under the clock). |
| 3 | Touch targets ≥ 48 dp; one primary action | ✓ | ✓ | — | ✓ | ✓ | Buttons are 203 × 52 dp; Stop is 62 × 52 dp; History rows are ≥ 52 dp tall. Start's primary action is Start wash (History and Settings are tonal); In-progress's is Stop. Summary, History and Settings are lists with no primary action, so none has more than one. |
| 4 | One-tap start | ✓ | — | — | — | — | Start wash is the first item. A single tap starts the service and opens In-progress. |
| 5 | No text entry; dialogs ≤ 2 options; depth ≤ 2 | ✓ | ✓ | ✓ | ✓ | ✓ | No text fields. The only dialog is the system notification permission (Allow / Don't allow). Wrist side is a two-state tap toggle. Deepest paths: Start → In-progress → Summary (In-progress is popped) and Start → History → Summary. Swipe-to-dismiss works from In-progress, Summary, History and Settings. A swipe mid-Wash leaves it running; Start then shows "Wash in progress" and returns to In-progress. |
| 6 | Haptics, patterns, ≤ 1 per 3 s | — | ✓ | ◐ | — | ✓ | From `dumpsys vibrator_manager`: start 80 ms; end 80 + 120 + 80 ms; missed-step 600 ms, starting 3.2 s after the end pulses start (13 s Wash, all `NOT_DETECTED`). A too-short Wash and a Timer-only Wash each got only start and end. Replays are silent. The Haptics setting is read at fire time (unit-tested in `HapticsTest`). See note A. |
| 7 | Ambient / screen-off | — | ✓ | — | — | — | `KEYCODE_SLEEP` mid-Wash put the device in `Dozing`: 17 windows logged in 25 s (1.5 s stride), and on wake the Wash was still running at 0:45. No crash in logcat. |
| 8 | Battery honesty | — | ✓ | — | — | ✓ | After Stop: 0 active `sensorservice` connections for the app, and the `HapticWash:session` wake lock was released in the same ms the Wash ended. Always-on is a disabled switch labelled "Coming later". Its battery warning belongs to M5. |
| 9 | No result as certainty | ✓ | ✓ | ✓ | ✓ | ✓ | "Looks covered", "May have been brief", "Not detected", "Too short to estimate", "n of 5 look covered". Enforced by `LanguageTest` (D29). |
| 10 | Accessibility; status not by colour | ◐ | ◐ | ◐ | ◐ | ◐ | See note B. Every Status is a glyph plus text in the same colour. The glyph is hidden from semantics, so the text is what gets read. Glyphs have a fixed width so Step names line up (fixed in this audit). |

**A — 3 s spacing.** §4.1.6 limits vibration to once per 3 s. Measured start to start:
3.2 s. The missed-step pulse is scheduled 3.0 s after the end pattern's nominal end.
Because of emulator vibrator latency it began 2.92 s after the end pulses actually
finished. That meets §4.1.6 but is 80 ms short of M4-PLAN's "≥ 3 s after the end pulses", hence ◐. Re-check on hardware (M6) if the "≥ 3 s after the end
pulses" wording in M4-PLAN is read strictly.

**B — labels.** No control is icon-only. Every interactive element is a Material 3
`Button`/`FilledTonalButton`/`SwitchButton`, and its accessible label is its merged text
label (`uiautomator dump`: each clickable node wraps exactly its label and secondary
label). Compose uses that text as the spoken name, so there is no separate
`contentDescription` to set. A live TalkBack pass on the AVD got stuck in its tutorial,
which also doesn't log what it speaks. Hence ◐: the labels are checked structurally, the spoken output is not. Repeat it on hardware (M6).

## Other M4 checks

- **History persistence (§6 M4).** Deviation from e-audit's `am kill`: a SIGKILL of the app process (`run-as … kill -9`;
  `am kill` leaves a recently-foreground app running) and then `adb reboot` both kept
  `history.json` (4 Washes, same ids), and History renders them after the reboot. Replay
  Washes are in `replay_washes.json` and never appear in History.
- **Timer-only (R6).** A build with a corrupt `model.tflite` logs "Model unavailable,
  timer-only mode". Washes are timed (16 s) and stored, with one non-blocking notice on
  Start, In-progress and Summary. No Coverage is shown and nothing crashes.
- **Cold start (§4.3), release build** (`scripts/release_smoke.sh`, then `am start -W`
  after `force-stop`): 374, 355, 389, 346, 389, 429 ms. Median ≈ 380 ms against the 1.5 s
  budget. Model load is off first composition. The 1.84 s in `reports/M4_replay.md` is a
  debug build. Indicative only (emulator).
- **Heap (§4.3).** See `reports/M4_replay.md`: about 22 MB of Java + native PSS
  mid-replay, within 40 MB.

## M4 acceptance

| Criterion | Evidence | Result |
|---|---|---|
| State machine unit tests (6 cases) | `CoverageStateMachineTest` | pass |
| Replay ≥ 4 of 5 Steps on ≥ 8 of 10 sessions | `reports/M4_replay.md`, D26 | **not met**: 5 of 10, reported as measured |
| History survives process death and reboot | this audit; `WashStoreTest` | pass |
| Room persistence (deliverable) | JSON file + `SharedPreferences` instead, D27 | deviation, recorded |
| UI audit with a screenshot of each screen | this file, `docs/ux/` | pass |
| Banned-language lint test | `LanguageTest` (D29) | pass |

Test runs for this audit, same day: `./gradlew :wear:testDebugUnitTest`: 32 tests, 0 failures
(1 skipped: the gated replay report). `./gradlew :wear:connectedDebugAndroidTest` on the AVD:
`GoldenParityTest` 1/1, `ModelLoadFailureTest` 3/3.
