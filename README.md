# HapticWash — Wear OS App

> **This is the APP repo** (Kotlin, runs on the watch). The model it runs is trained in
> the separate **`HapticWash-AI`** repo and arrives here as a release artifact
> (`model.tflite` + `model_meta.json`). No training happens in this repo.
>
> - App spec: [`docs/SPEC-APP.md`](docs/SPEC-APP.md)
> - Shared contracts (core spec): `HapticWash-AI/docs/HapticWash-SPEC.md`

## What it is

HapticWash is an on-device Wear OS application that observes wrist motion during
handwashing and reports which WHO handwashing gestures appear to have been performed,
which appear to have been missed, and keeps a local history of recent washes.

It is a hygiene coach, not a medical device and not a compliance-verification system. No
output may be phrased as a guarantee that hands are clean.

## Running it

The model and golden files come from `HapticWash-AI/artifacts/` (release `step-classifier`
0.1.0). To take a new model, copy `model.tflite` + `model_meta.json` into
`wear/src/main/assets/model/` and `golden/*.npy` into `wear/src/androidTest/assets/golden/`.

Needs `JAVA_HOME` (JDK 17+), `ANDROID_HOME` (or `sdk.dir` in `local.properties`), `adb` on
`PATH`, and a running Wear OS AVD for the last four. Run the scripts from Git Bash.

```sh
./gradlew :wear:testDebugUnitTest            # parity, Coverage, History, haptics, manifest and banned-language rules
./gradlew :wear:connectedDebugAndroidTest    # inference parity + latency, model-load failure (Wear AVD)
scripts/replay_demo.sh session.csv [speed]   # replay a canonical CSV on the AVD, stream predictions
scripts/replay_batch.sh <csv dir> [speed]    # replay 10 held-out sessions, write reports/M4_replay.md
# the report also lists per-Step NOT_DETECTED precision with raw counts (Target check gate, ticket 07); run it on labelled own_watch CSVs with the Target check off
scripts/showcase.sh [good|mixed|right|csv] [speed]  # demo: replay a Wash with the screens following along
scripts/release_smoke.sh                     # R8 release build loads the model
```

`scripts/replay_demo.sh` and `scripts/replay_batch.sh` document how to cut `zhang_who` (and
held-out `uwash`) session CSVs from the AI repo. Never commit them (licence, D4/D15).
The M4 UX audit, with screenshots from a round AVD, is in [`docs/UX_AUDIT.md`](docs/UX_AUDIT.md).
