package com.mascotasperdidas.app.app.ui.screens.report.creation

import android.net.Uri
import org.osmdroid.util.GeoPoint

sealed class SightingReportFormUiEvent {
    data class SpeciesSelected(val value: String) : SightingReportFormUiEvent()
    data class SizeSelected(val value: String) : SightingReportFormUiEvent()
    data class ColorSelected(val value: String) : SightingReportFormUiEvent()
    data class StatusToggled(val status: String) : SightingReportFormUiEvent()
    data class CollarSelected(val value: String) : SightingReportFormUiEvent()
    data class StillInAreaChanged(val value: Boolean) : SightingReportFormUiEvent()
    data class LocationPicked(val geoPoint: GeoPoint) : SightingReportFormUiEvent()
    data class LocationRefChanged(val value: String) : SightingReportFormUiEvent()
    data class DescriptionChanged(val value: String) : SightingReportFormUiEvent()
    data class PhotoAdded(val uri: Uri) : SightingReportFormUiEvent()
    data class PhotoRemoved(val index: Int) : SightingReportFormUiEvent()
    object PublishSighting : SightingReportFormUiEvent()
    object ShowDiscardDialog : SightingReportFormUiEvent()
    object DismissDiscardDialog : SightingReportFormUiEvent()
    object DiscardConfirmed : SightingReportFormUiEvent()
    object DismissError : SightingReportFormUiEvent()
}
