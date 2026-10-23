# Tuyo — Engineering Notes

## 1. Stack

| Area | Choice |
| --- | --- |
| Language | Kotlin 2.3.21 |
| UI | Jetpack Compose, Material 3 primitives where useful |
| Build | Gradle 8.13, AGP 8.10.1 |
| SDK | `compileSdk 36`, `targetSdk 36`, `minSdk 26` |
| Java | 17 |
| Fonts | Inter Variable (bundled) |
| Tests | JUnit 4 for pure domain, persistence, and content checks |
| Persistence | DataStore Preferences + kotlinx.serialization JSON |
| Scheduling | WorkManager, one-shot worker that reschedules itself |
| Network | None; no `INTERNET` permission |

Versions are pinned in `gradle/libs.versions.toml`. Lint may report newer
available versions; upgrades are deliberately separate from feature work.

## 2. Architecture

```text
MainActivity
└── TuyoTheme (dark/light semantic tokens)
    └── TuyoApp                         ← navigation, dialogs, composition
        ├── TuyoViewModel              ← persisted learner actions
        │   └── UserRepository
        │       └── DataStore<Preferences>  ("tuyo_user_v1"/"state_json")
        ├── TodayScreen
        ├── MistakesScreen → MistakeDetailScreen
        ├── TopicsScreen
        ├── GuideScreen (Taktikler / Sınav / Motivasyon)
        ├── SettingsScreen
        ├── OnboardingScreen
        ├── CommandPalette (Dialog)
        ├── AddMistakeDialog (Dialog)
        └── AddNetDialog (Dialog)

domain/
├── ReviewSchedule.kt   pure function: (mastery, outcome, today) → ReviewPlan
├── UserProgress.kt     streak, current streak, topic statistics
└── model/Models.kt     serializable user state + read-only content models

data/
├── StudyContent.kt         curated static content + StudyData projection
├── UserRepository.kt       DataStore JSON read/update/clear
├── ReminderScheduler.kt    WorkManager enqueue/cancel + notification
├── ReminderWorker.kt       state-aware reminder + next-day hand-off
└── TacticSubmission.kt     share sheet + external source link
```

`TuyoApp` owns ephemeral UI navigation. All learner state is owned by
`TuyoViewModel` and persisted through `UserRepository`; there is no fake
in-memory fallback. `StudyContent.studyData(user)` projects the persisted
document plus curated content into the read-only shape the screens consume.

### Why there is no database yet

The dataset is small, single-user, and written one document at a time. DataStore
serialises writes, so two quick reviews cannot clobber each other. One JSON
document makes migrations visible and keeps the persistence boundary honest. If
review history or search grows, Room can replace the repository behind the same
`Flow<UserData>` and `update {}` API.

### Why the state is not hoisted into TuyoApp

`MistakeDetailScreen` keeps local `mastery` and `dueEpoch` for immediate feedback,
but the review callback writes through the ViewModel. The local values are a
display cache; the persisted document remains the source of truth. The next
DataStore emission reconciles the screen. Without this split, every review would
wait for disk before the button felt responsive.

## 3. Persistence

`UserRepository` stores a `UserData` document:

```kotlin
UserData(
    schemaVersion = 1,
    profile = UserProfile(...),
    mistakes = listOf(Mistake(...)),
    nets = listOf(ExamNet(...)),
    streakDays = 0,
    lastActivityEpochDay = null,
    reviewsCompleted = 0,
)
```

`UserData` and every nested type are `@Serializable`. `LocalDate` uses a custom
ISO serializer (`yyyy-MM-dd`) so the stored JSON is readable and timezone-free.
Decode policy:

- missing key or blank value → empty `UserData()` (first run),
- malformed JSON → empty `UserData()`; the app does not crash or restore a
  hidden fixture,
- unknown fields → ignored (`ignoreUnknownKeys = true`),
- defaults → encoded (`encodeDefaults = true`) to keep the document stable.

Writes are atomic at the DataStore level. `update {}` reads the current document,
applies a pure transform, and writes the result. No screen writes directly.

`android:allowBackup="false"` is deliberate: the UI promises local-only data, so
the notebook must not be copied to Android cloud backup.

### Migration path

`schemaVersion` is present but no migration is needed yet. Before the first
release with real user data:

1. Add a decoded-document migration function keyed by `schemaVersion`.
2. Keep the old document until the new one is written successfully.
3. Add a unit test with a serialized fixture of the previous version.
4. Only then add fields that change meaning.

## 4. Navigation

`Destination` is a four-value enum: **Today**, **Mistakes**, **Topics**, **Guide**.
Detail is not a destination; it is `detailId: String?`. Settings is a boolean
overlay. The animated content key is settings, `detail:<id>`, or the destination
name.

- Bottom navigation clears detail, settings, guide focus, and section focus.
- System back clears Settings first, then detail.
- The command palette calls the same navigation functions as the UI, so there is
  no second navigation path.
- `tacticFocusId` lets Today jump to Tüyolar with the related tactic expanded;
  selecting a tab manually clears it.
- `guideSection` lets Today open Motivasyon directly; opening a destination
  clears it.
- Rotation/process recreation is covered by `rememberSaveable` for tab, detail,
  settings, section, and scroll-adjacent state.

The root handles `Ctrl/Cmd + K` with `onPreviewKeyEvent`, so the shortcut works
after a text field has focus.

## 5. Review schedule

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

## 6. Data model and derived state

All models are immutable data classes. Key relationships:

- `Mistake.topicId` points at `Topic.id`; `topicTitle` is denormalized for display
  so a row never needs a lookup.
- `Mistake.topicStepDone` (mastery ≥ 1) and `questionStepDone` (mastery ≥ 3)
  derive the three-step timeline instead of storing step flags.
- `UserData.dueMistakes` filters with `LocalDate.now()`.
- `StudyData.dueMistakes` applies the same rule to the projected content.
- `StudyData.latestNet` is the last net; `previousNet` and `netDelta` compare only
  the same exam type. `netTrend` also filters to that exam type, so a TYT line is
  never plotted against AYT.
- `List<Mistake>.topicStats(topics, today)` computes mistake count, due count,
  average mastery, and last log date per topic. A topic is weak only when it has
  user data and either a due review or average mastery ≤ 2.
- `UserData.currentStreak(today)` shows zero after a missed day instead of a
  stale badge. `withActivity(today)` updates the stored value on real activity.
- `reviewsCompleted` counts review outcomes, not app opens.

Dates are `java.time.LocalDate`. No timezone conversion is applied; the app uses
the device’s local date, which matches a student’s “today”.

### AYT net maximum

`ExamType.AYT.maxNet` is `80.0`, not `160.0`. A student’s AYT score is based on
two relevant tests (for example, mathematics + science for sayısal), so the
tracking unit is the relevant-test total. The add-net dialog states this limit
explicitly. If the product later adds a per-test breakdown, the model should
store test-level nets rather than raising the total and making mixed values look
comparable.

## 7. Content

`StudyContent` is read-only curated content:

- 14 topics across Türkçe, matematik, geometri, fizik, kimya, biyoloji, tarih,
  coğrafya, and edebiyat.
- 18 tactics with evidence level, time cost, steps, category, related topics, and
  source notes.
- 34 attributed quotes and 11 official TED talk links across six motivation
  themes, plus 20 original notes. Disputed quote attributions are labelled
  “atfedilen söz”; TED cards store a short editorial summary, not a transcript.
- TYT/AYT/YDT overviews, 2026 ÖSYM averages, four general commentary cards, and
  seven exam sources plus four motivation sources.
- `studyData(user)` projects user data into the UI shape and chooses a featured
  tactic from the weakest topic. It never adds a learner name, net, mistake,
  streak, or mastery of its own.

The tactics are evidence-labelled honestly. Research labels are reserved for
methods with direct experimental support; exam-specific tactics are usually
experience-based; contested methods are labelled as debated. Content test
coverage checks IDs, tactic-topic references, counts, source URLs, and the
absence of hidden learner state.

Sources:

- ÖSYM 2026 numerical information:
  `https://cdn.osym.gov.tr/pdfdokuman/2026/YKS/SB/sayisal_ykdd21072026.pdf`
- MEB 2026 YKS guide and topic distribution:
  `https://ttkb.meb.gov.tr/meb_iys_dosyalar/2025_11/26164023_2026_yks.pdf`
- 2025 AYT exam commentary:
  `https://www.hurriyet.com.tr/bilgi/galeri/ayt-sinav-yorumlari-2025-yks-ayt-kolay-miydi-zor-muydu-42848769`
- MEB study methods:
  `https://yenimahalle.meb.gov.tr/meb_iys_dosyalar/2025_12/05121922_verimliderscalismayontemleri.pdf`
- Dunlosky et al. (2013), Roediger & Karpicke (2006), Diekelmann & Born (2010)
  via DOI links in the app.

## 8. Reminders

`ReminderScheduler.schedule(context, hour, minute)`:

1. Computes the next local occurrence; if the time has passed today, it uses
   tomorrow.
2. Enqueues a unique one-shot `ReminderWorker` with an initial delay.
3. Uses `ExistingWorkPolicy.REPLACE`, so changing the time replaces the chain.

When the worker runs:

1. Reads the local `UserData` document.
2. Stops without notification if onboarding is incomplete or the reminder is
   disabled.
3. Chooses copy from real state: empty notebook, due items, idle days, or clear
   queue.
4. Shows a local notification only if `POST_NOTIFICATIONS` is granted.
5. Reschedules tomorrow’s worker, preserving the chain across process death and
   reboot.

No exact-alarm permission is requested. A reminder may be a few minutes late; the
product accepts that trade-off. The notification channel is created at app start
and again defensively before showing a notification.

Permission handling:

- Onboarding asks only when the student enables the reminder.
- Settings refreshes permission state on resume and exposes a direct link to the
  app’s notification settings when it is missing.
- A denied permission never causes a crash; the worker simply skips the
  notification and keeps the schedule alive.

## 9. Strings and Turkish text

- UI strings live in `res/values/strings.xml` (UTF-8). Navigation, command
  palette, dialog chrome, notifications, and shared actions use `stringResource`.
- Longer screen copy is currently inline Kotlin; it is valid UTF-8 and is the
  next localisation candidate if the app ships in another language.
- Search normalisation uses `lowercase(Locale.forLanguageTag("tr"))`. Do not use
  the default locale for Turkish `I/ı` handling.
- The project had a double-encoding incident during development. If a file shows
  `Ã¼` or `ÅŸ`, it was decoded as Windows-1254 and re-encoded as UTF-8. Repair it
  by re-encoding the mojibake with Windows-1254 and decoding as UTF-8, then verify
  with a UTF-8 reader. Never round-trip source files through a console that is
  not UTF-8.
- `strings.xml` contains `%1$d`/`%1$s` formats with `translatable` defaults. If
  the app is localised, convert count-bearing lines to plurals rather than
  concatenating suffixes.

## 10. Theme implementation

`TuyoColors` is a semantic token set exposed through `LocalTuyoColors`
(`staticCompositionLocalOf`). Screens read tokens through this local, never raw
`Color(0x...)` values. `TuyoTheme` maps the tokens onto a Material 3 `ColorScheme`
so Material components (dialogs, text fields, ripples) stay consistent.

Typography is a single Inter variable font declared at weights 400/500/600/700.
Spacing and radii are plain objects (`Space`, `Radius`); motion is an object with
durations and cubic-bezier easings. There is no design-token code generation.

Subject tints are centralised in `Primitives.subjectTint`; adding a subject
requires a tint entry, otherwise the exhaustive `when` fails at compile time.

## 11. Tests

Current JUnit 4 suites:

- `ReviewScheduleTest` — scheduling branches, caps, invalid mastery.
- `UserDataTest` — JSON round-trip, ISO date encoding, due ordering, empty-document
  invariants.
- `UserProgressTest` — streak transitions, stale-streak zero, topic stats derived
  only from user mistakes.
- `StudyContentTest` — unique IDs, tactic-topic integrity, quote/TED completeness,
  official TED URLs, valid source URLs, no hidden learner state, like-for-like
  net trends.

Run:

```bash
./gradlew :app:testDebugUnitTest
```

Last local run: 27 tests, 0 failures.

Highest-value next tests:

- Repository/DataStore integration with a temporary file and corrupt payload.
- ReminderWorker tests with a fake repository/clock.
- Compose UI tests for onboarding permission denial and capture under an IME.
- An on-device signed release/R8 smoke test.

## 12. Quality gates

Before a release candidate:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Expected state:

- Build succeeds.
- All unit tests pass.
- Lint has no errors. Warnings about available dependency versions, plural
  candidates, `UseKtx`, and obsolete SDK checks are accepted until a dedicated
  cleanup.
- `mipmap-anydpi-v26` remains; an unqualified `mipmap-anydpi` fails AAPT in this
  toolchain.

Manual device checklist (physical device, Android 13+ recommended):

- [ ] First run collects name/track and cannot skip into fake data.
- [ ] Denying notification permission still finishes onboarding.
- [ ] Killing and reopening the app preserves profile, mistakes, nets, and reviews.
- [ ] Reboot preserves the reminder; changing time replaces the old job.
- [ ] Empty notebook reminder asks for a first mistake.
- [ ] Due reminder count matches the notebook.
- [ ] Review updates mastery, due date, Today, Defter, and Konular.
- [ ] Add dialog remains usable with the keyboard open; footer is reachable.
- [ ] Command palette filters Turkish text and closes on Esc/backdrop.
- [ ] Topics and tactics expand/collapse and keep scroll position.
- [ ] Settings permission warning recovers after granting in system settings.
- [ ] Reset returns to onboarding and clears the notebook.
- [ ] Launcher icon (normal, round, themed) renders.
- [ ] Light and dark system themes both render with readable contrast.

Visual verification on the previous physical test device was blocked by a locked
screen and low battery; the local emulator lacks hypervisor acceleration. Run the
checklist before calling the release device-verified.

## 13. Release configuration

- Debug builds get `.debug` as an application-id suffix and `-debug` as a version
  suffix, so they can coexist with release builds.
- Release enables R8 and resource shrinking. Signing reads a gitignored
  `keystore.properties` at the repository root (`storeFile`, `storePassword`,
  `keyAlias`, `keyPassword`). Without it the release APK is unsigned, so the
  shrink pipeline can still be validated by contributors.
- `android:enableOnBackInvokedCallback="true"` is set for predictive back, with
  `tools:targetApi="33"` to keep lint quiet on the API 26 minimum.
- The app is edge-to-edge (`enableEdgeToEdge`) and handles insets in Compose.
- `allowBackup="false"` aligns Android backup behaviour with the privacy copy.
- No `INTERNET` permission is declared. WorkManager contributes normal
  permissions (wake lock, boot receiver, foreground service, and network-state
  read access); the app never uses a network constraint, and none of these grants
  network access.

## 14. Known engineering debt

1. **No repository integration test** against a real temporary DataStore file.
2. **No instrumentation/UI tests** in CI.
3. **Touch targets** for compact chips are below 48 dp; visual density was
   prioritised, and this must be corrected.
4. **No max-width/tablet layout**; lists stretch on large screens.
5. **Inline Turkish copy** outside `strings.xml`; fine for one language, not for
   localisation.
6. **Dependency versions** are older than the latest available; upgrade in a
   dedicated change with a full build/lint/device pass.
7. **No baseline profile** or startup measurement. The app is small today; add
   Macrobenchmark before claiming performance.
8. **Content is Kotlin**, so the moderation pipeline still requires a code change
   per approved tactic. A versioned JSON asset is the next step.
9. **No schema migration yet**; `schemaVersion` is a hook, not a guarantee.
10. **Reminder clock is wall-clock based**; a major timezone or manual clock
    change can shift the next notification until the chain reschedules.
11. **R8/Kotlin metadata warning.** The pinned AGP 8.10.1 ships an R8 that warns
    about Kotlin 2.3 metadata during minification. The release APK builds, and
    serializers are referenced statically, but AGP/R8 should be upgraded in a
    dedicated change and the signed build smoke-tested before shipping.

## 15. Decision log

| Decision | Reason |
| --- | --- |
| One module, manual navigation state | Keeps the app legible; no premature DI or nav library. |
| Immutable models | Makes review updates explicit and persistence-friendly. |
| Pure scheduler | Testable without Android; easy to replace with FSRS. |
| DataStore JSON | Small, durable, atomic, and easy to inspect; no server. |
| WorkManager one-shot chain | Local reminders without exact-alarm permission or a backend. |
| No network permission | Enforces the offline promise and removes a privacy surface. |
| `allowBackup=false` | The UI promises local-only data; backup would contradict it. |
| AYT maxNet 80 | Tracks the relevant two-test total instead of mixed 160-question data. |
| Same-exam net trend | A TYT delta must not be compared with an AYT result. |
| Evidence labels | Community tactics must show how strong their support is. |
| Semantic colour tokens | Dark/light parity and no stray hex in screens. |
| `mipmap-anydpi-v26` | AAPT fails to resolve the adaptive icon in unqualified `mipmap-anydpi` here. |
| Debug suffix | Allows a release build and a debug build on the same device. |
