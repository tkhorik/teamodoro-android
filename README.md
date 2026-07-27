# Teamodoro: Android Platform

Technical specification for the Android mobile client.

## Project Description

Teamodoro for Android is a high-performance productivity client designed to stay synchronized with the team even under aggressive system battery optimization. It uses modern Android architectural patterns to ensure reliability.

## Core Sync Specification

The Android client implements the same algorithm as the reference web client,
[BaseSecrete/teamodoro](https://github.com/BaseSecrete/teamodoro), which defines
the cycle as a predicate over wall-clock minutes:

```js
// javascripts/teamodoro.js
inBreak: function() {
  var minutes = this.getDate().getMinutes();
  return (minutes >= 25 && minutes <= 29) || (minutes >= 55 && minutes <= 59);
}
```

That is a **30-minute cycle: 25 minutes focus, then 5 minutes break**, twice an
hour, with boundaries at `:00`, `:25`, `:30` and `:55`.

```kotlin
val position = System.currentTimeMillis().mod(30 * 60 * 1000L)
val phase = if (position < 25 * 60 * 1000L) WORK else BREAK
```

**There is no offset, no room and no server.** Because 30 divides 60 evenly, the
phase boundaries land on the same wall-clock instants for everyone, so any two
clients with a correct clock are synchronised for free. That implicit
synchronisation *is* the algorithm — anything persisted would only be something
that could drift out of agreement.

### Divergence from the reference

The reference reads local `getMinutes()`; this client takes the modulo of epoch
millis instead, which keeps the domain layer free of timezone and DST handling.
The two agree in every timezone whose UTC offset is a whole or half hour. In the
handful of 45-minute zones (Nepal, Chatham Islands) this client's boundaries sit
15 minutes away from the website's. This is deliberate; raise an issue if it
matters to you.

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
| Persistence | SharedPreferences (one boolean — see below) |
| DI | Hilt |

The only durable state is whether the user has switched notifications on. The
cycle itself is derived from the clock and needs no storage, so there is no
database, no schema and no migration path to maintain.

## Confidentiality

Confidential Technical Document | Teamodoro Project 2026
