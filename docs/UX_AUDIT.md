# UX audit — M4

SPEC-APP §4.1 items 1–10, checked on every M4 screen. Done 2026-09-30 on the
`Wear_OS_Large_Round` AVD (454 × 454 px, 320 dpi, so 48 dp = 96 px; API 37, Wear SDK 7),
with a debug build of `feat/ui-haptics` unless a row says release. Evidence comes from
screenshots, `uiautomator dump` bounds, `dumpsys` (vibrator_manager, sensorservice,
power) and the `HapticWash` logcat tag.

## Screens

`screencap` saves the full square frame. The watch shows only the inscribed circle. Screenshots
replaced by the EdgeButton redesign (below) are kept as `ux/old_*.png`; the EdgeButton Start is
`ux/old_start_edge.png`.

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
| History, with items | [history](ux/history.png), [scrolled to the end](ux/history_scrolled.png) |
| History, empty | [history_empty](ux/history_empty.png) |
| Settings | [settings](ux/settings.png), [scrolled to the end](ux/settings_scrolled.png) |
| Tile | [tile](ux/tile.png) |
| Complication | [on the watch face](ux/complication.png), [in the picker](ux/complication_picker.png) |

## Checklist

Columns are the screens: **St** Start, **IP** In-progress, **Su** Summary, **Hi** History,
**Se** Settings. ✓ pass, ◐ pass with a caveat in the notes, — not applicable.

| § | Item | St | IP | Su | Hi | Se | Notes |
|---|---|---|---|---|---|---|---|
| 1 | Glanceable < 5 s | ✓ | ✓ | ✓ | ✓ | ✓ | Each screen has one header and short lines. Summary is five rows of a Step name and its Status. |
| 2 | Round-safe, scaling list, `TimeText` | ✓ | ✓ | ✓ | ✓ | ✓ | Lists use `TransformingLazyColumn`, the Wear Material 3 successor to `ScalingLazyColumn`. `TimeText` comes from `AppScaffold` on every screen. Start is a short list centred on its Start wash button; In-progress is centred and not scrollable. Settings and History can scroll their last row up to the centre of the screen. The ring leaves a 60° gap at the top for `TimeText` (fixed in this audit: it used to run under the clock). |
| 3 | Touch targets ≥ 48 dp; one primary action | ✓ | ✓ | — | ✓ | ✓ | List buttons are 203 × 52 dp; Stop is a hold anywhere on the screen (see "Hold to stop"; was a 162 × 52 dp button); History rows are ≥ 52 dp tall. Start wash is a 203 × 52 dp button in the middle of Start, History and Settings 48 × 48 dp tonal icon buttons below it, Settings rows 203 × 52 dp (centred Start, below). Start's primary action is Start wash; In-progress's is Stop. Summary, History and Settings are lists with no primary action, so none has more than one. |
| 4 | One-tap start | ✓ | — | — | — | — | Start wash is the button in the middle of Start, shown at full size without scrolling. A single tap starts the service and opens In-progress. The Tile and the complication start a Wash from outside the app (below). |
| 5 | No text entry; dialogs ≤ 2 options; depth ≤ 2 | ✓ | ✓ | ✓ | ✓ | ✓ | No text fields. The only dialog is the system notification permission (Allow / Don't allow), now asked on the first Start tap. Wrist side is one row that flips between Left and Right on a tap. Deepest paths: Start → In-progress → Summary (In-progress is popped) and Start → History → Summary. Swipe-to-dismiss works from In-progress, Summary, History and Settings. A swipe mid-Wash leaves it running; Start then shows "Wash in progress" and returns to In-progress. |
| 6 | Haptics, patterns, ≤ 1 per 3 s | — | ✓ | ◐ | — | ✓ | Since C1 (below) a fourth pattern, time reached, ticks at 40 s. From `dumpsys vibrator_manager`: start 80 ms; end 80 + 120 + 80 ms; missed-step 600 ms, starting 3.2 s after the end pulses start (13 s Wash, all `NOT_DETECTED`). A too-short Wash and a Timer-only Wash each got only start and end. Replays are silent. The Haptics setting is read at fire time (unit-tested in `HapticsTest`). See note A. |
| 7 | Ambient / screen-off | — | ✓ | — | — | — | `KEYCODE_SLEEP` mid-Wash put the device in `Dozing`: 17 windows logged in 25 s (1.5 s stride), and on wake the Wash was still running at 0:45. No crash in logcat. |
| 8 | Battery honesty | — | ✓ | — | — | ✓ | After Stop: 0 active `sensorservice` connections for the app, and the `HapticWash:session` wake lock was released in the same ms the Wash ended. Always-on isn't offered in M4 (the disabled "Coming later" switch was removed in the polish pass below). It arrives with its battery warning in M5. |
| 9 | No result as certainty | ✓ | ✓ | ✓ | ✓ | ✓ | "Looks covered", "May have been brief", "Not detected", "Too short to estimate", "n of 5 look covered". Enforced by `LanguageTest` (D29). |
| 10 | Accessibility; status not by colour | ◐ | ◐ | ◐ | ◐ | ◐ | See note B. Every Status is a glyph plus text in the same colour. The glyph is hidden from semantics, so the text is what gets read. Glyphs have a fixed width so Step names line up (fixed in this audit). |

**A — 3 s spacing.** §4.1.6 limits vibration to once per 3 s. Measured start to start:
3.2 s. The missed-step pulse is scheduled 3.0 s after the end pattern's nominal end.
Because of emulator vibrator latency it began 2.92 s after the end pulses actually
finished. That meets §4.1.6 but is 80 ms short of M4-PLAN's "≥ 3 s after the end pulses", hence ◐. Re-check on hardware (M6) if the "≥ 3 s after the end
pulses" wording in M4-PLAN is read strictly.

**B — labels.** History and Settings on Start are icon-only; their icons carry the
`contentDescription` "History" and "Settings" (`uiautomator dump`). The Summary ring is one
node read as "n of 5 look covered". Every other interactive element is a Material 3
`Button`/`FilledTonalButton`/`SwitchButton`, and its accessible label is its merged text
label (`uiautomator dump`: each clickable node wraps exactly its label and secondary
label). The Settings icons are decorative (`contentDescription` null), so each row is read by its text. Compose uses that text as the spoken name, so there is no separate
`contentDescription` to set. A live TalkBack pass on the AVD got stuck in its tutorial,
which also doesn't log what it speaks. Hence ◐: the labels are checked structurally, the spoken output is not. Repeat it on hardware (M6).

## Changes after the audit

Made the same day on the same AVD, from a UI review. `MainActivity.kt` and `Theme.kt` only.

- **Stop** is wider (162 × 52 dp) so wet, soapy fingers hit it.
- **Summary** leads with "n of 5 look covered", then the duration, then the five rows.
- **History** rows are tonal buttons, and a too-short Wash reads "Too short".
- **Wrist side** is two radio buttons with "Applies from the next wash" under them.
- **Notification permission** is asked on the first Start tap, not at launch. The Wash starts
  whatever the answer.
- **Theme** follows the device's Material You colours (`dynamicColorScheme`) and falls back to
  the default palette. This AVD returns no dynamic scheme, so only the fallback was seen; the
  dynamic path is unchecked until a watch that has it (M6).

Screenshots retaken: In-progress (ring off and on), Summary (mixed), History, Settings. Start,
Summary (too short, Timer-only), History (empty) are unchanged by these edits.
`in_progress_timer_only.png` shows the old narrow Stop; it needs a corrupt-model build to retake.
(All screenshots were retaken after the polish pass below.)

## Polish pass

A motion and hierarchy pass the same day, on the same AVD. `MainActivity.kt` and `strings.xml` only.

- **Lists** (Start, Summary, History, Settings) shrink and fade items at the round edge
  (`rememberTransformationSpec`, `SurfaceTransformation`) instead of cutting them off. Headers
  are left out, so a header at the top of a screen isn't dimmed.
- **In-progress:** the timer uses tabular digits, so it doesn't shift sideways as it ticks. The ring
  fades from `primary` to `tertiary` when it fills at 40 s (colour only; no extra vibration, see §4.1.6).
  Start and Stop shrink to 96 % while pressed, using a no-bounce spring. Tapping Stop disables it
  and shows "Stopping…" until the Wash has saved.
- **Summary:** the duration reads "Washed for m:ss". Status lines use `onSurfaceVariant` and the ✓
  glyph `primary`; glyph and text still carry the Status on their own (§4.1.10). Rows fade in and
  rise 6 dp in a 40 ms stagger (about 360 ms in total). Scrolling isn't blocked, and the animation
  plays instantly when the system animation scale is 0. The cascade was checked by eye, not in a
  screenshot.
- **Settings:** the disabled Always-on switch and its strings are removed.

Screenshots retaken for every screen in the table above. The Timer-only ones come from a
temporary build with a corrupt `model.tflite` (the log shows `model=none (timer-only)`); the real
model was restored and confirmed loading (`step-classifier 0.1.0`). Summary (mixed) is the
zhang_who_6 right-wrist replay at 4× speed. History shows this session's test Washes.
`./gradlew :wear:testDebugUnitTest`: 32 tests, 0 failures, 1 skipped.

## Tile and complication

Added after the polish pass, on the same AVD, as the first step of the redesign: start a Wash
without opening the app first (§4.1.4 one-tap start, now from the watch face and the Tile
carousel too).

- **Tile** (`WashTileService`, ProtoLayout Material3): app name, the last Wash in the same words
  as History ("Last wash: 0 of 5 look covered", or "No washes yet") and a Start wash edge button.
  `WashService` asks for a Tile refresh after each real Wash is saved; replays don't.
- **Complication** (`WashComplicationService`): static `SHORT_TEXT` ("Wash" and a drop icon) or
  `MONOCHROMATIC_IMAGE`, never polled (`UPDATE_PERIOD_SECONDS` 0).
- Both open `MainActivity` with a start extra that is handled exactly like a tap on Start wash,
  including the notification permission on first use. `MainActivity` is `singleTask`, so a tap
  also reaches an app that is already open.

| Case | Result |
|---|---|
| Tile tap, app not running | a Wash starts |
| Tile tap, app open on History | back to Start, a Wash starts |
| Tile tap while a Wash runs | rejoins it at its elapsed time; no second `Session started` |
| Tile after a Wash ends | shows that Wash |
| Complication picked in the face editor, then tapped | a Wash starts |
| Plain launch from the app list | Start screen; nothing starts |

The Tile was added with the `DEBUG_SURFACE` broadcast (`add-tile`, `show-tile`); the complication
through the watch face editor. No new permission: `ManifestTest` passes, and the release build
(R8) succeeds. `./gradlew :wear:testDebugUnitTest`: 32 tests, 0 failures, 1 skipped.

## Redesign: Start and Summary

Same day, same AVD, after the Tile and complication. `MainActivity.kt`, `strings.xml` and two
icons.

- **Start** is one large Start wash button with a drop icon (first round, now a pill: see below), and History and Settings as
  52 dp tonal icon buttons under it. The screen no longer scrolls. In Timer-only the button is
  92 dp and the notice reads "Timer only", so the icon buttons keep 52 dp (first try squeezed
  them to about 33 dp). While a Wash runs the button reads "Wash in progress".
- **Summary** leads with a ring: five arcs, clockwise from 12 o'clock in list order, around
  "n of 5 / look covered". Looks covered fills an arc, May have been brief fills half, Not detected
  leaves the track, matching the ✓ ◐ ○ glyphs, so shape carries the Status and colour only
  repeats it (§4.1.10). The arcs grow in with the row cascade. The list below is unchanged.
  Too-short and Timer-only Summaries keep the duration header.

Screenshots retaken: Start, Start (Timer-only, corrupt-model build again; the real model was
restored and loads), Summary (mixed) and scrolled. `./gradlew :wear:testDebugUnitTest`: 32 tests,
0 failures, 1 skipped.

**Not done:**
- A five-segment pacing ring on In-progress: may read as naming a Step during the Wash.
- A weekly strip on History: History keeps the last 20 Washes (M4), so weekly counts would
  undercount.

## Spec changes C1–C3 and the Start pill

The countdown, the time-reached ticks and the always-on placeholder needed SPEC-APP changes;
they are recorded in [SPEC-CHANGELOG](SPEC-CHANGELOG.md) as C1–C3.

- **In-progress (C2)** counts down to the 40 s target ("0:35 / to go"), then shows the total
  ("0:43 / 40 s reached"). The ring, when on, is unchanged.
- **Time reached (C1)**, from `dumpsys vibrator_manager` on a 43 s Wash that ended with a Step not
  detected: start 07:49:23.05; ticks (40 ms on, 80 ms off, three times) 07:50:03.05, 40.0 s after
  the start; end pulses 07:50:06.83, 3.8 s after the ticks; missed-step pulse 07:50:10.11. Every
  gap is ≥ 3 s. `HapticsTest` also checks the tick time and that all four patterns differ.
- **Settings (C3):** no always-on placeholder, as already built.
- **Start wash is a pill** with the drop icon and label side by side, 80 % of the width. On press
  its corners tighten from 50 % to 30 % and spring back with no bounce (Material 3 Expressive shape
  morph); History and Settings use Wear's own `IconButtonDefaults.animatedShapes()`. In Timer-only
  the pill is 64 dp tall; `uiautomator` bounds: pill 162 × 64 dp, icon buttons 52 × 52 dp.

Screenshots retaken: Start, Start (Timer-only), In-progress (ring off, ring on, Timer-only). The
Timer-only ones come from the corrupt-model build again; the real model was restored.
`./gradlew :wear:testDebugUnitTest`: 33 tests, 0 failures, 1 skipped.

## Redesign: EdgeButton Start, Settings and Tile

Same AVD, after the Start pill. `MainActivity.kt`, `WashTileService.kt`, `strings.xml` and three
icons. The pill put a large button and two icon buttons above the middle of the round screen,
left the bottom third empty, and said nothing until tapped.

- **Start** is built on Wear Material 3's `EdgeButton`: Start wash hugs the bottom curve, where
  the Tile puts it. Above it, centred: "Last wash · 7:15 AM" (the date once it is not today) over
  "n of 5 look covered" (or "Too short", "No washes yet"), "Timer only" when the model is
  unavailable, then the History and Settings icon buttons. The list is kept short on purpose:
  a longer one shrinks the EdgeButton until scrolled to the end, which would hide one-tap start
  (§4.1.4). The label alone, no icon, so it fits on one line; "Wash in progress" wraps to two.
  The pill and its press-shape morph are gone; the EdgeButton's own press feedback remains.
- **Settings** fits on one screen. Wrist side is one row showing its value, flipped by a tap: two
  options, no dialog, and a second tap undoes it (§4.1.5). Wrist side, Haptics and Progress ring
  have icons. "Applies from the next wash" shows only while a Wash runs, when it matters.
- **Settings and History** can scroll their last row up to the centre of the screen, with black
  below it, like the watch's own Settings.
- **Tile** uses Start's words and hierarchy: a small "Last wash · 7:15 AM" over the takeaway in
  `TITLE_MEDIUM`, in place of one wrapped "Last wash: …" line. It also refreshes hourly, so the
  time turns into a date.

| Screen | Clickable bounds (`uiautomator`) |
|---|---|
| Start | Start wash 227 × 70 dp (EdgeButton), History 48 × 48 dp, Settings 48 × 48 dp |
| Settings | Wrist side 203 × 52 dp, Haptics 203 × 52 dp, Progress ring 156 × 40 dp while scaled at the bottom edge, 203 × 52 dp scrolled into view |
| History | rows 203 × 52 dp |

Screenshots retaken: Start, Start (Timer-only, corrupt-model build again; the real model was
restored and is byte-identical), Settings and scrolled, History and scrolled (new), Tile.
`./gradlew :wear:testDebugUnitTest`: 33 tests, 0 failures, 1 skipped.

## Centred Start, tonal Stop and the live ring fix

Same AVD, 2026-10-01. `MainActivity.kt` only.

- **Start wash moves to the middle of Start**, with History and Settings below it, so the screen
  stops reading as a face: the Coverage ring and text as eyes over the EdgeButton as a mouth.
  The order, top to bottom: the last Wash's ring and takeaway, Start wash, then the two icon
  buttons. The button is centred with the group rather than on the exact centre of the screen:
  the takeaway above it is taller than the icon row below, and pinning the button to the centre
  would leave too little room for it on small screens. Start wash has the drop icon again and
  shrinks slightly on press, like Stop. The History and Settings row is no longer edge-scaled:
  this low on the screen the transformation shrank them to 45 × 45 dp.
- **The Tile keeps its EdgeButton.** The ring is the Tile's hero and the edge slot is where
  every Tile puts its action, so app and Tile no longer share Start's position; they still share
  its words, ring and colours.
- **Stop is tonal** (`FilledTonalButton`), so the countdown leads; same 162 × 52 dp target.

| Screen | Clickable bounds (`uiautomator`) |
|---|---|
| Start | Start wash 203 × 52 dp, History 48 × 48 dp, Settings 48 × 48 dp |

- **Fix: the live ring stood still.** Since the frame-smooth ring (C5), the ring stayed empty
  through a Wash started from Start and jumped to full at 40 s. Wear's `CircularProgressIndicator`
  restarts its own progress animation on every new value; fed a new value every frame, it never
  got past the animation's first frame, and only caught up once the value stopped changing at
  100 %. A replayed Wash hid it. The ring is now drawn with `drawCircularProgressIndicator`, the
  same arc without the animation; the frame loop already makes it smooth. Checked on a Wash
  started from Start: about an eighth full at "0:35 to go", half at "0:20", full in the reached
  colour at "40 s reached".

Screenshots retaken: Start, In-progress (ring on). Not retaken: Start (Timer-only, needs the
corrupt-model build), In-progress (ring off and Timer-only), which still show the filled Stop.

## Hold to stop

2026-10-08. `MainActivity.kt` and `strings.xml`. Replaces the Stop `EdgeButton` (the 162 × 52 dp
tonal button above, and the 60° gap the ring kept at 6 o'clock for it). **Not run on the AVD or a
watch yet:** compiled, and `./gradlew :wear:testDebugUnitTest` passes (33 tests, 0 failures, 1
skipped). Everything below is by construction and needs a device pass (M6).

- **Stop is a hold anywhere on In-progress.** The target is the whole screen, the largest
  possible for wet, soapy fingers (§4.1.3). A hold also ignores the brief touches water makes on
  the glass. The hold takes 700 ms (`HOLD_MS`, a guess to tune with wet hands). Releasing early
  undoes it in 200 ms; a swipe-back cancels it. Touch-down is answered at once (a 3 % head start,
  the colour change), and a full hold gives one `LongPress` haptic (the existing end pulses are
  unchanged, §4.1.6). The hold is not a one-tap action, but In-progress still has one primary
  action, so §4.1.3 and §4.1.5 hold.
- **The ring is one arc again**, with only the 60° gap at 12 o'clock for `TimeText`.
- **What the hold looks like.** The ring is drawn a second time with the same
  `drawCircularProgressIndicator`, in the error colour. A stop square grows in at the centre as
  the countdown fades out, and the glow shifts to the error colour and brightens. The shift is
  shape and movement as well as colour (§4.1.10). The hold works with the progress ring off.
- **Telling people.** "Hold to stop" shows under the countdown 0.7 s after the bloom, for 3 s,
  then fades. "Stopping…" replaces it once the hold completes.
- **Accessibility.** The screen exposes a long-click action named "Stop" to TalkBack. Not tested
  with TalkBack (see note B).
- **After the hold.** The stop square and ring stroke spring past their size once. If the Wash
  ended by a hold, the ring that shrinks into the Summary's Coverage ring leaves full and in the
  error colour, then settles into the Wash's own. Ended another way (notification, Tile), it
  does not.
- **Start → Wash.** A copy of the Start pill shrinks and fades over the first 40 % of the ring's
  bloom. Only with the ring on.

- **Stop → Summary, checked on the AVD** at 30× slowed animations (`animator_duration_scale 30`,
  screenshots every ~0.5 s), after a first version didn't land. The nav host slid the Summary in
  from the right over the ring morph, and the save was so quick that the commit pop never showed.
  Now the Summary fades in over In-progress in the same place (`WashScreen` hosts both; there is
  no `result` route), a hold keeps its commit on screen for at least 450 ms, and the ring and stop
  square are drawn by the Summary alone from then on. Seen: the ring and square carry across, the
  ring shrinks into the Coverage ring, and the numeral grows from the square's place. The glow
  is `errorContainer`, a deep red, so the stop state is plain on the black screen. Swipe-back
  from the Summary goes to Start. Not checked at normal speed or on a watch.

**Not done:** the bloom in reverse when swiping back out of a running Wash. The nav host's own
swipe-dismiss handles it, and `wear-compose-navigation` 1.7.1 offers no way to give it a custom
exit that I found.

## Other M4 checks

- **History persistence (§6 M4).** Deviation from e-audit's `am kill`: a SIGKILL of the app process (`run-as … kill -9`;
  `am kill` leaves a recently-foreground app running) and then `adb reboot` both kept
  `history.json` (4 Washes, same ids), and History renders them after the reboot. Replay
  Washes are in `replay_washes.json` and never appear in History.
- **Timer-only (R6).** A build with a corrupt `model.tflite` logs "Model unavailable,
  timer-only mode". Washes are timed (16 s) and stored, with one non-blocking notice on
  Start, In-progress and Summary. No Coverage is shown and nothing crashes.
- **Cold start (§4.3), release build** (`scripts/release_smoke.sh`, then `am start -W`
  after `force-stop`), re-measured 2026-10-10 at 1002c2f on `Wear_OS_Small_Round`: 477, 462,
  452, 541, 522, 443 ms. Median ≈ 470 ms against the 1.5 s budget (was ≈ 380 ms on
  2026-09-30, before the Start redesign). TotalTime is first frame; the Start button takes
  taps from then, and its entrance fade ends 240 ms later, so ≈ 710 ms to fully shown.
  Model load is off first composition. The 1.84 s in `reports/M4_replay.md` is a debug
  build. Indicative only (emulator).
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
| Hold to stop on a device | not run outside 30× slowed animations | **deferred to M6** |
| TalkBack spoken output | labels checked structurally (note B) | **deferred to M6** |

Test runs for this audit, same day: `./gradlew :wear:testDebugUnitTest`: 32 tests, 0 failures
(1 skipped: the gated replay report). `./gradlew :wear:connectedDebugAndroidTest` on the AVD:
`GoldenParityTest` 1/1, `ModelLoadFailureTest` 3/3.

## M4 close-out

Closed 2026-10-10 at a9c3f52. `./gradlew :wear:testDebugUnitTest`: 34 tests, 0 failures,
1 skipped. Written off, each with its reason and where it is picked up:

- **Replay ≥ 8 of 10 sessions: not met (5 of 10).** A model generalisation gap, not an app
  bug: the in-distribution uwash check passes 6 of 6 (D26). Thresholds stay untuned on
  zhang_who (D15). Picked up by the next model in the AI repo; re-evaluating is a rerun (D26).
- **Room persistence: replaced** by a JSON file and `SharedPreferences` (D27).
- **Hold to stop and TalkBack: deferred to M6**, on a real watch (sections "Hold to stop",
  note B). The 700 ms `HOLD_MS` needs tuning with wet hands there.
- **Screenshots** predate the Start hero ring and hold-to-stop (2026-10-08), so Start and
  In-progress no longer match the app, and the ambient Wash display (C10) has none. Retake
  before the first release.
- **Spanish strings** (`values-es`) are scanned by `LanguageTest`, but the banned list is
  English only; they were reviewed by hand at close-out and hold no result language.
