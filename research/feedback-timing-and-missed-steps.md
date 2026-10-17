# Feedback timing and commonly missed Steps

Research ticket: should HapticWash add one live **Target check**? At the 40 s target, if any
Step is `NOT_DETECTED`, the watch gives one haptic nudge and names the undetected Steps while
the hands are still soapy. The core spec currently bans live step-by-step prompting because
"live prompting changes user behaviour and invalidates the model's training distribution".

Researched 2026-10-17. Each claim is tagged **[P]** (read in the primary source: full text or
abstract) or **[S]** (only from a secondary summary). Inferences that are mine, not a source's,
are marked *Inference*.

## TL;DR

1. **Live feedback works, but it works only while it is switched on.** The best study of live,
   per-step feedback at a sink (Lacey 2020) raised full WHO-technique washes from 15.7 % to
   46 % (Hedges g = 1.54). Once the feedback was removed, performance went back to baseline
   (13.7 %). Wearable live feedback (Pires 2021, a stepped-wedge RCT) made people rub longer and
   use more product, but did not change compliance. Showing missed areas *right after* the rub
   (Lehotsky 2015) cut inadequate rubs from 50 % to 15 %. No study compares a single prompt at the
   end of the target time with a post-wash summary. That head-to-head evidence does not exist.
2. **Thumbs and Fingertips are the Steps people most often skip.** Back of hands is the area
   they most often leave uncovered, even when they do the step. Direct observation of the WHO
   steps shows that compliance falls sharply after the first two or three steps, and the
   thumbs and fingertips steps are the lowest. Fluorescent-coverage studies show the *dorsal*
   side (back of hands, backs of fingers, thumb base) is missed most.
3. **There is almost no evidence that prompting hurts sensor-based recognition.** The one
   relevant test (UWash "random" condition) suggests that a model trained on trained WHO
   washers still works when people reorder or skip steps. Recognisers that build in a fixed step
   order (WristWash's HMM grammar) would be hurt. The spec's concern is plausible but untested.
   It is not an established finding.

## (a) Feedback during the wash vs after it

| Study | When feedback is given | Outcome | Effect |
|---|---|---|---|
| Lacey et al. 2020, AJIC, automatic video auditing at 8 sinks in a surgical unit, 3,606 washes [P] | **During**: the screen shows each WHO step as a red/green light that turns green when the step is done | % of washes that met the full WHO protocol | 15.7 % (95 % CI 10.6–20.9) → **46.0 %** (43.8–48.1), P < .0001. Washes per patient-day went from 0.91 to 2.25. Hedges g = 1.54. **Back to 13.7 % (5.9–21.5) once feedback was turned off.** The authors suspect "cognitive offloading" onto the device. A weekly team report added nothing. |
| Pires et al. 2021, JAMA Netw Open, stepped-wedge cluster RCT, 97 HCWs, 12 wards, 6,878 opportunities [P] | **During**: the wristband vibrates after 15 s of rubbing, and the bottle vibrates once enough handrub volume has been dispensed | 5 Moments compliance, rub duration, rub volume | Compliance did not change: adjusted OR 1.03 (0.75–1.42). Median duration went from 6.5 s to **8 s**. The share of rubs reaching the correct duration went from 11.4 % to **26.9 %**, and the share with the correct volume from 10.2 % to 30.5 %. **Wearing the device with feedback switched off did not change compliance** (67.9 % vs 66.6 % at baseline). |
| Lehotsky et al. 2015, J Hosp Infect, 136 HCWs, 3 hospitals [P, abstract] | **Immediately after**: a UV image shows the areas the person missed | % of inadequate rubs | **50 % → 15 %** (P < .001). The authors say keeping this gain probably needs the device to stay in use. A companion abstract reports missed spots falling from 1.5 to 0.35 after 5 rubs ([P288](https://doi.org/10.1186/2047-2994-4-S1-P288)). |
| Stewardson et al. 2014, PLoS One, SureWash, controlled before–after study, 63 HCWs [P, abstract] | **During** (training kiosk with immediate feedback on each pose) | Complete 6-pose action of ≥ 20 s, and number of poses per action | No effect on complete actions: 0 at baseline and 1 participant afterwards. Median poses per action went from 2.0 to **3.8** (vs 2.0 in controls, p < .001), and was still 3.0 at 12 weeks. Device-maker conflict of interest. |
| Zwicker et al. 2023, J Hosp Infect, ICU transponder [P, abstract] | During (a reminder at the moment hand hygiene is due) | Adherence (whether people did hand hygiene, not how well) | Up to **+104.5 %** over baseline. **Fell to or below baseline once the intervention stopped.** |
| Levchenko et al. 2013, CIN, 14 nurses [P, abstract] | During (live prompting signals) | Compliance | Significantly higher only in the phase with live prompting turned on. No effect size in the abstract. |
| iWash (Samyoun et al. 2021, Smart Health), smartwatch [P] | **After**: a voice message at the end lists each missed step | n/a | **No behavioural outcome measured.** The 14-participant deployment reports no results. |
| UWash (Wang et al. 2025, IEEE TMC, local PDF 2112.06657v5) [P] | After (a score) | n/a | Feedback is suggested but not tested. Live corrective guidance is listed as future work. |

Motor-learning theory: the **guidance hypothesis** says frequent, concurrent knowledge of
results helps performance while it is present but hurts retention once it is removed.
Learners come to depend on it ([Salmoni, Schmidt & Walter 1984](https://doi.org/10.1037/0033-2909.95.3.355)).
For a review of concurrent vs terminal feedback across modalities, including haptic, see
[Sigrist et al. 2013](https://doi.org/10.3758/s13423-012-0333-8). The hand-hygiene pattern
above (gains that disappear when the feedback is removed: Lacey, Zwicker) is what this
hypothesis predicts.

What the evidence does and does not say about a single Target check:

- Every "during" system studied gives continuous or per-step feedback. **None tested a
  single nudge at the end of the target time.** The proposed Target check sits between the
  two options. It arrives late in the wash and only once, which makes it close to terminal
  feedback, but the user can still act on it. *Inference:* by the guidance hypothesis, one
  prompt per wash should create less dependence than live traffic lights. That has not been
  measured.
- Feedback right after the wash, with no chance to fix the current wash, also worked
  (Lehotsky: 50 % → 15 %), and it worked over repeated uses. A post-wash summary is not a
  weak option.
- The gains in duration and volume from in-the-moment haptic cues (Pires) are real but
  modest. Most rubs still fell short of the target (26.9 % reached the correct duration).
- **The evidence is thin.** These are almost all healthcare workers using alcohol handrub
  or hospital sinks, with small samples, before–after designs, and device makers among the
  authors (Lacey, Stewardson). No study covers consumers washing at home with soap, which is
  HapticWash's population.

## (b) Which Steps / hand areas are most often missed

HapticWash Steps vs WHO numbering. On the WHO handwash poster, Steps 3–8 are the rubbing poses
(the poster says 40–60 s in total; [WHO poster](https://www.who.int/multi-media/details/how-to-handwash-and-handrub)):
Palms = palm to palm. Back of hands = right palm over left dorsum, and vice versa.
Between fingers = palm to palm with fingers interlaced. "Backs of fingers to opposing palms"
has no matching HapticWash Step. Thumbs = rotational rubbing of each thumb. Fingertips =
fingertips rubbed in the opposite palm.

**Whether a step was performed (direct observation):**

- **Arias et al. 2016**, J Infect Public Health, 2,497 opportunities in Qatar [P, abstract]:
  compliance was high for the first steps and lower afterwards. "Rubbing of the thumbs and
  fingertips achieved the lowest rates of compliance in both HH types" (handrub and
  handwash). The authors' lay summary gives the per-step figures [S]. Handrub:
  backs of fingers 79.9 %, **thumbs 14.9 %**, **fingertips 36.6 %**. Handwash: 70.6 %,
  **30.3 %**, **40.9 %**. Steps 1–2 were at 100 %.
  ([abstract DOI](https://doi.org/10.1016/j.jiph.2015.11.006),
  [author summary](https://atlasofscience.org/assessment-of-hand-hygiene-techniques/))
- **Tschudin-Sutter et al. 2015**, ICHE, Basel, 520 h of observation [S]: 93.2 % did hand
  hygiene when indicated, but only **8.5 %** performed all 6 steps. The most common pattern
  was 3 of 6 steps (28.7 %). The summary does not say which steps were dropped.
  ([DOI](https://doi.org/10.1017/ice.2014.82))
- **Reilly et al. 2016**, ICHE, RCT of 6 steps vs 3 steps, 120 HCWs [P]: only **65 %** completed
  the 6-step technique, even with an instruction card in front of them and an observer present.
  Full completion reduced bacterial load more: the post/pre ratio was 0.31× that of partial
  completers (95 % CI 0.14–0.75).
  ([DOI](https://doi.org/10.1017/ice.2016.51), [full text](https://strathprints.strath.ac.uk/57931/))
- **Stewardson 2014** [P]: at baseline the median was 1–2 of 6 poses, and no one did all six.
- **WristWash** (local PDF, Li et al. 2018, [DOI](https://doi.org/10.1145/3267242.3267247)) [P]:
  even trained lab participants occasionally skipped steps. The skipped ones were 2 & 3,
  7 & 8, 4, 9, 5 & 6 and 3 in its 13-step scheme. The sample is too small to show a pattern.
- UWash's survey (local PDF) [P]: 96 % of 505 respondents had heard of the WHO steps, 48 % said
  they knew all of them, and 35 % said they follow them most of the time.

**Whether an area was covered (fluorescent / UV handrub):**

- **Taylor 1978**, Nursing Times 74(2):54–5 and 74(3):108–10 (PMIDs
  [622326](https://pubmed.ncbi.nlm.nih.gov/622326/), [622335](https://pubmed.ncbi.nlm.nih.gov/622335/)):
  **I could not read the original.** As summarised by Kampf 2008 [S]: dye in alcohol, 129
  observed procedures, median 20–21 s, **89 % missed some part of the hands**, and this was
  the basis for Ayliffe's 6-step technique. Many secondary sources reproduce the claim that
  the most-missed areas were **thumbs, fingertips, between the fingers and wrists**. That claim
  is unverified against the original.
- **Szilágyi et al. 2013**, BMC Infect Dis, n = 4,642, tested straight after WHO 6-step
  training [P]: only 72 % passed. The **dorsal** side was missed in 24 % and the palmar side
  in 18 %. Hotspots were the **dorsal fingers near the nails (16 %)** and the thenar/wrist
  crease area (18 %). Fingertips were missed by only 3.5 %. The authors suggest the 6 steps
  "may be too complex". ([PMC3689096](https://pmc.ncbi.nlm.nih.gov/articles/PMC3689096/))
- **Kampf et al. 2008**, BMC Infect Dis [P]: the **back of the hands** was incomplete in
  53–55 % of hands under every technique, against 7–15 % for the palmar side. Gaps also
  appeared around the thumb. Fingertips and palms were "hardly ever" missed. Several authors
  work for the handrub manufacturer.
  ([PMC2600642](https://pmc.ncbi.nlm.nih.gov/articles/PMC2600642/))
- **Reilly 2016** [P]: "The back of the hands, the back of the thumbs and the back of the
  index fingers were most frequently missed regardless of the techniques used."
- **Wang et al. 2022**, J Med Syst, n = 29 non-HCWs [P]: after the full 6 steps, mean coverage
  was 88.0 % dorsal vs 97.7 % palmar. The gaps were around the abductor digiti minimi (little
  finger edge) and the first dorsal interosseous (thumb–index web). The authors note that
  "no step of the six-step hand hygiene technique might be specifically designed to cover
  these areas." ([DOI](https://doi.org/10.1007/s10916-022-01817-z))
- Caveat on the method itself: UV-fluorescent coverage only weakly matches microbiology,
  and experts disagree when scoring it ([Bánsághi et al. 2023, arXiv preprint, 8 hands](https://arxiv.org/abs/2307.05650)).

**Synthesis.** "Missed" means two different things:
1. **The step was skipped** (observation): **Thumbs** is skipped most, then **Fingertips**,
   then backs of fingers. Palms and Back of hands are almost always performed (Arias).
2. **The area was left uncovered** (fluorescent): the **back of hands, the dorsal fingers and
   thumb, and the thumb base** are missed most, even when every step is done. Fingertips are
   covered more often than the folklore claims (Szilágyi 3.5 %, Kampf "hardly ever").

The IMU classifier detects *steps performed*, not *areas covered*, so
measure 1 is the one that matters for a Target check. Thumbs and Fingertips are the
Steps a nudge would most often name. Nearly all of this evidence comes from healthcare
workers using alcohol handrub. Data on soap-and-water handwashing by the public is thin
(the handwash arm of Arias is the closest).

## (c) Does prompting or feedback that changes behaviour hurt sensor recognition?

- **No study measures this directly.** I found no paper comparing recognition accuracy before
  and after users receive live prompts.
- **UWash "Wild Study"** (local PDF, §5.4(4)) [P]: 10 passersby washed under three conditions:
  their usual way, after WHO training, and deliberately shuffling or omitting steps. The model
  had been trained on WHO-trained washers. The score drop in the shuffled condition matched
  the omitted steps. Participants who only reordered (P4–P10) scored slightly lower,
  "because… they did not strictly adhere to the prescribed duration" for each step. This
  suggests that **changing step order or duration does not break a frame-wise segmentation
  model**. No accuracy against ground truth is reported for that condition, so the evidence
  is weak.
- **UWash cross-participant results** [P]: users with "special personal handwashing styles" fell
  below 60 % accuracy. *How* a person rubs matters more than step order.
- **WristWash** (local PDF) [P]: its "strong" recogniser uses a grammar that assumes WHO order
  with possible skips. Participants were trained and practised five times before recording,
  and the authors limit the claim to "continuous assessment of adherence to the already
  learned routine". *Inference:* a prompt that makes someone go back to Thumbs after
  Fingertips would break an order-based grammar. A frame-wise classifier like UWash, or
  HapticWash's own, is far less exposed.
- **Pires 2021** [P]: wearing a monitoring device with feedback switched off did not change
  behaviour, so wearing the watch alone is not the confound. Turning feedback on did change
  rub duration. That is the point of feedback, and it moves those washes away from
  unprompted ones.
- *Inference, specific to the Target check:* after the nudge, the wash is no longer the
  "natural" data the model was trained on. It may contain short, isolated, hurried rubs of
  the named Step, perhaps with an extra glance at the watch. The risks are (i) short segments
  that fall under the `BRIEF` / `OK` thresholds, and (ii) shifted priors if prompted washes are
  ever fed back into training. Both can be contained by logging the nudge time and treating
  the post-nudge segment as a separate, labelled condition. The 0–40 s portion stays
  untouched in either case.

## Bottom line for the Target check

- **Defensible, with care.** Live feedback reliably changes washing behaviour (Lacey g = 1.54;
  Pires duration 11 → 27 % correct). A single nudge late in the wash is the least intrusive kind
  of live feedback and the least likely to create dependence. The training-distribution
  objection is untested rather than refuted. A frame-wise model with logged nudge timestamps
  keeps the pre-nudge 0–40 s window clean.
- **Do not expect lasting habit change from the prompt alone.** Every live system lost its
  gains once removed. Keep the post-wash Coverage summary, which has its own evidence
  (Lehotsky).
- **Expect the nudge to name Thumbs and Fingertips most often.** Back of hands is a coverage
  problem that step detection cannot see.
- **Evidence gaps:** no study of consumers washing with soap, no comparison of a single end
  prompt with a post-wash summary, and no measure of recognition accuracy under prompting.

## References

- Lacey G et al. 2020. *Am J Infect Control* 48:162–6. https://doi.org/10.1016/j.ajic.2019.06.015
- Pires D et al. 2021. *JAMA Netw Open* 4:e2035331. https://doi.org/10.1001/jamanetworkopen.2020.35331
- Lehotsky Á et al. 2015. *J Hosp Infect* 91:81–4. https://doi.org/10.1016/j.jhin.2015.05.010
- Stewardson AJ et al. 2014. *PLoS One* 9:e105866. https://doi.org/10.1371/journal.pone.0105866
- Zwicker P et al. 2023. *J Hosp Infect* 133:73–80. https://doi.org/10.1016/j.jhin.2022.12.017
- Levchenko AI et al. 2013. *Comput Inform Nurs* 31:498–504. https://doi.org/10.1097/01.NCN.0000432124.53902.1d
- Samyoun S et al. 2021. iWash. *Smart Health* 19:100171. https://arxiv.org/abs/2009.10317
- Wang F et al. 2025. UWash. *IEEE TMC* (arXiv 2112.06657v5). https://arxiv.org/abs/2112.06657
- Li H et al. 2018. WristWash. *ISWC '18*:132–9. https://doi.org/10.1145/3267242.3267247
- Arias AV et al. 2016. *J Infect Public Health* 9:366–9. https://doi.org/10.1016/j.jiph.2015.11.006
- Tschudin-Sutter S et al. 2015. *ICHE* 36:482–3. https://doi.org/10.1017/ice.2014.82
- Reilly JS et al. 2016. *ICHE* 37:661–6. https://doi.org/10.1017/ice.2016.51
- Taylor LJ 1978. *Nurs Times* 74:54–5 and 74:108–10. PMID 622326, 622335
- Szilágyi L et al. 2013. *BMC Infect Dis* 13:249. https://doi.org/10.1186/1471-2334-13-249
- Kampf G et al. 2008. *BMC Infect Dis* 8:149. https://doi.org/10.1186/1471-2334-8-149
- Wang C et al. 2022. *J Med Syst* 46:36. https://doi.org/10.1007/s10916-022-01817-z
- Wang C et al. 2021. Electronic hand hygiene monitoring, a systematic review. *J Med Internet Res* 23:e27880. https://doi.org/10.2196/27880
- Salmoni AW, Schmidt RA, Walter CB 1984. *Psychol Bull* 95:355–86. https://doi.org/10.1037/0033-2909.95.3.355
- Sigrist R et al. 2013. *Psychon Bull Rev* 20:21–53. https://doi.org/10.3758/s13423-012-0333-8
- WHO. How to Handwash and Handrub (poster). https://www.who.int/multi-media/details/how-to-handwash-and-handrub
