#!/usr/bin/env bash
# Replay a canonical sensor CSV (SPEC 8.1) through the full app pipeline on a running Wear OS
# emulator (SPEC 8.5) and stream the per-window predictions until the session ends.
#
#   scripts/replay_demo.sh <session.csv> [speed]
#
# The CSV must be canonical (SPEC 8.1), cut from the AI corpus; raw dataset files won't parse.
# Make a held-out zhang_who session CSV from HapticWash-AI (never commit it: CC-BY-NC-ND, D4).
# Use a left-wrist session; right-wrist mirroring arrives with the wrist setting in M4.
#   cd ../HapticWash-AI && uv run python -c "from haptic_ai import corpus; d = corpus.load(); \
#     d[(d.subject_id == 'zhang_who_1') & (d.session_id == '1_left_t0')] \
#     .to_csv('data/replay/zhang_who_1_1_left.csv', index=False, lineterminator='\n')"
set -euo pipefail
export MSYS_NO_PATHCONV=1 # Git Bash on Windows would otherwise rewrite the device paths
csv=${1:?usage: $0 <session.csv> [speed]}
speed=${2:-1.0}
pkg=com.hapticwash
dest=/data/user/0/$pkg/files/replay.csv

cd "$(dirname "$0")/.."
./gradlew -q :wear:installDebug
adb shell pm grant $pkg android.permission.POST_NOTIFICATIONS 2>/dev/null || true
adb push "$csv" /data/local/tmp/replay.csv >/dev/null
adb shell "cat /data/local/tmp/replay.csv | run-as $pkg sh -c 'mkdir -p files && cat > $dest'"
adb logcat -c
adb shell am start -W -n $pkg/.ui.MainActivity >/dev/null # a background app can't start the service
adb shell am broadcast -a $pkg.REPLAY --es path $dest --ef speed "$speed" -n $pkg/.sensing.ReplayReceiver >/dev/null
adb logcat -v brief -s HapticWash:I &
logcat=$!
trap 'kill $logcat 2>/dev/null' EXIT
# Poll instead of piping into `sed q`: a pipe only closes on logcat's next write, which may never come.
until adb logcat -d -s HapticWash:I | grep -q 'Session ended\|Session failed'; do sleep 1; done
sleep 1
