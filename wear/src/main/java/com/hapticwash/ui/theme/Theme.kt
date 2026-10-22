package com.hapticwash.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

/** Five Rings (DESIGN.md): one cool app tint, a fixed colour per Step, true black. Names follow DESIGN.md's tokens. */
object Five {
    val Tint = Color(0xFF64D2FF)
    val TintLight = Color(0xFF9BE4FF)
    val TintMist = Color(0xFFB4E9FF)
    val TintSky = Color(0xFF3AA8FF)
    val TintDeep = Color(0xFF0A84FF)
    val OnTint = Color(0xFF00131F)
    val Done = Color(0xFF63E6BE)
    val DoneRing = Color(0xFF3FD9A8)
    val DoneLight = Color(0xFFA9F5DA)
    val Stop = Color(0xFFFF453A)
    val HubIndigo = Color(0xFF5E5CE6)
    val HubBlueText = Color(0xFF409CFF)
    val HubGrey = Color(0xFF8E8E93)
    val TextSecondary = Color(0x9EEBEBF5)
    val TextTertiary = Color(0x61EBEBF5)
    val TimeText = Color(0xFFF2F2F7)
    val Platter = Color(0xFF202024)
    val PlatterRaised = Color(0xFF2C2C31)
    val Hairline = Color(0xFF1F1F23)
    val SwitchOff = Color(0xFF39393D)
    val Glass = Color(0x24FFFFFF)
    val AmbientLine = Color(0xFF8A8A90)
    val AmbientText = Color(0xFFA8A8AD)

    /** One colour per Step, labels 1..5, outer ring to inner: Palms, Back of hands, Between fingers, Thumbs, Fingertips. */
    val Steps = listOf(Color(0xFFFF375F), Color(0xFFFF9F0A), Color(0xFFFFD60A), Color(0xFF30D158), Color(0xFFBF5AF2))

    /** Each Step colour's light partner: the far end of its ring's gradient. */
    val StepsLight = listOf(Color(0xFFFF8AA3), Color(0xFFFFC66B), Color(0xFFFFEC80), Color(0xFF86EFA4), Color(0xFFDFA6FF))

    fun step(label: Int) = Steps[label - 1]
    fun stepLight(label: Int) = StepsLight[label - 1]
}

/** DESIGN.md's colours in Material's roles, so stock Wear components (TimeText, scroll indicator, dialogs) match. */
private val FiveScheme = ColorScheme(
    primary = Five.Tint,
    primaryDim = Five.TintSky,
    primaryContainer = Five.PlatterRaised,
    onPrimary = Five.OnTint,
    onPrimaryContainer = Five.Tint,
    tertiary = Five.Done,
    tertiaryDim = Five.DoneRing,
    onTertiary = Five.OnTint,
    surfaceContainerLow = Five.Platter,
    surfaceContainer = Five.Platter,
    surfaceContainerHigh = Five.PlatterRaised,
    onSurface = Color.White,
    onSurfaceVariant = Five.TextSecondary,
    outline = Five.TextTertiary,
    outlineVariant = Five.Hairline,
    background = Color.Black,
    onBackground = Color.White,
    error = Five.Stop,
    errorDim = Five.Stop,
    errorContainer = Five.Stop,
    onError = Color.Black,
)

@Composable
fun HapticWashTheme(content: @Composable () -> Unit) = MaterialTheme(colorScheme = FiveScheme, content = content)

/** DESIGN.md's type roles. Counting numbers are rounded with tabular figures; prose keeps the default rounding. */
object FiveType {
    private fun num(size: Int, weight: Int = 650) =
        TextStyle(fontSize = size.sp, fontWeight = FontWeight(weight), lineHeight = 1.em, letterSpacing = (-0.01).em, fontFeatureSettings = "tnum").rounded()

    val display = num(58)
    val displaySmall = num(46)
    val headline = num(28)
    val headlineSmall = num(18).copy(lineHeight = 1.18.em)
    val title = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 1.18.em).rounded()
    val bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 1.18.em)
    val body = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, lineHeight = 1.18.em)
    val bodySmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 1.22.em)
    val labelLarge = TextStyle(fontSize = 13.sp, lineHeight = 1.18.em)
    val label = TextStyle(fontSize = 12.sp, lineHeight = 1.25.em)
    val labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 1.2.em)
}

/**
 * This style in rounded letterforms, like Apple's SF Compact Rounded timers: the watch's Google Sans
 * Flex at full `ROND`. Watches without that font fall back to their usual Roboto Flex.
 */
fun TextStyle.rounded(): TextStyle {
    val weight = fontWeight ?: FontWeight.Medium
    val round = FontVariation.Settings(FontVariation.weight(weight.weight), FontVariation.Setting("ROND", 100f))
    return copy(fontFamily = FontFamily(
        Font(DeviceFontFamilyName("google-sans-flex"), weight, variationSettings = round),
        Font(DeviceFontFamilyName("roboto-flex"), weight),
    ))
}
