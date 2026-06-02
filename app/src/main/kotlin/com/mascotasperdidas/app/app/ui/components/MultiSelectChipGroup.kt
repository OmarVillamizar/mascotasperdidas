package com.mascotasperdidas.app.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiSelectChipGroup(
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option in selected,
                onClick = { onToggle(option) },
                label = { Text(option) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MultiSelectChipGroupPreview() {
    MascotasPerdidasTheme {
        MultiSelectChipGroup(
            options = listOf("Asustado", "Herido", "Desnutrido", "Cariñoso", "Agresivo"),
            selected = setOf("Asustado", "Cariñoso"),
            onToggle = {},
        )
    }
}
