# ONECLICK archive integration audit

PR #2 integrated the parts of `GhostIT-ONECLICK-1.9.0` that could be made deterministic and reviewable immediately: Android project bootstrap, local chat, persona controls, Android TTS, one-click host build/install, CI and fail-closed signing.

PR #3 / GhostIT 1.11 integrates a **hardened Stage-1 capability slice** inspired by the deferred Juggernaut, topology/notary and industry-IREE material:

- deterministic topology symbols and room codes;
- an in-memory SHA-256 linked compute receipt chain;
- a small Juggernaut tool catalog exposed through `/jug` commands;
- explicit IREE Android-runtime vs host-AOT doctrine;
- validated IREE host command *plans* without shell execution.

Still deferred: unrestricted host IDE execution, actual IREE compiler/runtime bundling, bundled JNI/native binaries, topology/mining experiments with background compute, GhostKart/Godot, and other large experimental subsystems. Those remain isolated until they can be independently reviewed, tested and given explicit capability boundaries.
