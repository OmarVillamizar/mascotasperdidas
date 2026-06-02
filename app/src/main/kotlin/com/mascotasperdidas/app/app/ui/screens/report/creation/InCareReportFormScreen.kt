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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.LocalCareColor
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.ui.components.FormSectionHeader
import com.mascotasperdidas.app.app.ui.components.LocationMapPicker
import com.mascotasperdidas.app.app.ui.components.MultiSelectChipGroup
import com.mascotasperdidas.app.app.ui.components.PhotoPickerRow
import com.mascotasperdidas.app.app.ui.components.SingleSelectChipGroup
import com.mascotasperdidas.app.app.ui.components.UrgencySelector

private val speciesOptions = listOf("Perro", "Gato", "Otro")
private val sizeOptions = listOf("Pequeño", "Mediano", "Grande")
private val genderOptions = listOf("Macho", "Hembra", "No sé")
private val ageOptions = listOf("Cachorro", "Joven", "Adulto", "Senior")
private val physicalStatusOptions = listOf("Herido", "Desnutrido", "Saludable", "Sucio", "Con pulgas")
private val behaviorOptions = listOf("Cariñoso", "Asustado", "Tranquilo", "Nervioso", "Agresivo")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InCareReportFormScreen(
    state: InCareReportFormUiState,
    onEvent: (InCareReportFormUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val careColor = LocalCareColor.current

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onEvent(InCareReportFormUiEvent.PhotoAdded(it)) }
    }

    BackHandler(enabled = state.hasData) {
        onEvent(InCareReportFormUiEvent.ShowDiscardDialog)
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(InCareReportFormUiEvent.DismissError)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.bajo_mi_cuidado)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.hasData) onEvent(InCareReportFormUiEvent.ShowDiscardDialog)
                        else onEvent(InCareReportFormUiEvent.DiscardConfirmed)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = { onEvent(InCareReportFormUiEvent.PublishReport) },
                enabled = state.canPublish && !state.isPublishing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = careColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                if (state.isPublishing) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.publicar_resguardo))
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
                FormSectionHeader(title = "${stringResource(R.string.fotos_del_animal)} (opcional)")
                PhotoPickerRow(
                    photos = state.photos,
                    maxPhotos = 6,
                    onAdd = {
                        photoLauncher.launch("image/*")
                    },
                    onRemove = { onEvent(InCareReportFormUiEvent.PhotoRemoved(it)) },
                )
            }
            item {
                FormSectionHeader(title = "¿Cómo es?")
                Text(stringResource(R.string.especie) + " *", style = MaterialTheme.typography.labelMedium,
                    color = if (state.speciesError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                SingleSelectChipGroup(
                    options = speciesOptions,
                    selected = state.species.ifBlank { null },
                    onSelect = { onEvent(InCareReportFormUiEvent.SpeciesSelected(it)) },
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.tamano), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SingleSelectChipGroup(
                    options = sizeOptions,
                    selected = state.size.ifBlank { null },
                    onSelect = { onEvent(InCareReportFormUiEvent.SizeSelected(it)) },
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.breed,
                    onValueChange = { onEvent(InCareReportFormUiEvent.BreedChanged(it)) },
                    label = { Text("Raza (si la conoces)") },
                    placeholder = { Text("Ej: Labrador, Siamés, Mestizo...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.sexo), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SingleSelectChipGroup(
                    options = genderOptions,
                    selected = state.gender.ifBlank { null },
                    onSelect = { onEvent(InCareReportFormUiEvent.GenderSelected(it)) },
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.edad_aprox), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SingleSelectChipGroup(
                    options = ageOptions,
                    selected = state.ageRange.ifBlank { null },
                    onSelect = { onEvent(InCareReportFormUiEvent.AgeRangeSelected(it)) },
                )
            }
            item {
                FormSectionHeader(title = "Identificación")
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.tiene_collar), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = state.hasCollarPlate, onCheckedChange = { onEvent(InCareReportFormUiEvent.CollarPlateChanged(it)) })
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.tiene_microchip), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = state.hasMicrochip, onCheckedChange = { onEvent(InCareReportFormUiEvent.MicrochipChanged(it)) })
                }
            }
            item {
                FormSectionHeader(title = "Estado y comportamiento")
                Text(stringResource(R.string.estado_fisico), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                MultiSelectChipGroup(
                    options = physicalStatusOptions,
                    selected = state.physicalStatus,
                    onToggle = { onEvent(InCareReportFormUiEvent.PhysicalStatusToggled(it)) },
                )
                Spacer(Modifier.height(8.dp))
                Text("Comportamiento", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                MultiSelectChipGroup(
                    options = behaviorOptions,
                    selected = state.behaviors,
                    onToggle = { onEvent(InCareReportFormUiEvent.BehaviorToggled(it)) },
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { onEvent(InCareReportFormUiEvent.NotesChanged(it)) },
                    label = { Text(stringResource(R.string.notas)) },
                    placeholder = { Text("Cualquier detalle importante sobre su estado") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                FormSectionHeader(title = "Urgencia / disponibilidad")
                UrgencySelector(
                    selected = state.urgency,
                    onSelect = { onEvent(InCareReportFormUiEvent.UrgencySelected(it)) },
                )
            }
            item {
                FormSectionHeader(title = "Ubicación")
                LocationMapPicker(
                    selectedLocation = state.selectedLocation,
                    onLocationPicked = { onEvent(InCareReportFormUiEvent.LocationPicked(it)) },
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.locationRef,
                    onValueChange = { onEvent(InCareReportFormUiEvent.LocationRefChanged(it)) },
                    label = { Text("Referencia de ubicación") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (state.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(InCareReportFormUiEvent.DismissDiscardDialog) },
            title = { Text(stringResource(R.string.descartar_reporte)) },
            text = { Text(stringResource(R.string.descartar_confirmacion)) },
            confirmButton = {
                TextButton(onClick = { onEvent(InCareReportFormUiEvent.DiscardConfirmed) }) {
                    Text(stringResource(R.string.descartar), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(InCareReportFormUiEvent.DismissDiscardDialog) }) {
                    Text(stringResource(R.string.cancelar))
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InCareReportFormScreenPreview() {
    MascotasPerdidasTheme {
        InCareReportFormScreen(state = InCareReportFormUiState(), onEvent = {})
    }
}
