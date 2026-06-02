package com.mascotasperdidas.app.app.ui.screens.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.ui.components.AppTopBar
import com.mascotasperdidas.app.app.ui.components.LocalDrawerOpener
import com.mascotasperdidas.app.app.ui.components.PetCard
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.domain.model.ReportType.FOUND_SIGHTING
import com.mascotasperdidas.app.domain.model.ReportType.LOST

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    state: FeedUiState,
    onEvent: (FeedUiEvent) -> Unit,
    onNavigateToNewReport: () -> Unit = {},
    onNavigateToReportDetail: (reportId: String, reportType: String) -> Unit = { _, _ -> },
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val openDrawer = LocalDrawerOpener.current
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.feed_title),
                onMenuClick = openDrawer ?: {},
                userInitial = "?",
                photoUrl = null,
                onAvatarClick = {},
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(FeedUiEvent.OpenCreateDialog) },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Crear publicación",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(FeedUiEvent.QueryChanged(it)) },
                placeholder = { Text(stringResource(R.string.feed_search_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (state.showFilterBanner) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.feed_filter_banner),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            TabRow(
                selectedTabIndex = if (state.selectedTab == LOST) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Tab(
                    selected = state.selectedTab == LOST,
                    onClick = { onEvent(FeedUiEvent.TabSelected(LOST)) },
                    text = { Text(stringResource(R.string.feed_tab_lost)) },
                )
                Tab(
                    selected = state.selectedTab != LOST,
                    onClick = { onEvent(FeedUiEvent.TabSelected(FOUND_SIGHTING)) },
                    text = { Text(stringResource(R.string.feed_tab_found)) },
                )
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.reports.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.emptyMessage.ifBlank { "No hay publicaciones" },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.reports, key = { it.id }) { report ->
                            PetCard(
                                report = report,
                                isOwner = state.currentUserUid != null &&
                                    state.currentUserUid == report.ownerUid,
                                onMoreInfoClick = {
                                    onNavigateToReportDetail(report.id, report.type.name)
                                },
                                onContactClick = { onEvent(FeedUiEvent.ContactClicked(report.id)) },
                                onDeleteClick = { onEvent(FeedUiEvent.DeleteReport(report.id)) },
                            )
                        }
                    }
                }
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }

    if (state.showCreateDialog) {
        CreateReportDialog(state = state, onEvent = onEvent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateReportDialog(
    state: FeedUiState,
    onEvent: (FeedUiEvent) -> Unit,
) {
    var typeExpanded by remember { mutableStateOf(false) }
    var imageExpanded by remember { mutableStateOf(false) }
    val imageOptions = FeedViewModel.PRESET_IMAGES.keys.toList()

    AlertDialog(
        onDismissRequest = { onEvent(FeedUiEvent.DismissCreateDialog) },
        title = { Text("Crear publicación") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.newReportName,
                    onValueChange = { onEvent(FeedUiEvent.NewReportNameChanged(it)) },
                    label = { Text("Nombre de la mascota") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                ) {
                    OutlinedTextField(
                        value = if (state.newReportType == ReportType.LOST) "Perdido" else "Avistamiento",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Perdido") },
                            onClick = {
                                onEvent(FeedUiEvent.NewReportTypeChanged(ReportType.LOST))
                                typeExpanded = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Avistamiento") },
                            onClick = {
                                onEvent(FeedUiEvent.NewReportTypeChanged(ReportType.FOUND_SIGHTING))
                                typeExpanded = false
                            },
                        )
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = imageExpanded,
                    onExpandedChange = { imageExpanded = it },
                ) {
                    OutlinedTextField(
                        value = state.newReportImageKey,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Imagen") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = imageExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = imageExpanded,
                        onDismissRequest = { imageExpanded = false },
                    ) {
                        imageOptions.forEach { key ->
                            DropdownMenuItem(
                                text = { Text(key) },
                                onClick = {
                                    onEvent(FeedUiEvent.NewReportImageChanged(key))
                                    imageExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = state.newReportBreed,
                    onValueChange = { onEvent(FeedUiEvent.NewReportBreedChanged(it)) },
                    label = { Text("Raza") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.newReportLocation,
                    onValueChange = { onEvent(FeedUiEvent.NewReportLocationChanged(it)) },
                    label = { Text("Ubicación") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.newReportDescription,
                    onValueChange = { onEvent(FeedUiEvent.NewReportDescriptionChanged(it)) },
                    label = { Text("Descripción") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onEvent(FeedUiEvent.CreateReport) },
                enabled = state.canCreateReport && !state.isCreating,
            ) {
                Text(if (state.isCreating) "Guardando..." else "Publicar")
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(FeedUiEvent.DismissCreateDialog) }) {
                Text("Cancelar")
            }
        },
    )
}

private val sampleReports = listOf(
    PetReport(
        id = "pv-001", ownerUid = "u1", ownerInitial = "M", petName = "Max",
        type = LOST, breed = "Golden Retriever",
        description = "Max se escapó de casa el lunes por la tarde. Lleva collar rojo.",
        location = "Parque Simón Bolívar, Cúcuta",
        imageUrl = "https://placedog.net/400/300?id=1",
        recencyLabel = "RECIENTE", createdAtEpochMs = System.currentTimeMillis(),
    ),
    PetReport(
        id = "pv-002", ownerUid = "u2", ownerInitial = "A", petName = "Luna",
        type = FOUND_SIGHTING, breed = "Siamés",
        description = "Encontrada cerca del centro comercial. Collar azul sin placa.",
        location = "Centro Comercial Ventura Plaza, Cúcuta",
        imageUrl = "https://picsum.photos/seed/luna-cat/400/300",
        recencyLabel = "RECIENTE", createdAtEpochMs = System.currentTimeMillis(),
    ),
)

@Preview(showBackground = true)
@Composable
private fun FeedScreenPreview() {
    MascotasPerdidasTheme {
        FeedScreen(
            state = FeedUiState(
                selectedTab = LOST,
                reports = sampleReports,
                currentUserUid = "u1",
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedScreenEmptyPreview() {
    MascotasPerdidasTheme {
        FeedScreen(state = FeedUiState(), onEvent = {})
    }
}
