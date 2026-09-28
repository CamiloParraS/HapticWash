#!/usr/bin/env bash
# M4 batch replay (SPEC 9.3): replay held-out zhang_who sessions through the full app pipeline
# on a running Wear OS emulator, then write reports/M4_replay.md.
#
#   scripts/replay_batch.sh <csv dir> [speed] [results dir]
#
# <csv dir> holds exactly the 10 session CSVs: one per subject, odd subjects left wrist, even
# right, first trial. Cut them from HapticWash-AI (never commit them: CC-BY-NC-ND, D4/D15):
#   cd ../HapticWash-AI && uv run python -c "from haptic_ai import corpus; d = corpus.load(); \
#     [d[(d.subject_id == f'zhang_who_{i}') & (d.session_id == f'1_{w}_t0')] \
#      .to_csv(f'data/replay/zhang_who_{i}_1_{w}.csv', index=False, lineterminator='\n') \
#      for i in range(1, 11) for w in ['left' if i % 2 else 'right']]"
#
# In-distribution sanity set (D26): the held-out uwash subject (D22) cut into washes at > 5 s
# NULL/unlabelled gaps, fragments under the 10 s minimum dropped. Run it as a second <csv dir>
# with its own [results dir], and move reports/M4_replay.md aside before git restores it:
#   cd ../HapticWash-AI && uv run python -c "from haptic_ai import corpus; import numpy as np; \
#     d = corpus.load(); s = d[d.session_id == 'library_2_t7046'].reset_index(drop=True); \
#     i = np.flatnonzero(~s.label.isin([0, -1]).to_numpy()); cut = np.flatnonzero(np.diff(i) > 250); \
#     [s.iloc[max(a - 50, 0):b + 51].to_csv(f'data/replay_uwash/uwash_library_2_w{n:02d}.csv', \
#      index=False, lineterminator='\n') for n, (a, b) in enumerate(zip(np.r_[i[0], i[cut + 1]], \
#      np.r_[i[cut], i[-1]])) if (b - a) / 50 >= 10]"
#
# Each CSV replays with `wrist` from its own wrist column; right-wrist CSVs replay a second
# time forced to `left` (unmirrored comparison). Speed > 1 is fine: Coverage uses the sample
# clock. Cold start and mid-replay heap are recorded too (§4.3, indicative on an emulator).
set -euo pipefail
export MSYS_NO_PATHCONV=1 # Git Bash on Windows would otherwise rewrite the device paths
csvdir=$(cd "${1:?usage: $0 <csv dir> [speed] [results dir]}" && pwd)
speed=${2:-4.0}
cd "$(dirname "$0")/.."
out=${3:-build/m4_replay}
pkg=com.hapticwash
dest=/data/user/0/$pkg/files/replay.csv
winpath() { cygpath -m "$1" 2>/dev/null || echo "$1"; } # Windows tools (JVM, adb) can't read /c/...

rm -rf "$out" && mkdir -p "$out"
out=$(cd "$out" && pwd)
./gradlew -q :wear:installDebug
adb shell pm grant $pkg android.permission.POST_NOTIFICATIONS 2>/dev/null || true
adb shell run-as $pkg rm -f files/replay_washes.json
(adb emu avd name 2>/dev/null | head -1 || adb shell getprop ro.product.model) | tr -d '\r' > "$out/device.txt"

echo "Cold start, 5 runs"
for _ in 1 2 3 4 5; do
  adb shell am force-stop $pkg
  adb shell am start -W -n $pkg/.ui.MainActivity | tr -d '\r' | sed -n 's/^TotalTime: //p' >> "$out/coldstart.txt"
done

n=0
replay() { # <csv> <wrist>
  echo "Replay $(basename "$1") wrist=$2"
  adb push "$(winpath "$1")" /data/local/tmp/replay.csv >/dev/null 2>&1
  adb shell "cat /data/local/tmp/replay.csv | run-as $pkg sh -c 'mkdir -p files && cat > $dest'"
  adb logcat -c
  adb shell am start -W -n $pkg/.ui.MainActivity >/dev/null # a background app can't start the service
  adb shell am broadcast -a $pkg.REPLAY --es path $dest --ef speed "$speed" --es wrist "$2" -n $pkg/.sensing.ReplayReceiver >/dev/null
  if [ $((n++)) -eq 0 ]; then sleep 10; adb shell dumpsys meminfo $pkg > "$out/meminfo.txt"; fi
  until log=$(adb logcat -d -s HapticWash:I | grep 'Session ended\|Session failed'); do sleep 1; done
  case $log in *failed*) adb logcat -d -s HapticWash:E; echo "replay failed: $1" >&2; exit 1 ;; esac
  sleep 2 # let the service stop before the next replay starts it again
  printf '%s\t%s\n' "$(basename "$1")" "$2" >> "$out/runs.tsv"
}
wrist_of() { sed -n 2p "$1" | cut -d, -f11; }

for f in "$csvdir"/*.csv; do replay "$f" "$(wrist_of "$f")"; done
for f in "$csvdir"/*.csv; do if [ "$(wrist_of "$f")" = right ]; then replay "$f" left; fi; done

adb exec-out run-as $pkg cat files/replay_washes.json > "$out/replay_washes.json"
./gradlew -q :wear:testDebugUnitTest --tests '*ReplayReportTest*' --rerun \
  -Dreplay.dir="$(winpath "$out")" -Dreplay.csv="$(winpath "$csvdir")"
echo "Wrote reports/M4_replay.md (raw results in $out)"
