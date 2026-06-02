package com.mascotasperdidas.app.app.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.domain.port.`in`.DeleteReport
import com.mascotasperdidas.app.domain.port.`in`.ObserveCurrentUser
import com.mascotasperdidas.app.domain.port.`in`.ObserveReports
import com.mascotasperdidas.app.domain.port.`in`.SearchReports
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val observeReports: ObserveReports,
    private val searchReports: SearchReports,
    private val deleteReport: DeleteReport,
    private val observeCurrentUser: ObserveCurrentUser,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(ReportType.LOST)
    private val _query = MutableStateFlow("")

    // Avistadas tab merges FOUND_SIGHTING + FOUND_IN_CARE into one flow.
    private fun observeForTab(tab: ReportType): Flow<List<PetReport>> =
        if (tab == ReportType.FOUND_SIGHTING) {
            combine(
                observeReports(ReportType.FOUND_SIGHTING),
                observeReports(ReportType.FOUND_IN_CARE),
            ) { sightings, inCare ->
                (sightings + inCare).sortedByDescending { it.createdAtEpochMs }
            }
        } else {
            observeReports(tab)
        }

    init {
        viewModelScope.launch {
            combine(_selectedTab, _query) { tab, q -> Pair(tab, q) }
                .flatMapLatest { (tab, q) ->
                    _uiState.update { it.copy(isLoading = true) }
                    if (q.isBlank()) {
                        observeForTab(tab)
                    } else {
                        flow {
                            try {
                                emit(searchReports(q, tab))
                            } catch (e: Exception) {
                                emit(emptyList<PetReport>())
                            }
                        }
                    }
                }
                .collect { reports ->
                    _uiState.update { it.copy(reports = reports, isLoading = false, error = null) }
                }
        }

        viewModelScope.launch {
            observeCurrentUser().collect { user ->
                _uiState.update { it.copy(currentUserUid = user?.uid) }
            }
        }
    }

    fun onEvent(event: FeedUiEvent) {
        when (event) {
            is FeedUiEvent.TabSelected -> {
                _selectedTab.value = event.tab
                _uiState.update { it.copy(selectedTab = event.tab) }
            }
            is FeedUiEvent.QueryChanged -> {
                _query.value = event.query
                _uiState.update { it.copy(query = event.query) }
            }
            is FeedUiEvent.DeleteReport -> {
                viewModelScope.launch {
                    try {
                        deleteReport(event.id)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = "Error al eliminar publicación") }
                    }
                }
            }
            is FeedUiEvent.ReportClicked -> { /* handled via onNavigateToReportDetail in Screen */ }
            is FeedUiEvent.ContactClicked -> { /* future */ }
        }
    }
}
