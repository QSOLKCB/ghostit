# GhostIT 1.12 experimental capability boundaries

GhostIT 1.12 deliberately integrates the large ONECLICK experiments as separate capability planes rather than one ambient authority surface.

| Capability | Implementation | Default | Boundary |
|---|---|---|---|
| Host IDE / process execution | `tools/host_bridge.py` + Android loopback client | IDE-only | Binds loopback by default, bearer token required, no shell execution. Restricted mode allows only trusted IDE binaries with workspace-local path arguments and no IDE flags; arbitrary argv requires explicit host `--unrestricted`. |
| IREE compiler | `iree-base-compiler==3.11.0` | Host/CI only | Compiler never runs in the Android process. |
| IREE runtime | NDK library linked against pinned IREE 3.11.0 runtime source | Local | Android API 28+, CMake 3.26+; host `iree-flatcc-cli` is built natively for schema generation; local-sync + VMVX only. |
| JNI/native compute | `libghostit_compute.so`, `libghostit_iree.so` | Bundled | No network calls; mining kernel hard-clamps iteration count. |
| Topology background compute | Android WorkManager | OFF | Explicit opt-in; charging and battery-not-low constraints; finite local-only CPU budget; not cryptocurrency. |
| GhostKart | Godot 4.7.1 Android library + local assets | Manual launch | Non-exported Activity; no remote assets, ads, telemetry, or network game service. |

## Host bridge

Run `python3 tools/host_bridge.py --workspace /path/to/project`. It prints a generated token. For a USB-connected Android device run `adb reverse tcp:8765 tcp:8765`, then in GhostIT set `/host token <token>` and use `/host status`.

Restricted mode accepts only trusted installed IDE command names (`code`, `codium`, `idea`, `studio`, `android-studio` when present). Generic launchers such as macOS `open` and Linux `xdg-open` are intentionally excluded. Restricted arguments must be workspace-local paths and IDE flags are rejected, preventing a launcher or IDE option from becoming an indirect arbitrary-execution surface. To deliberately enable arbitrary host argv execution, restart the bridge with `--unrestricted`. Even unrestricted mode is bearer-authenticated, executes argv directly (`shell=False`), caps each process at 120 seconds, and writes its audit JSONL under `~/.ghostit/` with directory mode `0700` and file mode `0600`.

## IREE

`tools/prepare_iree_runtime.sh` prepares pinned IREE v3.11.0 source for the Android NDK build. `tools/prepare_iree_host_tools.sh` builds the native host `iree-flatcc-cli` required by IREE's cross-compilation path and exposes it through `IREE_HOST_BIN_DIR`. It prefers Ninja when available and otherwise falls back to CMake's default installed generator. `tools/compile_iree_model.sh` uses the pinned host compiler to generate a VMVX `.vmfb` that is packed into the APK. The native runtime probe creates an IREE runtime instance and a `local-sync` HAL device on Android and consumes failed IREE status objects before returning.

IREE 3.11 requires CMake 3.26 or newer; the one-click installer records the discovered CMake installation in `local.properties` so Gradle uses that toolchain instead of an older Android SDK CMake. Android Gradle is restricted to the two GhostIT JNI targets, so target-only IREE command-line executables are not cross-built or packaged. The Android floor is API 28 because the IREE 3.11 C11 runtime uses `aligned_alloc`, available from that API level.

## Background topology compute

`/mine on` opts into periodic WorkManager execution. `/mine off` cancels it. `/mine once [iterations]` is an explicit one-shot run. Iterations are clamped to 1,000..2,000,000. The kernel performs deterministic score search only; there is no cryptocurrency protocol, wallet, pool, remote coordinator, proof-of-work submission, or network access.

## GhostKart

`/kart` opens an embedded Godot Activity. The game is intentionally asset-free and draws its own primitive track/kart graphics. This keeps the engine integration reviewable and avoids importing third-party game assets.
