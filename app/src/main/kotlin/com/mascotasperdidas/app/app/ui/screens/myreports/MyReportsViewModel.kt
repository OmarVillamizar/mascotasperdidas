package com.mascotasperdidas.app.app.ui.screens.myreports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.domain.port.`in`.DeleteReport
import com.mascotasperdidas.app.domain.port.`in`.ObserveCurrentUser
import com.mascotasperdidas.app.domain.port.`in`.ObserveReports
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyReportsViewModel @Inject constructor(
    private val observeReports: ObserveReports,
    private val observeCurrentUser: ObserveCurrentUser,
    private val deleteReport: DeleteReport,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyReportsUiState())
    val uiState: StateFlow<MyReportsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeReports(ReportType.LOST),
                observeReports(ReportType.FOUND_SIGHTING),
                observeReports(ReportType.FOUND_IN_CARE),
                observeCurrentUser(),
            ) { lost, sightings, inCare, user ->
                val uid = user?.uid.orEmpty()
                val lostByMe = lost.filter { it.ownerUid == uid }
                val foundByMe = (sightings + inCare).filter { it.ownerUid == uid }
                _uiState.update {
                    it.copy(
                        lostReports = lostByMe,
                        foundReports = foundByMe,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onEvent(event: MyReportsUiEvent) {
        when (event) {
            is MyReportsUiEvent.DeleteReportRequested ->
                _uiState.update { it.copy(reportToDelete = event.report) }

            MyReportsUiEvent.ConfirmDelete -> {
                val report = _uiState.value.reportToDelete ?: return
                viewModelScope.launch {
                    _uiState.update { it.copy(reportToDelete = null) }
                    try {
                        deleteReport(report.id)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = "Error al eliminar. Intenta de nuevo.") }
                    }
                }
            }

            MyReportsUiEvent.DismissDelete ->
                _uiState.update { it.copy(reportToDelete = null) }

            is MyReportsUiEvent.ReportClicked -> { /* navigation handled in Screen */ }
        }
    }
}
