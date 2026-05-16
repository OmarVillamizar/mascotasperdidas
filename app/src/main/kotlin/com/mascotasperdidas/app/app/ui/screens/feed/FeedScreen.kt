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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.ui.components.AppTopBar
import com.mascotasperdidas.app.app.ui.components.PetCard
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.domain.model.ReportType.FOUND
import com.mascotasperdidas.app.domain.model.ReportType.LOST

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    state: FeedUiState,
    onEvent: (FeedUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.feed_title),
                onMenuClick = { /* Fase 13: drawer */ },
                userInitial = "?",
                photoUrl = null,
                onAvatarClick = { /* Fase 13 */ },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // ── Search field ────────────────────────────────────────
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

            // ── Filter banner ───────────────────────────────────────
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

            // ── Tab row ─────────────────────────────────────────────
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
                    selected = state.selectedTab == FOUND,
                    onClick = { onEvent(FeedUiEvent.TabSelected(FOUND)) },
                    text = { Text(stringResource(R.string.feed_tab_found)) },
                )
            }

            // ── Content ─────────────────────────────────────────────
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
                            text = state.emptyMessage.ifBlank {
                                stringResource(R.string.feed_tab_lost).let { "No hay mascotas en $it" }
                            },
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
                                onMoreInfoClick = {
                                    onEvent(FeedUiEvent.ReportClicked(report.id))
                                },
                                onContactClick = {
                                    onEvent(FeedUiEvent.ContactClicked(report.id))
                                },
                            )
                        }
                    }
                }
            }

            // ── Error ───────────────────────────────────────────────
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
}

// ── Mock data para previews ────────────────────────────────────────
private val sampleReports = listOf(
    PetReport(
        id = "pv-001", ownerUid = "u1", ownerInitial = "M", petName = "Max",
        type = LOST, breed = "Golden Retriever",
        description = "Max se escapó de casa el lunes por la tarde. Lleva collar rojo.",
        location = "Parque Simón Bolívar, Bogotá",
        imageUrl = "https://placedog.net/400/300?id=1",
        recencyLabel = "RECIENTE", createdAtEpochMs = System.currentTimeMillis(),
    ),
    PetReport(
        id = "pv-002", ownerUid = "u2", ownerInitial = "A", petName = "Luna",
        type = FOUND, breed = "Siamés",
        description = "Encontrada cerca del centro comercial. Collar azul sin placa.",
        location = "Centro Comercial Andino, Bogotá",
        imageUrl = "https://placekitten.com/400/300",
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
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedScreenEmptyPreview() {
    MascotasPerdidasTheme {
        FeedScreen(
            state = FeedUiState(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedScreenSearchPreview() {
    MascotasPerdidasTheme {
        FeedScreen(
            state = FeedUiState(
                selectedTab = FOUND,
                query = "gato",
                reports = sampleReports.filter { it.type == FOUND },
            ),
            onEvent = {},
        )
    }
}
