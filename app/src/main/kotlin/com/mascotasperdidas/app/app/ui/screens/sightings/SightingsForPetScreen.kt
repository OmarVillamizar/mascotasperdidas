package com.mascotasperdidas.app.app.ui.screens.sightings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.ui.components.OsmMapView
import com.mascotasperdidas.app.app.ui.components.OsmMarker
import com.mascotasperdidas.app.app.ui.components.SightingItem
import com.mascotasperdidas.app.app.util.createPinDrawable
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import org.osmdroid.util.GeoPoint

@Composable
fun SightingsForPetScreen(
    state: SightingsForPetUiState,
    onEvent: (SightingsForPetUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToReportDetail: (reportId: String, reportType: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pet = state.petReport

    val mapCenter = remember(pet) {
        if (pet?.latitude != null && pet?.longitude != null) {
            GeoPoint(pet.latitude!!, pet.longitude!!)
        } else {
            GeoPoint(7.89705, -72.50809)
        }
    }

    val markers = remember(state.sightings) {
        state.sightings.map { sighting ->
            OsmMarker(
                id = sighting.id,
                position = GeoPoint(sighting.latitude!!, sighting.longitude!!),
                title = sighting.petName,
                icon = createPinDrawable(context, ReportType.FOUND_SIGHTING),
                reportType = ReportType.FOUND_SIGHTING,
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // ── Map header ───────────────────────────────────
                    item(key = "map_header") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                        ) {
                            OsmMapView(
                                modifier = Modifier.fillMaxSize(),
                                center = mapCenter,
                                zoom = 13.0,
                                markers = markers,
                            )

                            // Back button overlay
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.85f))
                                    .size(40.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cancelar),
                                    tint = Color.Black,
                                )
                            }

                            // Pet name overlay at bottom of map
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.5f),
                                        shape = MaterialTheme.shapes.small,
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = "Avistamientos de ${pet?.petName.orEmpty()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                if (pet != null) {
                                    Text(
                                        text = "${pet.breed} • ${pet.recencyLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f),
                                    )
                                }
                            }
                        }
                    }

                    // ── "Ver mapa completo" button ───────────────────
                    item(key = "view_map") {
                        FilledTonalButton(
                            onClick = onNavigateToMap,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Map,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(stringResource(R.string.ver_mapa_completo))
                        }
                    }

                    // ── Sightings count ───────────────────────────────
                    item(key = "count") {
                        Text(
                            text = "${state.sightings.size} avistamiento(s)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }

                    // ── Sightings list ────────────────────────────────
                    if (state.sightings.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                text = stringResource(R.string.sin_reportes),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                            )
                        }
                    } else {
                        items(
                            items = state.sightings,
                            key = { it.id },
                        ) { sighting ->
                            SightingItem(
                                sighting = sighting,
                                onTap = {
                                    onNavigateToReportDetail(
                                        sighting.id,
                                        sighting.type.name,
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun SightingsForPetScreenLoadingPreview() {
    MascotasPerdidasTheme {
        SightingsForPetScreen(
            state = SightingsForPetUiState(),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToMap = {},
            onNavigateToReportDetail = { _, _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SightingsForPetScreenPopulatedPreview() {
    MascotasPerdidasTheme {
        SightingsForPetScreen(
            state = SightingsForPetUiState(
                isLoading = false,
                petReport = PetReport(
                    id = "lost-1",
                    ownerUid = "u",
                    ownerInitial = "M",
                    petName = "Luna",
                    type = ReportType.LOST,
                    breed = "Golden Retriever",
                    description = "Se perdió en el parque.",
                    location = "Parque Simón Bolívar, Cúcuta",
                    imageUrl = "https://placedog.net/400/300?id=1",
                    recencyLabel = "Hace 3 días",
                    createdAtEpochMs = System.currentTimeMillis(),
                    latitude = 7.89705,
                    longitude = -72.50809,
                    species = "Perro",
                ),
                sightings = listOf(
                    PetReport(
                        id = "s1",
                        ownerUid = "u2",
                        ownerInitial = "C",
                        ownerName = "Carlos M.",
                        petName = "Perro visto",
                        type = ReportType.FOUND_SIGHTING,
                        breed = "Golden Retriever",
                        description = "Lo vi cerca del parque principal.",
                        location = "Parque Simón Bolívar",
                        imageUrl = "",
                        recencyLabel = "Hace 2 horas",
                        createdAtEpochMs = System.currentTimeMillis(),
                        latitude = 7.8965,
                        longitude = -72.5075,
                        species = "Perro",
                    ),
                    PetReport(
                        id = "s2",
                        ownerUid = "u3",
                        ownerInitial = "P",
                        ownerName = "Paula A.",
                        petName = "Golden visto",
                        type = ReportType.FOUND_SIGHTING,
                        breed = "Golden Retriever",
                        description = "Caminando por la avenida.",
                        location = "Av. Libertadores",
                        imageUrl = "",
                        recencyLabel = "Ayer",
                        createdAtEpochMs = System.currentTimeMillis() - 86400000,
                        latitude = 7.8980,
                        longitude = -72.5090,
                        species = "Perro",
                    ),
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToMap = {},
            onNavigateToReportDetail = { _, _ -> },
        )
    }
}
