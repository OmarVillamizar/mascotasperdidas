package com.mascotasperdidas.app.app.ui.screens.feed

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.ui.components.AppTopBar
import com.mascotasperdidas.app.app.ui.components.ContactOptionsDialog
import com.mascotasperdidas.app.app.ui.components.LocalDrawerOpener
import com.mascotasperdidas.app.app.ui.components.PetCard
import com.mascotasperdidas.app.domain.model.PetReport
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
    val context = LocalContext.current
    var contactTarget by remember { mutableStateOf<PetReport?>(null) }
    val noPhoneMsg = stringResource(R.string.contact_no_phone)
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.feed_title),
                onMenuClick = openDrawer ?: {},
                userInitial = state.currentUserInitial,
                photoUrl = null,
                onAvatarClick = {},
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToNewReport() },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.nuevo_reporte),
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
                    ) { CircularProgressIndicator() }
                }
                state.reports.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.emptyMessage.ifBlank { stringResource(R.string.feed_empty) },
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
                                onContactClick = {
                                    if (report.ownerPhone.isBlank()) {
                                        Toast.makeText(context, noPhoneMsg, Toast.LENGTH_SHORT).show()
                                    } else {
                                        contactTarget = report
                                    }
                                },
                                onDeleteClick = { onEvent(FeedUiEvent.DeleteRequested(report)) },
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

        if (state.reportPendingDelete != null) {
            AlertDialog(
                onDismissRequest = { onEvent(FeedUiEvent.DismissDelete) },
                title = { Text(stringResource(R.string.feed_dialog_delete_title)) },
                text = { Text(stringResource(R.string.feed_dialog_delete_msg)) },
                confirmButton = {
                    TextButton(onClick = { onEvent(FeedUiEvent.ConfirmDelete) }) {
                        Text(
                            text = stringResource(R.string.confirmar),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(FeedUiEvent.DismissDelete) }) {
                        Text(stringResource(R.string.cancelar))
                    }
                },
            )
        }

        contactTarget?.let { target ->
            ContactOptionsDialog(
                phone = target.ownerPhone,
                contactName = target.ownerName.ifBlank { target.petName },
                onDismiss = { contactTarget = null },
            )
        }
    }
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
            state = FeedUiState(selectedTab = LOST, reports = sampleReports, currentUserUid = "u1"),
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
