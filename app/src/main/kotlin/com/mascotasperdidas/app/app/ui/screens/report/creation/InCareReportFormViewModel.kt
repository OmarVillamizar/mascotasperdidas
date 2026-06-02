package com.mascotasperdidas.app.app.ui.screens.report.creation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.app.ui.components.CareUrgency
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
class InCareReportFormViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val createReport: CreateReport,
    private val observeCurrentUser: ObserveCurrentUser,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InCareReportFormUiState())
    val uiState: StateFlow<InCareReportFormUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    private val _navigateToConfirmed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToConfirmed: SharedFlow<Unit> = _navigateToConfirmed.asSharedFlow()

    fun onEvent(event: InCareReportFormUiEvent) {
        when (event) {
            is InCareReportFormUiEvent.PhotoAdded -> {
                if (_uiState.value.photos.size < 6) {
                    _uiState.update { it.copy(photos = it.photos + event.uri) }
                }
            }
            is InCareReportFormUiEvent.PhotoRemoved -> {
                val updated = _uiState.value.photos.toMutableList().also { it.removeAt(event.index) }
                _uiState.update { it.copy(photos = updated) }
            }
            is InCareReportFormUiEvent.SpeciesSelected -> _uiState.update { it.copy(species = event.value, speciesError = false) }
            is InCareReportFormUiEvent.SizeSelected -> _uiState.update { it.copy(size = event.value) }
            is InCareReportFormUiEvent.BreedChanged -> _uiState.update { it.copy(breed = event.value) }
            is InCareReportFormUiEvent.GenderSelected -> _uiState.update { it.copy(gender = event.value) }
            is InCareReportFormUiEvent.AgeRangeSelected -> _uiState.update { it.copy(ageRange = event.value) }
            is InCareReportFormUiEvent.CollarPlateChanged -> _uiState.update { it.copy(hasCollarPlate = event.value) }
            is InCareReportFormUiEvent.MicrochipChanged -> _uiState.update { it.copy(hasMicrochip = event.value) }
            is InCareReportFormUiEvent.PhysicalStatusToggled -> {
                val updated = _uiState.value.physicalStatus.toMutableSet().apply {
                    if (event.status in this) remove(event.status) else add(event.status)
                }
                _uiState.update { it.copy(physicalStatus = updated) }
            }
            is InCareReportFormUiEvent.BehaviorToggled -> {
                val updated = _uiState.value.behaviors.toMutableSet().apply {
                    if (event.behavior in this) remove(event.behavior) else add(event.behavior)
                }
                _uiState.update { it.copy(behaviors = updated) }
            }
            is InCareReportFormUiEvent.NotesChanged -> _uiState.update { it.copy(notes = event.value) }
            is InCareReportFormUiEvent.UrgencySelected -> _uiState.update { it.copy(urgency = event.urgency) }
            is InCareReportFormUiEvent.LocationPicked -> _uiState.update { it.copy(selectedLocation = event.geoPoint) }
            is InCareReportFormUiEvent.LocationRefChanged -> _uiState.update { it.copy(locationRef = event.value) }
            InCareReportFormUiEvent.PublishReport -> publishReport()
            InCareReportFormUiEvent.ShowDiscardDialog -> _uiState.update { it.copy(showDiscardDialog = true) }
            InCareReportFormUiEvent.DismissDiscardDialog -> _uiState.update { it.copy(showDiscardDialog = false) }
            InCareReportFormUiEvent.DiscardConfirmed -> viewModelScope.launch { _navigateBack.emit(Unit) }
            InCareReportFormUiEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun publishReport() {
        val state = _uiState.value
        if (state.species.isBlank()) { _uiState.update { it.copy(speciesError = true) }; return }

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
                    petName = "Bajo mi cuidado",
                    type = ReportType.FOUND_IN_CARE,
                    breed = state.breed.trim(),
                    species = state.species,
                    size = state.size,
                    gender = state.gender,
                    ageApprox = state.ageRange,
                    hasCollarPlate = state.hasCollarPlate,
                    hasMicrochip = state.hasMicrochip,
                    physicalStatus = state.physicalStatus.toList(),
                    behaviors = state.behaviors.toList(),
                    notes = state.notes.trim(),
                    urgency = state.urgency.name,
                    latitude = state.selectedLocation?.latitude,
                    longitude = state.selectedLocation?.longitude,
                    description = state.notes.trim(),
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
