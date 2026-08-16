# Changelog

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
