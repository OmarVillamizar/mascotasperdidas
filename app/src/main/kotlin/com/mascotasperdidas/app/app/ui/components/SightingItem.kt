package com.mascotasperdidas.app.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.util.colorFromUid
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType

@Composable
fun SightingItem(
    sighting: PetReport,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = onTap,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(
                initial = sighting.ownerInitial,
                photoUrl = null,
                size = 44.dp,
                containerColor = colorFromUid(sighting.ownerUid, MaterialTheme.colorScheme),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sighting.ownerName.ifBlank { "Reportante" },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = sighting.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = sighting.recencyLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            StatusChip(reportType = sighting.type)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SightingItemPreview() {
    MascotasPerdidasTheme {
        SightingItem(
            sighting = PetReport(
                id = "2",
                ownerUid = "uid-abc",
                ownerInitial = "L",
                ownerName = "Luis Morales",
                petName = "Luna",
                type = ReportType.FOUND_SIGHTING,
                breed = "Siamés",
                description = "Vista cerca del parque",
                location = "Centro Comercial Ventura Plaza, Cúcuta",
                imageUrl = "",
                recencyLabel = "Hoy",
                createdAtEpochMs = System.currentTimeMillis(),
            ),
            onTap = {},
        )
    }
}
