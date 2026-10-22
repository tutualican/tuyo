# Tuyo — Engineering Notes

## 1. Stack

| Area | Choice |
| --- | --- |
| Language | Kotlin 2.3 |
| UI | Jetpack Compose, Material 3 primitives only where useful |
| Build | Gradle 8.13, AGP 8.10.1 |
| SDK | `compileSdk 36`, `targetSdk 36`, `minSdk 26` |
| Java | 17 |
| Fonts | Inter Variable (bundled) |
| Tests | JUnit 4 for the domain |
| Persistence | None in v0.1 (in-memory) |
| Network | None; no `INTERNET` permission |

Versions are pinned in `gradle/libs.versions.toml`. Lint may report newer
available versions; upgrades are deliberately separate from feature work.

## 2. Architecture

```text
MainActivity
└── TuyoTheme (dark/light semantic tokens)
    └── TuyoApp                      ← owns all app state
        ├── TodayScreen
        ├── MistakesScreen ──────────┐
        │   └── MistakeDetailScreen  │ review callback updates StudyData
        ├── TopicsScreen             │
        ├── TacticsScreen            │
        ├── CommandPalette (Dialog)  │
        └── AddMistakeDialog (Dialog)┘

domain/
├── ReviewSchedule.kt   pure function: (mastery, outcome, today) → ReviewPlan
└── model/Models.kt     immutable data classes and enums

data/
└── SampleContent.kt    bundled fixture shaped like a future repository
```

There is no ViewModel, DI framework, or database yet. State is hoisted to
`TuyoApp` and passed down as values and lambdas. This is intentional for a small
slice: it keeps every transition visible in one file and makes the persistence
boundary obvious. When the state grows, the same boundaries can move into a
`StudyRepository` + `ViewModel` without rewriting the screens.

### Why the state is hoisted

`MistakeDetailScreen` keeps a local `mastery` and `dueEpoch` for immediate UI
feedback, but also calls `onReview(plan)`. `TuyoApp` applies the plan to
`StudyData`, so the Today queue and Mistakes list update when the user returns.
The local state exists only to avoid waiting for a recomposition round-trip; the
source of truth remains the aggregate.

## 3. Navigation

`Destination` is a four-value enum (Today, Mistakes, Topics, Tactics). Detail is
not a destination; it is represented by `detailId: String?`. The animated content
key is either the destination name or `detail:<id>`.

- Bottom navigation clears `detailId`.
- System back clears `detailId` when a detail is open.
- The command palette opens dialogs/screens by calling the same navigation
  functions as the UI, so there is no second navigation path.
- `tacticFocusId` lets Today jump to Tactics with the related card expanded;
  selecting a tab manually clears it.

The command palette uses `Dialog` with `decorFitsSystemWindows = false`, its own
insets, and `onPreviewKeyEvent` on the app root. `Ctrl/Cmd + K` is handled at the
root so it works after a text field has taken focus.

## 4. Review schedule

`ReviewSchedule` is a pure object with no Android dependencies:

```kotlin
intervals = [1, 3, 7, 14, 30, 60]   // days, indexed by mastery
MAX_MASTERY = 5

AGAIN → mastery 0, due tomorrow
HARD  → mastery unchanged, due intervals[mastery]
GOOD  → mastery +1 (capped), due intervals[new mastery]
EASY  → mastery +2 (capped), due intervals[new mastery]
```

Edge cases covered by tests:

- Again resets even from high mastery.
- Good at mastery 1 schedules the 7-day interval (index 2).
- Easy from 0 advances two steps.
- Hard schedules a future review without advancing.
- Mastery is capped at 5.
- Intervals are strictly increasing.
- Out-of-range mastery is clamped rather than crashing.

The design is deliberately not FSRS. Predictability is a product feature; the
function can later delegate to a richer scheduler while preserving `ReviewPlan`.

## 5. Data model

All models are immutable data classes. Key relationships:

- `Mistake.topicId` points at `Topic.id`; `topicTitle` is denormalized for display
  so a row never needs a lookup.
- `StudyData` holds the learner, streak, nets, mistakes, topics, tactics, and the
  featured tactic.
- `Topic.mastery` and `Mistake.mastery` are both 0..5, but they mean different
  things: topic confidence vs. re-solve progress.
- `Mistake.topicStepDone` (mastery ≥ 1) and `questionStepDone` (mastery ≥ 3)
  derive the Y:K:S timeline instead of storing step flags.
- `dueMistakes` is a computed convenience on `StudyData`; screens that need the
  injected `today` filter explicitly.

Dates are `java.time.LocalDate`. No timezone conversion is applied; the app uses
the device’s local date, which matches a student’s “today”.

## 6. Content and persistence

v0.1 reads `SampleContent.studyData(today)`, a Kotlin fixture with six subjects,
topics, confusions, mistakes, tactics, and net history. The shape mirrors the
planned JSON repository, so moving to assets + parsing is mechanical.

Current persistence boundary:

- Review plans update the in-memory `StudyData`.
- Added mistakes prepend to the in-memory list.
- Process death resets to the fixture; there is no database, DataStore, or
  `SavedStateHandle` for the notebook.

This is the largest known limitation and the first v0.2 task. The recommended
order is:

1. Room entities for mistakes and topic mastery; a DAO with a single observable
   query.
2. A repository that seeds from `SampleContent` only on first run.
3. Move `StudyData` assembly into a ViewModel.
4. Keep `ReviewSchedule` pure and test it independently of Room.
5. Add a migration test before the first release with real user data.

Content JSON should be versioned (`schemaVersion`) and validated at load. Bundled
content is read-only; user-added mistakes never mutate the asset.

## 7. Strings and Turkish text

- UI strings live in `res/values/strings.xml` (UTF-8). Nav labels, command palette
  text, due-date formats, and shared actions use `stringResource`.
- Longer in-screen copy is currently inline Kotlin; it is still valid UTF-8 and
  is the next localization candidate if the app ever ships in another language.
- Search normalization uses `lowercase(Locale.forLanguageTag("tr"))`. Do not use
  the default locale for Turkish `I/ı` handling.
- The project had a double-encoding incident during development. If a file shows
  `Ã¼` or `ÅŸ`, it was decoded as Windows-1254 and re-encoded as UTF-8. Repair it
  by re-encoding the mojibake with Windows-1254 and decoding as UTF-8, then verify
  with a UTF-8 reader. Never round-trip source files through a console that is
  not UTF-8.

## 8. Theme implementation

`TuyoColors` is a semantic token set, exposed through
`LocalTuyoColors` (a `staticCompositionLocalOf`). Screens read tokens through
this local, never raw `Color(0x...)` values. `TuyoTheme` maps the tokens onto a
Material 3 `ColorScheme` so Material components (dialogs, text fields, ripples)
stay consistent.

Typography is a single Inter variable font declared at weights 400/500/600/700.
Spacing and radii are plain objects (`Space`, `Radius`); motion is an object with
durations and cubic-bezier easings. There is no design-token code generation
step.

## 9. Quality gates

Before a release candidate:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Expected state:

- Build succeeds.
- All unit tests pass.
- Lint has no errors. Warnings about available dependency versions are accepted;
  `ObsoleteSdkInt` for `mipmap-anydpi-v26` is deliberately suppressed in
  `app/lint.xml` because AAPT does not resolve the adaptive icon from an
  unqualified `mipmap-anydpi` folder in this toolchain.

Manual device checklist (Android 16, 1080×2340, 480 dpi was used):

- [ ] Today renders with status-bar and navigation-bar insets.
- [ ] Next move follows the due queue, not list order.
- [ ] Review updates mastery, next due date, confirmation copy, and Today.
- [ ] Add dialog remains usable with the keyboard open; footer is reachable.
- [ ] Command palette filters Turkish text and closes on Esc/backdrop.
- [ ] Topics and Tactics expand/collapse and keep their scroll position.
- [ ] Launcher icon (normal, round, themed) renders without legacy mipmaps.
- [ ] Light and dark system themes both render with readable contrast.

Visual verification on the physical test device was done through `adb`; the local
emulator lacks hardware acceleration. Screenshots are not committed.

## 10. Testing

Current tests are JUnit 4:

- `ReviewScheduleTest.kt` covers the scheduler’s non-obvious branching.
- `StudyDataTest.kt` covers applying a review to an immutable aggregate, the
  unknown-id no-op, the Y:K:S step derivation, and due-date boundaries.

Highest-value next tests:

- Serialization/migration round-trip once persistence exists.
- A fake repository test for “add mistake → appears in due queue”.
- Compose UI tests for the capture dialog under an IME (requires an emulator or
  Robolectric with a proper Compose test setup).
- Snapshot tests for dark/light tokens if the design system grows.

## 11. Release configuration

- Debug builds get `.debug` as an application-id suffix and `-debug` as a version
  suffix, so they can coexist with release builds.
- Release enables R8 and resource shrinking. A signing config is not checked in.
- `android:enableOnBackInvokedCallback="true"` is set for predictive back, with
  `tools:targetApi="33"` to keep lint quiet on the API 26 minimum.
- The app is edge-to-edge (`enableEdgeToEdge`) and handles insets in Compose.

## 12. Known engineering debt

1. **No persistence** across process death (see §6).
2. **No instrumentation/UI tests** in CI.
3. **Touch targets** for compact chips are below 48 dp; visual density was
   prioritised, and this must be corrected.
4. **No max-width/tablet layout**; lists stretch on large screens.
5. **Inline Turkish copy** outside strings.xml; fine for one language, not for
   localization.
6. **Dependency versions** are older than the latest available; upgrade in a
   dedicated change with a full build/lint/device pass.
7. **No baseline profile** or startup measurement. The app is small today; add
   Macrobenchmark before claiming performance.

## 13. Decision log

| Decision | Reason |
| --- | --- |
| One module, manual state | Keeps the first slice legible; no premature DI. |
| Immutable models | Makes review updates explicit and persistence-friendly. |
| Pure scheduler | Testable without Android; easy to replace with FSRS. |
| No network permission | Enforces the offline promise and removes a privacy surface. |
| Fixture instead of Room | Ships the interaction first; persistence has a clean seam. |
| Semantic colour tokens | Dark/light parity and no stray hex in screens. |
| `mipmap-anydpi-v26` | AAPT fails to resolve the adaptive icon in unqualified `mipmap-anydpi` here. |
| Debug suffix | Allows a release build and a debug build on the same device. |
