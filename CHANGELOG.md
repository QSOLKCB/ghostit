# Changelog

## 1.12.0

- Integrates the deferred ONECLICK experimental tranche behind explicit capability boundaries.
- Adds bearer-authenticated loopback host IDE/process bridge; arbitrary argv requires explicit `--unrestricted` host mode.
- Pins IREE 3.11.0, compiles MLIR to VMVX on host/CI, and links the IREE runtime into Android JNI.
- Adds bundled native deterministic topology compute with hard iteration bounds.
- Adds opt-in WorkManager background topology compute constrained to charging and battery-not-low.
- Embeds Godot 4.7.1 and an asset-free GhostKart prototype in a non-exported Android Activity.
- Preserves read-only PR CI and packages a compiled GhostIT 1.12 APK ZIP as an Actions artifact.

## 1.11.0

- Adds the hardened Juggernaut Stage-1 capability plane, deterministic topology/notary tools, IREE planning boundary, and compiled APK snapshot workflow.

## 1.10.0

- Replaces the recovery scaffold with an Android app, local Hector/persona/TTS, one-click build/install, CI and fail-closed release signing.
