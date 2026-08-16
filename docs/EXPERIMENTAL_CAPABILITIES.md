# GhostIT 1.12 experimental capability boundaries

GhostIT 1.12 deliberately integrates the large ONECLICK experiments as separate capability planes rather than one ambient authority surface.

| Capability | Implementation | Default | Boundary |
|---|---|---|---|
| Host IDE / process execution | `tools/host_bridge.py` + Android loopback client | IDE-only | Binds loopback by default, bearer token required, no shell execution. Arbitrary argv requires explicit host `--unrestricted`. |
| IREE compiler | `iree-base-compiler==3.11.0` | Host/CI only | Compiler never runs in the Android process. |
| IREE runtime | NDK library linked against pinned IREE 3.11.0 runtime source | Local | local-sync + VMVX only; JNI probe validates instance/device creation. |
| JNI/native compute | `libghostit_compute.so`, `libghostit_iree.so` | Bundled | No network calls; mining kernel hard-clamps iteration count. |
| Topology background compute | Android WorkManager | OFF | Explicit opt-in; charging and battery-not-low constraints; finite local-only CPU budget; not cryptocurrency. |
| GhostKart | Godot 4.7.1 Android library + local assets | Manual launch | Non-exported Activity; no remote assets, ads, telemetry, or network game service. |

## Host bridge

Run `python3 tools/host_bridge.py --workspace /path/to/project`. It prints a generated token. For a USB-connected Android device run `adb reverse tcp:8765 tcp:8765`, then in GhostIT set `/host token <token>` and use `/host status`.

Restricted mode accepts IDE/open commands only. To deliberately enable arbitrary host argv execution, restart the bridge with `--unrestricted`. Even unrestricted mode is bearer-authenticated, executes argv directly (`shell=False`), caps each process at 120 seconds, and writes an audit JSONL file under `~/.ghostit/`.

## IREE

`tools/prepare_iree_runtime.sh` prepares pinned IREE v3.11.0 source for the Android NDK build. `tools/compile_iree_model.sh` uses the pinned host compiler to generate a VMVX `.vmfb` that is packed into the APK. The native runtime probe creates an IREE runtime instance and a `local-sync` HAL device on Android.

## Background topology compute

`/mine on` opts into periodic WorkManager execution. `/mine off` cancels it. `/mine once [iterations]` is an explicit one-shot run. Iterations are clamped to 1,000..2,000,000. The kernel performs deterministic score search only; there is no cryptocurrency protocol, wallet, pool, remote coordinator, proof-of-work submission, or network access.

## GhostKart

`/kart` opens an embedded Godot Activity. The game is intentionally asset-free and draws its own primitive track/kart graphics. This keeps the engine integration reviewable and avoids importing third-party game assets.
