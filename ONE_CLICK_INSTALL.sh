#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; cd "$ROOT"
SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
for candidate in "$SDK" "$HOME/Android/Sdk" "$HOME/Library/Android/sdk"; do [ -n "$candidate" ] && [ -d "$candidate" ] && SDK="$candidate" && break; done
[ -n "$SDK" ] && [ -d "$SDK" ] || { echo "Android SDK not found; set ANDROID_SDK_ROOT." >&2; exit 2; }
export ANDROID_SDK_ROOT="$SDK" ANDROID_HOME="$SDK"; export PATH="$SDK/platform-tools:$PATH"; printf 'sdk.dir=%s\n' "$SDK" > local.properties
GRADLE="./gradlew"; [ -x "$GRADLE" ] || GRADLE="$(command -v gradle || true)"; [ -n "$GRADLE" ] || { echo "Gradle wrapper/system Gradle unavailable." >&2; exit 3; }
"$GRADLE" :app:testDebugUnitTest :app:assembleDebug --no-daemon
APK="app/build/outputs/apk/debug/app-debug.apk"; test -f "$APK"
if command -v sha256sum >/dev/null 2>&1; then
  sha256sum "$APK"
elif command -v shasum >/dev/null 2>&1; then
  shasum -a 256 "$APK"
else
  echo "No SHA-256 tool found (need sha256sum or shasum). APK is built at $APK." >&2
  exit 4
fi
command -v adb >/dev/null 2>&1 || { echo "No adb; sideload $APK manually."; exit 0; }
DEVICES="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"
SERIAL="${ANDROID_SERIAL:-}"
if [ -z "$SERIAL" ]; then
  COUNT="$(printf '%s\n' "$DEVICES" | awk 'NF {count++} END {print count+0}')"
  [ "$COUNT" -eq 1 ] || { echo "Need exactly one device or ANDROID_SERIAL; APK is built."; exit 0; }
  SERIAL="$(printf '%s\n' "$DEVICES" | awk 'NF {print; exit}')"
fi
adb -s "$SERIAL" get-state >/dev/null 2>&1 || { echo "Device $SERIAL is not available; APK is built." >&2; exit 5; }
adb -s "$SERIAL" install -r "$APK"
adb -s "$SERIAL" shell am start -n com.osv01d.client.debug/com.osv01d.client.ui.MainActivity
