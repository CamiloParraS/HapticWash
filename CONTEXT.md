# HapticWash — Domain Glossary

Terms used across the app, its spec and its reports. Shared contracts (label set, tensors,
preprocessing) live in the core spec in `HapticWash-AI`.

**Wash** — one episode from Start to Stop (or to the end of a replay file). A Wash always
has a duration; it has Coverage only when the model is loaded and the Wash is long enough.
Replayed Washes are Washes too, marked with their Source.

**Source** — where a Wash's motion came from: `sensor` (the watch) or `replay` (a recorded
file). Replay Washes exist only in debug builds, are kept apart from real ones, and never
appear in History.

**History** — the user's last 20 real (`sensor`) Washes.

**Step** — one of the five WHO hand-rubbing gestures: Palms, Back of hands, Between
fingers, Thumbs, Fingertips. `NULL` (no washing) and Washing (`WASH_OTHER`) are *not*
Steps.

**Washing** — wash motion that is not a Step (wetting, soaping, rinsing, anything else).
Counts as the wash being in progress but gives no Step any Coverage.

**Coverage** — the estimated seconds and Step Status of each of the five Steps in one
Wash. Always all five, in canonical order.

**Step Status** — `OK` ("looks covered"), `BRIEF` ("may have been brief"), `NOT_DETECTED`
("not detected"). An estimate, never a verdict: no Status means the hands are or are not
clean.

**Timer-only mode** — the app's behaviour when the model cannot be used: Washes are
counted and timed, with no Coverage.

**Wrist side** — which wrist the watch is worn on. The model is trained on the left wrist;
right-wrist motion is mirrored into the left-wrist frame.

**Twist** — three quick forearm rotations, back and forth, inside a short window. A deliberate
gesture, not wash motion.

**Twist start** — starting a Wash with a Twist instead of a tap. Off by default; while on, the
watch listens in the background between Washes. Not spotting: the app never decides on its own
that a wash is happening.

**Auto-end** — a Wash ending itself on sustained `NULL`: cancelled (not saved) if no washing is seen
within 30 s of the start, a keep-going nudge if washing pauses before the 40 s target, ended normally if
washing stops after it. Needs the model, so Timer-only Washes end by tap only.

**Target check** — the one live prompt a Wash may give: at the 40 s target, if any Step is
`NOT_DETECTED`, a single nudge and a screen naming those Steps. Not continuous prompting. An
estimate like any Step Status. Ships only if M6 passes its gate; has a Settings toggle.

**Focus Step** — the one Step a user habitually misses: `NOT_DETECTED` or `BRIEF` in at least 60 % of
their last 10 Washes with Coverage (and `NOT_DETECTED` alone in at least 30 %), at least 5 such Washes
needed. Worked out from History each time, never stored; there is none when no Step qualifies. An
estimate like any Step Status.

**Step guide** — the page behind a Summary row: the Step's name, an illustration and its why line.
Reached only from Summary (or the Focus Step card), never from Start or Settings.

**Why line** — the one short sentence on a Step guide saying why that part of the hand needs its own attention, as contact reasoning
(what it touches and picks up). No statistics, no named germs or illnesses, no prevention or outcome claims.
