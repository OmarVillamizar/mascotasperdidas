package com.mascotasperdidas.app.app.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.domain.port.`in`.ObserveReports
import com.mascotasperdidas.app.domain.port.`in`.SearchReports
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    // ── Flows separados para tab y query ────────────────────────────
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _selectedTab = MutableStateFlow(ReportType.LOST)

    private val _query = MutableStateFlow("")

    init {
        viewModelScope.launch {
            combine(_selectedTab, _query) { tab, q -> Pair(tab, q) }
                .flatMapLatest { (tab, q) ->
                    _uiState.update { it.copy(isLoading = true) }
                    if (q.isBlank()) {
                        observeReports(tab)
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
                    _uiState.update {
                        it.copy(reports = reports, isLoading = false, error = null)
                    }
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
            is FeedUiEvent.ReportClicked -> { /* Fase futura: detalle */ }
            is FeedUiEvent.ContactClicked -> { /* Fase futura: chat */ }
        }
    }
}
