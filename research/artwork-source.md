# Where the Step artwork comes from

Research ticket 08 (`.scratch/coaching/issues/08-artwork-source.md`), captured 2026-10-18. HapticWash is a
hygiene coach, not a medical device. This is licence research, not legal advice.

## TL;DR

- **CDC posters: licence fine, artwork wrong.** The current CDC poster is a five-step "wet, soap, scrub,
  rinse, dry" graphic. "Scrub" is one pair of clapping hands. None of the five HapticWash poses exist in it.
  It is also a CDC Foundation / GOJO / Staples campaign piece, so public-domain status is not certain.
- **Generic icon sets (Material Symbols, Font Awesome Free, Tabler): licence fine, poses missing.** They have
  hand, wash and sanitizer glyphs. None has interlaced fingers, thumb rotation or fingertips-in-palm.
- **Wikimedia Commons "Hand Disinfection according to DIN EN 1500" (Hegasy), CC BY-SA 4.0: the only found
  source that draws all five poses.** Realistic hands, full colour. Thumbs and Palms read at ~100 px, but Back of
  hands and Between fingers are nearly the same picture. ShareAlike applies to the cropped images (see below).
- **Commissioning original art: the only route that guarantees all five poses, a style built for ~100 px and
  clean terms.** Needs a signed assignment or licence in writing. Cost and time not researched.

Suggested answer for 08: commission, with the Hegasy sheet as a pose reference for the brief. Fallback if nothing
is commissioned: ship the five Hegasy crops with BY-SA attribution and accept the Back of hands / Between fingers
weakness.

## Poses to cover

From 02: Palms (WHO 2, "palm to palm"), Back of hands (WHO 3, palm over dorsum, fingers interlaced), Between
fingers (WHO 4, palm to palm fingers interlaced), Thumbs (WHO 6, rotational rubbing), Fingertips (WHO 7, clasped
fingers rotated in opposing palm). WHO 5 (backs of fingers) is not a HapticWash Step.

## Source 1: CDC posters and graphics

**Licence.** CDC "Use of Agency Materials" (<https://cdc.gov/other/agencymaterials.html>), quoted:
- Most content "is in the public domain, and may be freely used or reproduced without obtaining copyright
  permission."
- "Attribution to the agency that developed the material must be provided."
- A disclaimer that use "does not imply endorsement by CDC, ATSDR, HHS or the United States Government" must be
  "prominently and unambiguously displayed."
- CDC, ATSDR and HHS logos "may not be used without express written permission".
- Exception: some materials "are restricted in their use because they were developed by government contractors
  or grantees," or "have been licensed by a third party."

This matches the conditions already recorded in 02.

**Five poses?** No. I downloaded the current poster
(<https://www.cdc.gov/clean-hands/media/pdfs/2025/09/320599-D_Lundak_5StepsHandwashPoster-Update_1.pdf>, listed on
<https://www.cdc.gov/handwashing/materials.html>) and rendered it. It shows five panels: Wet (tap), Get Soap,
Scrub (one pair of hands with bubbles), Rinse, Dry. No back of hands, between fingers, thumbs or fingertips. The
materials page lists only that poster, a key-times poster and social graphics.
- The footer says "The Life is Better with Clean Hands campaign is made possible by a partnership between the CDC
  Foundation, GOJO, and Staples", and the file name carries an illustrator name. That is the contractor/third-party
  exception above, so the public-domain claim for this specific artwork would need confirming with CDC.
- The materials page states no licence of its own ("Partners, organizations, and individuals can download the
  resources below").
- Public Health Image Library items I found are photographs (kitchen sink, children washing); several say public
  domain, none show the five poses.

**Legible at ~100 px?** Moot, since the poses are absent. The poster's CDC logo and campaign text would also need
removing, which conflicts with 02's "keep substance unchanged" condition.

## Source 2: permissive icon sets

| Set | Licence (primary) | Relevant glyphs | All five poses? |
|---|---|---|---|
| Material Symbols (Google) | Apache 2.0 (<https://developers.google.com/fonts/docs/material_symbols>, <https://github.com/google/material-design-icons>); attribution "not required" per the repo README | `back_hand`, `front_hand`, `clean_hands`, `wash`, `sanitizer`, `pinch`, `hand_gesture`, `thumb_up`, `fingerprint` (checked against the Google Fonts icon metadata, 6128 entries) | No |
| Font Awesome Free 6 | Icons CC BY 4.0, fonts SIL OFL 1.1, code MIT (<https://raw.githubusercontent.com/FortAwesome/Font-Awesome/6.x/LICENSE.txt>). CC BY needs credit | `hand`, `hands-bubbles`, `hand-sparkles`, `hand-back-fist`, `hand-dots`, `soap`, `hands-praying`, `hands` (file list of `svgs/solid` in the 6.x repo) | No |
| Tabler Icons | MIT (<https://github.com/tabler/tabler-icons>) | `wash-hand`, `hand-stop`, `hand-finger`, `hand-sanitizer`, `hand-three-fingers`, `hand-grab`, `hand-move` (file list of `icons/outline`) | No |

None has two hands interlaced, a thumb clasped in a fist, or fingers rubbing a palm. The poses are the relation
between two hands, which a single-object glyph does not carry.

**Legible at ~100 px?** Yes, icons are designed for 24 px. But they only say "hand", "washing" or "sanitizer", so
they cannot tell the five Steps apart. Fine as a generic header glyph, not as the Step art.

Not checked: the Noun Project (its licensing page returned 404 to my fetch). Its icons are per-artist, with mixed
CC BY and paid licences.

## Source 3: Wikimedia Commons illustrations

I searched Commons by API for hand hygiene, hand rub, handwashing and EN 1500 terms and read each hit's licence.

- **Found: "Hand Disinfection according to DIN EN 1500", by Hegasy (hegasy.de), CC BY-SA 4.0.**
  - Files: EN `File:04_Hegasy_Hand_Disinfection_Wiki_EN_CCBYSA.png`
    (<https://commons.wikimedia.org/wiki/File:04_Hegasy_Hand_Disinfection_Wiki_EN_CCBYSA.png>, 3508 x 2480,
    author field "Guido4", own work). There are also a plain, a DE and an ES version; the plain one lists a
    different uploader ("Raimundo Pastor") crediting hegasy.de. The author of record is unclear, so confirm on the
    file page before shipping.
  - Licence (<https://creativecommons.org/licenses/by-sa/4.0/deed.en>): use, even commercially, allowed. You must
    give credit (creator, link to the material, link to the licence, licence notice, copyright and disclaimer
    notices) and "indicate whether you modified the material". If you "remix, transform, or build upon" it you
    "must distribute your contributions under the same license". The deed is a summary; the legal code governs.
  - **Poses:** all of them, as separate panels around a central hand: palm to palm; hand over back with fingers
    interlaced; palm to palm fingers interlaced; backs of fingers to opposing palms; thumb rotation; fingertips
    rotating in palm; plus a wrist panel and a dispensing panel. The poses are EN 1500, a method, which matches 02.
    The panel-to-Step mapping is from my reading of the image, so check it.
  - **ShareAlike in an app:** cropping a panel is a modification, so each cropped image is "adapted material" and
    must be offered under BY-SA 4.0 with the changes noted. Whether that spreads to the app code is a legal
    question I could not settle (the CC FAQ passage on collections was truncated when fetched). CC says BY-SA 4.0
    is one-way compatible with GPLv3 and that "our licenses are currently not compatible with the major software
    licenses". The cautious design keeps the images as separate asset files with their licence notice in an About
    screen and does not mix them into code.
- Also seen and rejected:
  - `Steps to hand washing.png` (CK-12 Foundation, Laura Guerin, CC BY-SA 3.0): the generic wet / soap / scrub /
    rinse / dry sequence, one "scrub" panel. No five poses.
  - `Steps on How to wash your hands Steps - Infographic.png` (CC0 by the uploader): a collage of Pexels and
    Pixabay photos, and its CC0 label does not clearly cover the photographers' work. Generic steps. Not usable.

**Legible at ~100 px?** I cropped the five panels (rough crops, background bleeds in) and shrank them to 100 px:
- Palms: readable (two hands pressed together).
- Thumbs: clearest. A thumb clasped in a fist against an open hand is distinctive even at 100 px.
- Fingertips: readable as one hand rubbing a palm, but the forearms in the frame shrink the pose.
- Back of hands and Between fingers: almost the same silhouette (two hands overlapping with spread fingers). At
  100 px I could not reliably tell them apart without the arrow or a caption.
- The style is realistic hands with thin outlines in skin tones on a grey panel with a small arrow. Expect to
  remove the panel and arrows for a dark round screen, which is another modification to record. This was a desktop
  check, so on-device viewing is still needed.

## Source 4: commission original art

- **Licence terms:** whatever the contract says, so write it down. Under US law a commissioned work is "work made
  for hire" only if it falls in one of nine listed categories and the parties sign a written agreement saying so
  (US Copyright Office Circular 30, <https://www.copyright.gov/circs/circ30.pdf>). A stand-alone illustration is
  not obviously in the nine, so use a signed written assignment, or an exclusive licence with the right to modify
  and ship in the app. A drafting detail for whoever commissions, not legal advice.
- **Five poses:** yes by design. The brief can force what 04 found missing: two clearly different silhouettes for
  Back of hands and Between fingers. Brief = the pose list above, WHO / EN 1500 as the motion reference, "don't
  trace WHO's drawings" (02), no text or arrows in the art.
- **Legible at ~100 px:** controlled by the brief: bold, simplified, high-contrast, checked on a round AVD at the
  layout B (04) size. This is what the Hegasy realistic style cannot fix and the 04 hand-drawn line art did not
  reach.
- **Not researched:** price, turnaround, marketplace. Needs the owner's budget before it is a real option.

## Comparison

| | Licence workable offline in app? | All five poses | Legible at ~100 px | Main risk |
|---|---|---|---|---|
| CDC poster | Yes (credit, disclaimer, no logo); contractor status unchecked | No | n/a | Wrong content |
| Material / FA / Tabler | Yes (Apache / CC BY / MIT) | No | Yes, but generic | Cannot tell Steps apart |
| Hegasy (Commons) | Yes, BY-SA 4.0 with attribution; ShareAlike on the images | Yes (one sheet, EN 1500) | Partly: Thumbs good, Back of hands vs Between fingers weak | ShareAlike, print-style art |
| Commissioned | Yes, with a signed assignment | By brief | By brief | Cost, time |

## Open items

- Confirm the Hegasy panel-to-Step mapping and the author of record on the Commons file page.
- Legal read on whether BY-SA images bundled in an Android app affect the app's own licence.
- Test any shortlisted art on a round AVD at layout B size.
- Budget and a brief if commissioning.
