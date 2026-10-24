# Tuyo — Design System

## 1. Direction

Tuyo borrows the calm of a well-kept paper planner and the clarity of a good
notebook, not the urgency of a productivity feed:

- **Warm paper, not black glass.** The canvas is a soft off-white or a warm
  charcoal, never pure white or near-black.
- **Quiet density.** A phone screen can hold a lot if the hierarchy is strict.
- **Precision with softness.** Hairlines and aligned columns, softened by 18 dp
  cards and generous breathing room.
- **One accent plus weather.** Blue still means “act or selected”. A few pale
  peach, sky, and leaf washes give screens a human rhythm; they never compete
  with meaning.
- **No theatre.** Motion confirms a change; it does not celebrate it.
- **A visible stopping point.** When the day's work is done, the interface says
  so and invites rest. It does not manufacture another loop.
- **Editorial Turkish.** Copy is short, specific, and calm. No exclamation marks,
  no guilt, no “you are behind”.

The result should feel like a serious study instrument that is pleasant to open
and easy to close — not a productivity toy, and not a dark control room.

## 2. Colour

Colours are semantic tokens in `ui/theme/Color.kt`. Screens never use raw hex.
The light palette is designed independently; it is not a mechanical inversion.

| Token | Dark | Light | Use |
| --- | --- | --- | --- |
| `canvas` | `#111417` | `#FAF7F2` | Warm app background |
| `surface` | `#181C21` | `#FFFDFA` | Cards, fields |
| `raised` | `#22272E` | `#F4EFE7` | Secondary fills |
| `overlay` | `#1D2228` | `#FFFDFA` | Dialogs, palette |
| `hairline` | white 10% | black 8% | Default 1 px border |
| `hairlineStrong` | white 18% | black 15% | Dialog/chrome border |
| `textPrimary` | `#F5F0E9` | `#26221D` | Titles, body |
| `textSecondary` | `#B3AAA0` | `#6E6458` | Supporting copy |
| `textTertiary` | `#91887D` | `#786E61` | Metadata, placeholders |
| `accent` | `#8EA2FF` | `#3F63D8` | Primary action, selection |
| `accentSoft` | blue 15% | `#E9EDFC` | Selected nav pill, focus wash |
| `success` | `#6BC9A0` | `#1F714D` | Completed step, rest |
| `warning` | `#F0C16B` | `#985A0B` | Attention, countdown |
| `danger` | `#F08C8C` | `#C25151` | Error kind, destructive action |
| `selection` | `#252B33` | `#F2EADF` | Selected list row |
| `warmWash` | `#2B241D` | `#FFF0E1` | Motivation, countdown wash |
| `coolWash` | `#1E2838` | `#ECF1FD` | Snapshot, running timer wash |
| `freshWash` | `#1C2B25` | `#EAF5EE` | Rest, weekly, day-complete wash |

Brand blue `#3B5BFE` and brand amber `#FFC83D` are reserved for the mark and
launcher. Amber is not a general-purpose accent inside the app.

Rules:

- A card is one surface step above the canvas; a dialog is one step above that.
- Borders are 1 dp and low-contrast. Shadows are not used. Depth comes from the
  surface step, a hairline, and an optional vertical wash.
- A wash is applied to at most a few cards per screen. It is atmosphere, not
  status; never use colour as the only signal.
- The ambient background carries three low-opacity radial washes: warm light at
  the top, a little sky at the upper left, and fresh green at the bottom. They
  should be felt before they are noticed.
- Subject tiles use restrained, distinct tints; a tint never changes the meaning
  of a label.

## 3. Typography

**Inter Variable** is bundled once and declared with real weight axes so bold
weights are not synthesized. Display sizes use negative tracking; body copy uses
normal tracking.

| Style | Size / line | Weight | Use |
| --- | --- | --- | --- |
| Display | 38 / 42 sp | 600 | Net value, hero numbers |
| Display small | 30 / 34 sp | 600 | Large headings, motivation hero |
| Headline | 24 / 29 sp | 600 | Screen titles |
| Headline small | 20 / 25 sp | 600 | Dialog titles |
| Title | 17 / 22 sp | 600 | Card titles |
| Title small | 15 / 20 sp | 500 | Row titles, buttons |
| Body | 15 / 23 sp | 400 | Primary reading |
| Body small | 14 / 21 sp | 400 | Supporting copy |
| Caption | 13 / 18 sp | 400 | Notes |
| Label | 13 / 16 sp | 500 | Buttons, chips |
| Micro | 11 / 14 sp | 500 | Eyebrows, metadata |

`NumericStyle` (42 sp, weight 600, tabular figures) is used for nets and counts
so digits do not jump when values change. Eyebrows are uppercase, 11 sp, and
letter-spaced; they label a region without competing with its content.

## 4. Space, shape, motion

- **Grid:** 4 pt. Screen gutters are 20 dp; card padding is 16–18 dp; section
  gaps are 28 dp.
- **Radii:** 7 dp (chips, small controls), 11 dp (fields, buttons), 18 dp (cards),
  22 dp (dialogs), pill for avatars, progress, and the selected nav state.
- **Motion:** 110 ms (feedback), 170 ms (standard), 240 ms (larger transitions).
  Standard easing `cubic-bezier(0.2, 0, 0, 1)`; enter `cubic-bezier(0.16, 1, 0.3, 1)`.
  Nothing bounces or overshoots. No idle animation, confetti, or streak flame.
- **Elevation:** no shadows. Depth is expressed by surface step + hairline +
  optional tint wash.

## 5. Components

| Component | Contract |
| --- | --- |
| `TuyoCard` | One content unit. 18 dp radius, hairline, configurable padding, optional pale tint wash. A clickable card never also contains a button for the same action. |
| `TuyoButton` | `Primary`, `Secondary`, `Ghost`, `Danger`; optional leading icon; 36 dp compact height with an 11 dp radius. |
| `TuyoTextAction` | The quiet text-link affordance for section actions and contextual follow-ups; 32 dp minimum hit area. |
| `CaptureButton` | The single global “Yanlış ekle” pill, anchored above the bottom navigation on main tabs. |
| `ScreenHeader` | Shared eyebrow / title / optional subtitle / optional back chrome and actions. |
| `TuyoChoiceChip` | Radio-style chip for one-of-many choices; selected state uses accent wash + border. All filters use this component. |
| `TuyoSegmented` | Horizontal, scrollable segmented control for sections. |
| `TuyoDialog` | Shared modal surface with scrim, header, close action, scrollable body, and fixed footer. |
| `TuyoConfirmDialog` | Destructive confirmation built on `TuyoDialog`; cancel is the safe default. |
| `TuyoSwitch` | Semantic switch; permission-dependent states are explained in copy. |
| `TuyoTextField` | Compact single-line field with placeholder and max length. |
| `Tag` | Small metadata label with an optional semantic colour. |
| `EvidenceTag` | Research / experience / debated, each with a distinct tint and label. |
| `SubjectTile` | Two/three-letter subject abbreviation on a tinted tile. |
| `Hairline` | 1 dp divider at the correct opacity. |
| `SectionHeader` | Title plus optional text action, aligned on one baseline. |
| `Eyebrow` | Uppercase micro-label for a region. |
| `MasteryDots` | Five dots; filled up to mastery. Never the only signal. |
| `ProgressLine` | Animated 3 dp line for topic mastery. |
| `IconButtonGhost` | 40 dp chrome button with no filled background. |
| `MetaText` | 11–12 sp tertiary metadata, tabular where numeric. |
| `MistakeRow` | Subject tile, topic, error kind, due date, mastery. |
| `Sparkline` | Net trend without axes; only a like-for-like exam series. |
| `CommandPalette` | Modal command surface with grouped results and key hints. |

## 6. Layout

The phone layout is a single scrollable column per surface with a persistent
bottom navigation bar. Cards span the full content width; lists use dividers
rather than nested cards.

- **Onboarding:** two steps. Profile and privacy first; reminder choice and time
  second. The time picker is a readable 3×2 grid, not six squeezed chips.
- **Bugün:** header → countdown → today snapshot → daily plan → net card → review
  loop → next repeat → pending list → focus topic → weekly summary → motivation
  → featured tactic → optional day-complete pause. The pause card appears only
  after the daily goal (or every daily task) is complete and sits at the natural
  end of the page: it is a stopping cue, not a reward loop. The net card owns one
  “Net ekle” action in its header; the next repeat, motivation and tactic cards
  navigate by tapping the card and show a single trailing arrow hint.
- **Defter:** title, subtitle, subject filters, notebook card with rows. There is
  no per-screen add button; the global capture action handles every new entry.
- **Detail:** back chrome → title/note → three-step timeline → error diagnosis →
  critical idea → review plan → related tactic → delete.
- **Konular:** title, derived-data summary, search, `Odağım` / `Tüm konular`
  segments, expandable topic rows. An expanded topic may offer one quiet
  “Bu konudan yanlış ekle” text action because it pre-fills the topic.
- **Tüyolar:** title, `Taktikler` / `Sınav` / `Motivasyon` segments. Taktikler has
  category filters and the contribution note at the end; Sınav has structure
  cards, official averages, commentary, and sources; Motivasyon has a daily quote
  hero and a `Sözler` / `TED` / `Kısa notlar` segment, with theme filters on the
  quote library.
- **Ayarlar:** back chrome, profile card, reminder card, local-data card, about
  card. Notification permission is explained inline, with a system-settings
  action when needed.

All main tabs reserve bottom scroll padding for the global capture button. The
command palette is centered at the top of the window, above the keyboard, with a
dimmed backdrop. Add and confirmation dialogs share one centered panel with a
scrollable body and a fixed footer, so the primary action remains reachable when
the IME is open.

### Responsive behaviour

Current layouts are phone-first. Content stretches on tablets and the palette is
capped at 560 dp; there is no two-pane or max-width mode yet. This is a known
future design task, not a finished claim.

## 7. Interaction

- **Navigation:** five bottom destinations. Detail and Settings are overlays in
  the current surface; system back returns to the previous surface. Çalışma
  switches between calendar, plan, timer, and exams without leaving the tab.
- **Command palette:** `Ctrl/Cmd + K` or the search button. Type to filter; arrow
  keys move; Enter opens; Esc closes. Filtering uses Turkish locale rules, so
  “yan” matches “Yanlışlar”.
- **Review:** four explicit outcomes, never a hidden algorithm. The chosen
  outcome is confirmed inline (“İyi · Yarın · 3/5”). The confirmation is local
  feedback; the source of truth is the persisted document.
- **Capture:** one global “Yanlış ekle” action sits above the bottom navigation
  on every main tab. Topic and error kind are required by sensible defaults; the
  note is optional and stored verbatim. Submitting opens the new entry
  immediately. Topic pages may pre-fill the topic through one contextual text
  action, but they never repeat the full primary button.
- **Expand/collapse:** topics and tactics expand in place; the chevron rotates and
  the row announces its state.
- **Reminder:** the switch is the consent. Turning it on may open the system
  permission prompt. If permission is missing, the card explains it and offers a
  direct path to notification settings.
- **Reset/delete:** destructive actions require the shared confirmation dialog;
  cancel is the safe default and destructive confirmation uses the `Danger`
  button variant.

## 8. Voice and microcopy

- Use verbs for actions: **Yanlış ekle**, **Taktiği aç**, **Deftere ekle**.
- Name the behaviour, not the person: **İşlem hatası**, not “acelecisin”.
- Prefer concrete time: **7 gün sonra**, not “yakında”.
- Empty states explain the next action, never scold.
- Distinguish **senin verin** from **resmî ortalama**. The exam section labels
  commentary and sources.
- Turkish characters are preserved (ı, İ, ş, ğ, ü, ö, ç). Case conversion uses
  a Turkish locale; `i` must not become `I` in a way that breaks search.
- Avoid English UI terms unless they are established in Turkish study culture
  (“net”, “deneme”, “soru kökü” are fine; “dashboard” is not).

## 9. Empty states

Empty states are part of the product, not an afterthought:

| Surface | Empty state |
| --- | --- |
| Notebook | “Defter henüz boş”; copy points to the global “Yanlış ekle” capture action. |
| Today next move | “Önce bir yanlış kaydet” when there are no mistakes; “Bugünlük tamam” when nothing is due. Neither card repeats a capture button. The completed state points toward rest, not another task. |
| Topics | “Henüz konu verin yok” when there are no mistakes; “Şu an odakta bir konu yok” when data exists but nothing needs attention. Copy points to the global capture action. |
| Net card | “İlk deneme netini ekle” with one header-level “Net ekle” action. |
| Permission | Explain the missing permission and provide a settings action. |

No empty state invents a learner name, a score, a mistake, or a streak.

## 10. Accessibility

Implemented:

- Text and icon colours are chosen against their surfaces for contrast.
- Interactive icons have content descriptions.
- State is conveyed by text as well as colour (mastery number, due label,
  selected breadcrumb, evidence label).
- The layout survives font scaling reasonably; text is measured, not clipped by
  fixed heights except in deliberate single-line rows.
- The command palette provides a keyboard path to every destination and action.

Known gaps for the next pass:

- Compact chips and buttons are 34–36 dp tall; the Android touch-target guideline
  is 48 dp. Expand hit areas without changing the visual density.
- Custom choice chips expose a radio role but not always a selected semantics
  state.
- No formal screen-reader or 200% font-scale audit has been run.
- Tablet layouts stretch rather than adapt.
- The 3×2 reminder grid is readable, but a true time picker would be better for
  arbitrary times.

## 11. Launcher icon

The mark is a speech bubble with a “T” cut out and an amber dot. The adaptive
foreground is placed on a blue background at the standard 68/108 safe zone. A
monochrome layer is generated at every density for themed icons. Legacy PNG
mipmaps are intentionally absent; `minSdk` is 26, and adaptive icons cover every
supported device.

## 12. What to avoid

- Purple gradients, glassmorphism, neon glows.
- Stock illustrations of students at desks.
- More than one accent per screen; use washes for warmth, not extra accents.
- Card-on-card nesting without a strong reason.
- Celebration animations for a review; the reward is a calmer queue.
- Streak pressure, “don't break the chain”, or any loop that manufactures one
  more task after the day is already complete.
- Copy that implies the student is lazy, behind, or failing.
- Mixing official statistics with personal nets without a label.
