package com.mascotasperdidas.app.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme

data class WizardOption(
    val label: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
fun SelectionWizardStep(
    instruction: String,
    options: List<WizardOption>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        options.forEach { option ->
            ElevatedCard(
                onClick = option.onClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = option.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectionWizardStepPreview() {
    MascotasPerdidasTheme {
        SelectionWizardStep(
            instruction = "¿Qué deseas reportar?",
            options = listOf(
                WizardOption(
                    label = "Mascota perdida",
                    description = "Mi mascota se escapó o fue robada",
                    icon = Icons.Outlined.Pets,
                    onClick = {},
                ),
                WizardOption(
                    label = "Avistamiento",
                    description = "Vi un animal en la calle",
                    icon = Icons.Outlined.Visibility,
                    onClick = {},
                ),
            ),
        )
    }
}
