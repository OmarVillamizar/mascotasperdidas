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
class LostReportFormViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val createReport: CreateReport,
    private val observeCurrentUser: ObserveCurrentUser,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LostReportFormUiState())
    val uiState: StateFlow<LostReportFormUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    private val _navigateToConfirmed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToConfirmed: SharedFlow<Unit> = _navigateToConfirmed.asSharedFlow()

    fun onEvent(event: LostReportFormUiEvent) {
        when (event) {
            is LostReportFormUiEvent.PhotosAdded -> {
                val combined = (_uiState.value.photos + event.uris).take(6)
                _uiState.update { it.copy(photos = combined) }
            }
            is LostReportFormUiEvent.PhotoRemoved -> {
                val updated = _uiState.value.photos.toMutableList().also { it.removeAt(event.index) }
                _uiState.update { it.copy(photos = updated) }
            }
            is LostReportFormUiEvent.NameChanged -> _uiState.update { it.copy(name = event.value, nameError = false) }
            is LostReportFormUiEvent.SpeciesSelected -> _uiState.update { it.copy(
                species = event.value,
                speciesError = false
            ) }
            is LostReportFormUiEvent.BreedChanged -> _uiState.update { it.copy(breed = event.value) }
            is LostReportFormUiEvent.GenderSelected -> _uiState.update { it.copy(gender = event.value) }
            is LostReportFormUiEvent.AgeChanged -> _uiState.update { it.copy(ageApprox = event.value) }
            is LostReportFormUiEvent.ColorChanged -> _uiState.update { it.copy(color = event.value) }
            is LostReportFormUiEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.value) }
            is LostReportFormUiEvent.LocationChanged -> _uiState.update { it.copy(locationRef = event.value) }
            LostReportFormUiEvent.PublishReport -> publishReport()
            LostReportFormUiEvent.ShowDiscardDialog -> _uiState.update { it.copy(showDiscardDialog = true) }
            LostReportFormUiEvent.DismissDiscardDialog -> _uiState.update { it.copy(showDiscardDialog = false) }
            LostReportFormUiEvent.DiscardConfirmed -> viewModelScope.launch { _navigateBack.emit(Unit) }
            LostReportFormUiEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun publishReport() {
        val state = _uiState.value
        var hasError = false
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = true) };
            hasError = true
        }
        if (state.species.isBlank()) {
            _uiState.update { it.copy(speciesError = true) };
            hasError = true
        }
        if (hasError) return

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
                    petName = state.name.trim(),
                    type = ReportType.LOST,
                    breed = state.breed.trim(),
                    species = state.species,
                    gender = state.gender,
                    ageApprox = state.ageApprox.trim(),
                    color = state.color.trim(),
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
