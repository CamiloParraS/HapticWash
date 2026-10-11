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

**Auto-end** — a Wash ending itself on sustained `NULL`: cancelled (not saved) if the first 10 s
are all `NULL`, a keep-going nudge if washing pauses before the 40 s target, ended normally if
washing stops after it. Needs the model, so Timer-only Washes end by tap only.
