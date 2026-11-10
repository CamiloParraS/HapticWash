#!/usr/bin/env bash
# M6 on a real watch: probe it, record labelled raw IMU (own_watch), pull, measure.
# Debug build only (RecordService lives in wear/src/debug). Run from Git Bash.
#
#   scripts/watch_record.sh connect <ip:port>          adb over Wi-Fi (pair first: adb pair <ip:port>)
#   scripts/watch_record.sh probe                      device + sensor specs -> reports/hardware/
#   scripts/watch_record.sh start <session> <left|right> [label]   install if needed, start recording
#   scripts/watch_record.sh label                      keys mark the current label live (q stops)
#   scripts/watch_record.sh mark <label|id>            one mark: NULL PALM_TO_PALM ... WASH_OTHER, -1
#   scripts/watch_record.sh stop                       stop, print the rate stats
#   scripts/watch_record.sh pull                       recordings -> ../HapticWash-AI/data/own_watch/
#   scripts/watch_record.sh perf [n]                   cold start x n (default 5) + current heap
#   scripts/watch_record.sh battery-reset | battery    batterystats window around a usage block
#
# Recordings carry your own biometric data: they go to the AI repo's gitignored data/, never git.
# A row's label is the last mark at or before it; marks via adb lag ~0.2-0.5 s, so for gate data
# film the Wash too (start buzzes once as a sync cue) and relabel from <session>.marks.csv.
set -euo pipefail
export MSYS_NO_PATHCONV=1 # Git Bash would otherwise rewrite device paths
pkg=com.hapticwash
svc=$pkg/.sensing.RecordService
cd "$(dirname "$0")/.."

send() { # action [extras...]
    adb shell am start-foreground-service -n $svc -a "$pkg.$1" "${@:2}" >/dev/null
}

case ${1:-} in
connect)
    adb connect "${2:?usage: $0 connect <ip:port>}"
    adb devices -l
    ;;
probe)
    out=reports/hardware
    mkdir -p "$out"
    model=$(adb shell getprop ro.product.model | tr -d '\r' | tr ' ' '_')
    {
        for p in ro.product.manufacturer ro.product.model ro.build.version.release ro.build.version.sdk ro.build.fingerprint; do
            echo "$p=$(adb shell getprop $p | tr -d '\r')"
        done
        echo "battery: $(adb shell dumpsys battery | tr -d '\r' | grep -E 'level|scale' | tr -s ' \n' ' ')"
    } >"$out/$model.device.txt"
    adb shell dumpsys sensorservice | tr -d '\r' >"$out/$model.sensorservice.txt"
    cat "$out/$model.device.txt"
    echo "--- accelerometer / gyroscope (wake-up flag, FIFO, rate range):"
    grep -iE "accel|gyro" "$out/$model.sensorservice.txt" | grep -iE "Hz|fifo|wakeUp|non-wakeUp" | head -20 || true
    echo "full dump: $out/$model.sensorservice.txt"
    ;;
start)
    session=${2:?usage: $0 start <session> <left|right> [label]}
    wrist=${3:?usage: $0 start <session> <left|right> [label]}
    adb shell pm list packages | grep -q "^package:$pkg\$" || ./gradlew -q :wear:installDebug
    adb shell pm grant $pkg android.permission.POST_NOTIFICATIONS 2>/dev/null || true
    adb shell am start -W -n $pkg/.ui.MainActivity >/dev/null # foreground, so the service may start
    send RECORD_START --es session "$session" --es wrist "$wrist" --es label "${4:--1}"
    sleep 2
    adb logcat -d -s HapticWash:I | grep "Record" | tail -2
    echo "recording $session ($wrist). Live rates: adb logcat -s HapticWash:I | grep Record"
    ;;
mark)
    send RECORD_MARK --es label "${2:?usage: $0 mark <label|id>}"
    ;;
label)
    echo "0 NULL  1 PALM_TO_PALM  2 BACK_OF_HAND  3 INTERLACED_FINGERS  4 THUMBS  5 FINGERTIPS  6 WASH_OTHER  u unlabelled  q stop"
    while IFS= read -rsn1 k; do
        case $k in
        [0-6]) send RECORD_MARK --es label "$k"; echo "mark $k" ;;
        u) send RECORD_MARK --es label -1; echo "mark unlabelled" ;;
        q) "$0" stop; break ;;
        esac
    done
    ;;
stop)
    send RECORD_STOP
    sleep 2
    adb logcat -d -s HapticWash:I | grep "Record stopped" | tail -1
    ;;
pull)
    dest=../HapticWash-AI/data/own_watch
    mkdir -p "$dest"
    for f in $(adb shell run-as $pkg ls files/recordings | tr -d '\r'); do
        adb exec-out run-as $pkg cat "files/recordings/$f" >"$dest/$f"
        echo "$dest/$f ($(wc -l <"$dest/$f") lines)"
    done
    ;;
perf)
    n=${2:-5}
    for _ in $(seq "$n"); do
        adb shell am force-stop $pkg
        sleep 1
        adb shell am start -W -n $pkg/.ui.MainActivity | tr -d '\r' | grep -E "TotalTime|WaitTime"
    done
    echo "--- heap now (run this during a Wash for the session budget, <= 40 MB):"
    adb shell dumpsys meminfo $pkg | tr -d '\r' | grep -E "TOTAL PSS|Java Heap|Native Heap" | head -3
    ;;
battery-reset)
    adb shell dumpsys batterystats --reset
    adb shell dumpsys battery | tr -d '\r' | grep level
    echo "unplug the watch (Wi-Fi adb only), use it, then: $0 battery"
    ;;
battery)
    out=reports/hardware
    mkdir -p "$out"
    f="$out/batterystats_$(date +%Y%m%d_%H%M%S).txt"
    adb shell dumpsys batterystats $pkg | tr -d '\r' >"$f"
    adb shell dumpsys battery | tr -d '\r' | grep level
    grep -iE "Estimated power use|Uid u0a.*:|Discharge|Time on battery" "$f" | head -10 || true
    echo "full: $f"
    ;;
*)
    sed -n 2,17p "$0"
    exit 1
    ;;
esac
