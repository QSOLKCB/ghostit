# GhostIT 1.12

Android GhostIT experimental capability lab: offline Hector/persona/TTS, Juggernaut, explicit host execution bridge, pinned IREE compiler + Android runtime/JNI, bounded topology background compute, and embedded GhostKart/Godot.

## Build

Requirements: JDK 17, Android SDK/API 35, Git, Python 3.10+, and Gradle 8.7 (or a local Gradle wrapper).

```bash
./ONE_CLICK_INSTALL.sh
```

The build prepares pinned IREE v3.11.0 runtime source, installs/uses `iree-base-compiler==3.11.0`, compiles the VMVX sample, runs unit tests, builds native JNI libraries, resolves the Godot Android AAR, and assembles the APK.

PR CI is read-only: it never commits generated binaries back to the PR branch, avoiding the maintainer-approval loop encountered during 1.11 development.

## Experimental commands

`/lab status` describes capability boundaries. `/iree probe` tests the native runtime. `/kart` launches GhostKart. `/mine status|on|off|once [iterations]` controls local topology compute. `/host status|endpoint|token|exec` talks to the explicitly started host bridge. Existing `/jug`, TTS and persona commands remain available.

See `docs/EXPERIMENTAL_CAPABILITIES.md` and `docs/ONECLICK_SOURCE_AUDIT.md` before enabling host or background capabilities.
