# Release signing

No release keystore is committed. Official release CI requires `GHOSTIT_KEYSTORE_B64`, `GHOSTIT_KEYSTORE_PASSWORD`, `GHOSTIT_KEY_ALIAS`, and `GHOSTIT_KEY_PASSWORD`; missing values fail the release job.
