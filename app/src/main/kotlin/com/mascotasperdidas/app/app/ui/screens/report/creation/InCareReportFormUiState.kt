package com.mascotasperdidas.app.app.ui.screens.report.creation

import android.net.Uri
import com.mascotasperdidas.app.app.ui.components.CareUrgency
import org.osmdroid.util.GeoPoint

data class InCareReportFormUiState(
    val photos: List<Uri> = emptyList(),
    val species: String = "",
    val size: String = "",
    val breed: String = "",
    val gender: String = "",
    val ageRange: String = "",
    val hasCollarPlate: Boolean = false,
    val hasMicrochip: Boolean = false,
    val physicalStatus: Set<String> = emptySet(),
    val behaviors: Set<String> = emptySet(),
    val notes: String = "",
    val urgency: CareUrgency = CareUrgency.INDEFINITE,
    val selectedLocation: GeoPoint? = null,
    val locationRef: String = "",
    val isPublishing: Boolean = false,
    val publishSuccess: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val error: String? = null,
    val speciesError: Boolean = false,
) {
    val canPublish: Boolean get() = species.isNotBlank()
    val hasData: Boolean get() = species.isNotBlank() || notes.isNotBlank() || photos.isNotEmpty()
}
