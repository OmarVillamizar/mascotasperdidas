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
fun SingleSelectChipGroup(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(option) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SingleSelectChipGroupPreview() {
    MascotasPerdidasTheme {
        SingleSelectChipGroup(
            options = listOf("Perro", "Gato", "Otro"),
            selected = "Perro",
            onSelect = {},
        )
    }
}
