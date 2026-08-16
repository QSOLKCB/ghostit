# GhostIT 1.11

Android Studio-ready GhostIT with offline local Hector chat, Android `TextToSpeech` persona voices, persistent pitch/rate/volume controls, hardened one-click build/install, and a deterministic **Juggernaut Stage-1 capability plane**.

`COMEDY_CHAOS` is an original exaggerated stand-up caricature preset. It does **not** clone or impersonate a real person's biometric voice.

## Juggernaut

GhostIT 1.11 adds local topology minting, a SHA-256 linked receipt notary, and an explicit industry-IREE capability layer. Use `/jug tools` in chat to see the tool surface. Stage 1 is deliberately no-shell/no-network and does not run compilers or hidden background compute.

See `docs/JUGGERNAUT.md` for the command contract and safety boundary.

## One-click build/install

Requirements: JDK 17, Android SDK/API 35, and Gradle 8.7 available on `PATH` (or an executable `./gradlew` if you add the standard wrapper locally).

```bash
./ONE_CLICK_INSTALL.sh
```

The script runs unit tests, assembles the debug APK, prints its SHA-256, installs with `adb` when a single device (or `ANDROID_SERIAL`) is available, and launches GhostIT.

GitHub Actions publishes `GhostIT-1.11.0-debug.apk`, `SHA256SUMS.txt`, and `GhostIT-1.11.0-debug.apk.zip` as workflow artifacts. A reviewed compiled snapshot is also stored under `/binaries` for direct download.

See `docs/ONECLICK_SOURCE_AUDIT.md` for what remains intentionally deferred from the supplied experimental archive.
