package com.mascotasperdidas.app.app.ui.screens.myreports

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.ui.components.MyReportItem
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    state: MyReportsUiState,
    onEvent: (MyReportsUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToReportDetail: (reportId: String, reportType: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.mis_reportes),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancelar),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.lostReports.isEmpty() && state.foundReports.isEmpty() -> {
                    EmptyMyReports(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (state.foundReports.isNotEmpty()) {
                            item(key = "header_found") {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.hallazgos),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            items(
                                items = state.foundReports,
                                key = { "found_${it.id}" },
                            ) { report ->
                                MyReportItem(
                                    report = report,
                                    onTap = {
                                        onEvent(
                                            MyReportsUiEvent.ReportClicked(
                                                report.id,
                                                report.type,
                                            ),
                                        )
                                        onNavigateToReportDetail(report.id, report.type.name)
                                    },
                                    onDelete = {
                                        onEvent(MyReportsUiEvent.DeleteReportRequested(report))
                                    },
                                )
                            }
                        }

                        if (state.lostReports.isNotEmpty()) {
                            item(key = "header_lost") {
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.desapariciones),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            items(
                                items = state.lostReports,
                                key = { "lost_${it.id}" },
                            ) { report ->
                                MyReportItem(
                                    report = report,
                                    onTap = {
                                        onEvent(
                                            MyReportsUiEvent.ReportClicked(
                                                report.id,
                                                report.type,
                                            ),
                                        )
                                        onNavigateToReportDetail(report.id, report.type.name)
                                    },
                                    onDelete = {
                                        onEvent(MyReportsUiEvent.DeleteReportRequested(report))
                                    },
                                )
                            }
                        }

                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    // ── Delete confirmation dialog ────────────────────────────────────
    state.reportToDelete?.let { report ->
        AlertDialog(
            onDismissRequest = { onEvent(MyReportsUiEvent.DismissDelete) },
            title = { Text(stringResource(R.string.eliminar)) },
            text = {
                Text(
                    text = "¿Eliminar el reporte de ${report.petName}?",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(MyReportsUiEvent.ConfirmDelete) },
                ) {
                    Text(
                        text = stringResource(R.string.eliminar),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(MyReportsUiEvent.DismissDelete) }) {
                    Text(stringResource(R.string.cancelar))
                }
            },
        )
    }
}

@Composable
private fun EmptyMyReports(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Inbox,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.sin_reportes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Previews ───────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun MyReportsScreenLoadingPreview() {
    MascotasPerdidasTheme {
        MyReportsScreen(
            state = MyReportsUiState(),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToReportDetail = { _, _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MyReportsScreenPopulatedPreview() {
    MascotasPerdidasTheme {
        MyReportsScreen(
            state = MyReportsUiState(
                isLoading = false,
                foundReports = listOf(
                    PetReport(
                        id = "1", ownerUid = "u", ownerInitial = "M", petName = "Max",
                        type = ReportType.FOUND_SIGHTING, breed = "Golden Retriever",
                        description = "Se escapó de casa el lunes.",
                        location = "Parque Simón Bolívar, Cúcuta",
                        imageUrl = "https://placedog.net/400/300?id=1",
                        recencyLabel = "RECIENTE",
                        createdAtEpochMs = System.currentTimeMillis(),
                    ),
                    PetReport(
                        id = "2", ownerUid = "u", ownerInitial = "M", petName = "Luna",
                        type = ReportType.FOUND_IN_CARE, breed = "Siamés",
                        description = "Encontrada en la calle.",
                        location = "Barrio La Riviera, Cúcuta",
                        imageUrl = "https://placedog.net/400/300?id=2",
                        recencyLabel = "AYER",
                        createdAtEpochMs = System.currentTimeMillis() - 86400000,
                    ),
                ),
                lostReports = listOf(
                    PetReport(
                        id = "3", ownerUid = "u", ownerInitial = "M", petName = "Rocky",
                        type = ReportType.LOST, breed = "Bulldog Francés",
                        description = "Se perdió en la avenida.",
                        location = "Av. Libertadores, Cúcuta",
                        imageUrl = "https://placedog.net/400/300?id=3",
                        recencyLabel = "RECIENTE",
                        createdAtEpochMs = System.currentTimeMillis(),
                    ),
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToReportDetail = { _, _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MyReportsScreenDeleteDialogPreview() {
    MascotasPerdidasTheme {
        MyReportsScreen(
            state = MyReportsUiState(
                isLoading = false,
                reportToDelete = PetReport(
                    id = "1", ownerUid = "u", ownerInitial = "M", petName = "Max",
                    type = ReportType.FOUND_SIGHTING, breed = "Golden Retriever",
                    description = "Se escapó de casa el lunes.",
                    location = "Parque Simón Bolívar, Cúcuta",
                    imageUrl = "https://placedog.net/400/300?id=1",
                    recencyLabel = "RECIENTE",
                    createdAtEpochMs = System.currentTimeMillis(),
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToReportDetail = { _, _ -> },
        )
    }
}
