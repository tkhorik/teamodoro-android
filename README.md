# Teamodoro: Android Platform

Technical specification for the Android mobile client.

## Project Description

Teamodoro for Android is a high-performance productivity client designed to stay synchronized with the team even under aggressive system battery optimization. It uses modern Android architectural patterns to ensure reliability.

## Core Sync Specification

The Android client uses a local-first timing model. By using `System.currentTimeMillis()` and a shared UTC offset, the app keeps its state aligned with the macOS and iOS versions without constant server polling.

```kotlin
val cycleMillis = 130 * 60 * 1000L
val currentTime = System.currentTimeMillis()
val roomOffset = database.getRoomOffset()
val elapsed = currentTime - roomOffset
val position = elapsed % cycleMillis
```

## Platform Features

### Background Reliability

- Foreground Service: a sticky service with a low-priority notification to keep the timer running in the background
- AlarmManager: exact triggers for transition alerts to wake the device if it enters Doze mode

### UI/UX

- Material 3: adaptive dynamic color that changes the system theme based on the timer phase
- Tiles: a custom Quick Settings tile to view remaining time at a glance from the notification shade

## Technical Stack

| Layer | Technology |
| --- | --- |
| Language | Kotlin 2.1+ |
| UI Framework | Jetpack Compose |
| Asynchrony | Kotlin Coroutines & Flow |
| Database | Room |
| DI | Hilt or Koin |

## Confidentiality

Confidential Technical Document | Teamodoro Project 2026
