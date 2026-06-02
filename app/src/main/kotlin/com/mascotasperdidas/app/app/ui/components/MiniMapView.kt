package com.mascotasperdidas.app.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.util.createPinDrawable
import com.mascotasperdidas.app.domain.model.ReportType
import org.osmdroid.util.GeoPoint

@Composable
fun MiniMapView(
    latitude: Double,
    longitude: Double,
    type: ReportType,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val geoPoint = remember(latitude, longitude) { GeoPoint(latitude, longitude) }
    val pinIcon = remember(type) { createPinDrawable(context, type) }

    OsmMapView(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp)),
        center = geoPoint,
        zoom = 15.0,
        markers = listOf(
            OsmMarker(
                id = "pin",
                position = geoPoint,
                title = "",
                icon = pinIcon,
                reportType = type,
            )
        ),
        onMapClick = null,
    )
}

@Preview(showBackground = true)
@Composable
private fun MiniMapViewPreview() {
    MascotasPerdidasTheme {
        MiniMapView(
            latitude = 7.8939,
            longitude = -72.5078,
            type = ReportType.LOST,
        )
    }
}
