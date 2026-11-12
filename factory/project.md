# Project notes for the factory

## Project

Teamodoro for Android: a native Android client (Kotlin, Jetpack Compose, Hilt)
for teamodoro.com that synchronizes a 30-minute focus/break cycle derived purely
from wall-clock time, with no backend, network or user accounts.

## UI

Jetpack Compose Material 3 theme and tokens in
`app/src/main/java/com/teamodoro/ui/theme/` (Color.kt, Theme.kt, Type.kt),
phase-adaptive styling (Work vs Break), string resources in
`res/values/strings.xml` (and translations), and neighbouring composables in
`app/src/main/java/com/teamodoro/ui/`.

## Security

- **Platform & permissions**: new or changed permissions (`POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `FOREGROUND_SERVICE_SPECIAL_USE`); the foreground service type matches in both code and manifest (`FOREGROUND_SERVICE_TYPE_SPECIAL_USE`).
- **Exported components & IPC**: services, receivers, activities and Quick Settings tile declarations in `AndroidManifest.xml`; `android:exported` set explicitly and safely; every `PendingIntent` uses `FLAG_IMMUTABLE`.
- **API level guarding**: version-gated platform calls (`minSdk` 26, `targetSdk` 35) don't crash on older API versions.
- **Signing & keystores**: signing keys, passwords, `keystore.properties` or `*.jks` files never enter git or build logs.
- **Data & persistence**: `SharedPreferences` store only expected non-sensitive settings (tracking, theme, language); `allowBackup` is safe.
- **Input & broadcasts**: intent extras, broadcasts and system event handlers are validated.
- **Dependencies**: new entries in `gradle/libs.versions.toml` are needed, pinned and reputable.

## Escalate

- modifies `CalculateTimerUseCase` or `TimerConstants` (the core phase algorithm);
- adds Android platform permissions, or changes foreground service configuration in `AndroidManifest.xml`;
- touches signing material, keystores, release configuration, or ProGuard/R8 keep rules;
- changes Gradle build logic, the wrapper, or `gradle/libs.versions.toml`;
- introduces network requests or complex persistence (the app is offline with no database).
