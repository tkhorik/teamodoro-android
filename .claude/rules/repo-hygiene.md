---
paths:
  - "gradle/**"
  - ".github/**"
  - "gradlew"
  - "gradlew.bat"
---

# Repo hygiene

- **`gradle/` is not a normal directory here.** `GRADLE_USER_HOME` resolves into it, so Gradle writes caches, daemon state and profiles inside the repo. `.gitignore` **whitelists** it — only `libs.versions.toml` and `wrapper/` are tracked. Never `git add -f` anything under it: this mistake previously put 23k files / 787 MB into history.
- `gradle/wrapper/gradle-wrapper.jar`, `gradlew`, `gradlew.bat` — regenerate via `./gradlew wrapper`, never hand-edit.
- **`~/keystores/teamodoro-release.jks` is unrecoverable.** Losing it means no future update can install over an existing copy. Signing material never enters the repo; CI decodes `KEYSTORE_BASE64` into `$RUNNER_TEMP` and deletes it in an `always()` step.
- Release build: `./gradlew assembleRelease`, unsigned unless `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` are exported.
