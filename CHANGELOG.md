# Changelog

## 1.11.0

- Adds the hardened Juggernaut Stage-1 capability plane from the deferred ONECLICK concepts.
- Adds deterministic topology symbol/room-code minting.
- Adds a SHA-256 linked in-memory compute notary with verification.
- Adds explicit IREE Android-runtime vs host-AOT doctrine and safe host plan generation without command execution.
- Adds `/jug status|tools|topo|receipt|verify|iree|iree-plan|mission` chat commands.
- Adds regression tests for topology determinism, notary verification, IREE path validation and geometric fail-closed behavior.
- Adds `/binaries` as the repository location for reviewed compiled APK ZIP snapshots.
- CI now packages the compiled APK and checksum manifest into a downloadable ZIP.

## 1.10.0

- Replaces the temporary recovery/probe scaffold with actual Android application source.
- Adds offline-first local Hector chat guarded by the geometric invariant window.
- Adds Android `TextToSpeech` persona synthesis with persistent pitch, rate, volume and auto-speak controls.
- Adds the original `COMEDY_CHAOS` caricature preset with explicit non-impersonation language.
- Adds `/voice`, `/tts`, `/speak` chat commands and a Compose persona/voice panel.
- Adds a host-side one-click test/build/hash/install/launch script with deterministic ADB device selection.
- Adds Android CI that produces a hashed debug APK artifact.
- Removes committed signing material and makes release signing secret-driven and fail-closed.
- Defers Juggernaut, IREE, topology/notary, JNI and large experimental subsystems from the supplied ONECLICK archive to later reviewable PRs.
