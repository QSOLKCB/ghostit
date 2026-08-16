#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEST="$ROOT/app/src/main/cpp/third_party/iree"
VERSION="v3.11.0"
if [ -f "$DEST/CMakeLists.txt" ] && [ "$(git -C "$DEST" describe --tags --exact-match 2>/dev/null || true)" = "$VERSION" ]; then
  echo "IREE runtime source $VERSION already prepared."
  exit 0
fi
rm -rf "$DEST"
mkdir -p "$(dirname "$DEST")"
git clone --branch "$VERSION" --depth 1 --filter=blob:none https://github.com/iree-org/iree.git "$DEST"
git -C "$DEST" submodule update --init --recursive --depth 1
printf 'Prepared IREE %s runtime source at %s\n' "$VERSION" "$DEST"
