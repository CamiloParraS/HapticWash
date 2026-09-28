#!/usr/bin/env bash
# Release-build smoke test (SPEC-APP M3): install the R8-minified release build on a running
# emulator, launch it, and check that LiteRT loaded the bundled model.
set -euo pipefail
export MSYS_NO_PATHCONV=1
cd "$(dirname "$0")/.."
./gradlew -q :wear:installRelease
adb logcat -c
adb shell am start -W -n com.hapticwash/.ui.MainActivity >/dev/null
sleep 3
if adb logcat -d -s HapticWash:I | grep "Model loaded"; then
  echo "release smoke: OK"
else
  adb logcat -d -s HapticWash:W
  echo "release smoke: FAILED (model did not load)" >&2
  exit 1
fi
