#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEST="$ROOT/app/src/main/cpp/third_party/iree"
VERSION="v3.11.0"

prepare_submodules() {
  git -C "$DEST" submodule sync --recursive
  git -C "$DEST" submodule update --init --recursive --depth 1
  test -f "$DEST/third_party/flatcc/CMakeLists.txt"
  test -f "$DEST/third_party/flatcc/include/flatcc/flatcc_flatbuffers.h"
}

if [ -f "$DEST/CMakeLists.txt" ] && [ "$(git -C "$DEST" describe --tags --exact-match 2>/dev/null || true)" = "$VERSION" ]; then
  echo "IREE runtime source $VERSION cached; repairing/verifying submodules."
  if prepare_submodules; then
    echo "IREE runtime source $VERSION ready."
    exit 0
  fi
  echo "Cached IREE checkout is incomplete; recreating it." >&2
  rm -rf "$DEST"
fi

rm -rf "$DEST"
mkdir -p "$(dirname "$DEST")"
git clone --branch "$VERSION" --depth 1 --filter=blob:none https://github.com/iree-org/iree.git "$DEST"
prepare_submodules
printf 'Prepared IREE %s runtime source at %s\n' "$VERSION" "$DEST"
