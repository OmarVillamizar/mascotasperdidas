package com.mascotasperdidas.app.app.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.domain.port.`in`.CreateReport
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val observeReports: ObserveReports,
    private val searchReports: SearchReports,
    private val createReport: CreateReport,
    private val deleteReport: DeleteReport,
    private val observeCurrentUser: ObserveCurrentUser,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(ReportType.LOST)
    private val _query = MutableStateFlow("")

    companion object {
        val PRESET_IMAGES = linkedMapOf(
            "Perro 1" to "https://placedog.net/400/300?id=1",
            "Perro 2" to "https://placedog.net/400/300?id=3",
            "Perro 3" to "https://placedog.net/400/300?id=5",
            "Gato 1" to "https://picsum.photos/seed/luna-cat/400/300",
            "Gato 2" to "https://picsum.photos/seed/gato2/400/300",
            "Gato 3" to "https://picsum.photos/seed/gato3/400/300",
        )
    }

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
            FeedUiEvent.OpenCreateDialog -> {
                _uiState.update {
                    it.copy(showCreateDialog = true, newReportType = _uiState.value.selectedTab)
                }
            }
            FeedUiEvent.DismissCreateDialog -> {
                _uiState.update { it.copy(showCreateDialog = false) }
                resetCreateForm()
            }
            is FeedUiEvent.NewReportNameChanged -> _uiState.update { it.copy(newReportName = event.name) }
            is FeedUiEvent.NewReportBreedChanged -> _uiState.update { it.copy(newReportBreed = event.breed) }
            is FeedUiEvent.NewReportDescriptionChanged -> _uiState.update { it.copy(newReportDescription = event.description) }
            is FeedUiEvent.NewReportLocationChanged -> _uiState.update { it.copy(newReportLocation = event.location) }
            is FeedUiEvent.NewReportTypeChanged -> _uiState.update { it.copy(newReportType = event.type) }
            is FeedUiEvent.NewReportImageChanged -> _uiState.update { it.copy(newReportImageKey = event.key) }
            FeedUiEvent.CreateReport -> createNewReport()
            is FeedUiEvent.DeleteReport -> deleteReportById(event.id)
            is FeedUiEvent.ReportClicked -> { /* wired in Phase 2D-1 */ }
            is FeedUiEvent.ContactClicked -> { /* wired in Phase 2D-1 */ }
        }
    }

    private fun deleteReportById(id: String) {
        viewModelScope.launch {
            try {
                deleteReport(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al eliminar publicación") }
            }
        }
    }

    private fun createNewReport() {
        viewModelScope.launch {
            val state = _uiState.value
            if (!state.canCreateReport) return@launch

            _uiState.update { it.copy(isCreating = true) }
            try {
                val currentUser = observeCurrentUser().firstOrNull()
                val ownerUid = currentUser?.uid ?: "anon"
                val ownerInitial = currentUser?.displayName?.firstOrNull()?.uppercase() ?: "?"
                val imageUrl = PRESET_IMAGES[state.newReportImageKey]
                    ?: PRESET_IMAGES.values.first()

                val report = PetReport(
                    id = "",
                    ownerUid = ownerUid,
                    ownerInitial = ownerInitial,
                    ownerName = currentUser?.displayName ?: "",
                    petName = state.newReportName.trim(),
                    type = state.newReportType,
                    breed = state.newReportBreed.trim(),
                    description = state.newReportDescription.trim(),
                    location = state.newReportLocation.trim(),
                    imageUrl = imageUrl,
                    recencyLabel = "RECIENTE",
                    createdAtEpochMs = System.currentTimeMillis(),
                )
                createReport(report, emptyList())

                _uiState.update { it.copy(isCreating = false, showCreateDialog = false) }
                resetCreateForm()
            } catch (e: Exception) {
                _uiState.update { it.copy(isCreating = false, error = "Error al crear publicación") }
            }
        }
    }

    private fun resetCreateForm() {
        _uiState.update {
            it.copy(
                newReportName = "",
                newReportBreed = "",
                newReportDescription = "",
                newReportLocation = "",
                newReportImageKey = "Perro 1",
            )
        }
    }
}
