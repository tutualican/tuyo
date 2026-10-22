# Tuyo

**Tuyo** is an offline-first study notebook for Turkish YKS students. It turns a
wrong answer into a repeatable loop:

> **Y**anlış → **K**onu → **S**oru

A mistake is not a number to feel bad about. It is a signal: the student missed a
piece of knowledge, misread the question, rushed the arithmetic, or ran out of
time. Tuyo records that signal, attaches it to a topic, and schedules the next
re-solve so the same mistake does not survive the week.

The interface is built around one calm surface rather than a dashboard of
badges. It is intentionally closer to a working tool than to a game.

---

## Status

`v0.1` is a runnable, offline prototype. The core loop works end to end on a
phone:

- **Today** — greeting, last exam net with a sparkline, the Y:K:S loop, the next
  due mistake, pending analyses, and a featured tactic.
- **Mistakes** — the notebook, filterable by subject, with mastery and next due
  date on every row.
- **Mistake detail** — the Y:K:S timeline, critical idea, related tactic, and a
  four-outcome review step (Again / Hard / Good / Easy).
- **Topics** — a searchable knowledge base grouped by subject, with expandable
  “frequently confused” pairs.
- **Tactics** — evidence-labelled study methods with step-by-step instructions.
- **Capture** — a three-field dialog (topic, error kind, optional note) designed
  to take less time than re-reading the question.
- **Command palette** — `Ctrl/Cmd + K` (or the search button) for navigation and
  actions, with Turkish-aware filtering.

Reviews update mastery and the next due date immediately, and Today reflects the
change when you return. The notebook lives in memory for this slice: closing the
process resets it to the bundled fixture. Persistence is the next engineering
step, not an accident.

No network permission is declared. No account, analytics, or server is involved.

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

Install the debug build:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The debug application id is `io.github.tutualican.tuyo.debug`; the release id is
`io.github.tutualican.tuyo`.

`local.properties` is machine-specific and ignored by git. Point `sdk.dir` at
your Android SDK.

---

## Project map

```text
app/src/main/java/io/github/tutualican/tuyo/
├── MainActivity.kt              Edge-to-edge host
├── data/SampleContent.kt        Bundled YKS fixture
├── domain/
│   ├── ReviewSchedule.kt        Six-step spaced-repetition ladder
│   └── model/Models.kt          Immutable product model
└── ui/
    ├── TuyoApp.kt               Shell, navigation, state, commands
    ├── components/              Primitives, rows, palette, sparkline
    ├── screens/                 Today, Mistakes, Detail, Topics, Tactics
    └── theme/                   Semantic colours, type, spacing, motion
```

More detail:

- [Product brief](docs/PRODUCT.md)
- [Design system](docs/DESIGN.md)
- [Engineering notes](docs/ENGINEERING.md)

---

## Design in one paragraph

Dark-first, with a light palette that is designed rather than inverted. Inter
Variable, tight display tracking, 4 pt spacing, 14 dp cards, 1 px hairlines, and
motion between 110 and 240 ms. Accent blue is reserved for action and selection;
amber is used sparingly for the brand mark and attention. No drop shadows, no
confetti, no shaming copy. See [docs/DESIGN.md](docs/DESIGN.md).

---

## Y:K:S

| Step | Meaning | What the app does |
| --- | --- | --- |
| **Y** | Yanlış | Capture the wrong answer with its cause. |
| **K** | Konu | Map it to a topic and the one idea that was missing. |
| **S** | Soru | Re-solve it on a spaced ladder: 1, 3, 7, 14, 30, 60 days. |

The letters are also the product name’s rhythm: **Tuyo** is short, soft, and easy
to say in a classroom. The branding and the exact expansion of Y:K:S are still
worth confirming with the first cohort; see the open questions in the product
brief.

---

## Roadmap

1. **Persistence** — Room for mistakes, topics mastery, and review history.
2. **Content pipeline** — bundled JSON with a versioned schema and migrations.
3. **Real capture** — photo/OCR-assisted entry, still offline-first.
4. **Community tactics** — GitHub-based submission and moderation, compiled into
   releases; no paid backend.
5. **Review notifications** — local, opt-in, tied to the due queue.
6. **Accessibility pass** — 48 dp touch targets for compact chips, screen-reader
   labels, and large-font layouts.

---

## Fonts

Inter is bundled under the SIL Open Font License. The license text is at
[`licenses/Inter-OFL.txt`](licenses/Inter-OFL.txt).
