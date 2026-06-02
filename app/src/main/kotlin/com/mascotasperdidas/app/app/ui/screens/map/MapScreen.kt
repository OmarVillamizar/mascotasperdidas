package com.mascotasperdidas.app.app.ui.screens.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.ui.components.MapBottomBar
import com.mascotasperdidas.app.app.ui.components.OsmMapView
import com.mascotasperdidas.app.app.ui.components.OsmMarker
import com.mascotasperdidas.app.app.util.createPinDrawable
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.R
import org.osmdroid.util.GeoPoint

private val CucutaCenter = GeoPoint(7.89705, -72.50809)

@Composable
fun MapScreen(
    state: MapUiState,
    onEvent: (MapUiEvent) -> Unit,
    onNavigateToNewReport: () -> Unit,
    onNavigateToReportDetail: (reportId: String, reportType: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val markers = remember(state.allReports, state.filterLost, state.filterFound) {
        state.filteredReports.map { report ->
            OsmMarker(
                id = report.id,
                position = GeoPoint(report.latitude!!, report.longitude!!),
                title = report.petName,
                icon = createPinDrawable(context, report.type),
                reportType = report.type,
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        OsmMapView(
            modifier = Modifier.fillMaxSize(),
            center = CucutaCenter,
            zoom = 13.0,
            markers = markers,
            onMarkerClick = { marker -> onEvent(MapUiEvent.PinClicked(marker.id)) },
        )

        MapBottomBar(
            onAdd = { onNavigateToNewReport() },
            onFilter = { onEvent(MapUiEvent.OpenFilterSheet) },
            onSearch = { onEvent(MapUiEvent.OpenSearch) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        )
    }

    when (val sheet = state.bottomSheetState) {
        is MapBottomSheetState.Filters -> {
            MapFiltersSheet(state = state, onEvent = onEvent)
        }
        is MapBottomSheetState.PinPreview -> {
            PinPreviewSheet(
                report = sheet.report,
                onEvent = onEvent,
                onNavigateToDetail = onNavigateToReportDetail,
            )
        }
        MapBottomSheetState.None -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapFiltersSheet(
    state: MapUiState,
    onEvent: (MapUiEvent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onEvent(MapUiEvent.CloseSheet) },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.filtrar),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = state.filterLost,
                        onValueChange = { onEvent(MapUiEvent.FilterLostChanged(it)) },
                        role = Role.Checkbox,
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = state.filterLost, onCheckedChange = null)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.perdidos_label), style = MaterialTheme.typography.bodyLarge)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = state.filterFound,
                        onValueChange = { onEvent(MapUiEvent.FilterFoundChanged(it)) },
                        role = Role.Checkbox,
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = state.filterFound, onCheckedChange = null)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.hallazgos_label), style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${stringResource(R.string.radio_busqueda)}: ${state.searchRadiusKm.toInt()} km",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Slider(
                value = state.searchRadiusKm,
                onValueChange = { onEvent(MapUiEvent.RadiusChanged(it)) },
                valueRange = 1f..20f,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { onEvent(MapUiEvent.ApplyFilters) },
                enabled = state.filterLost || state.filterFound,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.aplicar_filtros))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PinPreviewSheet(
    report: PetReport,
    onEvent: (MapUiEvent) -> Unit,
    onNavigateToDetail: (String, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onEvent(MapUiEvent.CloseSheet) },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.petName.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = report.description.ifBlank { report.location },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(12.dp))
                AsyncImage(
                    model = report.imageUrl,
                    contentDescription = report.petName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onNavigateToDetail(report.id, report.type.name) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.mas_informacion))
                }
                Button(
                    onClick = { onEvent(MapUiEvent.ContactClicked(report.id)) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                    ),
                ) {
                    Text(stringResource(R.string.contactar))
                }
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun MapScreenPreview() {
    MascotasPerdidasTheme {
        MapScreen(
            state = MapUiState(isLoading = false),
            onEvent = {},
            onNavigateToNewReport = {},
            onNavigateToReportDetail = { _, _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PinPreviewSheetPreview() {
    MascotasPerdidasTheme {
        PinPreviewSheet(
            report = PetReport(
                id = "1", ownerUid = "u", ownerInitial = "M", petName = "Max",
                type = ReportType.LOST, breed = "Golden Retriever",
                description = "Se escapó de casa el lunes.",
                location = "Parque Simón Bolívar, Cúcuta",
                imageUrl = "https://placedog.net/400/300?id=1",
                recencyLabel = "RECIENTE", createdAtEpochMs = System.currentTimeMillis(),
            ),
            onEvent = {},
            onNavigateToDetail = { _, _ -> },
        )
    }
}
