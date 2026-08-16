#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; cd "$ROOT"
SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
for candidate in "$SDK" "$HOME/Android/Sdk" "$HOME/Library/Android/sdk"; do [ -n "$candidate" ] && [ -d "$candidate" ] && SDK="$candidate" && break; done
[ -n "$SDK" ] && [ -d "$SDK" ] || { echo "Android SDK not found; set ANDROID_SDK_ROOT." >&2; exit 2; }
command -v cmake >/dev/null 2>&1 || { echo "CMake >= 3.26 is required for the bundled IREE runtime." >&2; exit 2; }
cmake_version="$(cmake --version | awk 'NR==1 {print $3}')"
cmake_major="${cmake_version%%.*}"
cmake_rest="${cmake_version#*.}"
cmake_minor="${cmake_rest%%.*}"
if [ "$cmake_major" -lt 3 ] || { [ "$cmake_major" -eq 3 ] && [ "$cmake_minor" -lt 26 ]; }; then
  echo "IREE 3.11 requires CMake >= 3.26; found $cmake_version" >&2
  exit 2
fi
cmake_prefix="$(cd "$(dirname "$(command -v cmake)")/.." && pwd)"
export ANDROID_SDK_ROOT="$SDK" ANDROID_HOME="$SDK"; export PATH="$SDK/platform-tools:$PATH"
printf 'sdk.dir=%s\ncmake.dir=%s\n' "$SDK" "$cmake_prefix" > local.properties
./tools/prepare_iree_runtime.sh
./tools/prepare_iree_host_tools.sh
export IREE_HOST_BIN_DIR="$ROOT/.iree-host/tools"

IREE_VENV="$ROOT/.iree-compiler-3.11"
IREE_PY="$IREE_VENV/bin/python"
IREE_BIN="$IREE_VENV/bin/iree-compile"
iree_ok=false
if [ -x "$IREE_PY" ] && [ -x "$IREE_BIN" ]; then
  if "$IREE_PY" - <<'PY'
from importlib.metadata import version
raise SystemExit(0 if version("iree-base-compiler") == "3.11.0" else 1)
PY
  then
    iree_ok=true
  fi
fi
if [ "$iree_ok" != true ]; then
  rm -rf "$IREE_VENV"
  python3 -m venv "$IREE_VENV"
  "$IREE_PY" -m pip install --disable-pip-version-check "iree-base-compiler==3.11.0"
fi
export IREE_COMPILE_BIN="$IREE_BIN"
./tools/compile_iree_model.sh
GRADLE="./gradlew"; [ -x "$GRADLE" ] || GRADLE="$(command -v gradle || true)"; [ -n "$GRADLE" ] || { echo "Gradle wrapper/system Gradle unavailable." >&2; exit 3; }
"$GRADLE" :app:testDebugUnitTest :app:assembleDebug --no-daemon
APK="app/build/outputs/apk/debug/app-debug.apk"; test -f "$APK"
if command -v sha256sum >/dev/null 2>&1; then sha256sum "$APK"; elif command -v shasum >/dev/null 2>&1; then shasum -a 256 "$APK"; else echo "No SHA-256 tool found." >&2; exit 4; fi
command -v adb >/dev/null 2>&1 || { echo "No adb; sideload $APK manually."; exit 0; }
DEVICES="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"; SERIAL="${ANDROID_SERIAL:-}"
if [ -z "$SERIAL" ]; then COUNT="$(printf '%s\n' "$DEVICES" | awk 'NF {count++} END {print count+0}')"; [ "$COUNT" -eq 1 ] || { echo "Need exactly one device or ANDROID_SERIAL; APK is built."; exit 0; }; SERIAL="$(printf '%s\n' "$DEVICES" | awk 'NF {print; exit}')"; fi
adb -s "$SERIAL" install -r "$APK"
adb -s "$SERIAL" shell am start -n com.osv01d.client.debug/com.osv01d.client.ui.MainActivity
