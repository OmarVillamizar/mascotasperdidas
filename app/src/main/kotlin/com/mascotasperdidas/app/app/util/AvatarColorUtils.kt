package com.mascotasperdidas.app.app.util

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

fun colorFromUid(uid: String, colorScheme: ColorScheme): Color {
    val palette = listOf(
        colorScheme.primaryContainer,
        colorScheme.secondaryContainer,
        colorScheme.tertiaryContainer,
        colorScheme.errorContainer,
        colorScheme.surfaceVariant,
    )
    val index = uid.hashCode().let { if (it < 0) -it else it } % palette.size
    return palette[index]
}
