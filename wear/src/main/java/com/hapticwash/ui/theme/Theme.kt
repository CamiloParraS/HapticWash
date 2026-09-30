package com.hapticwash.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
