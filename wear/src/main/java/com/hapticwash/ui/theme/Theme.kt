package com.hapticwash.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.dynamicColorScheme

/** Material You: the colours the watch derives from its theme. Default palette where the device offers none. */
@Composable
fun HapticWashTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    MaterialTheme(
        colorScheme = remember(context) { dynamicColorScheme(context) } ?: ColorScheme(),
        content = content
    )
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
