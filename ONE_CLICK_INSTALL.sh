#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; cd "$ROOT"
SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
for candidate in "$SDK" "$HOME/Android/Sdk" "$HOME/Library/Android/sdk"; do [ -n "$candidate" ] && [ -d "$candidate" ] && SDK="$candidate" && break; done
[ -n "$SDK" ] && [ -d "$SDK" ] || { echo "Android SDK not found; set ANDROID_SDK_ROOT." >&2; exit 2; }
export ANDROID_SDK_ROOT="$SDK" ANDROID_HOME="$SDK"; export PATH="$SDK/platform-tools:$PATH"; printf 'sdk.dir=%s\n' "$SDK" > local.properties
GRADLE="./gradlew"; [ -x "$GRADLE" ] || GRADLE="$(command -v gradle || true)"; [ -n "$GRADLE" ] || { echo "Gradle wrapper/system Gradle unavailable." >&2; exit 3; }
"$GRADLE" :app:testDebugUnitTest :app:assembleDebug --no-daemon
APK="app/build/outputs/apk/debug/app-debug.apk"; test -f "$APK"; sha256sum "$APK"
command -v adb >/dev/null 2>&1 || { echo "No adb; sideload $APK manually."; exit 0; }
mapfile -t DEVICES < <(adb devices | awk 'NR>1 && $2=="device" {print $1}')
SERIAL="${ANDROID_SERIAL:-}"; if [ -z "$SERIAL" ]; then [ "${#DEVICES[@]}" -eq 1 ] || { echo "Need exactly one device or ANDROID_SERIAL; APK is built."; exit 0; }; SERIAL="${DEVICES[0]}"; fi
adb -s "$SERIAL" install -r "$APK"
adb -s "$SERIAL" shell am start -n com.osv01d.client.debug/com.osv01d.client.ui.MainActivity
