# M3 parity and replay report

Measured 2026-09-28 on a Wear OS emulator (API 37, x86_64, `emulator-5554`) with
`step-classifier` 0.1.0 from HapticWash-AI (D22, D23). Emulator timings are indicative only;
hardware numbers come at M6.

| check | result | requirement |
|---|---|---|
| Preprocessing parity, `golden/raw.npy` -> `inputs.npy` (JVM) | 298 windows, max \|Δ\| 0.0 | ≤ 1e-3 |
| Inference parity, on-device LiteRT vs Python TFLite | 298 windows, max \|Δp\| 6.6e-7 | ≤ 1e-2 per class |
| Top-1 agreement | 298 / 298 (100 %) | ≥ 98 % |
| Inference latency, golden windows | median 0.05 ms, p95 0.14 ms | ≤ 15 ms |
| Inference latency, replay session | median 0.27 ms, p95 0.58 ms | ≤ 15 ms |
| Corrupt / truncated / empty model, unknown `spec_version` | timer-only (null), no crash | R6 |
| Release build (R8) loads the model | `Model loaded: step-classifier 0.1.0` | M3 |
| Merged manifests: no `INTERNET`; replay receiver debug-only | pass | R1, §8.5 |

## Replay end to end

`scripts/replay_demo.sh <csv> 4` on `zhang_who_1` / `1_left_t0` (held out, never trained on;
85 s, 4× speed): 55 windows, one top-1 label per window. The stream goes NULL, then palm,
back of hand, interlaced/fingertips, WASH_OTHER, thumbs, then NULL. That is the protocol's
step order, but M3 does not score it; per-step accuracy against ground truth is M4 (§9.3).

Commands: `./gradlew :wear:testDebugUnitTest`, `./gradlew :wear:connectedDebugAndroidTest`,
`scripts/replay_demo.sh`, `scripts/release_smoke.sh`.
