---
paths:
  - "app/src/main/**"
  - "app/build.gradle.kts"
  - "app/proguard-rules.pro"
  - "gradle.properties"
---

# Platform hazards

- **Every API-gated call must be version-guarded.** `minSdk` 26, `targetSdk` 35 — this is a live hazard. `Tile.setSubtitle` (API 29+) and `startForeground` service types (API 34+) both shipped as bugs and were caught **only by `./gradlew lintDebug`**, which is the only thing catching this. Prefer `ServiceCompat`/`NotificationCompat` over raw platform calls.
- Foreground service type must stay `FOREGROUND_SERVICE_TYPE_SPECIAL_USE` in **both** `TimerService.onCreate()` and `AndroidManifest.xml`. A mismatch kills the service on Android 14+.
- All `PendingIntent`s use `FLAG_IMMUTABLE`.
- **R8 keep rules**: Hilt injects into `TimerService`, `BootReceiver` and `TeamodoroTileService`. New reflective or generated types may need rules in `app/proguard-rules.pro`. `assembleDebug` will **not** catch this — verify with `assembleRelease` and by installing the result. The shipped `v0.1.0-beta` APK is R8-minified and has never been executed on a device; treat runtime behaviour under minification as unverified.
- Constructor injection everywhere (`@Inject constructor`); field injection only where the framework owns construction (`Service`, `BroadcastReceiver`).
- `android:allowBackup="true"` includes SharedPreferences in cloud backup — acceptable for one non-sensitive boolean, revisit if user content is added.
- Gradle does **not** read `org.gradle.jvmargs` from `local.properties`; it must live in `gradle.properties` or R8 OOMs on the 512 MB default. Machine-specific `org.gradle.java.home` belongs in `~/.gradle/gradle.properties`, never in the committed `gradle.properties`.
