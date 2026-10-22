# Tuyo — Product Brief

## 1. The problem

YKS preparation produces a lot of data and very little diagnosis. Students count
nets, collect deneme results, and highlight questions, but the same mistakes
return because the cause is never separated from the score. A wrong answer can
mean:

- a missing fact,
- a misread question root,
- a rushed operation,
- a timing decision,
- or a topic that was never actually understood.

Those are different problems. They need different next actions. A notebook that
only says “35 wrong” cannot tell a student what to do on Tuesday evening.

## 2. The promise

Tuyo is a **mistake notebook that closes the loop**. Every entry is cheap to
capture, points at a topic, and comes back as a question to re-solve at the right
interval. The student always knows the next move.

The product is not an exam simulator, a video library, or a social network. It
is the layer between a student’s deneme and their next study session.

## 3. Audience

- Turkish high-school students preparing for YKS (TYT/AYT), roughly 16–19.
- Students who already solve questions but do not review them systematically.
- Students with limited time, unreliable connectivity, and a phone as their
  primary computer.
- Teachers/coaches who want a shared vocabulary for error types (future).

The app speaks Turkish by default. UI copy is short, concrete, and non-moralising.
It never calls a student “başarısız” for missing a review.

## 4. The Y:K:S loop

| Step | Student action | Product response |
| --- | --- | --- |
| **Y — Yanlış** | Log a wrong answer in under 20 seconds. | Creates a notebook entry with date, subject, topic, and error kind. |
| **K — Konu** | Choose the topic and read the one critical idea. | Connects the error to a knowledge card and a related tactic. |
| **S — Soru** | Re-solve the question when it is due. | Advances a six-step mastery ladder and schedules the next review. |

The loop is deliberately small. More fields at capture time reduce the number of
entries; fewer fields make the notebook useless. Three fields is the current
balance: **topic**, **error kind**, and an optional **note**.

### Review outcomes

The student chooses an outcome; the app does not grade them.

| Outcome | Meaning | Scheduling effect |
| --- | --- | --- |
| **Again** | I could not reconstruct the solution. | Mastery resets to 0; review tomorrow. |
| **Hard** | I solved it, but with effort. | Mastery stays; repeat at the current interval. |
| **Good** | I solved it cleanly. | Mastery +1. |
| **Easy** | It was immediate. | Mastery +2. |

The ladder is 1, 3, 7, 14, 30, 60 days. It is intentionally predictable: a
student can trust a rule they can explain. FSRS can replace the internals later
without changing the interaction.

## 5. Product principles

1. **Diagnosis before volume.** A logged cause is worth more than ten unlogged
   wrong answers.
2. **Behavioural language.** Error kinds describe what happened (“İşlem hatası”),
   not what kind of student you are.
3. **One next move.** Today always answers “what should I do now?” instead of
   presenting a backlog.
4. **Calm over gamified.** No streak loss anxiety, no confetti, no leaderboard.
   The streak is context, not a threat.
5. **Offline by default.** The core loop must work on a bus with no connection.
   Accounts and sync are optional later, never prerequisites.
6. **Curated community.** Tactics can come from students, but they enter the app
   through review, with evidence labels, not through an unmoderated feed.
7. **Small, honest scope.** A feature that cannot be maintained without a paid
   backend does not ship.

## 6. Objects

- **Mistake** — a wrong answer, its topic, its cause, its review state.
- **Topic** — a knowledge card: summary, key idea, common confusions, mastery.
- **Tactic** — a study method with steps, an evidence level, and a time cost.
- **ExamNet** — a deneme result used for context, not for grading.
- **StudyData** — the aggregate the UI renders.

The model is immutable. Review actions produce a new `Mistake` rather than
mutating shared state, which keeps the door open for persistence and undo.

## 7. Core flows

### Capture

Mistakes tab → **Yanlış ekle** → choose topic → choose error kind → optional note
→ add. The new entry opens immediately so the student can attach the critical
idea while it is fresh.

### Review

Today → **Sıradaki hamle** (first due mistake) → detail → read the Y:K:S timeline
→ choose an outcome. The due queue shrinks and the next move updates.

### Browse

- **Topics** answers “what do I keep confusing?” with searchable confusion pairs.
- **Tactics** answers “how should I practise?” with evidence labels and steps.

### Navigate

Bottom navigation for the four surfaces; the command palette for keyboard users
and quick actions. `Ctrl/Cmd + K` is the expected shortcut on tablets and
Chromebooks.

## 8. v0.1 scope

Included:

- Bundled Turkish fixture across six subjects.
- Today, notebook, detail, topics, tactics.
- Capture dialog and command palette.
- In-session review scheduling and persistence of the updated state in memory.
- Dark and light palettes, Inter, adaptive launcher icon, monochrome layer.
- Unit tests for the review ladder.

Explicitly excluded:

- Accounts, sync, analytics, push notifications.
- Room/database persistence across process death.
- Photo capture/OCR.
- Community submission UI.
- Tablet-specific layout and accessibility audit.

## 9. Community tactics without a server

The zero-recurring-cost constraint rules out a custom moderation backend. The
intended pipeline:

1. A student submits a tactic through a GitHub issue form (title, steps, subject,
   evidence, time cost, source).
2. A maintainer reviews it for safety, clarity, duplication, and evidence.
3. Approved tactics are merged as JSON in the repository.
4. The JSON is bundled into the next app release.

No runtime network call is required. The app never accepts unreviewed content
from the network. This also makes the review policy legible: every tactic in the
app has a public history.

Evidence labels:

- **Araştırma destekli** — backed by a study or established method.
- **Deneyim** — a credible practitioner/student account, not a controlled study.
- **Tartışmalı** — plausible but contested; presented with a caveat.

## 10. Success signals

For v0.1, success is qualitative and local:

- A student can capture a mistake without breaking study flow.
- The next review is obvious without opening a menu.
- The notebook feels worth returning to after a deneme.
- Nothing in the UI makes a missed day feel like a failure.

If analytics are added later, they must be opt-in and local-first. The core
questions are: capture completion rate, review completion rate, and seven-day
return rate — never time-in-app as a vanity metric.

## 11. Open questions

1. **Y:K:S** — confirm that the intended expansion is **Yanlış → Konu → Soru**
   (not simply the YKS acronym).
2. **Branding** — keep **Tuyo** and the speech-bubble mark, or explore a wordmark
   for the store listing?
3. **Persistence** — Room in v0.2, or a lighter JSON store first?
4. **Scope of “topic”** — should topics be curriculum-level (Türev) or
   micro-skills (zincir kuralında iç fonksiyonun türevi)?
5. **Teacher view** — is a shareable weekly summary a real need, or scope creep?
