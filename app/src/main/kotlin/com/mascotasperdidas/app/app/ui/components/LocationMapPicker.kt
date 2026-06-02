package com.mascotasperdidas.app.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.util.createPinDrawable
import com.mascotasperdidas.app.domain.model.ReportType
import org.osmdroid.util.GeoPoint

private val DefaultCenter = GeoPoint(7.89705, -72.50809)

@Composable
fun LocationMapPicker(
    selectedLocation: GeoPoint?,
    onLocationPicked: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pinIcon = remember { createPinDrawable(context, ReportType.FOUND_SIGHTING) }

    Column(modifier = modifier) {
        OsmMapView(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp)),
            center = selectedLocation ?: DefaultCenter,
            zoom = 14.0,
            markers = selectedLocation?.let {
                listOf(OsmMarker("picked", it, "", pinIcon, ReportType.FOUND_SIGHTING))
            } ?: emptyList(),
            onMapClick = onLocationPicked,
        )
        Text(
            text = stringResource(R.string.toca_para_ajustar_ubicacion),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationMapPickerPreview() {
    MascotasPerdidasTheme {
        LocationMapPicker(
            selectedLocation = null,
            onLocationPicked = {},
        )
    }
}
