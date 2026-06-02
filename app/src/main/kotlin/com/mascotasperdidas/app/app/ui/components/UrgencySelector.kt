package com.mascotasperdidas.app.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.app.theme.LocalCareColor
import com.mascotasperdidas.app.app.theme.LocalCareContainerColor
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme

@Composable
fun UrgencySelector(
    selected: CareUrgency?,
    onSelect: (CareUrgency) -> Unit,
    modifier: Modifier = Modifier,
) {
    val careColor = LocalCareColor.current
    val careContainer = LocalCareContainerColor.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CareUrgency.entries.forEach { urgency ->
            val isSelected = urgency == selected
            ElevatedCard(
                onClick = { onSelect(urgency) },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = 2.dp,
                                color = careColor,
                                shape = RoundedCornerShape(12.dp),
                            )
                        } else Modifier
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = urgency.icon,
                        contentDescription = null,
                        tint = if (isSelected) careColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(urgency.titleRes),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isSelected) careColor else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(urgency.subtitleRes),
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
private fun UrgencySelectorPreview() {
    MascotasPerdidasTheme {
        UrgencySelector(
            selected = CareUrgency.FEW_DAYS,
            onSelect = {},
        )
    }
}
