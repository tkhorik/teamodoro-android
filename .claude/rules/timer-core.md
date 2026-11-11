---
paths:
  - "app/src/main/java/com/teamodoro/domain/**"
  - "app/src/main/java/com/teamodoro/ui/**"
  - "app/src/main/java/com/teamodoro/service/**"
  - "app/src/main/java/com/teamodoro/tile/**"
  - "app/src/test/**"
---

# Timer state — one source, three consumers

The phase is `System.currentTimeMillis().mod(CYCLE_MILLIS)`. There is **no networking layer and no persisted anchor**: 30 minutes divides an hour evenly, so boundaries land on `:00`, `:25`, `:30`, `:55` and any two clients with a correct clock agree for free. The only durable state is `TimerPreferences.isTracking` — whether the user wants notifications on.

- `CalculateTimerUseCase.timerStateFlow()` is the single source of truth: a 1-second ticker mapped through `calculate()`. It takes no arguments and reads no storage. `TimerViewModel`, `TimerService` and `TeamodoroTileService` each collect it **independently — do not add a fourth parallel source.** A change to `TimerState` or the flow's semantics hits the UI, the ongoing notification, the QS tile and alarm scheduling at once.
- **Never seed timer UI with `TimerState.DEFAULT`.** It is a placeholder for `9:00`-style zero state, not the current phase. Seed with `calculate(System.currentTimeMillis())`, or a device opening mid-break flashes "Focus 25:00" and can fire a spurious transition alert.
- **Modular arithmetic on the clock uses `Long.mod()`, never `%`.** `%` is truncated and yields a negative position for pre-epoch timestamps; `mod()` is floored and always non-negative.
- UI collects via `stateIn(WhileSubscribed(5_000))` and `collectAsStateWithLifecycle()`. Never collect a Flow directly in a composable.
- `TimerState` is immutable; derive values (`progressFraction`) as computed properties. It carries **no `isRunning`** — the cycle is always running; "is this device tracking it" lives in `TimerPreferences`.
- Side effects go through `TimerViewModel` via explicit `Intent` actions on `TimerService`. Composables must not touch `Context` or start services.
- Entering `TimerService` from the background requires `ContextCompat.startForegroundService`, not `startService` — plain `startService` throws on API 26+.
- Transition alerts are de-duplicated in `TimerService` by `cycleIndex * 2 + phase.ordinal`, because the ticker and the AlarmManager broadcast can both observe the same boundary. A third trigger must route through `alertIfTransitionUnseen()`, not notify directly.
- `AlarmScheduler` falls back to an inexact alarm when `canScheduleExactAlarms()` is false (API 31+ without the permission). Phase alerts can then land minutes late while the ticker stays correct, so notification and screen may briefly disagree. Known, not a bug to "fix" blindly.
- Layering is `ui → domain → data`, unidirectional. Nothing in `domain` or `data` may import Android UI types; `domain` is the only place timer math lives. `common/` at the repo root is the old Room layer, parked out of the build and in no source set — see `common/README.md`.
