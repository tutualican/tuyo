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

The product is not a video library, a social network, or a replacement for a
teacher. It is the layer between a student’s deneme and their next study
session: capture, plan, focus, and analyse, without an account.

## 3. Audience

- Turkish high-school students preparing for YKS (TYT/AYT/YDT), roughly 16–19.
- Students who already solve questions but do not review them systematically.
- Students with limited time, unreliable connectivity, and a phone as their
  primary computer.
- Teachers/coaches who want a shared vocabulary for error types (future).

The app speaks Turkish by default. UI copy is short, concrete, and non-moralising.
It never calls a student “başarısız” for missing a review.

## 4. The review loop

YKS is the exam. The notebook has three steps, and they are deliberately not
presented as a second acronym.

| Step | Student action | Product response |
| --- | --- | --- |
| **Yanlış** | Log a wrong answer in under 20 seconds. | Creates a notebook entry with date, subject, topic, and error kind. |
| **Konu** | Choose the topic and read the one critical idea. | Connects the error to a knowledge card and a related tactic. |
| **Soru** | Re-solve the question when it is due. | Advances a six-step mastery ladder and schedules the next review. |

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

The ladder is 1, 3, 7, 21, 45, 90 days. It is intentionally predictable: a
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
   The streak is context, not a threat; a missed day is shown as zero rather than
   a stale badge.
5. **Offline by default.** The core loop must work on a bus with no connection.
   Accounts and sync are optional later, never prerequisites.
6. **Curated community.** Tactics can come from students, but they enter the app
   through review, with evidence labels, not through an unmoderated feed.
7. **Small, honest scope.** A feature that cannot be maintained without a paid
   backend does not ship.

## 6. Objects

- **UserProfile** — the student’s name, target track, exam date, study goals,
  onboarding state, and reminder preference.
- **Mistake** — a wrong answer, its topic, its cause, confidence, optional local
  photo, and review state.
- **Topic** — a reference knowledge card: summary, key idea, common confusions.
- **TopicProgress** — explicit status and the next spaced review for a topic.
- **QuestionLog** — D/Y/B/net for one subject on one day.
- **PlanTask** — a daily or weekly target, with completion and carry-over.
- **StudySession** — a completed focus block, optionally tied to a subject.
- **NetGoal** — a monthly total or subject net target.
- **Tactic** — a study method with steps, an evidence level, and a time cost.
- **ExamNet** — a deneme result with optional section detail, duration, and
  post-exam analysis.
- **MotivationNote** — a short, non-clinical note tied to a theme.
- **MotivationQuote** — a short attributed quote with source context and theme.
- **TedTalk** — an official TED URL with an editorial Turkish summary.
- **UserData** — the single persisted document.
- **StudyData** — a read-only projection of user data plus curated content for the
  UI.

The model is immutable. Review actions produce a new `UserData` rather than
mutating shared state, which keeps persistence and future undo straightforward.

## 7. Core flows

### Onboarding

Name and target track are required. The student may opt into a daily reminder;
notification permission is requested only if they do. The privacy card explains
that no account or internet is used and that data stays on the device.

### Capture

Any main tab → global **Yanlış ekle** → choose subject and topic → choose error
kind → optional note → add. The new entry opens immediately so the student can
attach the critical idea while it is fresh. A topic page may pre-fill the topic
through a quiet contextual text action, but it does not repeat the primary
button. Empty notes are stored as empty; the UI uses the error kind’s advice as a
fallback, never as invented user text.

### Review

Bugün → **Sıradaki hamle** (first due mistake) → detail → read the three-step
timeline, critical idea, and related tactic → choose an outcome. The due queue
shrinks and the next move updates. Reviews are persisted, not held in memory.

### Browse

- **Konular** answers “what do I keep confusing?” with searchable confusion pairs
  and weaknesses derived from the student’s own mistakes. Each topic also carries
  a reference exam-weight block: yearly counts, average, subject share, subject
  rank, a last-three-year trend, and an expected range. The block names its
  reference years and source, and says that the range is an estimate rather than
  an official weight or a guarantee.
- The **Dağılım** segment under Konular is a full distribution browser. It shows
  every published topic row for TYT and AYT, including rows without a detailed
  Tuyo card. TYT is the default and first exam because it is the common priority;
  AYT stays one tap away. A subject menu, search across all subjects, yearly
  totals, subject averages, table shares, and per-row “done” marks make the table
  usable rather than decorative. A weekly strip shows this week’s topic marks,
  completed topics, question count, and study minutes. Linked rows open the
  detailed Tuyo card; unlinked rows remain reference rows. The browser labels the
  school-site source, says it is not an official ÖSYM weight table, and explains
  how totals, averages, and shares are derived.
- **Tüyolar** answers “how should I practise, what is the exam like, and how do I
  keep going?” It contains **Taktikler**, **Sınav**, and **Motivasyon**.
- **Motivasyon** contains a daily attributed quote, a filterable quote library,
  official TED talk links about hope, resilience, and motivation, and short
  original notes. Disputed attributions are marked “atfedilen söz”; source and
  context stay on the card.
- **Sınav** contains official structure, 2026 ÖSYM averages, general commentary,
  and sources. It distinguishes official data from post-exam opinion.

### Navigate

Five bottom destinations: **Bugün**, **Defter**, **Çalışma**, **Konular**,
**Tüyolar**. Çalışma contains **Takvim**, **Plan**, **Sayaç**, and **Denemeler**.
The global **Yanlış ekle** pill sits above the bottom navigation on every main
tab; Settings and Detail hide it. Settings is reached from the Today header and
the command palette. Detail is a child of the current tab, so the tab remains
selected and system back returns to the list. `Ctrl/Cmd + K` opens the command
palette on tablets and Chromebooks.

## 8. v0.7 scope

Included:

- First-run profile collection; no bundled learner data.
- Persistent notebook, nets, streak, review history, and settings.
- Five-tab information architecture with Çalışma subsections.
- Question log with calendar, D/Y/B/net, and daily target progress.
- Daily and weekly plans with carry-over, rest days, and rule-based suggestions.
- Pomodoro/free/exam timer as a foreground service with per-subject totals.
- TYT 165 and AYT 180 minute simulations; section-level exam entry and analysis.
- One-day post-exam analysis reminder; transparent 100–500 score estimate.
- Monthly total or subject net goals measured against the latest deneme.
- Topic statuses, 1/3/7/21/45/90-day review ladder, and weak-topic priority.
- Local photo attachment and confidence flags on mistakes.
- Local automatic backups (seven days), SAF export/import, and a confirmed reset.
- 14 topics with reference exam-frequency data, 18 tactics, TYT/AYT/YDT overviews,
  2026 averages, commentary, and sources.
- Topic exam weight from published 2018–2025 TYT and 2019–2025 AYT distributions:
  average question count, subject share, subject rank, last-three-year trend, and
  a floor/ceiling expected range. The range is the simple mean’s floor and
  ceiling, not a regression or a forecast.
- Full TYT/AYT topic distribution browser with all published rows, subject menus,
  search, table shares, row completion marks, a weekly progress strip, and links
  to the matching detailed topic cards. TYT is the default exam; AYT is kept
  separate. AYT Tarih uses 2019–2024 because the source’s 2025 column is blank.
- Motivation library: 34 attributed quotes (including Atatürk, Yunus Emre, Cahit
  Arf, and Aziz Sancar), 11 official TED talk links, and 20 original notes.
- One global capture action, shared screen headers, shared dialogs, and one chip
  component across filters and forms.
- Local, opt-in reminders with permission handling and quiet rest days.
- Zero-backend tactic submission through the system share sheet and GitHub issue
  form.
- Dark and light palettes, Inter, adaptive launcher icon, monochrome layer.
- Unit tests for scheduling, persistence round-trips, derived progress, plans,
  summaries, net goals, exam sections, topic weight, and content integrity.

Explicitly excluded:

- Accounts, cloud sync, analytics, push notifications, and any network permission.
- In-app camera capture or OCR.
- A server-side community feed or moderation queue.
- Tablet-specific layout and a completed accessibility audit.
- Medical or clinical mental-health advice.

## 9. Community tactics without a server

The zero-recurring-cost constraint rules out a custom moderation backend. The
pipeline is:

1. A student submits a tactic through the GitHub issue form (title, category,
   steps, evidence, time cost, source, and safety confirmations).
2. A maintainer reviews it for safety, clarity, duplication, and evidence.
3. Approved tactics are added to the bundled content.
4. The content ships in the next app release.

No runtime network call is required. The app never accepts unreviewed content
from the network. Every tactic in the app has a public history. See
[MODERATION.md](MODERATION.md).

Evidence labels:

- **Araştırma destekli** — backed by a study or established method.
- **Deneyim** — a credible practitioner/student account, not a controlled study.
- **Tartışmalı** — plausible but contested; presented with a caveat.

## 10. Privacy and trust

- No `INTERNET` permission.
- WorkManager contributes normal permissions (wake lock, boot receiver, network
  state for constraints); none grants network access. Only `POST_NOTIFICATIONS`
  is a runtime permission.
- No account, analytics SDK, ad SDK, or remote config.
- DataStore is app-private; Android backup is disabled.
- Reset is explicit, confirmed, and deletes the whole document.
- Reminder notifications are local; the worker reads only the local document.
- Source links open in the browser, where the browser’s own privacy policy
  applies.

## 11. Success signals

For v0.7, success is qualitative and local:

- A student can capture a mistake or a question log without breaking study flow.
- The next review or plan task is obvious without opening a menu.
- The notebook feels worth returning to after a deneme.
- Nothing in the UI makes a missed day or a rest day feel like a failure.
- A student can tell which numbers are theirs, which are official statistics, and
  which are estimates.

If analytics are added later, they must be opt-in and local-first. The core
questions are: capture completion rate, review completion rate, and seven-day
return rate — never time-in-app as a vanity metric.

## 12. Open questions

1. **Topic granularity** — should topics stay curriculum-level (Türev) or grow
   micro-skills (zincir kuralında iç fonksiyonun türevi)?
2. **Net model** — section-level AYT entry now exists; is per-question timing
   worth the extra capture cost?
3. **Content format** — when should tactics move from Kotlin to versioned JSON?
4. **Teacher view** — is a shareable weekly summary a real need, or scope creep?
5. **Reminder intelligence** — should the app skip a reminder when the student
   already reviewed today? The current worker sends a calm confirmation instead.
