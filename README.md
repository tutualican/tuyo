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

`v0.5.1` is a working, offline Android app. The v0.3 review notebook is now a
full study planner, still free and ad-free:

- **First run** — the student enters a name and target track. No sample learner,
  sample mistakes, sample nets, or invented mastery values are shipped.
- **Bugün** — greeting, YKS countdown, daily question/time summary, today's plan,
  latest exam net with a like-for-like trend, weak-topic nudge, weekly summary,
  due mistakes, pending analyses, a daily attributed quote, and a featured tactic.
- **Defter** — persisted mistakes with confidence flags, optional local photos,
  subject/confidence filters, mastery and next due date. A mistake opens into its
  detail.
- **Çalışma** — a calendar-backed question log (D/Y/B/net in under 20 seconds),
  daily and weekly plan tasks with targets, carry-over and rest days, a
  Pomodoro/free/exam foreground timer with per-subject totals, monthly net goals,
  TYT 165 / AYT 180 minute simulations, and section-level exam entry.
- **Konular** — topic statuses (`başlanmadı`, `çalışılıyor`, `bitti`, `tekrar
  gerekli`) plus a 1/3/7/21/45/90-day review ladder. Weak topics are derived from
  the student's own notebook or an explicit status, never invented.
- **Deneme analizi** — subject loss/time breakdown, a transparent 100–500 score
  estimate (explicitly not an ÖSYM score), a one-day analysis reminder, and
  rule-based plan suggestions that name the data behind them.
- **Tüyolar** — 18 evidence-labelled tactics, TYT/AYT/YDT structure, researched
  2026 averages and commentary, sources, and a motivation library with 34
  attributed quotes, 11 official TED talk links, and 20 original notes.
- **Ayarlar** — profile, exam date, daily/weekly goals, reminder time,
  notification permission, local backup export/import, and a confirmed reset.
- **Reminders** — opt-in local WorkManager notifications. Rest days stay quiet;
  reminders can surface due reviews or pending plan tasks without shaming copy.

The motivation library separately labels disputed attributions as
“atfedilen söz”, keeps source/context on every quote card, and opens TED talks
in the browser. Quotes and short summaries are bundled; TED transcripts are not.

Persistence uses DataStore Preferences with one versioned JSON document. Review
outcomes, mistakes, nets, questions, topics, plans, sessions, goals, streak,
profile, and reminder settings survive process death and device restart. A dated
local backup is kept after each update (seven days retained) and can be exported
or restored through the system document picker.

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
│   ├── BackupManager.kt            Local automatic + SAF backups
│   ├── StudyTimer.kt               Process-wide timer state
│   ├── StudyTimerService.kt        Foreground study/exam timer
│   ├── ExamAnalysisScheduler.kt    One-day exam analysis reminder
│   ├── ReminderScheduler.kt        WorkManager scheduling + notification
│   └── ReminderWorker.kt           Content chosen from real due/idle state
├── domain/
│   ├── ExamCatalog.kt              Track-aware official section templates
│   ├── StudyMetrics.kt             Plans, summaries, net goals, suggestions
│   ├── ReviewSchedule.kt           Pure spaced-repetition ladder
│   ├── UserProgress.kt             Derived streak and topic statistics
│   └── model/                      Serializable user/content models
└── ui/
    ├── TuyoApp.kt                  Shell, navigation, dialogs, commands
    ├── TuyoViewModel.kt            Persisted learner actions
    ├── components/                 Primitives, rows, inputs, palette
    ├── screens/                    Onboarding, Today, Notebook, Detail,
    │                               Work, Topics, Guide, Settings, dialogs
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
- Automatic backups are local app-private files; manual export/import uses the
  Android document picker. No account or network is involved.
- Source links and tactic submissions open the device browser or share sheet;
  the app itself never requests network access.
- Reset removes the profile, nets, mistakes, questions, plans, sessions, goals,
  streak, and review history, then returns to onboarding.

---

## Reminders

The app schedules one durable WorkManager job at the chosen local time. The
worker reschedules itself for the next day, so the chain survives process death
and reboot. It does not request exact-alarm permission; a reminder may be a few
minutes late by design.

The notification copy is derived from state:

| State | Message |
| --- | --- |
| Notebook empty, no plan | Ask for the first mistake. |
| Reviews due | Show how many mistakes are waiting. |
| Pending daily plan | Mention the plan task count. |
| Nothing due, 2+ idle days | Invite the student back without shaming. |
| Nothing due | Confirm the queue is clear. |
| Rest day | Stay quiet by design. |

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
- topic weakness derived only from logged mistakes or an explicit status,
- weekly summaries, daily/weekly plan separation, carry-over candidates, and
  latest-month net goals,
- section net, estimated-score bounds, and official section templates,
- content integrity, quote/TED IDs, official TED URLs, source URLs, and the
  absence of hidden learner state,
- like-for-like net trends.

The last local run completed 36 unit tests with no failures. Lint has no errors;
remaining warnings are dependency, pluralisation, and KTX style suggestions.
`assembleDebug` and `assembleRelease` (R8 + resource shrinking + APK signing)
both succeed with a local release keystore. The keystore and
`keystore.properties` are deliberately gitignored; without them the release
build produces an unsigned APK for shrink validation.

---

## Design in one paragraph

Warm-paper first, in both light and dark: soft off-white or warm charcoal, not
pure white or black. Inter Variable, tight display tracking, 4 pt spacing, 18 dp
cards, 1 px hairlines, and motion between 110 and 240 ms. Accent blue is reserved
for action and selection; pale peach, sky, and leaf washes add warmth without
becoming extra accents. No drop shadows, no confetti, no streak pressure, no
shaming copy — and when the day is done, the app says so and invites rest. See
[docs/DESIGN.md](docs/DESIGN.md).

---

## Roadmap

1. **Device verification** — onboarding, permission denial, reboot persistence,
   keyboard layout, and dark/light rendering on a physical phone.
2. **Content workflow** — move curated tactics to versioned JSON with a schema
   test, so approved issues can be compiled without touching Kotlin.
3. **Accessibility** — 48 dp touch targets, selected-state semantics, screen
   reader labels, and 200% font scaling.
4. **Capture speed** — offline photo/OCR assist, still optional and local.
5. **Review depth** — optional FSRS experiments behind the same four-outcome
   interaction.

---

## Fonts

Inter is bundled under the SIL Open Font License. The license text is at
[`licenses/Inter-OFL.txt`](licenses/Inter-OFL.txt).
