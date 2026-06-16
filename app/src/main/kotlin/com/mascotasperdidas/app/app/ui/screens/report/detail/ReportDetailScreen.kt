package com.mascotasperdidas.app.app.ui.screens.report.detail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.ui.components.AttributesGrid
import com.mascotasperdidas.app.app.ui.components.HeroImage
import com.mascotasperdidas.app.app.ui.components.MiniMapView
import com.mascotasperdidas.app.app.ui.components.SectionLabel
import com.mascotasperdidas.app.app.ui.components.StickyContactFooter
import com.mascotasperdidas.app.app.ui.components.UserAvatar
import com.mascotasperdidas.app.app.util.colorFromUid
import com.mascotasperdidas.app.app.util.launchDialer
import com.mascotasperdidas.app.app.util.launchWhatsApp
import com.mascotasperdidas.app.app.util.petImageModel
import com.mascotasperdidas.app.app.util.petRecencyLabel
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    state: ReportDetailUiState,
    onEvent: (ReportDetailUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.contactSnackbar) {
        state.contactSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(ReportDetailUiEvent.DismissSnackbar)
        }
    }

    val topBarTitle = when (state.report?.type) {
        ReportType.LOST -> stringResource(R.string.mascota_perdida)
        ReportType.FOUND_SIGHTING -> stringResource(R.string.avistamiento_en_calle)
        ReportType.FOUND_IN_CARE -> stringResource(R.string.bajo_mi_cuidado)
        null -> ""
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(topBarTitle, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (state.isOwner && state.report != null) {
                        IconButton(onClick = { onEvent(ReportDetailUiEvent.DeleteReport) }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            val report = state.report
            // Show contact actions whenever the report carries a phone (owner or not),
            // so they are reachable from "Más información".
            if (report != null && report.ownerPhone.isNotBlank()) {
                val context = LocalContext.current
                val phone = report.ownerPhone
                val connectingMsg = stringResource(R.string.contact_connecting_whatsapp)
                StickyContactFooter(
                    onWhatsApp = {
                        Toast.makeText(context, connectingMsg, Toast.LENGTH_SHORT).show()
                        launchWhatsApp(context, phone)
                    },
                    onCall = { launchDialer(context, phone) },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }
            state.report == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.detail_not_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                val report = state.report
                ReportDetailContent(
                    report = report,
                    isOwner = state.isOwner,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(ReportDetailUiEvent.DismissDelete) },
            title = { Text(stringResource(R.string.settings_dialog_delete_title)) },
            text = { Text(stringResource(R.string.descartar_reporte)) },
            confirmButton = {
                TextButton(onClick = { onEvent(ReportDetailUiEvent.ConfirmDelete) }) {
                    Text(
                        text = stringResource(R.string.eliminar),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ReportDetailUiEvent.DismissDelete) }) {
                    Text(stringResource(R.string.cancelar))
                }
            },
        )
    }
}

@Composable
private fun ReportDetailContent(
    report: PetReport,
    isOwner: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            HeroImage(
                url = report.imageUrl,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        // Additional photos for LOST
        if (report.type == ReportType.LOST && report.additionalPhotos.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    itemsIndexed(report.additionalPhotos) { index, url ->
                        AsyncImage(
                            model = petImageModel(url),
                            contentDescription = stringResource(R.string.detail_additional_photo, index + 1),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    }
                }
            }
        }

        // Attributes grid
        item {
            val yes = stringResource(R.string.comun_si)
            val no = stringResource(R.string.comun_no)
            val attrs = when (report.type) {
                ReportType.LOST -> listOfNotNull(
                    stringResource(R.string.especie) to report.species.ifBlank { "-" },
                    stringResource(R.string.sexo) to report.gender.ifBlank { "-" },
                    stringResource(R.string.edad_aprox) to report.ageApprox.ifBlank { "-" },
                    stringResource(R.string.attr_color) to report.color.ifBlank { "-" },
                )
                ReportType.FOUND_SIGHTING -> listOfNotNull(
                    stringResource(R.string.especie) to report.species.ifBlank { "-" },
                    stringResource(R.string.tamano) to report.size.ifBlank { "-" },
                    stringResource(R.string.attr_estado) to (report.statuses.firstOrNull() ?: report.physicalStatus.firstOrNull() ?: "-"),
                    stringResource(R.string.attr_collar) to report.collarColor.ifBlank { "-" },
                )
                ReportType.FOUND_IN_CARE -> listOfNotNull(
                    stringResource(R.string.especie) to report.species.ifBlank { "-" },
                    stringResource(R.string.tamano) to report.size.ifBlank { "-" },
                    stringResource(R.string.attr_estado) to (report.physicalStatus.firstOrNull() ?: "-"),
                    stringResource(R.string.attr_collar) to (if (report.hasCollarPlate) yes else no),
                    stringResource(R.string.edad_aprox) to report.ageApprox.ifBlank { "-" },
                    stringResource(R.string.attr_microchip) to (if (report.hasMicrochip) yes else no),
                )
            }
            AttributesGrid(
                items = attrs,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        // Description
        if (report.description.isNotBlank()) {
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = report.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        // Location + MiniMap (LOST + FOUND_SIGHTING only)
        if (report.type != ReportType.FOUND_IN_CARE) {
            if (report.latitude != null && report.longitude != null) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        SectionLabel(text = stringResource(R.string.ultima_ubicacion))
                        MiniMapView(
                            latitude = report.latitude,
                            longitude = report.longitude,
                            type = report.type,
                        )
                    }
                }
            } else if (report.location.isNotBlank()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        SectionLabel(text = stringResource(R.string.ultima_ubicacion))
                        Text(
                            text = report.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        // Reported by (FOUND_SIGHTING)
        if (report.type == ReportType.FOUND_SIGHTING) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SectionLabel(text = stringResource(R.string.reportado_por))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            initial = report.ownerInitial,
                            photoUrl = null,
                            size = 40.dp,
                            containerColor = colorFromUid(
                                report.ownerUid,
                                MaterialTheme.colorScheme,
                            ),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = report.ownerName.ifBlank { report.ownerInitial },
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = petRecencyLabel(report.createdAtEpochMs),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Physical status chips (FOUND_IN_CARE)
        if (report.type == ReportType.FOUND_IN_CARE && report.physicalStatus.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SectionLabel(text = stringResource(R.string.estado_fisico))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        report.physicalStatus.forEach { status ->
                            AssistChip(
                                onClick = {},
                                label = { Text(status, style = MaterialTheme.typography.labelMedium) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                ),
                            )
                        }
                    }
                }
            }
        }

        // Notes (FOUND_IN_CARE)
        if (report.type == ReportType.FOUND_IN_CARE && report.notes.isNotBlank()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SectionLabel(text = stringResource(R.string.notas))
                    Text(
                        text = report.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

private val sampleLostReport = PetReport(
    id = "1", ownerUid = "uid-1", ownerInitial = "M", ownerName = "María García",
    petName = "Max", type = ReportType.LOST,
    breed = "Golden Retriever", species = "Perro", gender = "Macho",
    color = "Dorado", ageApprox = "3 años",
    description = "Max se escapó de casa el lunes por la tarde. Lleva collar rojo con placa de identificación.",
    location = "Parque Simón Bolívar, Cúcuta",
    imageUrl = "https://placedog.net/400/300?id=1",
    recencyLabel = "RECIENTE", createdAtEpochMs = System.currentTimeMillis(),
    latitude = 7.8939, longitude = -72.5078,
)

private val sampleSightingReport = PetReport(
    id = "2", ownerUid = "uid-2", ownerInitial = "L", ownerName = "Luis Morales",
    petName = "Luna", type = ReportType.FOUND_SIGHTING,
    breed = "Siamés", species = "Gato", size = "Pequeño",
    collarColor = "Azul", statuses = listOf("Asustado"),
    description = "Gata siamesa avistada cerca del centro comercial.",
    location = "Centro Comercial Ventura Plaza, Cúcuta",
    imageUrl = "https://picsum.photos/seed/luna-cat/400/300",
    recencyLabel = "Hoy", createdAtEpochMs = System.currentTimeMillis(),
    latitude = 7.8820, longitude = -72.4960,
)

private val sampleInCareReport = PetReport(
    id = "3", ownerUid = "uid-3", ownerInitial = "R", ownerName = "Rosa Quintero",
    petName = "Rocky", type = ReportType.FOUND_IN_CARE,
    breed = "Bulldog Francés", species = "Perro", size = "Mediano",
    collarColor = "Ninguno", microchip = "No visible",
    physicalStatus = listOf("Saludable", "Cariñoso"),
    notes = "Está comiendo bien. Necesito encontrar a su dueño pronto.",
    description = "Lo encontré deambulando solo en la Zona Rosa.",
    location = "Zona Rosa, Cúcuta",
    imageUrl = "https://placedog.net/400/300?id=3",
    recencyLabel = "Ayer", createdAtEpochMs = System.currentTimeMillis(),
)

@Preview(showBackground = true)
@Composable
private fun ReportDetailLostPreview() {
    MascotasPerdidasTheme {
        ReportDetailScreen(
            state = ReportDetailUiState(report = sampleLostReport, isLoading = false, isOwner = false),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportDetailSightingPreview() {
    MascotasPerdidasTheme {
        ReportDetailScreen(
            state = ReportDetailUiState(report = sampleSightingReport, isLoading = false, isOwner = false),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportDetailInCareOwnerPreview() {
    MascotasPerdidasTheme {
        ReportDetailScreen(
            state = ReportDetailUiState(report = sampleInCareReport, isLoading = false, isOwner = true),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}
