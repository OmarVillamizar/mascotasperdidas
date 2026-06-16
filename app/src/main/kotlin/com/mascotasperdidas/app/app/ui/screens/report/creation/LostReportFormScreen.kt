package com.mascotasperdidas.app.app.ui.screens.report.creation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.ui.components.FormSectionHeader
import com.mascotasperdidas.app.app.ui.components.LocationMapPicker
import com.mascotasperdidas.app.app.ui.components.PhotoPickerRow
import com.mascotasperdidas.app.app.ui.components.SingleSelectChipGroup
import org.osmdroid.util.GeoPoint

private val speciesOptions = listOf("Perro", "Gato", "Otro")
private val genderOptions = listOf("Macho", "Hembra", "No sé")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostReportFormScreen(
    state: LostReportFormUiState,
    onEvent: (LostReportFormUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) onEvent(LostReportFormUiEvent.PhotosAdded(uris))
    }

    BackHandler(enabled = state.hasData) {
        onEvent(LostReportFormUiEvent.ShowDiscardDialog)
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(LostReportFormUiEvent.DismissError)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.generar_reporte)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.hasData) {
                            onEvent(LostReportFormUiEvent.ShowDiscardDialog)
                        } else {
                            onEvent(LostReportFormUiEvent.DiscardConfirmed)
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = { onEvent(LostReportFormUiEvent.PublishReport) },
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
                    Text(stringResource(R.string.publicar_reporte))
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
                FormSectionHeader(title = stringResource(R.string.fotos_del_animal))
                PhotoPickerRow(
                    photos = state.photos,
                    maxPhotos = 6,
                    onAdd = {
                        photoLauncher.launch("image/*")
                    },
                    onRemove = { index -> onEvent(LostReportFormUiEvent.PhotoRemoved(index)) },
                )
                Text(
                    text = stringResource(R.string.sube_fotos),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            item {
                FormSectionHeader(title = "Información básica")
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { onEvent(LostReportFormUiEvent.NameChanged(it)) },
                    label = { Text(stringResource(R.string.nombre_mascota) + " *") },
                    isError = state.nameError,
                    supportingText = if (state.nameError) {
                        { Text(stringResource(R.string.error_campo_requerido)) }
                    } else {
                        null
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.especie) + " *",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (state.speciesError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SingleSelectChipGroup(
                    options = speciesOptions,
                    selected = state.species.ifBlank { null },
                    onSelect = { onEvent(LostReportFormUiEvent.SpeciesSelected(it)) },
                )
                if (state.speciesError) {
                    Text(
                        text = stringResource(R.string.error_selecciona_especie),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.breed,
                    onValueChange = { onEvent(LostReportFormUiEvent.BreedChanged(it)) },
                    label = { Text(stringResource(R.string.raza)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.sexo),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SingleSelectChipGroup(
                    options = genderOptions,
                    selected = state.gender.ifBlank { null },
                    onSelect = { onEvent(LostReportFormUiEvent.GenderSelected(it)) },
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.ageApprox,
                    onValueChange = { onEvent(LostReportFormUiEvent.AgeChanged(it)) },
                    label = { Text(stringResource(R.string.edad_aprox)) },
                    placeholder = { Text(stringResource(R.string.form_age_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.color,
                    onValueChange = { onEvent(LostReportFormUiEvent.ColorChanged(it)) },
                    label = { Text(stringResource(R.string.color_predominante)) },
                    placeholder = { Text(stringResource(R.string.form_color_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                FormSectionHeader(title = stringResource(R.string.descripcion_detallada))
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { onEvent(LostReportFormUiEvent.DescriptionChanged(it)) },
                    label = { Text(stringResource(R.string.descripcion_detallada)) },
                    placeholder = { Text(stringResource(R.string.form_description_hint)) },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                FormSectionHeader(title = stringResource(R.string.ultima_ubicacion))
                OutlinedTextField(
                    value = state.locationRef,
                    onValueChange = { onEvent(LostReportFormUiEvent.LocationChanged(it)) },
                    label = { Text(stringResource(R.string.form_location_ref_label)) },
                    placeholder = { Text(stringResource(R.string.form_location_ref_hint_lost)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                LocationMapPicker(
                    selectedLocation = if (state.latitude != null && state.longitude != null) {
                        GeoPoint(state.latitude!!, state.longitude!!)
                    } else {
                        null
                    },
                    onLocationPicked = { geo ->
                        onEvent(LostReportFormUiEvent.LocationPicked(geo.latitude, geo.longitude))
                    },
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (state.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(LostReportFormUiEvent.DismissDiscardDialog) },
            title = { Text(stringResource(R.string.descartar_reporte)) },
            text = { Text(stringResource(R.string.descartar_confirmacion)) },
            confirmButton = {
                TextButton(onClick = { onEvent(LostReportFormUiEvent.DiscardConfirmed) }) {
                    Text(stringResource(R.string.descartar), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(LostReportFormUiEvent.DismissDiscardDialog) }) {
                    Text(stringResource(R.string.cancelar))
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LostReportFormScreenPreview() {
    MascotasPerdidasTheme {
        LostReportFormScreen(state = LostReportFormUiState(), onEvent = {})
    }
}
