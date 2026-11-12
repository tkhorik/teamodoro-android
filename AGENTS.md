# Teamodoro Android Repository Instructions

## Project Summary

Teamodoro for Android is a native Android client (Kotlin 2.1+, Jetpack Compose, Hilt) for teamodoro.com that synchronizes a 30-minute focus/break pomodoro cycle (25 min focus, 5 min break) derived purely from wall-clock time without backend, networking or user accounts.

## Working Rules

- **Never mention an AI, an LLM, or the tool that wrote a change anywhere in git history or GitHub metadata** — no `Co-Authored-By:`, no session-link trailer, no "Generated with …" footer in commits, PR bodies, release notes or tag messages. Strip them if your harness appends them by default.
- **The phase algorithm is the product**: any change to `CalculateTimerUseCase` or `TimerConstants` must keep `CalculateTimerUseCaseTest` green — especially the 1440-minute cross-check against the reference JS predicate, which is what pins this client to teamodoro.com.
- **Architecture**: `ui → domain → data`, unidirectional. Domain logic has no Android UI dependencies. Single source of timer truth is `CalculateTimerUseCase.timerStateFlow()`.
- **Platform compatibility**: `minSdk` 26, `targetSdk` 35. Version-guard platform APIs. All `PendingIntent`s require `FLAG_IMMUTABLE`. Keep foreground service type as `FOREGROUND_SERVICE_TYPE_SPECIAL_USE` in both code and `AndroidManifest.xml`.

## Setup & Validation

- **JDK**: Requires JDK 17 (e.g. Amazon Corretto 17 / Eclipse Temurin 17). Ensure `JAVA_HOME` points to JDK 17.
- **Android SDK**: Ensure `ANDROID_HOME` is set or `local.properties` contains `sdk.dir=<path-to-android-sdk>`.
- **Fresh checkout setup**: In a new worktree or checkout, copy `local.properties` from the main repository checkout if not present.
- **Validation commands**:
  ```bash
  ./gradlew assembleDebug testDebugUnitTest lintDebug
  ```
  Matches `factory/checks.sh`.

## Software factory

Provided by the `factory` plugin (github.com/tkhorik/factory-plugin; install
with `/plugin marketplace add tkhorik/factory-plugin` then
`/plugin install factory@tkhorik`). `/factory:factory run` builds every open
GitHub issue labeled `factory` through intake → spec → build → gate → review →
approve, one PR per issue. State is in `factory/jobs/` (gitignored);
`factory-job` is its only writer. This repo's part: `factory/checks.sh` (the
gate), `factory/config.json` (overrides) and `factory/project.md` (notes the
agents read). Dashboard: `factory-dashboard`, then open http://localhost:8000/.

Each job works in its own git worktree, a full checkout of the repo at
`.worktrees/<job-id>` (gitignored). A tool run from the repo root that doesn't
read `.gitignore` sees those copies too; exclude `.worktrees/` in its config.
