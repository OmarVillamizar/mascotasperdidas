package com.mascotasperdidas.app.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.util.petImageModel
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType

@Composable
fun PetCard(
    report: PetReport,
    onMoreInfoClick: () -> Unit,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOwner: Boolean = false,
    onDeleteClick: () -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // ── Header: avatar + info + more options ──────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UserAvatar(
                    initial = report.ownerInitial,
                    photoUrl = null,
                    size = 40.dp,
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.petName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = report.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (isOwner) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = null,
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Eliminar",
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Pet image ─────────────────────────────────────────────
            AsyncImage(
                model = petImageModel(report.imageUrl),
                contentDescription = stringResource(R.string.content_desc_pet_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )

            Spacer(Modifier.height(12.dp))

            // ── Chips row: status + breed + recency ────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusChip(reportType = report.type)
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = report.breed,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = report.recencyLabel,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        labelColor = MaterialTheme.colorScheme.onSecondary,
                    ),
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Description ───────────────────────────────────────────
            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(12.dp))

            // ── Action buttons ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = onMoreInfoClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(stringResource(R.string.feed_btn_more_info))
                }
                Button(
                    onClick = onContactClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                    ),
                ) {
                    Text(stringResource(R.string.feed_btn_contact))
                }
            }
        }
    }
}

private val sampleReport = PetReport(
    id = "preview-001",
    ownerUid = "user-001",
    ownerInitial = "M",
    petName = "Max",
    type = ReportType.LOST,
    breed = "Golden Retriever",
    description = "Max se escapó de casa el lunes por la tarde. Lleva collar " +
        "rojo con placa de identificación. Es muy amigable pero puede estar asustado.",
    location = "Parque Simón Bolívar, Bogotá",
    imageUrl = "https://placedog.net/400/300?id=1",
    recencyLabel = "RECIENTE",
    createdAtEpochMs = System.currentTimeMillis() - 24 * 60 * 60 * 1000L,
)

@Preview(showBackground = true)
@Composable
private fun PetCardOwnerPreview() {
    MascotasPerdidasTheme {
        PetCard(
            report = sampleReport,
            isOwner = true,
            onMoreInfoClick = {},
            onContactClick = {},
            onDeleteClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PetCardFoundPreview() {
    MascotasPerdidasTheme {
        PetCard(
            report = sampleReport.copy(
                type = ReportType.FOUND_SIGHTING,
                petName = "Luna",
                breed = "Siamés",
                imageUrl = "https://picsum.photos/seed/luna-cat/400/300",
            ),
            isOwner = false,
            onMoreInfoClick = {},
            onContactClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
