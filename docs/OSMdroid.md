Aquí tienes un archivo **.md** con las reglas o guía de implementación de **OSMDroid** de forma completamente gratuita, usando los tiles de OpenStreetMap (u otras fuentes libres). Está pensado para tu proyecto universitario en Android Studio con Kotlin.

```markdown
# Implementación de OSMDroid en Android (Kotlin) – Gratis y sin API Key

Esta guía te permite añadir un mapa geográfico funcional a tu app de «mascotas perdidas» con costo **cero**, sin necesidad de registrarte, sin límites de uso y sin tarjeta de crédito.

## ¿Qué es OSMDroid?

OSMDroid (OpenStreetMap Viewer for Android) es una librería open‑source que reemplaza a `MapView` de Google Maps. Descarga y muestra los **tiles** (fragmentos de mapa) desde servidores públicos como OpenStreetMap (OSM). No requiere clave de API y se puede usar offline con caché.

## 1. Agregar la dependencia

En tu archivo `build.gradle.kts` (nivel de módulo `app`):

```kotlin
dependencies {
    implementation("org.osmdroid:osmdroid-android:6.1.20")
}
```

Sincroniza el proyecto.

## 2. Permisos en AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /> <!-- caché -->
<uses-permission android:name="android.permission.INTERNET" />
```

> **Nota**: En Android 6+ debes solicitar los permisos de localización en tiempo de ejecución (ver código más abajo).

## 3. Configuración inicial (directorio de caché)

En tu `Application` o en la primera actividad:

```kotlin
import org.osmdroid.config.Configuration

class MiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Configura la carpeta donde se guardarán los tiles (offline)
        Configuration.getInstance().load(
            applicationContext,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        // Opcional: aumentar el tamaño de caché
        Configuration.getInstance().cacheMapTileCount = 5000
    }
}
```

Recuerda declarar `MiApp` en el manifest:

```xml
<application android:name=".MiApp" ... >
```

## 4. Añadir el MapView al layout

**activity_main.xml**

```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <org.osmdroid.views.MapView
        android:id="@+id/mapView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</LinearLayout>
```

## 5. Inicializar y usar el mapa en tu Activity (Kotlin)

```kotlin
import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private val REQUEST_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar configuración de OSMDroid (si no lo hiciste en Application)
        Configuration.getInstance().load(
            applicationContext,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        mapView = findViewById(R.id.mapView)
        // Fuente de tiles: MAPNIK = OpenStreetMap estándar
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true) // zoom con dos dedos

        // Centrar en Cúcuta (Colombia)
        val cucuta = GeoPoint(7.89705, -72.50809)
        mapView.controller.setZoom(14.0)
        mapView.controller.setCenter(cucuta)

        // Solicitar permisos de localización (opcional, para mostrar la ubicación actual)
        requestLocationPermission()

        // Agregar un marcador de ejemplo
        addMarker(cucuta, "Mascota perdida aquí")
    }

    private fun addMarker(point: GeoPoint, title: String) {
        val marker = Marker(mapView)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        // Puedes cambiar el icono
        // marker.icon = ContextCompat.getDrawable(this, R.drawable.ic_pet)
        mapView.overlays.add(marker)
        mapView.invalidate() // refrescar
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION
            )
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
```

## 6. Fuentes de tiles gratuitas (más allá de OpenStreetMap)

OSMDroid permite usar otros proveedores de tiles sin costo. Solo cambia `TileSourceFactory`:

| TileSourceFactory constante | Descripción |
|-----------------------------|-------------|
| `TileSourceFactory.MAPNIK`  | Estándar de OSM (calles, nombres) |
| `TileSourceFactory.CYCLEMAP`| Ciclovías, rutas (OpenCycleMap) |
| `TileSourceFactory.HIKEBIKEMAP` | Mapas para senderismo |
| `TileSourceFactory.USGS_SAT` | Imágenes satelitales (USGS) |
| `TileSourceFactory.USGS_TOPO` | Mapas topográficos |

Si quieres usar un servidor de tiles personalizado (por ejemplo, de algún proveedor gratuito como CartoDB), puedes definir tu propio `ITileSource`:

```kotlin
val cartoDb = XYTileSource(
    "CartoDB",
    1, 18, 256, ".png",
    arrayOf("https://a.basemaps.cartocdn.com/light_all/")
)
mapView.setTileSource(cartoDb)
```

> **Importante**: Siempre verifica los términos de uso del proveedor. Para un proyecto universitario, OpenStreetMap (MAPNIK) es suficiente y libre.

## 7. Agregar funcionalidad de «marcar mascota perdida»

Puedes permitir que el usuario toque el mapa y guarde la posición. Escucha el evento `setOnMapClickListener`:

```kotlin
mapView.setOnMapClickListener { geoPoint ->
    addMarker(geoPoint, "Mascota reportada aquí")
    // Guardar geoPoint.latitude y geoPoint.longitude en tu base de datos
}
```

## 8. Opcional: Usar OSMDroid con Jetpack Compose

Si tu proyecto usa Compose, crea un `AndroidView` que envuelva el `MapView` tradicional:

```kotlin
@Composable
fun OSMComposeView() {
    AndroidView(factory = { context ->
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            controller.setZoom(14.0)
            controller.setCenter(GeoPoint(7.89705, -72.50809))
            setMultiTouchControls(true)
        }
    })
}
```

## 9. ¿Qué costo tiene esto? Cero absoluto

- **No pagas nada** por usar los tiles de OpenStreetMap.
- No hay API key que vencer o que genere factura.
- Los tiles se descargan y se almacenan en caché para reducir el tráfico.
- Puedes distribuir tu app en Google Play sin restricciones adicionales.

## 10. Posibles limitaciones y cómo solucionarlas

| Limitación | Solución |
|------------|----------|
| El mapa tarda en cargar tiles | La caché mejora tras la primera visita. Puedes precargar un área usando `TileLoader`. |
| Necesitas offline total | Usa `MapView.getTileProvider().getTileCache().put()` o librerías como `OSMDroidBonusPack` para descargar regiones. |
| El diseño de OSM es muy simple | Cambia la fuente de tiles a una más vistosa (por ejemplo, `TileSourceFactory.MAPNIK` es la más clara). |

## Conclusión

Con esta guía tienes un mapa geográfico completamente funcional, gratuito y sin ataduras comerciales, ideal para tu proyecto universitario en Cúcuta. OSMDroid es estable, está en producción en miles de apps y te permite centrarte en la lógica de «mascotas perdidas» sin preocuparte por costos inesperados.

**¡Manos a la obra!**
```

Este archivo puedes guardarlo como `OSMDroid-Guia.md` y seguir sus pasos. Si necesitas ajustar algo o añadir más detalles (como la opción de usar MapLibre), solo dímelo.