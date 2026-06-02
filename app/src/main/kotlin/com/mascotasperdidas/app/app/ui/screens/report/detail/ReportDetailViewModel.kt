package com.mascotasperdidas.app.app.ui.screens.report.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.app.navigation.Routes
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.domain.port.`in`.DeleteReport
import com.mascotasperdidas.app.domain.port.`in`.ObserveCurrentUser
import com.mascotasperdidas.app.domain.port.`in`.ObserveReports
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeReports: ObserveReports,
    private val observeCurrentUser: ObserveCurrentUser,
    private val deleteReport: DeleteReport,
) : ViewModel() {

    private val reportId: String =
        savedStateHandle[Routes.ReportDetail.ARG_REPORT_ID] ?: ""

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    private val _navigateBack = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    init {
        viewModelScope.launch {
            var hadReport = false
            combine(
                observeReports(ReportType.LOST),
                observeReports(ReportType.FOUND_SIGHTING),
                observeReports(ReportType.FOUND_IN_CARE),
                observeCurrentUser(),
            ) { lost, sightings, inCare, user ->
                val report = (lost + sightings + inCare).find { it.id == reportId }
                Pair(report, user)
            }.collect { (report, user) ->
                if (report != null) hadReport = true

                if (hadReport && report == null) {
                    _navigateBack.emit(Unit)
                    return@collect
                }

                _uiState.update {
                    it.copy(
                        report = report,
                        isLoading = false,
                        isOwner = user != null && user.uid == report?.ownerUid,
                    )
                }
            }
        }
    }

    fun onEvent(event: ReportDetailUiEvent) {
        when (event) {
            ReportDetailUiEvent.NavigateBack -> viewModelScope.launch { _navigateBack.emit(Unit) }
            ReportDetailUiEvent.ContactOwner -> _uiState.update {
                it.copy(contactSnackbar = "Contacto disponible próximamente")
            }
            ReportDetailUiEvent.DeleteReport -> _uiState.update { it.copy(showDeleteDialog = true) }
            ReportDetailUiEvent.DismissDelete -> _uiState.update { it.copy(showDeleteDialog = false) }
            ReportDetailUiEvent.ConfirmDelete -> {
                _uiState.update { it.copy(showDeleteDialog = false) }
                viewModelScope.launch {
                    try {
                        deleteReport(_uiState.value.report?.id ?: return@launch)
                        _navigateBack.emit(Unit)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = "Error al eliminar. Intenta de nuevo.") }
                    }
                }
            }
            ReportDetailUiEvent.DismissSnackbar -> _uiState.update { it.copy(contactSnackbar = null) }
        }
    }
}
