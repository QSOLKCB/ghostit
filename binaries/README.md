# GhostIT Android binaries

This directory contains reviewed, already-compiled Android APK snapshots packaged as ZIP files.

Each ZIP contains:

- the compiled `.apk`;
- `SHA256SUMS.txt` for the APK.

The binary is produced by the same Android CI path used by the pull request. Debug snapshots are suitable for direct testing/sideloading; official release builds remain separately signed through the fail-closed release workflow.

For each checked-in snapshot, the adjacent `*.SOURCE.txt` records the exact source commit and workflow run that produced the binary. This lets later documentation-only commits be validated without pretending the binary was rebuilt from a different source revision.

Do not treat an APK in this directory as a signed production release unless its filename and release metadata explicitly say so.
