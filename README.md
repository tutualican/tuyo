# Tuyo

**Tuyo** is an offline-first study notebook for Turkish YKS students. It turns a
wrong answer into a repeatable loop:

> **Yanlış → Konu → Soru**

A mistake is not a number to feel bad about. It is a signal: the student missed a
piece of knowledge, misread the question, rushed the arithmetic, or ran out of
time. Tuyo records that signal, attaches it to a topic, and schedules the next
re-solve so the same mistake does not survive the week.

YKS is the exam. The three steps above are the notebook's review loop, not an
acronym. The interface is one calm surface rather than a dashboard of badges; it
is closer to a working tool than to a game.

---

## Status

`v0.2.0` is a working, offline Android app. The core loop and the data around it
are real:

- **First run** — the student enters a name and target track. No sample learner,
  sample mistakes, sample nets, or invented mastery values are shipped.
- **Bugün** — greeting, latest exam net with a like-for-like trend, the review
  loop, the next due mistake, pending analyses, a motivation note, and a
  featured tactic.
- **Defter** — the persisted notebook, filterable by subject, with mastery and
  next due date on every row. A mistake opens into its detail.
- **Konular** — weak topics are derived from the student's own mistakes. The
  full reference library stays browsable, but it never claims personal mastery
  without data.
- **Tüyolar** — 18 evidence-labelled tactics, TYT/AYT/YDT structure, researched
  2026 averages and commentary, sources, and 20 motivation notes.
- **Ayarlar** — profile, reminder time, notification permission, and a confirmed
  reset of all local data.
- **Reminders** — opt-in local WorkManager notifications. If the notebook is
  empty, the reminder asks for a first mistake instead of inventing progress.

Persistence uses DataStore Preferences with one versioned JSON document. Review
outcomes, mistakes, nets, streak, profile, and reminder settings survive process
death and device restart.

Build, unit tests, and lint pass on the development machine. A physical-device
verification pass is still recommended for the onboarding, notification, and
keyboard flows.

No `INTERNET` permission is declared. `allowBackup` is disabled, so the notebook
does not leave the device through Android backup. No account, analytics, or
server is involved. WorkManager contributes normal permissions such as
`WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`, and network-state read access for its
constraint API; none of them grants network access. The only runtime permission
is `POST_NOTIFICATIONS`.

---

## Build

Requirements:

- JDK 17
- Android SDK with platform 36
- Android device or emulator on API 26+

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

On Windows:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Install the debug build:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The debug application id is `io.github.tutualican.tuyo.debug`; the release id is
`io.github.tutualican.tuyo`. `local.properties` is machine-specific and ignored
by git.

---

## Project map

```text
app/src/main/java/io/github/tutualican/tuyo/
├── MainActivity.kt                 Edge-to-edge Compose host
├── TuyoApplication.kt              Notification channel setup
├── data/
│   ├── StudyContent.kt             Curated topics, tactics, exam data, sources
│   ├── TacticSubmission.kt         Zero-backend share-sheet submission
│   ├── UserRepository.kt           DataStore + JSON persistence
│   ├── ReminderScheduler.kt        WorkManager scheduling + notification
│   └── ReminderWorker.kt           Content chosen from real due/idle state
├── domain/
│   ├── ReviewSchedule.kt           Pure spaced-repetition ladder
│   ├── UserProgress.kt             Derived streak and topic statistics
│   └── model/                      Serializable user/content models
└── ui/
    ├── TuyoApp.kt                  Shell, navigation, dialogs, commands
    ├── TuyoViewModel.kt            Persisted learner actions
    ├── components/                 Primitives, rows, inputs, palette
    ├── screens/                    Onboarding, Today, Notebook, Detail,
    │                               Topics, Guide, Settings, dialogs
    └── theme/                      Semantic colours, type, spacing, motion
```

More detail:

- [Product brief](docs/PRODUCT.md)
- [Design system](docs/DESIGN.md)
- [Engineering notes](docs/ENGINEERING.md)
- [Community moderation](docs/MODERATION.md)

---

## Data and privacy

- The notebook is a single JSON document in app-private DataStore storage.
- A corrupt or unreadable document falls back to an empty state instead of
  crashing. There is no hidden fixture to restore.
- `android:allowBackup="false"` keeps the notebook out of Android cloud backup.
- Source links open in the device browser; the app itself never requests network
  access.
- Reset removes the profile, nets, mistakes, streak, and review history, then
  returns to onboarding.

---

## Reminders

The app schedules one durable WorkManager job at the chosen local time. The
worker reschedules itself for the next day, so the chain survives process death
and reboot. It does not request exact-alarm permission; a reminder may be a few
minutes late by design.

The notification copy is derived from state:

| State | Message |
| --- | --- |
| Notebook empty | Ask for the first mistake. |
| Reviews due | Show how many mistakes are waiting. |
| Nothing due, 2+ idle days | Invite the student back without shaming. |
| Nothing due | Confirm the queue is clear. |

Notification permission is requested only when the student turns the reminder
on. If it is denied, Settings explains how to open system notification settings.

---

## Community tactics without a backend

Students submit through the GitHub issue form, a maintainer reviews against a
public rubric, and approved tactics are added to the bundled content before the
next release. There is no moderation server and no recurring cost. See
[docs/MODERATION.md](docs/MODERATION.md).

The in-app action opens the system share sheet with a structured template. The
app never uploads the text itself.

---

## Quality gates

Current tests cover:

- the spaced-repetition ladder and its edge cases,
- JSON round-trips for the persisted document,
- current-streak reset behaviour,
- topic weakness derived only from logged mistakes,
- content integrity, source URLs, and the absence of hidden learner state,
- like-for-like net trends.

The last local run completed 26 unit tests with no failures. Lint has no errors;
remaining warnings are dependency, pluralisation, and KTX style suggestions.
`assembleDebug` and `assembleRelease` (R8 + resource shrinking + APK signing)
both succeed with a local release keystore. The keystore and
`keystore.properties` are deliberately gitignored; without them the release
build produces an unsigned APK for shrink validation.

---

## Design in one paragraph

Dark-first, with a light palette that is designed rather than inverted. Inter
Variable, tight display tracking, 4 pt spacing, 14 dp cards, 1 px hairlines, and
motion between 110 and 240 ms. Accent blue is reserved for action and selection;
amber is used sparingly for the brand mark and attention. No drop shadows, no
confetti, no shaming copy. See [docs/DESIGN.md](docs/DESIGN.md).

---

## Roadmap

1. **Device verification** — onboarding, permission denial, reboot persistence,
   keyboard layout, and dark/light rendering on a physical phone.
2. **Content workflow** — move curated tactics to versioned JSON with a schema
   test, so approved issues can be compiled without touching Kotlin.
3. **Accessibility** — 48 dp touch targets, selected-state semantics, screen
   reader labels, and 200% font scaling.
4. **Capture speed** — offline photo/OCR assist, still optional and local.
5. **Weekly summary** — a shareable, local-only review of repeated error kinds.
6. **Scheduler experiments** — optional FSRS internals behind the same
   four-outcome interaction.

---

## Fonts

Inter is bundled under the SIL Open Font License. The license text is at
[`licenses/Inter-OFL.txt`](licenses/Inter-OFL.txt).
