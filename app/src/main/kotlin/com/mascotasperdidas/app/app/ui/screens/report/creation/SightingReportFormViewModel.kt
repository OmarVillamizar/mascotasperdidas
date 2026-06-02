package com.mascotasperdidas.app.app.ui.screens.report.creation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.domain.port.`in`.CreateReport
import com.mascotasperdidas.app.domain.port.`in`.ObserveCurrentUser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SightingReportFormViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val createReport: CreateReport,
    private val observeCurrentUser: ObserveCurrentUser,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SightingReportFormUiState())
    val uiState: StateFlow<SightingReportFormUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    private val _navigateToConfirmed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToConfirmed: SharedFlow<Unit> = _navigateToConfirmed.asSharedFlow()

    fun onEvent(event: SightingReportFormUiEvent) {
        when (event) {
            is SightingReportFormUiEvent.SpeciesSelected -> _uiState.update { it.copy(species = event.value, speciesError = false) }
            is SightingReportFormUiEvent.SizeSelected -> _uiState.update { it.copy(size = event.value) }
            is SightingReportFormUiEvent.ColorSelected -> _uiState.update { it.copy(color = event.value) }
            is SightingReportFormUiEvent.StatusToggled -> {
                val updated = _uiState.value.statuses.toMutableSet().apply {
                    if (event.status in this) remove(event.status) else add(event.status)
                }
                _uiState.update { it.copy(statuses = updated) }
            }
            is SightingReportFormUiEvent.CollarSelected -> _uiState.update { it.copy(hasCollar = event.value) }
            is SightingReportFormUiEvent.StillInAreaChanged -> _uiState.update { it.copy(stillInArea = event.value) }
            is SightingReportFormUiEvent.LocationPicked -> _uiState.update { it.copy(selectedLocation = event.geoPoint) }
            is SightingReportFormUiEvent.LocationRefChanged -> _uiState.update { it.copy(locationRef = event.value) }
            is SightingReportFormUiEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.value) }
            is SightingReportFormUiEvent.PhotoAdded -> {
                if (_uiState.value.photos.size < 6) {
                    _uiState.update { it.copy(photos = it.photos + event.uri) }
                }
            }
            is SightingReportFormUiEvent.PhotoRemoved -> {
                val updated = _uiState.value.photos.toMutableList().also { it.removeAt(event.index) }
                _uiState.update { it.copy(photos = updated) }
            }
            SightingReportFormUiEvent.PublishSighting -> publishSighting()
            SightingReportFormUiEvent.ShowDiscardDialog -> _uiState.update { it.copy(showDiscardDialog = true) }
            SightingReportFormUiEvent.DismissDiscardDialog -> _uiState.update { it.copy(showDiscardDialog = false) }
            SightingReportFormUiEvent.DiscardConfirmed -> viewModelScope.launch { _navigateBack.emit(Unit) }
            SightingReportFormUiEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun publishSighting() {
        val state = _uiState.value
        if (state.species.isBlank()) {
            _uiState.update { it.copy(speciesError = true) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true) }
            try {
                val user = observeCurrentUser().firstOrNull()
                val imageBytesList = state.photos.map { uri ->
                    withContext(Dispatchers.IO) {
                        appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: byteArrayOf()
                    }
                }
                val report = PetReport(
                    id = "",
                    ownerUid = user?.uid ?: "anon",
                    ownerInitial = user?.displayName?.firstOrNull()?.uppercase() ?: "?",
                    ownerName = user?.displayName ?: "",
                    petName = "Avistamiento",
                    type = ReportType.FOUND_SIGHTING,
                    breed = "",
                    species = state.species,
                    size = state.size,
                    color = state.color,
                    statuses = state.statuses.toList(),
                    collarColor = state.hasCollar,
                    stillInArea = state.stillInArea,
                    latitude = state.selectedLocation?.latitude,
                    longitude = state.selectedLocation?.longitude,
                    description = state.description.trim(),
                    location = state.locationRef.trim(),
                    imageUrl = "",
                    recencyLabel = "RECIENTE",
                    createdAtEpochMs = System.currentTimeMillis(),
                )
                createReport(report, imageBytesList)
                _uiState.update { it.copy(isPublishing = false, publishSuccess = true) }
                _navigateToConfirmed.emit(Unit)
            } catch (e: Exception) {
                _uiState.update { it.copy(isPublishing = false, error = e.message ?: "Error al publicar") }
            }
        }
    }
}
