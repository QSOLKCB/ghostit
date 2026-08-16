#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
INPUT="${1:-$ROOT/app/src/main/mlir/simple_abs.mlir}"
OUTPUT="${2:-$ROOT/app/src/main/assets/iree/simple_abs_vmvx.vmfb}"
command -v iree-compile >/dev/null 2>&1 || { echo "iree-compile not found. Install iree-base-compiler==3.11.0" >&2; exit 2; }
mkdir -p "$(dirname "$OUTPUT")"
iree-compile --iree-hal-target-device=local --iree-hal-local-target-device-backends=vmvx "$INPUT" -o "$OUTPUT"
printf 'Compiled %s -> %s\n' "$INPUT" "$OUTPUT"
