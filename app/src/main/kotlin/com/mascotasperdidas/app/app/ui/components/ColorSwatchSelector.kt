package com.mascotasperdidas.app.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorSwatchSelector(
    options: List<Pair<String, Color>>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (label, color) ->
            val isSelected = label == selected
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            )
                        } else {
                            Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape,
                            )
                        }
                    )
                    .clickable { onSelect(label) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorSwatchSelectorPreview() {
    MascotasPerdidasTheme {
        ColorSwatchSelector(
            options = listOf(
                "Dorado" to Color(0xFFFFD700),
                "Blanco" to Color(0xFFFFFFFF),
                "Negro" to Color(0xFF212121),
                "Marrón" to Color(0xFF795548),
                "Gris" to Color(0xFF9E9E9E),
                "Naranja" to Color(0xFFFF5722),
            ),
            selected = "Dorado",
            onSelect = {},
        )
    }
}
