# ONECLICK archive integration audit

PR #2 integrated the immediately reviewable Android bootstrap, local chat, persona/TTS, one-click build/install, CI and fail-closed signing material.

PR #3 / GhostIT 1.11 integrated the hardened Stage-1 Juggernaut, deterministic topology symbols, compute notary, and IREE host-plan boundary.

GhostIT 1.12 integrates the previously deferred large experimental tranche behind independent capability boundaries:

- explicit host IDE/process execution through a loopback-only bearer-authenticated bridge; arbitrary argv requires `--unrestricted` on the host;
- pinned IREE 3.11.0 host compiler and an Android JNI runtime build using local-sync + VMVX;
- bundled JNI/native compute libraries with finite input budgets;
- opt-in WorkManager topology/background compute constrained to charging + battery-not-low and explicitly non-cryptocurrency;
- embedded GhostKart using the Godot 4.7.1 Android library and local project assets;
- a machine-readable-in-code capability registry plus human-facing boundary documentation.

These features are no longer ambient/implicit authority. Host execution, background compute and engine/native integration each have explicit activation, locality, authentication or resource constraints described in `docs/EXPERIMENTAL_CAPABILITIES.md`.

Still isolated for future work: importing unknown prebuilt native binaries from the original archive without source/provenance, remote host execution without a local authenticated bridge, network mining/crypto protocols, and hidden/background execution that bypasses Android scheduling or user opt-in.
