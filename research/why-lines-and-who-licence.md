# Why lines per Step, and WHO poster licence

Research ticket, captured 2026-10-17. HapticWash is a hygiene coach, not a medical device. Nothing below
should be read as a claim that hands are clean, that any illness is prevented, or as legal advice.

## TL;DR

- **Why lines:** each of the five Steps maps to one WHO rubbing motion. Missed-area data does exist, but
  it comes from a few studies of health-care workers and the studies **disagree** (fingertips especially).
  So every line below names "one study" rather than stating a universal fact.
- **Licence:** the WHO "How to Handrub" / "How to Handwash" posters are dated **May 2009**. They are
  **not** under WHO's CC BY-NC-SA 3.0 IGO licence (that licence only covers publications from 12 Nov 2016,
  and WHO will not reissue older works under it). The 2009 material is "© WHO 2009, All rights reserved".
  **Bundling the posters as-is, or redrawing/adapting them, is not allowed without WHO's written permission.**
  **Fallback: original line art of the hand poses is fine.** The technique is a method, and copyright does
  not cover methods. Don't trace WHO's drawings, don't use the WHO name or emblem, and don't imply WHO
  endorses the app.

---

## Part 1: what each Step reaches, and how often it is missed

### WHO's own description of the motions (primary)

The WHO poster captions, taken verbatim from the May 2009 "How to Handrub?" PDF, are the same as Figure II.1
(p. 155) in the WHO Guidelines on Hand Hygiene in Health Care 2009:

| WHO # | Caption (verbatim) | HapticWash Step |
|---|---|---|
| 1a/1b | "Apply a palmful of the product in a cupped hand, covering all surfaces;" | (not a Step) |
| 2 | "Rub hands palm to palm;" | **Palms** |
| 3 | "Right palm over left dorsum with interlaced fingers and vice versa;" | **Back of hands** |
| 4 | "Palm to palm with fingers interlaced;" | **Between fingers** |
| 5 | "Backs of fingers to opposing palms with fingers interlocked;" | *no HapticWash Step* |
| 6 | "Rotational rubbing of left thumb clasped in right palm and vice versa;" | **Thumbs** |
| 7 | "Rotational rubbing, backwards and forwards with clasped fingers of right hand in left palm and vice versa;" | **Fingertips** |
| 8 | "Once dry, your hands are safe." | (do not reuse; see below) |

Sources: poster PDF as mirrored by Univ. of Washington,
<https://depts.washington.edu/edgh/app-ipc/web/project-resources/How_To_HandRub_Poster.pdf> (the original
`www.who.int/gpsc/5may/How_To_HandRub_Poster.pdf` now returns 404). Guidelines:
<https://www.who.int/publications/i/item/9789241597906> (ISBN 978 92 4 159790 6), Figure II.1 p. 155 and
Figure II.2 p. 156.

Two points for the product:
- WHO motion 5 ("backs of fingers") has no HapticWash Step. The Back of hands Step's motion is
  WHO #3 (palm over dorsum), so don't claim the app covers the backs of the fingers.
- The posters end with "Once dry, your hands are safe." / "Your hands are now safe." Those are exactly the
  "hands are clean" claims HapticWash must not make. That alone is a reason not to ship the posters unedited.

### Missed-area evidence (primary where reachable)

| Source | Population / method | Findings by area |
|---|---|---|
| **Taylor LJ.** An evaluation of handwashing techniques, parts 1 and 2. *Nurs Times* 1978;74(2):54-55 (PMID [622326](https://pubmed.ncbi.nlm.nih.gov/622326/)) and 74(3):108-110 (PMID [622335](https://pubmed.ncbi.nlm.nih.gov/622335/)) | Nurses' handwashing | **Original text not available online** (no abstract on PubMed). WHO 2009 Guidelines, Part I §16.1, p. 66, cite it (ref. 611) as follows: "HCWs often failed to cover all surfaces of their hands and fingers." The popular "most missed areas" diagram attributed to Taylor (fingertips, thumbs, between fingers) appears only in secondary reproductions. I could not verify it against the original. |
| **Škodová M et al.** Hand hygiene assessment in the workplace using a UV lamp. *Am J Infect Control* 2015;43(12):1360-2. doi:[10.1016/j.ajic.2015.07.003](https://doi.org/10.1016/j.ajic.2015.07.003), PMID [26297523](https://pubmed.ncbi.nlm.nih.gov/26297523/) | 705 health-care workers, Badajoz (Spain), fluorescent handrub + UV, no prior training | Abstract: "All 705 participants did the World Health Organization sequence for HH correctly, but only 9.5% actually achieved the highest rating (ie, all hand areas with hand rub distribution)." Per-area **complete coverage**, as reported by [European Cleaning Journal, 21 Sep 2015](https://www.europeancleaningjournal.com/magazine/articles/latest-news/healthcare-workers-miss-key-areas-when-using-hand-sanitisers) (the full text is paywalled; its n=705 and 9.5% figures match the abstract): thumbs **37.45%**, fingertips **44.54%**, back of hands **46.38%**, between fingers **~75%** ("three-quarters"), palms **~99%** (98.87%). |
| **Szilágyi L et al.** A large-scale assessment of hand hygiene quality and the effectiveness of the "WHO 6-steps". *BMC Infect Dis* 2013;13:249. doi:[10.1186/1471-2334-13-249](https://doi.org/10.1186/1471-2334-13-249), [PMC3689096](https://pmc.ncbi.nlm.nih.gov/articles/PMC3689096/) | 4642 records, clinical staff, Singapore, UV imaging **immediately after WHO 6-step training** | "only 72% of staff achieved satisfactory coverage. Failure to adequately clean the dorsal and palmar aspects of the hand occurred in 24% and 18% of the instances, respectively. Fingertips were missed by 3.5% of subjects." Most-missed sites: "the dorsal side of fingers in the proximity of the nails (16%)" and "the thenar eminence and in the proximity of the wrist crease (18%)". |
| **Kampf G et al.** Influence of rub-in technique on required application time and hand coverage in hygienic hand disinfection. *BMC Infect Dis* 2008;8:149. doi:[10.1186/1471-2334-8-149](https://doi.org/10.1186/1471-2334-8-149), [PMC2600642](https://pmc.ncbi.nlm.nih.gov/articles/PMC2600642/) | Fluorescent rub, UV, EN 1500 six steps vs. variants | Reference procedure: "53% of subjects with at least one untreated area". "Fingertips and palms were often covered completely." |
| **Widmer AF, Dangel M.** Alcohol-based handrub: evaluation of technique and microbiological efficacy with international infection control professionals. *Infect Control Hosp Epidemiol* 2004;25(3):207-9. doi:[10.1086/502379](https://doi.org/10.1086/502379) | 60 infection-control professionals, fluorescent dye | "Technique is of crucial importance ... Major deficiencies were detected among even highly trained HCWs." (No per-area numbers in the abstract.) |

**Reading the evidence.** Palms are almost never missed (Škodová ~99%; Kampf). Thumbs and the back
of the hand are commonly missed (Škodová, Szilágyi dorsal 24%). **Fingertips conflict.** Untrained staff
missed them often (Škodová: 55% didn't fully cover them), while staff tested straight after training rarely
did (Szilágyi: 3.5%). Kampf found them "often covered completely". So "the most often missed spot" is
**not** supportable for fingertips. In Škodová it's the **thumbs**.

**"Touches your face and food"**: the closest primary source is Kwok YL et al., *Am J Infect Control*
2015;43(2):112-4, doi:[10.1016/j.ajic.2014.10.015](https://doi.org/10.1016/j.ajic.2014.10.015). It
observed 26 students touching their faces "23 times per hour". But it measures *hand*-to-face contact, not
fingertips specifically, and it frames the result in infection terms. I found no source for "food". Don't use
that line.

### Candidate why lines (≤ ~60 chars, no illness or prevention claims)

| Step | Candidate line | Chars | Source |
|---|---|---|---|
| Palms | `Palms: rarely missed, but it's only the first Step` | 50 | Škodová 2015 (~99% covered, via ECJ); WHO poster (Palms is #2 of 7 rubbing motions) |
| Back of hands | `Back of hands: over half missed a patch in one study` | 52 | Škodová 2015: 46.38% complete → ~54% incomplete |
| Between fingers | `Between fingers: about 1 in 4 missed it in one study` | 52 | Škodová 2015: ~75% complete |
| Thumbs | `Thumbs: the most-missed spot in a 705-person study` | 50 | Škodová 2015: 37.45% complete, lowest of all areas |
| Fingertips | `Fingertips: about half missed some in one study` | 47 | Škodová 2015: 44.54% complete. **Caveat:** Szilágyi 2013 found only 3.5% missed after training |

Shorter fallbacks with no statistic (sourced only to the WHO poster's wording for each motion):
`Palms: palm to palm` · `Back of hands: palm over the back` · `Between fingers: interlace them` ·
`Thumbs: twist each thumb in a fist` · `Fingertips: rub them in the other palm`.

Caveat for every statistical line: Škodová studied health-care workers in one hospital, using handrub, with no
recent training. The per-area numbers come from press coverage of a paywalled paper. Before shipping,
check them against the full text (doi above).

---

## Part 2: licence for the WHO posters (and the CDC comparison)

### The WHO posters are pre-2017, so "All rights reserved"

- WHO open-access policy, <https://www.who.int/about/policies/publishing/open-access>: WHO publications
  have used CC BY-NC-SA 3.0 IGO since **12 November 2016**, and "WHO publications published prior to 2017
  will not be reissued under the CC BY-NC-SA 3.0 IGO licence". WHO "will continue to encourage their reuse
  for non-commercial educational and research purposes".
- The posters are dated **May 2009**. The text layer of the copy I fetched carries no © line. Third-party
  reproductions credit them as "© World Health Organization 2009" (e.g. Victoria Dept of Health,
  <https://www.health.vic.gov.au/sites/default/files/migrated/files/collections/policies-and-guidelines/r/racf-8-instructions--hand-hygiene.pdf>). The
  2009 Guidelines, which contain the same figures (II.1/II.2), say on the verso of the title page:
  > "© World Health Organization 2009. All rights reserved. ... Requests for permission to reproduce or
  > translate WHO publications – whether for sale or for noncommercial distribution – should be addressed to
  > WHO Press ... (e-mail: permissions@who.int)."
- Poster footer (verbatim): "WHO acknowledges the Hôpitaux Universitaires de Genève (HUG), in particular the
  members of the Infection Control Programme, for their active participation in developing this material."
  WHO holds the copyright; HUG is acknowledged, not credited as source.

### What the CC BY-NC-SA 3.0 IGO terms would say (they apply to post-2016 WHO works, not these posters)

From <https://www.who.int/about/policies/publishing/copyright> and the
[legal code](https://creativecommons.org/licenses/by-nc-sa/3.0/igo/legalcode):
- Attribution: "[Title]. [Place]: World Health Organization; [Year]. Licence: CC BY-NC-SA 3.0 IGO."
- Non-commercial: §4(c) bars use "primarily intended for or directed toward commercial advantage or private
  monetary compensation". WHO: "Permission is required for commercial uses".
- Adaptations: must carry "This adaptation was not created by WHO. WHO is not responsible for the content or
  accuracy of this adaptation", must be "clearly label[led]" as changed (§3(b)), and must be ShareAlike (§4(b)).
- Endorsement: "may not implicitly or explicitly assert or imply any connection with, sponsorship or
  endorsement by the Licensor" (§4(d)). WHO: "WHO publications cannot be used to promote or endorse products,
  services or any specific organization."
- Third-party content and figures: WHO's copyright page also says figures, tables, maps and photos generally
  need permission.

### WHO's permissions process

- <https://www.who.int/about/policies/publishing/permissions>: no permission is needed for CC material
  "subject to reuse being for non-commercial use only and outside of a commercial environment/context".
  Otherwise, submit the form at <https://who.appianportals.com/copyright-request> with "the maximum amount of
  detail" and a direct link to the material. You then get an email with a reference ID.
- If WHO grants permission, its terms apply,
  <https://www.who.int/about/policies/publishing/copyright/terms-and-conditions>:
  - "You may not use the Licensed Materials in association with any product marketing, promotional, or
    commercial activities"
  - "you may minimally alter or adapt figures and tables ... to match the style of your publication. Any
    other alteration or modification ... only with the prior written authorization of WHO."
  - "You shall not state or imply that WHO endorses or is affiliated with your publication"
  - "In no case shall you use the WHO name or emblem", except as an integral, non-separated part of
    the material.
  - Acknowledge WHO in a footnote or reference list, with the URL and access date for web material.
- WHO emblem, <https://www.who.int/about/policies/publishing/logo>: "it may only be used with express written
  permission"; its "use implies endorsement by WHO"; "the use of the WHO emblem on non-WHO websites is
  normally not allowed".

### CDC comparison

<https://www.cdc.gov/other/agencymaterials.html>:
- "Most of the information on the CDC and ATSDR websites is not subject to copyright, is in the public
  domain". The exceptions are contractor- or grantee-made material, third-party licensed content, and some
  images: "Images other than those in the PHIL may have been licensed for use by CDC", and the licence holder
  may prohibit "republication, retransmission, reproduction or other use".
- Conditions: "Attribution to the agency that developed the material must be provided" (e.g. "Source: CDC").
  Include a prominent non-endorsement disclaimer. "You may not change the substantive content of the
  materials".
- Logos "may not be used without express written permission from CDC, ATSDR and/or HHS".

So a CDC hand-washing poster can usually be bundled with attribution and a disclaimer, unedited in
substance, with no logo. Check each poster's image credits for licensed stock photos first.

### Copying the poses themselves

- Copyright covers expression, not methods. US 17 U.S.C. §102(b) (<https://www.copyright.gov/title17/92chap1.html>):
  "In no case does copyright protection ... extend to any idea, procedure, process, system, method of
  operation ... regardless of the form in which it is described, explained, illustrated, or embodied". TRIPS
  Art. 9(2) (<https://www.wto.org/english/docs_e/legal_e/27-trips_04_e.htm>): "Copyright protection shall extend
  to expressions and not to ideas, procedures, methods of operation or mathematical concepts as such."
- The rub-in steps also predate WHO. Kampf 2008 describes the "EN 1500 reference procedure, which consists of 6
  specific rub-in steps". (The EN 1500 steps are commonly traced to Ayliffe et al., *J Clin Pathol*
  1978;31:923-8, doi:[10.1136/jcp.31.10.923](https://doi.org/10.1136/jcp.31.10.923), but that abstract
  doesn't describe the steps. Not verified.)
- The risk lies in the drawing, not the pose. Tracing WHO's art, or copying its line style, colours, numbering
  layout or composition closely enough to be "substantially similar", would be copying expression. Draw from
  real hands or reference photos you own.

## Conclusion

- **Bundle WHO posters as-is: not allowed** without WHO's written permission. They're © 2009 All rights
  reserved, not CC. Even with permission, WHO's terms bar commercial or promotional context and the WHO
  emblem. The posters' "your hands are safe" line also breaks HapticWash's own copy rules.
- **Adapt or redraw from the WHO posters: not allowed** without WHO's prior written authorization.
- **CDC posters: allowed with conditions.** Attribute, add a disclaimer, don't change substance, no logo,
  and check third-party image credits.
- **Fallback: original line art showing the same poses: allowed.** The technique is an uncopyrightable
  method. Don't trace WHO art, don't use the WHO name or emblem, and don't imply WHO endorsement. A factual
  line such as "Steps follow the WHO hand-rub technique" is a reference, not an endorsement. Keep it that way.
