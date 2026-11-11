package com.hapticwash.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.ExperimentalTextApi
import com.hapticwash.R
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.Typography
import androidx.wear.compose.material3.timeTextCurvedText

/**
 * One look of Five Rings: every colour a screen draws with. [Dark] is the original, on true black; [Light] is
 * "Daylight" (DESIGN.md): the tint deepened so it holds ≥3:1 against a pale face. Rings, discs and washes take the Step
 * spectrum half-deepened, each ring edged in ink ([ringEdge]); Step-coloured words and small marks take [stepsText].
 */
class FivePalette(
    val bg: Color, val ink: Color, val textSecondary: Color, val textTertiary: Color, val timeText: Color,
    val tint: Color, val tintLight: Color, val tintMist: Color, val tintSky: Color, val tintDeep: Color, val onTint: Color,
    val done: Color, val doneRing: Color, val doneLight: Color, val stop: Color,
    val hubIndigo: Color, val hubBlueText: Color, val hubGrey: Color, val hubSlate: Color,
    val platter: Color, val platterRaised: Color, val hairline: Color, val switchOff: Color, val glass: Color,
    /** How strongly a screen's tint wash falls from the top: full on black, lighter where the base is already bright. */
    val washGain: Float,
    /** Washes that mean "nothing in particular" (a Summary with no Coverage): white on black, grey on light. */
    val neutral: Color,
    /** One colour per Step, labels 1..5, outer ring to inner: Palms, Back of hands, Between fingers, Thumbs, Fingertips. */
    val steps: List<Color>,
    /** Each Step colour's light partner: the far end of its ring's gradient. */
    val stepsLight: List<Color>,
    /** The glyph or mark that sits on a lit Step colour. */
    val onStep: List<Color>,
    /** Each Step's colour for words, thin marks and bare ring tracks: [steps] on black, deepened to ≥3:1 on the pale face. */
    val stepsText: List<Color> = steps,
    /** Opacity of a ring's bare track, in [stepsText]: stronger on the pale face so an empty ring still reads. */
    val trackAlpha: Float = 0.2f,
    /** A hairline along both edges of each lit ring, so pale colours hold against a pale face; transparent on black. */
    val ringEdge: Color = Color.Transparent,
) {
    companion object {
        val Dark = FivePalette(
            bg = Color.Black, ink = Color.White, textSecondary = Color(0x9EEBEBF5), textTertiary = Color(0x80EBEBF5), timeText = Color(0xFFF2F2F7),
            tint = Color(0xFF64D2FF), tintLight = Color(0xFF9BE4FF), tintMist = Color(0xFFB4E9FF), tintSky = Color(0xFF3AA8FF), tintDeep = Color(0xFF0A84FF), onTint = Color(0xFF00131F),
            done = Color(0xFF63E6BE), doneRing = Color(0xFF3FD9A8), doneLight = Color(0xFFA9F5DA), stop = Color(0xFFFF453A),
            hubIndigo = Color(0xFF5E5CE6), hubBlueText = Color(0xFF409CFF), hubGrey = Color(0xFF8E8E93), hubSlate = Color(0xFF48484A),
            platter = Color(0xFF202024), platterRaised = Color(0xFF2C2C31), hairline = Color(0xFF1F1F23), switchOff = Color(0xFF39393D), glass = Color(0x24FFFFFF),
            washGain = 1f, neutral = Color.White,
            steps = listOf(Color(0xFFFF375F), Color(0xFFFF9F0A), Color(0xFFFFD60A), Color(0xFF30D158), Color(0xFFBF5AF2)),
            stepsLight = listOf(Color(0xFFFF8AA3), Color(0xFFFFC66B), Color(0xFFFFEC80), Color(0xFF86EFA4), Color(0xFFDFA6FF)),
            onStep = List(5) { Color.Black },
        )

        val Light = FivePalette(
            bg = Color(0xFFDFE8E4), ink = Color(0xFF0A1822), textSecondary = Color(0xB80A1822), textTertiary = Color(0x9E0A1822), timeText = Color(0xFF0A1822),
            tint = Color(0xFF0072B2), tintLight = Color(0xFF1A8AD2), tintMist = Color(0xFF3FB6EC), tintSky = Color(0xFF0A6AB4), tintDeep = Color(0xFF0A84FF), onTint = Color.White,
            done = Color(0xFF07795B), doneRing = Color(0xFF12A57B), doneLight = Color(0xFF5ED3AE), stop = Color(0xFFD7261E),
            hubIndigo = Color(0xFF5E5CE6), hubBlueText = Color(0xFF0A66BF), hubGrey = Color(0xFF8E8E93), hubSlate = Color(0xFF3A4A5C),
            platter = Color(0xFFD1DBD7), platterRaised = Color(0xFFC2CDC8), hairline = Color(0xFFB6C2BD), switchOff = Color(0xFF98A5AF), glass = Color(0x140A1822),
            washGain = 0.5f, neutral = Color(0xFF8E8E93),
            // Rings: halfway between the dark Appearance's spectrum and stepsText, lit towards the bright dark-mode colour.
            steps = listOf(Color(0xFFEC2954), Color(0xFFE27F05), Color(0xFFD4A905), Color(0xFF23AE48), Color(0xFFA947E1)),
            stepsLight = listOf(Color(0xFFFF375F), Color(0xFFFF9F0A), Color(0xFFFFD60A), Color(0xFF30D158), Color(0xFFBF5AF2)),
            onStep = List(5) { Color.Black },
            stepsText = listOf(Color(0xFFD81B48), Color(0xFFC45F00), Color(0xFF9F7400), Color(0xFF168A38), Color(0xFF9333D0)),
            trackAlpha = 0.36f,
            ringEdge = Color(0x380A1822),
        )
    }
}

/**
 * Five Rings (DESIGN.md): one cool app tint, a fixed colour per Step. Names follow DESIGN.md's tokens, and read the
 * palette in use: [light] is snapshot state, so anything composed or drawn from it redraws when Appearance flips.
 * Ambient mode and the Tile take no part: ambient is always black, and the Tile is a system surface that stays dark.
 */
object Five {
    var light by mutableStateOf(false)

    private val p get() = if (light) FivePalette.Light else FivePalette.Dark

    val Bg get() = p.bg
    /** Primary text and glyphs on the face: white on black, ink on light. */
    val Ink get() = p.ink
    val Tint get() = p.tint
    val TintLight get() = p.tintLight
    val TintMist get() = p.tintMist
    val TintSky get() = p.tintSky
    val TintDeep get() = p.tintDeep
    val OnTint get() = p.onTint
    val Done get() = p.done
    val DoneRing get() = p.doneRing
    val DoneLight get() = p.doneLight
    val Stop get() = p.stop
    val HubIndigo get() = p.hubIndigo
    val HubBlueText get() = p.hubBlueText
    val HubGrey get() = p.hubGrey
    val HubSlate get() = p.hubSlate
    val TextSecondary get() = p.textSecondary
    val TextTertiary get() = p.textTertiary
    val TimeText get() = p.timeText
    val Platter get() = p.platter
    val PlatterRaised get() = p.platterRaised
    val Hairline get() = p.hairline
    val SwitchOff get() = p.switchOff
    val Glass get() = p.glass
    val WashGain get() = p.washGain
    val Neutral get() = p.neutral
    val Steps get() = p.steps
    val StepsLight get() = p.stepsLight
    val StepsText get() = p.stepsText
    val TrackAlpha get() = p.trackAlpha
    val RingEdge get() = p.ringEdge

    // Always dark: ambient mode is black whichever Appearance is set.
    val AmbientLine = Color(0xFF8A8A90)
    val AmbientText = Color(0xFFA8A8AD)

    fun step(label: Int) = Steps[label - 1]
    fun stepLight(label: Int) = StepsLight[label - 1]
    fun onStep(label: Int) = p.onStep[label - 1]
    /** A Step's colour for words and thin marks (titles, status marks, pager dot, a bare track's glyph). */
    fun stepText(label: Int) = StepsText[label - 1]
    /** The Focus Step's history line: the light partner on black, the deepened colour on the pale face. */
    fun focusLine(label: Int) = if (light) stepText(label) else stepLight(label)
}

/** [FivePalette] in Material's roles, so stock Wear components (TimeText, scroll indicator, dialogs) match. */
private fun schemeOf(p: FivePalette) = ColorScheme(
    primary = p.tint,
    primaryDim = p.tintSky,
    primaryContainer = p.platterRaised,
    onPrimary = p.onTint,
    onPrimaryContainer = p.tint,
    tertiary = p.done,
    tertiaryDim = p.doneRing,
    onTertiary = p.onTint,
    surfaceContainerLow = p.platter,
    surfaceContainer = p.platter,
    surfaceContainerHigh = p.platterRaised,
    onSurface = p.ink,
    onSurfaceVariant = p.textSecondary,
    outline = p.textTertiary,
    outlineVariant = p.hairline,
    background = p.bg,
    onBackground = p.ink,
    error = p.stop,
    errorDim = p.stop,
    errorContainer = p.stop,
    onError = p.bg,
)

/** The system clock at the top: [Five.TimeText] in bold on a near-opaque face-coloured pill, so it holds against whatever scrolls under it. */
@Composable
fun FiveTimeText() = TimeText(backgroundColor = Five.Bg.copy(alpha = 0.88f)) { time ->
    timeTextCurvedText(time, CurvedTextStyle(color = Five.TimeText, fontWeight = FontWeight.Bold))
}

private val DarkScheme = schemeOf(FivePalette.Dark)
private val LightScheme = schemeOf(FivePalette.Light)

@Composable
fun HapticWashTheme(content: @Composable () -> Unit) = MaterialTheme(colorScheme = if (Five.light) LightScheme else DarkScheme, typography = Typography(defaultFontFamily = AppFont), content = content)

/**
 * The app's own typeface, bundled so every watch shows the same letterforms: Google Sans Flex (OFL, `wear/third_party`),
 * one variable file driven per weight. Never the watch's system font, which on Samsung is a handwriting face.
 */
@OptIn(ExperimentalTextApi::class)
private fun family(round: Boolean) = FontFamily(
    (1..9).map { it * 100 }.map { w ->
        val settings = if (round) FontVariation.Settings(FontVariation.weight(w), FontVariation.Setting("ROND", 100f)) else FontVariation.Settings(FontVariation.weight(w))
        Font(R.font.google_sans_flex, FontWeight(w), variationSettings = settings)
    },
)

private val AppFont = family(round = false)
private val AppFontRound = family(round = true)

/** DESIGN.md's type roles. Counting numbers are rounded with tabular figures; prose keeps the default rounding. */
object FiveType {
    private fun num(size: Int, weight: Int = 650) =
        TextStyle(fontSize = size.sp, fontWeight = FontWeight(weight), lineHeight = 1.em, letterSpacing = (-0.01).em, fontFeatureSettings = "tnum").rounded()

    val display = num(58)
    val displaySmall = num(46)
    val headline = num(28)
    val headlineSmall = num(18).copy(lineHeight = 1.18.em)
    val title = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 1.18.em).rounded()
    val bodyLarge = TextStyle(fontFamily = AppFont, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 1.18.em)
    val body = TextStyle(fontFamily = AppFont, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, lineHeight = 1.18.em)
    val bodySmall = TextStyle(fontFamily = AppFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 1.22.em)
    val labelLarge = TextStyle(fontFamily = AppFont, fontSize = 13.sp, lineHeight = 1.18.em)
    val label = TextStyle(fontFamily = AppFont, fontSize = 12.sp, lineHeight = 1.25.em)
    val labelSmall = TextStyle(fontFamily = AppFont, fontSize = 11.sp, lineHeight = 1.2.em)
}

/** This style in rounded letterforms, like Apple's SF Compact Rounded timers: the bundled font at full `ROND`. */
fun TextStyle.rounded(): TextStyle = copy(fontFamily = AppFontRound)
