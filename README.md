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

## Running it (M3)

The model and golden files come from `HapticWash-AI/artifacts/` (release `step-classifier`
0.1.0). To take a new model, copy `model.tflite` + `model_meta.json` into
`wear/src/main/assets/model/` and `golden/*.npy` into `wear/src/androidTest/assets/golden/`.

Needs `JAVA_HOME` (JDK 17+), `ANDROID_HOME` (or `sdk.dir` in `local.properties`), `adb` on
`PATH`, and a running Wear OS AVD for the last three. Run the scripts from Git Bash.

```sh
./gradlew :wear:testDebugUnitTest            # preprocessing parity, replay parser, manifest rules
./gradlew :wear:connectedDebugAndroidTest    # inference parity + latency, model-load failure (Wear AVD)
scripts/replay_demo.sh session.csv [speed]   # replay a canonical CSV on the AVD, stream predictions
scripts/release_smoke.sh                     # R8 release build loads the model
```

`scripts/replay_demo.sh` documents how to cut a `zhang_who` session CSV from the AI repo.
