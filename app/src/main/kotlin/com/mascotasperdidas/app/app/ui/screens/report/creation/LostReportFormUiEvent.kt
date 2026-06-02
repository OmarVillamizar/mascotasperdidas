package com.mascotasperdidas.app.app.ui.screens.report.creation

import android.net.Uri

sealed class LostReportFormUiEvent {
    data class PhotosAdded(val uris: List<Uri>) : LostReportFormUiEvent()
    data class PhotoRemoved(val index: Int) : LostReportFormUiEvent()
    data class NameChanged(val value: String) : LostReportFormUiEvent()
    data class SpeciesSelected(val value: String) : LostReportFormUiEvent()
    data class BreedChanged(val value: String) : LostReportFormUiEvent()
    data class GenderSelected(val value: String) : LostReportFormUiEvent()
    data class AgeChanged(val value: String) : LostReportFormUiEvent()
    data class ColorChanged(val value: String) : LostReportFormUiEvent()
    data class DescriptionChanged(val value: String) : LostReportFormUiEvent()
    data class LocationChanged(val value: String) : LostReportFormUiEvent()
    data class LocationPicked(val lat: Double, val lng: Double) : LostReportFormUiEvent()
    object PublishReport : LostReportFormUiEvent()
    object ShowDiscardDialog : LostReportFormUiEvent()
    object DismissDiscardDialog : LostReportFormUiEvent()
    object DiscardConfirmed : LostReportFormUiEvent()
    object DismissError : LostReportFormUiEvent()
}
