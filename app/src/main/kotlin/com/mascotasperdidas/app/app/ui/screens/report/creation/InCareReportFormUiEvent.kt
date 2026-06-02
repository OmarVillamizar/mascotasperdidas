package com.mascotasperdidas.app.app.ui.screens.report.creation

import android.net.Uri
import com.mascotasperdidas.app.app.ui.components.CareUrgency
import org.osmdroid.util.GeoPoint

sealed class InCareReportFormUiEvent {
    data class PhotoAdded(val uri: Uri) : InCareReportFormUiEvent()
    data class PhotoRemoved(val index: Int) : InCareReportFormUiEvent()
    data class SpeciesSelected(val value: String) : InCareReportFormUiEvent()
    data class SizeSelected(val value: String) : InCareReportFormUiEvent()
    data class BreedChanged(val value: String) : InCareReportFormUiEvent()
    data class GenderSelected(val value: String) : InCareReportFormUiEvent()
    data class AgeRangeSelected(val value: String) : InCareReportFormUiEvent()
    data class CollarPlateChanged(val value: Boolean) : InCareReportFormUiEvent()
    data class MicrochipChanged(val value: Boolean) : InCareReportFormUiEvent()
    data class PhysicalStatusToggled(val status: String) : InCareReportFormUiEvent()
    data class BehaviorToggled(val behavior: String) : InCareReportFormUiEvent()
    data class NotesChanged(val value: String) : InCareReportFormUiEvent()
    data class UrgencySelected(val urgency: CareUrgency) : InCareReportFormUiEvent()
    data class LocationPicked(val geoPoint: GeoPoint) : InCareReportFormUiEvent()
    data class LocationRefChanged(val value: String) : InCareReportFormUiEvent()
    object PublishReport : InCareReportFormUiEvent()
    object ShowDiscardDialog : InCareReportFormUiEvent()
    object DismissDiscardDialog : InCareReportFormUiEvent()
    object DiscardConfirmed : InCareReportFormUiEvent()
    object DismissError : InCareReportFormUiEvent()
}
