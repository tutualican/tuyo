# Tuyo — Design System

## 1. Direction

Tuyo borrows the *attitude* of Linear and Orbiter, not their layouts:

- **Quiet density.** A phone screen can hold a lot if the hierarchy is strict.
- **Precision.** Hairlines, aligned columns, tabular numerals.
- **One accent.** Blue means “act or selected”; everything else earns its colour.
- **No theatre.** Motion confirms a change; it does not celebrate it.
- **Editorial Turkish.** Copy is short, specific, and calm. No exclamation marks.

The result should feel like a serious study instrument that happens to be
beautiful, not a productivity toy.

## 2. Colour

Colours are semantic tokens in `ui/theme/Color.kt`. Screens never use raw hex.
The light palette is designed independently; it is not a mechanical inversion.

| Token | Dark | Light | Use |
| --- | --- | --- | --- |
| `canvas` | `#08090A` | `#FBFBFC` | App background |
| `surface` | `#0D0E10` | `#FFFFFF` | Cards, fields |
| `raised` | `#14161A` | `#F3F4F6` | Secondary fills |
| `overlay` | `#1A1C21` | `#FFFFFF` | Dialogs, palette |
| `hairline` | white 8% | black 8% | Default 1 px border |
| `hairlineStrong` | white 15% | black 15% | Dialog/chrome border |
| `textPrimary` | `#F7F8F8` | `#151618` | Titles, body |
| `textSecondary` | `#8A8F98` | `#5E6268` | Supporting copy |
| `textTertiary` | `#5C6169` | `#8B8F96` | Metadata, placeholders |
| `accent` | `#6B7CFF` | `#3B5BFE` | Primary action, selection |
| `accentSoft` | blue 12% | blue 8% | Selected chip, focus wash |
| `success` | `#4CB782` | `#1F8A5B` | Completed step, saved state |
| `warning` | `#F2C94C` | `#8A6100` | Attention, never error |
| `danger` | `#EB5757` | `#C23B3B` | Error kind, destructive action |
| `selection` | `#1B1E24` | `#EFEFF2` | Selected list row |

Brand blue `#3B5BFE` and brand amber `#FFC83D` are reserved for the mark and
launcher. Amber is not a general-purpose accent inside the app.

Rules:

- A card is one surface step above the canvas; a dialog is one step above that.
- Borders are 1 dp and low-contrast. Shadows are not used.
- Colour never carries meaning alone; every state also has a label, icon, or
  position.
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
- **Radii:** 6 dp (chips, small controls), 10 dp (fields), 14 dp (cards),
  18 dp (dialogs), pill for avatars and progress.
- **Motion:** 110 ms (feedback), 170 ms (standard), 240 ms (larger transitions).
  Standard easing `cubic-bezier(0.2, 0, 0, 1)`; enter `cubic-bezier(0.16, 1, 0.3, 1)`.
  Nothing bounces or overshoots.
- **Elevation:** no shadows. Depth is expressed by surface step + hairline.

## 5. Components

| Component | Contract |
| --- | --- |
| `TuyoCard` | One content unit. 14 dp radius, hairline, configurable padding. |
| `TuyoButton` | `Primary`, `Secondary`, `Ghost`; optional leading icon; compact height. |
| `TuyoChoiceChip` | Radio-style chip for one-of-many choices; selected state uses accent wash + border. |
| `TuyoSegmented` | Horizontal, scrollable segmented control for sections. |
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
- **Bugün:** header → net card → review loop → next move → pending list →
  motivation → featured tactic.
- **Defter:** title, subtitle, subject filters, notebook card with rows.
- **Detail:** back chrome → title/note → three-step timeline → error diagnosis →
  critical idea → review plan → related tactic → delete.
- **Konular:** title, derived-data summary, search, `Eksiklerim` / `Tüm konular`
  segments, expandable topic rows.
- **Tüyolar:** title, `Taktikler` / `Sınav` / `Motivasyon` segments. Taktikler has
  a bundle note and category filters; Sınav has structure cards, official
  averages, commentary, and sources; Motivasyon has a daily hero and all notes.
- **Ayarlar:** back chrome, profile card, reminder card, local-data card, about
  card. Notification permission is explained inline, with a system-settings
  action when needed.

The command palette is centered at the top of the window, above the keyboard,
with a dimmed backdrop. The add dialog is a centered panel with a scrollable body
and a fixed footer, so the primary action remains reachable when the IME is open.

### Responsive behaviour

Current layouts are phone-first. Content stretches on tablets and the palette is
capped at 560 dp; there is no two-pane or max-width mode yet. This is a known
future design task, not a finished claim.

## 7. Interaction

- **Navigation:** four bottom destinations. Detail and Settings are overlays in
  the current surface; system back returns to the previous surface.
- **Command palette:** `Ctrl/Cmd + K` or the search button. Type to filter; arrow
  keys move; Enter opens; Esc closes. Filtering uses Turkish locale rules, so
  “yan” matches “Yanlışlar”.
- **Review:** four explicit outcomes, never a hidden algorithm. The chosen
  outcome is confirmed inline (“İyi · Yarın · 3/5”). The confirmation is local
  feedback; the source of truth is the persisted document.
- **Capture:** topic and error kind are required by sensible defaults; the note
  is optional and stored verbatim. Submitting opens the new entry immediately.
- **Expand/collapse:** topics and tactics expand in place; the chevron rotates and
  the row announces its state.
- **Reminder:** the switch is the consent. Turning it on may open the system
  permission prompt. If permission is missing, the card explains it and offers a
  direct path to notification settings.
- **Reset:** destructive action requires a confirmation dialog; cancel is the
  safe default.

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
| Notebook | “Defter henüz boş” + first-mistake action. |
| Today next move | “Önce bir yanlış kaydet” when there are no mistakes; “Bugünlük tamam” when nothing is due. |
| Topics | “Henüz konu verin yok” when there are no mistakes; “Eksik konu görünmüyor” when data exists but nothing is weak. |
| Net card | “İlk deneme netini ekle” and an add action. |
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
- More than one accent per screen.
- Card-on-card nesting without a strong reason.
- Celebration animations for a review; the reward is a calmer queue.
- Copy that implies the student is lazy, behind, or failing.
- Mixing official statistics with personal nets without a label.
