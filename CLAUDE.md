# HapticWash — Wear OS app

Kotlin app: `wear/` is the watch app, `mobile/` a thin phone module. The model is trained in `../HapticWash-AI` and arrives as a release artifact; no training here.
Vocabulary: `CONTEXT.md`. Read it before naming anything (Wash, Step, Coverage, Twist, Auto-end...).

## Where things live
Source root `wear/src/main/java/com/hapticwash/`:
- `sensing/` — MotionSource, real + replay (spec §8.4, §8.5)
- `pipeline/` — filter, window, normalize; mirrors the AI repo's preprocessing
- `inference/` — LiteRT wrapper, `model_meta` parsing
- `session/` — Coverage, AutoEnd, WashStore, Settings, Haptics (spec §8.6). `WashRun` wires them into one Wash and returns its Wash outcome (pure, tested in `WashRunTest`); `WashService` is only its Android adapter (wake lock, notifications, stores, Tile)
- `ui/` — Compose, one file per screen (`StartScreen`, `WashScreen`, `SummaryScreen`, ...) plus `App.kt` (nav graph), `FiveRings.kt` (all ring drawing), `UiKit.kt` (shared list rows, motion), `WashWording.kt` (what every surface says about a Wash), `LiveWash.kt` + `StopHold.kt` (the live Wash screen's rules and hold-to-stop). Screens reach the app only through `WashControls` (`LocalWashControls`); `PreviewWashControls` is its fake for previews and `androidTest/ui` tests; tile, complication; screenshots in `docs/ux/`

Tests: `wear/src/test` (unit), `wear/src/androidTest` (parity, replay E2E), `wear/src/sharedTest`.

## Specs: read a section, not the file
`docs/SPEC-APP.md` is large. `grep -n "^##" docs/SPEC-APP.md`, then `Read` only the section for the task:
- current milestone → §6 (`### M4`, `### M5-App`)
- interfaces and contracts → §8
- UI and offline constraints → §4
- verification → §9

Shared contracts (labels, tensors, preprocessing) live in `../HapticWash-AI/docs/HapticWash-SPEC.md` and win on conflict; look up the one § you need.
`docs/SPEC-CHANGELOG.md`, `docs/UX_AUDIT.md`, `docs/M4-PLAN.md`, `docs/m4/` are history; open only when asked.
Active work: `.scratch/m5-twist-start/issues/`.

## Commands
Run from Git Bash. Needs `JAVA_HOME` (JDK 17+), `ANDROID_HOME`, `adb`; instrumented tests need a running Wear AVD.
```sh
./gradlew :wear:testDebugUnitTest          # unit: parity, Coverage, History, haptics, banned-language rules
./gradlew :wear:connectedDebugAndroidTest  # instrumented: inference parity + latency
scripts/release_smoke.sh                   # R8 release build loads the model
```
Replay and demo scripts: `scripts/replay_demo.sh`, `replay_batch.sh`, `showcase.sh` (usage in README).

## File size
Keep a Kotlin file to about 500 lines. If it grows past that, split it (one screen or one concept per file) or list it in `JUSTIFIED` in `wear/src/test/java/com/hapticwash/FileSizeTest.kt` with a reason. The unit test enforces this and also fails on entries that are no longer needed.

## Gotchas
- Search only the repo root. `.claude/worktrees/` holds stale copies of the whole repo, so every grep there returns duplicates.
- Replay Washes exist only in debug builds and never reach History.
- Replay session CSVs carry dataset licences; keep them uncommitted.
- Output is an estimate, never a guarantee that hands are clean; unit tests enforce banned wording.
