package com.mascotasperdidas.app.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Color tokens — full palette defined in Phase 2
private val Primary = Color(0xFF6750A4)
private val PrimaryContainer = Color(0xFFEADDFF)
private val Surface = Color(0xFFF3E8F7)
private val Error = Color(0xFFE57373)
private val Tertiary = Color(0xFF2E7D32)
private val SecondaryContainer = Color(0xFFF6E27A)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    primaryContainer = PrimaryContainer,
    surface = Surface,
    error = Error,
    tertiary = Tertiary,
    secondaryContainer = SecondaryContainer,
)

@Composable
fun MascotasPerdidasTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content,
    )
}
