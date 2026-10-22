# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users
Everyday people wearing a round Wear OS watch, standing at a sink with wet, soapy hands. Their job: rub through the five WHO hand-rubbing Steps for about 40 s and get a quick read on what looked covered. They glance; they rarely touch mid-Wash.

## Product Purpose
HapticWash times a hand wash and, with an on-device model reading wrist motion, estimates which of the five Steps (Palms, Back of hands, Between fingers, Thumbs, Fingertips) looked covered. Success: a user washes a little more thoroughly over time and learns the Step they habitually skip (the Focus Step).

## Positioning
Coverage is estimated from the watch's own accelerometer and gyroscope, fully offline (no INTERNET permission). It never claims hands are clean.

## Operating Context
Wet hands, glanceable screen, haptics over sound. Start is one tap (or an optional Twist). A Wash auto-ends on sustained no-washing. Results live in History (last 20 real Washes). Vocabulary is fixed in `CONTEXT.md`.

## Capabilities and Constraints
- Screens: Start, live Wash (ring / timer-only / ambient / early-stop nudge / Target check), Summary (all covered, mixed, Focus Step, too short, timer-only, cancelled), Step guide x5, History (+ empty), Settings (During a wash, Starting a wash, Language, Twist battery warning), Credits, Tile, Complication.
- Round 227 dp screen, ≥48 dp targets, one primary action per screen, no dialogs with more than two options, nav at most two levels deep.
- Timer-only mode when the model cannot run: duration only, no Coverage.
- Languages: English and Spanish; every string must fit in both.

## Brand Commitments
- Estimate language only: "looks covered", "may have been brief", "not detected". Never "you missed", "failed", "clean". Unit tests enforce banned wording.
- Step status is never encoded by colour alone.
- Step illustrations: Hegasy, CC BY-SA 4.0, credited on the Credits screen.

## Evidence on Hand
Current screenshots in `docs/ux/*.png`. No user research, testimonials or efficacy data exist; do not invent any.

## Product Principles
1. Glanceable in under 5 seconds, readable with wet hands at arm's length.
2. Honest estimates over confident verdicts.
3. Encourage, never scold.
4. Battery honesty: anything that listens in the background says so.
