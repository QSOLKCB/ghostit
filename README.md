# GhostIT 1.10

Android Studio-ready GhostIT core with offline local Hector chat, Android `TextToSpeech` persona voices, persistent pitch/rate/volume controls, and a hardened host-side one-click build/install path.

`COMEDY_CHAOS` is an original exaggerated stand-up caricature preset. It does **not** clone or impersonate a real person's biometric voice.

## One-click build/install

Requirements: JDK 17, Android SDK/API 35, and Gradle 8.7 available on `PATH` (or an executable `./gradlew` if you add the standard wrapper locally).

```bash
./ONE_CLICK_INSTALL.sh
```

The script runs unit tests, assembles the debug APK, prints its SHA-256, installs with `adb` when a single device (or `ANDROID_SERIAL`) is available, and launches GhostIT.

GitHub Actions uses Gradle 8.7 directly and publishes `GhostIT-1.10.0-debug.apk` plus `SHA256SUMS.txt` as a workflow artifact.

See `docs/ONECLICK_SOURCE_AUDIT.md` for what was intentionally deferred from the supplied experimental archive.
