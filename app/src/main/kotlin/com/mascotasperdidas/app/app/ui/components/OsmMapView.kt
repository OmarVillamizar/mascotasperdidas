package com.mascotasperdidas.app.app.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mascotasperdidas.app.domain.model.ReportType
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

data class OsmMarker(
    val id: String,
    val position: GeoPoint,
    val title: String,
    val icon: Drawable?,
    val reportType: ReportType,
)

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    center: GeoPoint = GeoPoint(7.89705, -72.50809),
    zoom: Double = 14.0,
    markers: List<OsmMarker> = emptyList(),
    onMapClick: ((GeoPoint) -> Unit)? = null,
    onMarkerClick: ((OsmMarker) -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.controller.setZoom(zoom)
            view.controller.setCenter(center)

            view.overlays.clear()

            if (onMapClick != null) {
                val receiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        p?.let { onMapClick(it) }
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                }
                view.overlays.add(MapEventsOverlay(receiver))
            }

            markers.forEach { markerData ->
                val marker = Marker(view).apply {
                    position = markerData.position
                    title = markerData.title
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    markerData.icon?.let { icon = it }
                    setOnMarkerClickListener { _, _ ->
                        onMarkerClick?.invoke(markerData)
                        true
                    }
                }
                view.overlays.add(marker)
            }

            view.invalidate()
        }
    )
}
