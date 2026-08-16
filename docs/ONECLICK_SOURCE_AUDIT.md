# ONECLICK archive integration audit

PR #2 intentionally integrates the parts of `GhostIT-ONECLICK-1.9.0` that can be made deterministic and reviewable now: Android project bootstrap, local chat, persona controls, Android TTS, one-click host build/install, CI and fail-closed signing.

Deferred to a later PR: Juggernaut, topology/notary mining experiments, host IREE compiler control, bundled JNI binaries, GhostKart and other large experimental subsystems. The supplied archive mixed those with compile blockers and machine-specific/signing material; importing them wholesale would make this PR impossible to review safely.
