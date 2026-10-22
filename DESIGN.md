---
name: HapticWash
description: Five Rings — a watchOS-grammar visual system on a round 227 dp Wear OS screen.
colors:
  tint: "#64D2FF"
  tint-light: "#9BE4FF"
  tint-mist: "#B4E9FF"
  tint-sky: "#3AA8FF"
  tint-deep: "#0A84FF"
  on-tint: "#00131F"
  done: "#63E6BE"
  done-ring: "#3FD9A8"
  done-light: "#A9F5DA"
  stop: "#FF453A"
  step-palms: "#FF375F"
  step-palms-light: "#FF8AA3"
  step-backs: "#FF9F0A"
  step-backs-light: "#FFC66B"
  step-between: "#FFD60A"
  step-between-light: "#FFEC80"
  step-thumbs: "#30D158"
  step-thumbs-light: "#86EFA4"
  step-tips: "#BF5AF2"
  step-tips-light: "#DFA6FF"
  hub-indigo: "#5E5CE6"
  hub-blue-text: "#409CFF"
  black: "#000000"
  text-primary: "#FFFFFF"
  text-secondary: "rgba(235,235,245,0.62)"
  text-tertiary: "rgba(235,235,245,0.38)"
  time-text: "#F2F2F7"
  platter: "#202024"
  platter-raised: "#2C2C31"
  hairline: "#1F1F23"
  switch-track-off: "#39393D"
  glass: "rgba(255,255,255,0.14)"
  ambient-line: "#8A8A90"
  ambient-text: "#A8A8AD"
typography:
  display:
    fontFamily: "Google Sans Flex, Roboto, sans-serif"
    fontSize: "58sp"
    fontWeight: 650
    lineHeight: 1
    letterSpacing: "-0.01em"
    fontFeature: "\"tnum\""
    fontVariation: "\"ROND\" 100"
  display-small:
    fontFamily: "Google Sans Flex, Roboto, sans-serif"
    fontSize: "46sp"
    fontWeight: 650
    lineHeight: 1
    letterSpacing: "-0.01em"
    fontFeature: "\"tnum\""
    fontVariation: "\"ROND\" 100"
  headline:
    fontFamily: "Google Sans Flex, Roboto, sans-serif"
    fontSize: "28sp"
    fontWeight: 650
    lineHeight: 1
    letterSpacing: "-0.01em"
    fontFeature: "\"tnum\""
    fontVariation: "\"ROND\" 100"
  headline-small:
    fontFamily: "Google Sans Flex, Roboto, sans-serif"
    fontSize: "18sp"
    fontWeight: 650
    lineHeight: 1.18
    fontFeature: "\"tnum\""
    fontVariation: "\"ROND\" 100"
  title:
    fontFamily: "Google Sans Flex, Roboto, sans-serif"
    fontSize: "15sp"
    fontWeight: 700
    lineHeight: 1.18
    fontVariation: "\"ROND\" 100"
  body-large:
    fontFamily: "Google Sans Flex, Roboto, sans-serif"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: 1.18
  body:
    fontFamily: "Google Sans Flex, Roboto, sans-serif"
    fontSize: "15sp"
    fontWeight: 600
    lineHeight: 1.18
  body-small:
    fontFamily: "Google Sans Flex, Roboto, sans-serif"
    fontSize: "14sp"
    fontWeight: 500
    lineHeight: 1.22
  label-large:
    fontFamily: "Google Sans Flex, Roboto, sans-serif"
    fontSize: "13sp"
    fontWeight: 400
    lineHeight: 1.18
  label:
    fontFamily: "Google Sans Flex, Roboto, sans-serif"
    fontSize: "12sp"
    fontWeight: 400
    lineHeight: 1.25
  label-small:
    fontFamily: "Google Sans Flex, Roboto, sans-serif"
    fontSize: "11sp"
    fontWeight: 400
    lineHeight: 1.2
  time-text:
    fontFamily: "Google Sans Flex, Roboto, sans-serif"
    fontSize: "12.5sp"
    fontWeight: 600
    letterSpacing: "0.01em"
    fontFeature: "\"tnum\""
rounded:
  stop-square: "7dp"
  platter: "24dp"
  full: "9999dp"
spacing:
  hairline-gap: "2dp"
  row-gap: "6dp"
  inner-gap: "10dp"
  platter-x: "14dp"
  platter-y: "8dp"
  ring-inset: "5dp"
  edge-clear: "28dp"
components:
  button-primary:
    backgroundColor: "{colors.tint}"
    textColor: "{colors.on-tint}"
    typography: "{typography.body-large}"
    rounded: "{rounded.full}"
    height: "50dp"
    width: "179dp"
  button-primary-running:
    backgroundColor: "{colors.platter-raised}"
    textColor: "{colors.tint}"
    typography: "{typography.body-large}"
    rounded: "{rounded.full}"
    height: "50dp"
    width: "179dp"
  button-secondary:
    backgroundColor: "{colors.platter-raised}"
    textColor: "{colors.text-primary}"
    typography: "{typography.body}"
    rounded: "{rounded.full}"
    height: "48dp"
    width: "132dp"
  button-glass:
    backgroundColor: "{colors.glass}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.full}"
    size: "48dp"
  button-confirm:
    backgroundColor: "{colors.tint}"
    textColor: "{colors.on-tint}"
    rounded: "{rounded.full}"
    size: "50dp"
  platter-row:
    backgroundColor: "{colors.platter}"
    textColor: "{colors.text-primary}"
    typography: "{typography.body}"
    rounded: "{rounded.platter}"
    padding: "8dp 14dp"
    width: "198dp"
    height: "50dp"
  platter-row-focus:
    backgroundColor: "rgba(191,90,242,0.2)"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.platter}"
    padding: "8dp 14dp"
    width: "198dp"
    height: "56dp"
  platter-row-quiet:
    backgroundColor: "transparent"
    textColor: "{colors.text-secondary}"
    typography: "{typography.label}"
    rounded: "{rounded.platter}"
    padding: "8dp 14dp"
    width: "198dp"
    height: "48dp"
  switch-on:
    backgroundColor: "{colors.tint}"
    rounded: "{rounded.full}"
    width: "40dp"
    height: "24dp"
  switch-off:
    backgroundColor: "{colors.switch-track-off}"
    rounded: "{rounded.full}"
    width: "40dp"
    height: "24dp"
  screen-title:
    textColor: "{colors.tint}"
    typography: "{typography.title}"
---

# Design System: HapticWash

## Overview

**Creative North Star: "Five Rings"**

HapticWash speaks the grammar Apple Watch made familiar, translated onto a round 227 dp Wear OS screen: full-bleed black, an app tint that washes down from the top like light through water, Activity-style concentric rings with one ring and one colour per Step, big rounded numerals, platter lists, and glass buttons sitting on the bottom arc. The watch face is the whole canvas; there is no chrome, no card around the content, no frame inside the frame.

Each screen carries one idea at glance distance. The live Wash is a number, a ring and one encouraging line. The Summary is the rings, a fraction in words, then only the Steps that need a look. Colour orients (each Step has its own hue, each screen has its own wash), while shape and words carry status, so the system still reads to someone who cannot tell red from green, at arm's length, with wet hands.

The system is warm without being playful. Rounded type (`ROND` 100) and saturated jewel colours on true black give it the glow of a fitness ring; the copy stays estimate-only and the visuals never stage a verdict. Wear OS system pieces stay Wear OS: TimeText at the top, swipe-to-dismiss, the Tile's EdgeButton, ambient rules.

**Key Characteristics:**
- True black (#000000) everywhere; depth comes from light (tint washes) and tone (platters), never shadows.
- Five Step colours, fixed order outer to inner: Palms, Back of hands, Between fingers, Thumbs, Fingertips.
- Rounded variable numerals with tabular figures for every number that changes.
- Status as drawn shape (full disc with check, half disc, dashed outline) plus words.
- Secondary navigation as 48 dp glass circles on the bottom arc; one wide primary capsule per screen.
- A 60° gap at the top of every edge ring, so the clock is never covered.

## Colors

Jewel-saturated Apple-system hues on pure black, one cool app tint, and a fixed five-colour Step spectrum.

### Primary
- **Pool Light** (tint): the app's voice. Live countdown numerals, screen titles, the active ring, switches when on, the inline Language value, the sheet's ✓. Washes the top of most screens at 22–34 % opacity.
- **Pool Light gradient** (tint-light → tint → tint-sky, 135°): fills the primary capsule (Start, Tile EdgeButton) only. Text on it is **Deep Pool** (on-tint), never white.
- **Tint Mist** (tint-mist): the bright tail of the live ring's gradient.
- **Deep Pool Blue** (tint-deep): the "Starting a wash" Settings glyph and its screen wash.

### Secondary
- **Mint Reached** (done, done-ring, done-light): the 40 s target was reached and the "all five" praise line. The live ring turns done-ring → done-light and the numerals turn done. Mint means "time met", never "hands clean".
- **Hold Red** (stop): hold-to-stop only. The screen warms red from the centre, the ring refills in red, the stop square fills red. It never marks a Step or a result.

### Tertiary: the Step spectrum
Each Step owns a base colour and a light partner; rings draw a 135° gradient from base to light.
- **Palm Rose** (step-palms / step-palms-light): outermost ring.
- **Back Amber** (step-backs / step-backs-light).
- **Finger Lemon** (step-between / step-between-light).
- **Thumb Green** (step-thumbs / step-thumbs-light).
- **Tip Violet** (step-tips / step-tips-light): innermost ring. When Fingertips is the Focus Step its row takes a 20 % violet platter and its history line uses the light partner.

Settings hub glyph discs borrow from the same family: Back Amber for "During a wash", Deep Pool Blue for "Starting a wash", **Hub Indigo** (hub-indigo) for Language. Each Settings sub-screen washes in its hub colour; "Starting a wash" titles in **Hub Blue Text** (hub-blue-text) for contrast.

### Neutral
- **True Black** (black): every screen background, required for OLED and ambient.
- **White** (text-primary): primary text, glyphs in glass buttons, the switch thumb.
- **Secondary Grey** (text-secondary, 62 % of #EBEBF5): captions, "to go", timestamps, explanations.
- **Tertiary Grey** (text-tertiary, 38 %): the hold hint, track-only indicators, dashed no-Coverage rows.
- **TimeText White** (time-text): the clock at the top.
- **Platter** (platter) and **Raised Platter** (platter-raised): list rows, and secondary capsules (Close, "Wash in progress").
- **Hairline** (hairline): the outline of the quiet "covered Steps" row.
- **Switch Off** (switch-track-off): the switch track when off, and the EN|ES segmented track.
- **Ambient Grey** (ambient-line, ambient-text): always-on mode only; outlines at 1.5 dp, no fills.

### Named Rules
**The Never-Colour-Alone Rule.** Step status is carried by ring fill (full, half, track only), the drawn mark, and the status words. Remove all colour and every status must still read.

**The One Ring, One Colour Rule.** A Step's colour is the same on the ring, the row's glyph disc, the guide page wash, the pager dot and the credits strip. Never reassign or reuse a Step colour for anything else.

**The Tint-Washes-Down Rule.** Screen identity comes from a radial wash, `radial-gradient(130% 70% at 50% -8%, colour @ 22–34 %, colour @ 6 % 52 %, black 78 %)`, falling from the top. Hold-to-stop is the one exception: its red rises from the centre.

## Typography

**Display, Body and Label Font:** Google Sans Flex (fallback Roboto, the Wear system face)

**Character:** One variable family does everything. Numerals and titles turn the `ROND` axis to 100 for the soft, Activity-ring roundness; body text stays at the default rounding for legibility. Every changing number uses tabular figures (`tnum`), so a countdown never jitters.

### Hierarchy
- **Display** (650, 58 sp, line-height 1): the live countdown and count-up. Pool Light while counting down, Mint Reached past the target, white when timer-only.
- **Display Small** (650, 46 sp): the duration on Too short and Timer-only Summaries, where the duration is the answer.
- **Headline** (650, 28 sp): the "3/5" fraction on Start and Tile. The "/5" drops to 16 sp in Secondary Grey.
- **Headline Small** (650, 18 sp; 20 sp for "all five"): the Summary fraction in words under the rings.
- **Title** (700, 15 sp, `ROND` 100): screen titles in the tint or the screen's hub or Step colour; 16.5 sp on Step guide pages.
- **Body Large** (600, 16 sp): button labels (15.5 sp for Spanish Start), Target check Step names, sheet titles.
- **Body** (600, 15 sp): platter row labels, the live encouragement cue.
- **Body Small** (500, 14 sp): first-run intro, the guide's "how", secondary values.
- **Label Large / Label / Label Small** (400, 13 / 12 / 11 sp, Secondary Grey): "to go", status words beside marks, timestamps, footnotes, day headers.
- **TimeText** (600, 12.5 sp, `tnum`): the system clock, top centre.

### Named Rules
**The Rounded Numbers Rule.** Anything that counts (timers, fractions, durations) is set in `ROND` 100 with `tnum`. Prose never is.

**The Both-Languages Rule.** Every line must fit inside the round edge in English and Spanish at the documented size. The mockup's fit checker is the test; shrink by at most 0.5 sp for one language, otherwise rewrite the string.

## Layout

The canvas is a 227 dp circle, drawn 1:1. Content is centred on the vertical axis and narrows as it nears the edge: text blocks sit at 146–180 dp wide, platter rows at 198 dp, and lists pad 26–30 dp from the top so the first row clears TimeText (6 dp from the top).

- **Edge ring:** 300° of arc from 1 o'clock clockwise to 11 o'clock (rotated −60°), inset 5 dp from the bezel, 12 dp stroke, round caps. The 60° top gap clears the clock.
- **Bottom arc:** secondary actions live here as glass circles (48 dp, 7 dp apart, centred) or as the sheet's ✕ / ✓ pair (50 dp). The primary capsule sits above them, centred, 179 dp × 50 dp.
- **Lists:** platter rows stacked with a 6 dp gap; 10 dp between a row's glyph and text; a crown scroll indicator on the right arc (±28° around 3 o'clock, 4 dp); a 40 dp fade to black at the bottom (and at the top once scrolled).
- **Pager:** vertical dots on the right edge, 9 dp in: 5 dp dots, the active one stretched to 13 dp in its Step colour.
- **Targets:** every touch target is at least 48 dp; one primary action per screen.

## Elevation & Depth

Flat by default, lit from above. There are no structural shadows on the watch: depth comes from the tint wash behind the content, the two platter tones (platter, then platter-raised for anything pressable that is not primary), and translucency on glass buttons (`rgba(255,255,255,.14)`, an 8 dp backdrop blur where the platform can afford it, otherwise the flat fill).

### Shadow Vocabulary
- **Lap head** (drop shadow, 0 offset, 2.4 dp blur, black at 75 %): only on the second lap of the live ring past the 40 s target, so the overlapping head reads above the first lap, as an Activity ring does past 100 %.
- **Switch thumb** (0 1dp 3dp, black at 40 %): the white thumb on the switch track.

### Named Rules
**The Light-Not-Lift Rule.** Never raise an element with a shadow to make it important. Make it brighter (tint), bigger, or give it the screen's wash.

## Shapes

Everything is round or pill-shaped, echoing the screen itself.
- **Circles:** glyph discs (32–46 dp), glass buttons, the sheet's ✕ / ✓, rings, status marks, pager dots.
- **Capsules** (fully rounded): primary and secondary buttons, switch tracks, the EN|ES segmented control.
- **Platters** (24 dp radius): list rows, so a 50 dp row reads almost as a capsule.
- **The stop square** (26 dp, 7 dp radius) inside a 64 dp red disc: the one squared form, borrowed from the universal stop symbol.
- **Tile EdgeButton:** the Wear OS EdgeButton silhouette (24 dp top corners, deep curve following the bezel).
- **Strokes:** rings use round caps, always. Dashed strokes (2.2 / 1.8) mean "not detected" or "no Coverage"; they never decorate.

## Components

### Buttons
Confident and few: one bright capsule, everything else recedes.
- **Primary capsule:** Pool Light gradient, Deep Pool label with a 20 dp drop glyph, fully rounded, 179 × 50 dp. Start on Start; Start / Open wash on the Tile EdgeButton.
- **Running capsule:** Raised Platter with a Pool Light label and a 9 dp tint dot. It replaces Start while a Wash runs in the background.
- **Secondary capsule:** Raised Platter, white label, 132 × 48 dp (Close on short Summaries).
- **Glass button:** 48 dp translucent circle, 24 dp white glyph, on the bottom arc (History, Settings, sheet ✕).
- **Confirm circle:** 50 dp Pool Light circle with a Deep Pool check (the sheet's ✓).
- **Press:** use Wear Material 3's press feedback; no hover states exist on the watch.

### Platter rows
- **Default:** Platter fill, 24 dp radius, 8 × 14 dp padding, 198 dp wide, min 50 dp tall (54–56 dp when the row carries a subtitle or a glyph disc).
- **Leading:** a 32 dp glyph disc (Step colour, or a hub colour with a white glyph), or tiny rings (30 dp) in History.
- **Focus Step:** 20 % Tip Violet (or the Focus Step's colour) platter, plus a Label Small line in the light partner.
- **Quiet:** transparent with a Hairline outline: the folded "covered Steps" line on Summary, Credits.
- **Dimmed:** 62 % opacity with a dashed 32 dp circle: History Washes with no Coverage.

### Switches
- **Track:** 40 × 24 dp capsule; Switch Off grey, Pool Light when on. **Thumb:** 20 dp white disc, 2 dp inset.
- Toggles take the app tint, never iOS green, and stay Wear switch-buttons in code; the row label owns the width so long Spanish labels fit.

### Status marks
11 dp drawn marks beside the status words, in the Step's colour: **Looks covered** is a full disc with a black check; **May have been brief** is an outline with the right half filled; **Not detected** is a dashed outline.

### Five Rings (signature)
Concentric Activity rings, outer = Palms, inner = Fingertips. Each ring has a 20 % track in its colour and a round-capped fill: full for Looks covered, half for May have been brief, track only for Not detected, starting at 12 o'clock. Proportions scale with size: 108 dp (7.4 stroke, 2 gap) on Summary, 62 dp (5.4 / 1.5) on Start and Tile, 46 dp (3.6 / 1) in the complication, 30 dp (2.6 / 0.9) in History. Empty rings never appear on their own: a first-run Start shows the drop and one line instead, because empty rings read as a score of zero. All five closed adds a ring of ten droplets in the Step colours.

### Live ring (signature)
The 300° edge ring (see Layout). Counting down: Pool Light gradient over a 16 % tint track. Past 40 s: it turns mint and keeps lapping itself, the second lap shadowed with a round head. Timer-only: a 2.5 dp white hairline. Target check: a 3 dp tint hairline that drains the screen closed in 6 s. Ambient: a 1.5 dp grey outline, nothing that moves.

### Hold to stop
Holding anywhere warms the screen red from the centre, refills the ring in red over the tint arc, and shows the stop square in its disc with one line ("Hold to stop", or the early-stop nudge before the target). On commit the ring goes full red at 14 dp and the square breathes while the Wash saves.

### Sheet
A two-option sheet replaces dialogs: a glyph, a title (Body Large), one line of explanation, then glass ✕ and tint ✓ on the bottom arc. The screen washes in the subject's colour (Finger Lemon for the battery warning).

### Navigation
Start is the root. History and Settings open from the glass buttons; the Step guide pages vertically with the crown; swipe right is back everywhere. Nav stays at most two levels deep.

## Do's and Don'ts

### Do:
- **Do** keep every background True Black (#000000) and let the screen's wash supply its identity.
- **Do** keep the 60° gap at the top of every edge ring.
- **Do** pair every Step status with its drawn mark and its words.
- **Do** set every counting number in `ROND` 100 with tabular figures.
- **Do** keep the Step order and colours fixed: Palms rose, Back of hands amber, Between fingers lemon, Thumbs green, Fingertips violet, outer to inner.
- **Do** check every string in English and Spanish against the round edge before shipping.
- **Do** keep Wear OS system pieces native: TimeText, swipe-to-dismiss, EdgeButton, ambient.

### Don't:
- **Don't** encode status by colour alone, or use Hold Red or Mint for Step status.
- **Don't** show empty rings as a result; zero rings read as a failing score.
- **Don't** add drop shadows or cards to create hierarchy; the only shadows are the lap head and the switch thumb.
- **Don't** put more than one primary capsule on a screen or more than two options on a sheet.
- **Don't** use white text on the Pool Light gradient; use Deep Pool.
- **Don't** use iOS green for switches, or port iOS controls; the grammar is watchOS, the components stay Wear Material 3.
- **Don't** animate anything in ambient mode, or show seconds there.
