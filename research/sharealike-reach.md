# Does CC BY-SA ShareAlike reach the app licence?

Researched 2026-10-18. Not legal advice. Backs ticket 12 (`.scratch/coaching/issues/12-sharealike-reach.md`); feeds ticket 13 (credit placement).

## Verdict

**(b): ShareAlike attaches to the cropped images only. It does not reach the app code or the app's licence, as long as the images stay separate asset files.** (a) is not supported by the licence text; (c) is wrong because the crops themselves are not exempt.

Caveat: no CC primary source speaks to APKs or Android resources specifically. The verdict applies the licence text and the CC FAQ on collections and adaptations; it is not a CC statement about APKs, and no court ruling was sought.

## The app's current licence

**None.** No `LICENSE*`, `COPYING*` or `NOTICE*` file is tracked in the repo. `README.md`, `build.gradle.kts`, `wear/build.gradle.kts`, `CLAUDE.md` and `docs/` contain no licence declaration or SPDX tag; the only "licence" hits concern replay-dataset CSVs and the Hegasy plan. With no licence the code is default all-rights-reserved, so nothing conflicts with ShareAlike today. The question only bites when the owner picks a licence.

## Reasoning from the primary sources

1. **ShareAlike is triggered by sharing "Adapted Material You produce".** BY-SA 4.0 s3(b): "if You Share Adapted Material You produce, the following conditions also apply". The Adapter's License must be CC BY-SA (this version or later) or a BY-SA Compatible License (3(b)(1)); include its text or a link (3(b)(2)); add no extra terms or Effective Technological Measures to the Adapted Material that restrict rights under that licence (3(b)(3)).
2. **What is Adapted Material.** s1: material "derived from or based upon the Licensed Material and in which the Licensed Material is translated, altered, arranged, transformed, or otherwise modified in a manner requiring permission". The "Adapter's License" is what you apply "to Your Copyright and Similar Rights in Your contributions to Adapted Material" (s1). Scope is the adapted material and your contribution to it, not unrelated works shipped alongside.
3. **Treat a crop as an adaptation, to be safe.** CC FAQ "When is my use considered an adaptation?": it "depends primarily on the applicable copyright law"; "a modification rises to the level of an adaptation under copyright law when the modified work is based on the prior work but manifests sufficient new creativity to be copyrightable". A pure crop may fall below that bar, but it is a modification of an illustration, so plan as if ShareAlike applies to each crop. Contrast s2(a)(4): "simply making modifications authorized by this Section 2(a)(4) never produces Adapted Material" (format and media changes, e.g. lossless recompression by the build).
4. **Putting an adaptation inside a bigger work is a collection, not a transfer of the licence.** CC FAQ, "If I create a collection that includes a work offered under a CC license, which license(s) may I choose for the collection?": "You may choose a license for the collection, however this does not change the license applicable to the original material." The database FAQ says the same of SA: it "do[es] not require the collection or the compilation itself to be made available under an SA license, even though each individual work is still licensed individually", and a modified item "would need to be licensed under the same terms". An APK is a zip of separate files (`res/drawable/*.png`, `classes.dex`), which fits that picture. FAQ "Can I combine material...": "If the combination does not create an adaptation, then you may combine any CC-licensed content so long as you provide attribution".
5. **Where it could reach the code (avoid).** CC's only statement on content mixed into software is narrow: BY-SA 4.0 is one-way compatible with GPLv3, a mechanism "designed for situations in which content is integrated into software code in a way that makes it difficult or impossible to distinguish the two" (FAQ, software entry). That is the case where content and code become one work. Do not do that: no tracing the art into a Kotlin/Compose `Path` or a vector XML generated into source, no base64 PNG in a `.kt` file, no code written "based upon" the art. Keep the files as standalone resources. The same FAQ entry says CC licences may be used "for separate artistic elements such as game art or music", which matches this design.
6. **When the owner picks a licence for the app.** CC "recommend[s] against using Creative Commons licenses for software" and says CC licences "are currently not compatible with the major software licenses". So do not put the app under CC BY-SA to match; choose a software licence for the code and state that the Hegasy crops are the exception, under CC BY-SA 4.0. Any app terms must not forbid extracting or reusing the images (see below).

## Does shipping in an APK change the answer?

No for scope: the APK is a container and the licence follows each file. It matters in three practical ways.

- **Technical restrictions (s2(a)(5)(C), s3(b)(3)).** "You may not offer or impose any additional or different terms or conditions on, or apply any Effective Technological Measures to, the Licensed Material if doing so restricts exercise of the Licensed Rights by any recipient." CC FAQ on DRM: "merely converting material into a different format that is difficult to access or is only available for certain platforms does not violate the restriction". An ETM is a measure whose circumvention is penalised under laws implementing WIPO Copyright Treaty art. 11 (s1). So ordinary APK packaging, aapt2 compression and resource shrinking are format changes, not an ETM (R8 does not touch PNG assets anyway). Do not add encryption, a signature-checked asset decryptor, or obfuscation whose purpose is to stop extraction, and do not put a EULA clause forbidding extraction of the images. This is my reading; CC has no APK-specific statement.
- **Recipient access.** Anyone with the APK can unzip it and get the crops, which satisfies "every recipient receives an offer" (s2(a)(5)(A)-(B)). Also keep the crops in the repo in their own folder with the licence notice, so the Adapter's License text is available.
- **Store-side DRM or signing** applied by the platform is the platform's measure (FAQ: a streaming app's DRM is not your breach); just do not opt into extra restrictions on these files.

## Concrete obligations for the five crops (CC BY-SA 4.0)

Commons metadata (API) for `File:04_Hegasy_Hand_Disinfection_Wiki_EN_CCBYSA.png`: `AttributionRequired = true`, licence CC BY-SA 4.0, Artist shown as "Guido4", credit "Own work" with http://www.hegasy.de/, no custom attribution text. Earlier research (`research/artwork-source.md`) found the author of record unclear, so confirm the file page before shipping.

1. **Credit the creator** as designated, plus any copyright notice supplied (3(a)(1)(A)(i)-(ii)). Use the names the file page shows.
2. **Link to the material**, "to the extent reasonably practicable" (3(a)(1)(A)(v)): https://commons.wikimedia.org/wiki/File:04_Hegasy_Hand_Disinfection_Wiki_EN_CCBYSA.png
3. **Name the licence and give its text or URI** (3(a)(1)(C)): CC BY-SA 4.0, https://creativecommons.org/licenses/by-sa/4.0/. Also a notice referring to the warranty disclaimer if supplied (3(a)(1)(A)(iv)); the licence link covers it in practice.
4. **Say the images were changed**: "cropped" (3(a)(1)(B)). Agreed ES wording from ticket 10: "Ilustraciones: Hegasy, CC BY-SA 4.0, recortadas."
5. **ShareAlike on the crops**: offer the cropped images under CC BY-SA 4.0 (or a BY-SA Compatible License) and include the licence text or link (3(b)(1)-(2)). Label them so in the repo.
6. **No extra terms or ETMs** on the images (3(b)(3), 2(a)(5)(C)).
7. **No implied endorsement** by the creator (2(a)(6); FAQ "Do I need to be aware of anything else when providing attribution?").
8. **If the licensor asks, remove the credit** where reasonably practicable (3(a)(3)).
9. **Keep them separate**: images stay resource files, never inlined into code.

## Where the notice may live

- s3(a)(2): "any reasonable manner based on the medium, means, and context", and "it may be reasonable to satisfy the conditions by providing a URI or hyperlink to a resource that includes the required information". FAQ: a licensor cannot "insist on the exact placement of the attribution credit". No single spot is mandated.
- For a no-INTERNET watch app, a short on-device line (creator, licence name, "cropped") plus a fuller notice in the repo (e.g. a `NOTICE` file next to the assets) and the store listing covers both readings. URLs on the watch are printed text, not clickable. Whether on-device-only or repo-only counts as "reasonable" is a judgement under 3(a)(2) that CC text does not settle. CC's TASL checklist (Title, Author, Source, License) is the practical template. Hand this to ticket 13.

## Open points

- The author of record on Commons is not one clean name; use exactly what the file page shows at ship time.
- No primary source addresses APKs, Android resource packing or R8; the APK points apply generic text.
- Whether a bare crop is a copyright "adaptation" in the owner's jurisdiction is untested. Treating it as one costs nothing.
- BY-SA 4.0 has no s3(a)(4). The "Adapter's License must not prevent recipients complying" clause is s3(a)(4) of BY 4.0 only.

## Sources

- BY-SA 4.0 legal code: https://creativecommons.org/licenses/by-sa/4.0/legalcode.en (s1, s2(a)(4), s2(a)(5), s2(a)(6), s3(a), s3(b))
- CC BY 4.0 legal code (for the s3(a)(4) note): https://creativecommons.org/licenses/by/4.0/legalcode.en
- CC FAQ: https://creativecommons.org/faq/ (entries: software; adaptation; combining licences; collections; databases and SA; attribution placement; ETMs/DRM; implied endorsement)
- ShareAlike compatibility and GPLv3 note: https://wiki.creativecommons.org/wiki/ShareAlike_compatibility
- Attribution best practice (TASL): https://wiki.creativecommons.org/wiki/Best_practices_for_attribution
- Commons file: https://commons.wikimedia.org/wiki/File:04_Hegasy_Hand_Disinfection_Wiki_EN_CCBYSA.png
- Earlier repo note: `research/artwork-source.md` on branch `research/artwork-source`
