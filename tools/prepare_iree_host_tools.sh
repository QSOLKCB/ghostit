#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IREE="$ROOT/app/src/main/cpp/third_party/iree"
BUILD="$ROOT/.iree-host"
BIN="$BUILD/tools"

[ -f "$IREE/CMakeLists.txt" ] || { echo "IREE source missing; run tools/prepare_iree_runtime.sh first." >&2; exit 2; }

if [ -x "$BIN/iree-flatcc-cli" ]; then
  echo "IREE host schema tool already prepared: $BIN/iree-flatcc-cli"
  exit 0
fi

CMAKE_ARGS=(
  -S "$IREE"
  -B "$BUILD"
  -DCMAKE_BUILD_TYPE=Release
  -DIREE_BUILD_COMPILER=OFF
  -DIREE_BUILD_TESTS=OFF
  -DIREE_BUILD_SAMPLES=OFF
  -DIREE_BUILD_BENCHMARKS=OFF
  -DIREE_BUILD_PYTHON_BINDINGS=OFF
  -DIREE_BUILD_BINDINGS_TFLITE=OFF
  -DIREE_BUILD_BINDINGS_TFLITE_JAVA=OFF
  -DIREE_BUILD_ALL_CHECK_TEST_MODULES=OFF
  -DIREE_ENABLE_CPUINFO=OFF
)

if command -v ninja >/dev/null 2>&1; then
  echo "Configuring IREE host tools with Ninja."
  cmake -GNinja "${CMAKE_ARGS[@]}"
else
  echo "Ninja not found; using CMake's default available generator."
  cmake "${CMAKE_ARGS[@]}"
fi

cmake --build "$BUILD" --target iree-flatcc-cli
[ -x "$BIN/iree-flatcc-cli" ] || { echo "Host iree-flatcc-cli was not produced at $BIN" >&2; exit 3; }
echo "Prepared IREE host tools at $BIN"
