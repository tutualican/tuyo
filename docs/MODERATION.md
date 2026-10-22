# Tuyo — Community Tactic Moderation

Tuyo accepts study-tactic suggestions without a moderation server. The app is
offline and has no runtime submission endpoint. The whole pipeline runs on GitHub
and ships reviewed content inside the next app release.

## 1. Pipeline

```text
Student in Tuyo
  └─ Tüyolar → Taktik öner
       └─ system share sheet / GitHub issue form
            └─ maintainer review
                 ├─ decline with a reason
                 └─ approve
                      └─ add to StudyContent.kt (or future versioned JSON)
                           └─ tests + release + public history
```

There is no feed, no voting, no upload, and no paid backend. A submission is a
normal public issue, so the community can see the evidence and the decision.

## 2. Submission form

The structured form lives at `.github/ISSUE_TEMPLATE/tactic.yml`. It asks for:

- a short title,
- category (study / TYT / AYT / exam strategy / exam day / wellbeing),
- one-paragraph summary,
- numbered steps,
- evidence level and source,
- why it should help,
- confirmation that the text contains no personal data and no medical claims.

The in-app share action opens the system share sheet with the same structure.
The app never uploads the text itself and never reads the student's notebook for
a submission.

## 3. Review rubric

A maintainer checks the following before approving.

### Safety and scope

- No diagnosis, treatment, medication, or crisis advice. Wellbeing tactics must
  be framed as general study support and include a caveat when relevant.
- No encouragement to skip sleep, food, medication, or professional care.
- No exam-leak claims, “guaranteed score” promises, or paid-course advertising.
- No political, religious, or discriminatory content unrelated to study.
- No personal data, contact details, school names, or identifying screenshots.

### Clarity and actionability

- The tactic has a concrete trigger, a sequence of steps, and a stopping point.
- It fits one category and one realistic time cost.
- A student can try it without buying anything or creating an account.
- It is not a duplicate of an existing tactic. If it improves one, edit the
  existing entry instead.

### Evidence

Choose exactly one label:

| Label | Standard |
| --- | --- |
| **Araştırma destekli** | Direct experimental or review support for the method (for example retrieval practice or distributed practice). Cite a paper or a reputable summary. |
| **Deneyim** | A credible practitioner/student account, exam-specific strategy, or a method that is plausible but not directly tested. Say so. |
| **Tartışmalı** | Popular but contested, or context-dependent. Include the caveat in the step text or source note. |

A source URL is required for research-labelled tactics. Anecdotal tactics should
still name their basis (exam format, classroom practice, published guidance).
Unsupported “this worked for me” claims can be accepted only as `Deneyim`, and
only if they are safe and actionable.

### Turkish copy

- Short, concrete, non-moralising Turkish.
- No shaming words (“tembel”, “başarısız”).
- Preserve Turkish characters.
- Avoid English jargon unless it is established in Turkish study culture.

## 4. Decision labels

Suggested GitHub labels:

- `tactic` — every submission.
- `needs-review` — waiting for a maintainer.
- `needs-source` — evidence or attribution is missing.
- `approved` — ready to merge into bundled content.
- `declined` — closed with a short reason.
- `duplicate` — points to an existing tactic.

A maintainer should reply with one of:

- **Approved** — what will be added and in which category.
- **Needs work** — the specific missing piece (source, steps, safety).
- **Declined** — the concrete reason (unsafe, duplicate, advertising, out of
  scope).

Best-effort response time is one to two weeks; this is a volunteer project, not a
paid support channel.

## 5. Adding an approved tactic

1. Add a `Tactic` entry in `StudyContent.kt` with a stable kebab-case `id`.
2. Fill `title`, `summary`, `steps`, `evidence`, `minutes`, `category`,
   `topicIds`, and `sourceNote`.
3. Keep steps imperative and numbered; the UI numbers them automatically.
4. Run `StudyContentTest` so IDs, topic references, and counts stay valid.
5. Run the full quality gate:

   ```bash
   ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
   ```

6. Mention the issue number in the commit message.
7. Release the content in the next app version.

Content is bundled, so an approved tactic is not visible until the next release.
This is intentional: the app has no network permission and cannot fetch a live
feed.

## 6. Privacy

- Submissions are public GitHub issues. Ask contributors not to include personal
  data.
- The in-app template includes a reminder: “Kişisel bilgi eklemeyin.”
- The app never attaches the learner's name, nets, mistakes, or device data.
- A maintainer must remove accidental personal data from an issue before
  discussing it further.

## 7. Why this stays free

GitHub Issues and pull requests are the queue, the audit log, and the review
interface. There is no database, no hosting bill, no push service, and no
moderation dashboard to maintain. The trade-off is latency and a code change per
approved tactic; both are acceptable for a small, curated library.

If the volume ever makes code changes impractical, the next step is versioned
JSON in the repository, still reviewed and bundled at build time. A live server
is not required.
