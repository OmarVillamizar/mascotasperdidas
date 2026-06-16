package com.mascotasperdidas.app.app.ui.screens.report.creation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.ui.components.ColorSwatchSelector
import com.mascotasperdidas.app.app.ui.components.FormSectionHeader
import com.mascotasperdidas.app.app.ui.components.LocationMapPicker
import com.mascotasperdidas.app.app.ui.components.MultiSelectChipGroup
import com.mascotasperdidas.app.app.ui.components.PhotoPickerRow
import com.mascotasperdidas.app.app.ui.components.SingleSelectChipGroup

private val speciesOptions = listOf("Perro", "Gato", "Otro")
private val sizeOptions = listOf("Pequeño", "Mediano", "Grande")
private val collarOptions = listOf("Sí", "No", "No recuerdo")
private val statusOptions = listOf("Herido", "Asustado", "Tranquilo", "Agresivo", "Hambriento")
private val petColorSwatches = listOf(
    "Negro" to Color(0xFF212121),
    "Marrón" to Color(0xFF795548),
    "Blanco" to Color(0xFFFFFFFF),
    "Gris" to Color(0xFF9E9E9E),
    "Dorado" to Color(0xFFFFD700),
    "Crema" to Color(0xFFFFF8DC),
    "Naranja" to Color(0xFFFF5722),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SightingReportFormScreen(
    state: SightingReportFormUiState,
    onEvent: (SightingReportFormUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onEvent(SightingReportFormUiEvent.PhotoAdded(it)) }
    }

    BackHandler(enabled = state.hasData) {
        onEvent(SightingReportFormUiEvent.ShowDiscardDialog)
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(SightingReportFormUiEvent.DismissError)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.avistamiento_en_calle)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.hasData) {
                            onEvent(SightingReportFormUiEvent.ShowDiscardDialog)
                        } else {
                            onEvent(SightingReportFormUiEvent.DiscardConfirmed)
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = { onEvent(SightingReportFormUiEvent.PublishSighting) },
                enabled = state.canPublish && !state.isPublishing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                ),
            ) {
                if (state.isPublishing) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.publicar_avistamiento))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            item {
                FormSectionHeader(title = "¿Qué viste?")
                Text(
                    text = stringResource(R.string.especie) + " *",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (state.speciesError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SingleSelectChipGroup(
                    options = speciesOptions,
                    selected = state.species.ifBlank { null },
                    onSelect = { onEvent(SightingReportFormUiEvent.SpeciesSelected(it)) },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.tamano),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SingleSelectChipGroup(
                    options = sizeOptions,
                    selected = state.size.ifBlank { null },
                    onSelect = { onEvent(SightingReportFormUiEvent.SizeSelected(it)) },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.color_predominante),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ColorSwatchSelector(
                    options = petColorSwatches,
                    selected = state.color.ifBlank { null },
                    onSelect = { onEvent(SightingReportFormUiEvent.ColorSelected(it)) },
                )
            }
            item {
                FormSectionHeader(title = "¿Cómo estaba?")
                Text(
                    "Estado (puedes seleccionar varios)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MultiSelectChipGroup(
                    options = statusOptions,
                    selected = state.statuses,
                    onToggle = { onEvent(SightingReportFormUiEvent.StatusToggled(it)) },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "¿Tenía collar?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SingleSelectChipGroup(
                    options = collarOptions,
                    selected = state.hasCollar.ifBlank { null },
                    onSelect = { onEvent(SightingReportFormUiEvent.CollarSelected(it)) },
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.aun_en_zona),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = state.stillInArea,
                        onCheckedChange = { onEvent(SightingReportFormUiEvent.StillInAreaChanged(it)) },
                    )
                }
            }
            item {
                FormSectionHeader(title = "¿Cuándo y dónde?")
                LocationMapPicker(
                    selectedLocation = state.selectedLocation,
                    onLocationPicked = { onEvent(SightingReportFormUiEvent.LocationPicked(it)) },
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.locationRef,
                    onValueChange = { onEvent(SightingReportFormUiEvent.LocationRefChanged(it)) },
                    label = { Text(stringResource(R.string.form_location_ref_label)) },
                    placeholder = { Text(stringResource(R.string.form_location_ref_hint_sighting)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                FormSectionHeader(title = stringResource(R.string.descripcion_detallada))
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { onEvent(SightingReportFormUiEvent.DescriptionChanged(it)) },
                    label = { Text(stringResource(R.string.form_add_details_label)) },
                    placeholder = { Text(stringResource(R.string.form_add_details_hint)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                FormSectionHeader(title = "${stringResource(R.string.fotos_del_animal)} (opcional)")
                PhotoPickerRow(
                    photos = state.photos,
                    maxPhotos = 1,
                    onAdd = {
                        photoLauncher.launch("image/*")
                    },
                    onRemove = { onEvent(SightingReportFormUiEvent.PhotoRemoved(it)) },
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (state.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(SightingReportFormUiEvent.DismissDiscardDialog) },
            title = { Text(stringResource(R.string.descartar_reporte)) },
            text = { Text(stringResource(R.string.descartar_confirmacion)) },
            confirmButton = {
                TextButton(onClick = { onEvent(SightingReportFormUiEvent.DiscardConfirmed) }) {
                    Text(stringResource(R.string.descartar), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(SightingReportFormUiEvent.DismissDiscardDialog) }) {
                    Text(stringResource(R.string.cancelar))
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SightingReportFormScreenPreview() {
    MascotasPerdidasTheme {
        SightingReportFormScreen(state = SightingReportFormUiState(), onEvent = {})
    }
}
