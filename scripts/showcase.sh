#!/usr/bin/env bash
# Showcase: replay a recorded Wash through the app on the connected watch or emulator,
# with the screen following along (Start -> Wash -> Summary). Debug builds only (SPEC 8.5).
#
#   scripts/showcase.sh [preset | session.csv] [speed]
#
# Presets cut from HapticWash-AI's held-out data (see replay_demo.sh; never commit the CSVs):
#   good   zhang_who_3, left   4 Steps "looks covered", 1 "not detected"   (default)
#   mixed  zhang_who_5, left   4 "looks covered", Between fingers "may have been brief"
#   right  zhang_who_10, right wrist mirroring, 4 covered + 1 brief
# The Wash is saved to History and shows on the Tile and complication like a real one.
# scripts/showcase_nosave.sh does the same without saving (the old behaviour).
# A number 1-10 picks that subject (zhang_who_<n>). The wrist comes from the CSV, not the app
# setting; WRIST=left|right overrides it, e.g. WRIST=right scripts/showcase.sh good
# w00-w06 pick a UWash wash (uwash_library_2). INSTALL=1 rebuilds and installs the app first (needed after code changes).
# With several devices attached, pick one with ANDROID_SERIAL=<serial>.
set -euo pipefail
export MSYS_NO_PATHCONV=1 # Git Bash on Windows would otherwise rewrite the device paths
cd "$(dirname "$0")/.."
data=../HapticWash-AI/data/replay
case ${1:-good} in
  good) csv=$data/zhang_who_3_1_left.csv ;;
  mixed) csv=$data/zhang_who_5_1_left.csv ;;
  right) csv=$data/zhang_who_10_1_right.csv ;;
  [0-9]|10) csv=$(ls $data/zhang_who_${1}_1_*.csv) ;; # any subject by number
  w[0-9][0-9]) csv=$data/../replay_uwash/uwash_library_2_$1.csv ;; # UWash wash, e.g. w04 (w00-w06, no w03)
  *) csv=$1 ;;
esac
speed=${2:-1.0}
persist=${PERSIST:-true}
pkg=com.hapticwash
dest=/data/user/0/$pkg/files/replay.csv
[ -f "$csv" ] || { echo "no such CSV: $csv" >&2; exit 1; }
adb get-state >/dev/null || { echo "no watch or emulator connected (adb devices)" >&2; exit 1; }

[ -z "${INSTALL:-}" ] || ./gradlew -q :wear:installDebug
adb shell pm grant $pkg android.permission.POST_NOTIFICATIONS 2>/dev/null || true
adb push "$csv" /data/local/tmp/replay.csv >/dev/null 2>&1
adb shell "cat /data/local/tmp/replay.csv | run-as $pkg sh -c 'mkdir -p files && cat > $dest'"
adb logcat -c
adb shell am start -W -n $pkg/.ui.MainActivity >/dev/null # a background app can't start the service
adb shell am broadcast -a $pkg.REPLAY --es path $dest --ef speed "$speed" --ez persist "$persist" --es wrist "${WRIST:-$(sed -n 2p "$csv" | cut -d, -f11)}" \
  -n $pkg/.sensing.ReplayReceiver >/dev/null
until adb logcat -d -s HapticWash:I | grep -q 'Session started\|Session failed'; do sleep 0.5; done
# Start on a running Wash just opens the Wash screen; its end opens the Summary.
adb shell am start -n $pkg/.ui.MainActivity --ez start true >/dev/null 2>&1
echo "Replaying $(basename "$csv") at ${speed}x..."
until log=$(adb logcat -d -s HapticWash:I | grep 'Session ended\|Session failed'); do sleep 1; done
case $log in *failed*) adb logcat -d -s HapticWash:E; exit 1 ;; esac
adb logcat -d -s HapticWash:I | grep 'HapticWash: Wash:' | tail -1
