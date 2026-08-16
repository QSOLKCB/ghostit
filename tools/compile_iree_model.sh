#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
INPUT="${1:-$ROOT/app/src/main/mlir/simple_abs.mlir}"
OUTPUT="${2:-$ROOT/app/src/main/assets/iree/simple_abs_vmvx.vmfb}"
COMPILER="${IREE_COMPILE_BIN:-$(command -v iree-compile || true)}"
[ -n "$COMPILER" ] && [ -x "$COMPILER" ] || { echo "iree-compile not found. Install iree-base-compiler==3.11.0 or set IREE_COMPILE_BIN." >&2; exit 2; }
mkdir -p "$(dirname "$OUTPUT")"
"$COMPILER" --iree-hal-target-device=local --iree-hal-local-target-device-backends=vmvx "$INPUT" -o "$OUTPUT"
printf 'Compiled %s -> %s with %s\n' "$INPUT" "$OUTPUT" "$COMPILER"
