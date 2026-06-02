# Technical Documentation — MapScreen
## Pantallas 6 · 7 · 8: Home Mapa · Mapa con Filtros · Mapa con Preview de Pin

> **Nota:** Las tres pantallas son **estados de una misma pantalla** (`MapScreen`).  
> Pantalla 6 = estado base. Pantalla 7 = BottomSheet de filtros abierto. Pantalla 8 = BottomSheet de preview de pin abierto.

---

## 1. SCREEN IDENTITY

| Campo | Valor |
|---|---|
| **Screen name** | `MapScreen` |
| **Route** | `Routes.Map` |
| **Screen type** | Stack (con `DrawerShell`) |
| **Entry points** | `DrawerShell` → item "Mapa" (desde cualquier pantalla con drawer: Feed, Profile, Settings, Permissions) |
| **Exit points** | Drawer → cualquier otra ruta con drawer; "Más Información" → [ASSUMED] `Routes.ReportDetail`; "CONTACTAR" → [ASSUMED] intent externo (llamada/WhatsApp) o `Routes.Contact` |

---

## 2. VISUAL COMPONENTS

### 2.1 Componentes Visibles — Pantalla 6 (Estado Base)

| # | Nombre Composable | Equivalente MD3 (Compose) | Variante | Estados visibles |
|---|---|---|---|---|
| 1 | `AppTopBar` | `CenterAlignedTopAppBar` | Existente en `app/ui/components/` | default |
| 2 | `MapView` | **No es MD3** — `AndroidView` wrapping Google Maps SDK o `MapboxMap` | Mapa interactivo full-screen debajo del TopAppBar | default, loading [ASSUMED] |
| 3 | `PetMapPin` (LOST) | Composable custom | Pin rojo con foto circular del reporte (imagen del animal recortada en círculo dentro del marcador) | default, selected (pin agrandado [ASSUMED]) |
| 4 | `PetMapPin` (FOUND) | Composable custom | Pin verde con foto circular del reporte | default, selected [ASSUMED] |
| 5 | `MapBottomBar` | `BottomAppBar` o `Surface` con `Row` | Barra flotante con 3 íconos centrados, fondo blanco, esquinas redondeadas (pill shape) | default |
| 6 | `AddReportFabIcon` | `Icon` dentro de `MapBottomBar` | Ícono `+` | default, pressed [ASSUMED] |
| 7 | `FilterIcon` | `Icon` dentro de `MapBottomBar` | Ícono embudo/filtro `⛉` | default, pressed, active (cuando hay filtros aplicados [ASSUMED]) |
| 8 | `SearchMapIcon` | `Icon` dentro de `MapBottomBar` | Ícono lupa `🔍` | default, pressed [ASSUMED] |

### 2.2 Componentes Visibles — Pantalla 7 (Filtros Abiertos)

| # | Nombre Composable | Equivalente MD3 (Compose) | Variante | Estados visibles |
|---|---|---|---|---|
| 9 | `FiltersBottomSheet` | `ModalBottomSheet` | Con `dragHandle` visible (pill gris centrado arriba) | expanded, hidden |
| 10 | `LostFilterCheckbox` | `Row { Checkbox(...) + Text(...) }` | Label "Perdidos" a la izquierda, `Checkbox` a la derecha del label | checked, unchecked |
| 11 | `FoundFilterCheckbox` | `Row { Checkbox(...) + Text(...) }` | Label "Hallazgos" a la izquierda, `Checkbox` a la derecha | checked, unchecked |
| 12 | `SpeciesFilterRow` | `ListItem` o `Row` con `Text` + `Icon(chevron_right)` | Ítem navegable → [ASSUMED] abre sub-sheet o dropdown | default, pressed |
| 13 | `SearchRadiusLabel` | `Text` | `bodyMedium` / `bodyLarge` | default |
| 14 | `SearchRadiusSlider` | `Slider` | Color `primary` (`#6750A4`), thumb visible, track fill | default, dragging |
| 15 | `ApplyFiltersButton` | `Button` (FilledButton) | Color `primary`, texto "Aplicar Filtros", alineado a la derecha | default, pressed, loading [ASSUMED] |

### 2.3 Componentes Visibles — Pantalla 8 (Preview de Pin)

| # | Nombre Composable | Equivalente MD3 (Compose) | Variante | Estados visibles |
|---|---|---|---|---|
| 16 | `PinPreviewBottomSheet` | `ModalBottomSheet` | Con `dragHandle` visible | expanded, hidden |
| 17 | `PetPreviewTitle` | `Text` | `titleLarge` bold, centrado, texto "MAX" | default |
| 18 | `PetPreviewDescription` | `Text` | `bodyMedium`, máximo 4–5 líneas, color `onSurface` | default |
| 19 | `PetPreviewImage` | `AsyncImage` (Coil) dentro de `Box` con `clip(CircleShape)` | Imagen circular, ~80–100dp, alineada a la derecha del texto | default, loading (placeholder), error (placeholder) |
| 20 | `MoreInfoButton` | `OutlinedButton` | Texto "Mas Información", esquinas redondeadas | default, pressed |
| 21 | `ContactButton` | `Button` (FilledButton) | Color `tertiary` (`#2E7D32`), texto "CONTACTAR" en blanco | default, pressed |

> **[ASSUMED]** El mapa en Pantallas 7 y 8 queda visible pero oscurecido/scrimmed por el BottomSheet, comportamiento nativo de `ModalBottomSheet` en Compose.

---

## 3. DATA & STATE

### 3.1 Local State (UiState)

```kotlin
data class MapUiState(
    val reports: List<PetReport> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Filtros
    val filterLost: Boolean = true,
    val filterFound: Boolean = true,
    val filterSpecies: String? = null,         // null = todas las especies [ASSUMED]
    val searchRadiusKm: Float = 5f,            // [ASSUMED] valor inicial mitad del slider
    
    // UI state de sheets
    val bottomSheetState: MapBottomSheetState = MapBottomSheetState.None,
    val selectedReport: PetReport? = null,     // El pin tocado para el preview
    
    // Mapa
    val userLocation: LatLng? = null,          // [ASSUMED] para centrar mapa
    val cameraPosition: LatLng = LatLng(-4.0, -79.2) // [ASSUMED] coordenadas Cúcuta/Colombia
)

sealed class MapBottomSheetState {
    object None : MapBottomSheetState()
    object Filters : MapBottomSheetState()
    data class PinPreview(val report: PetReport) : MapBottomSheetState()
}
```

### 3.2 UiEvent

```kotlin
sealed class MapUiEvent {
    object OpenFilterSheet : MapUiEvent()
    object CloseSheet : MapUiEvent()
    object ApplyFilters : MapUiEvent()
    object OpenSearch : MapUiEvent()               // [ASSUMED] abre SearchBar o navega
    object AddReport : MapUiEvent()                // [ASSUMED] mismo flujo que FAB del Feed
    data class PinClicked(val reportId: String) : MapUiEvent()
    data class FilterLostChanged(val checked: Boolean) : MapUiEvent()
    data class FilterFoundChanged(val checked: Boolean) : MapUiEvent()
    data class RadiusChanged(val km: Float) : MapUiEvent()
    data class SpeciesChanged(val species: String?) : MapUiEvent()
    data class MoreInfoClicked(val reportId: String) : MapUiEvent()
    data class ContactClicked(val reportId: String) : MapUiEvent()
}
```

### 3.3 Global State (Stores/Ports)

| Store / Puerto | Lectura | Escritura |
|---|---|---|
| `ObserveReports` (puerto in) | Lista completa de `PetReport` — filtrada localmente por tipo/radio | No |
| `ObserveCurrentUser` (puerto in) | `User` para avatar en TopAppBar y para determinar ownership [ASSUMED] | No |
| `SearchReports` (puerto in) | [ASSUMED] si se implementa búsqueda textual desde esta pantalla | No |

### 3.4 API / Firebase Calls

| Operación | Puerto In | Cuándo se dispara | Qué hace |
|---|---|---|---|
| `ObserveReports(LOST)` | `observeReports(ReportType.LOST)` | `LaunchedEffect(Unit)` al montar | Stream de reportes perdidos para pines rojos |
| `ObserveReports(FOUND)` | `observeReports(ReportType.FOUND)` | `LaunchedEffect(Unit)` al montar | Stream de reportes hallados para pines verdes |
| `SearchReports` | `searchReports(query, type)` | [ASSUMED] al tocar el ícono de lupa | Filtra reportes por texto |

> **Nota de arquitectura:** Los dos `observeReports` se pueden combinar con `combine()` en el ViewModel en un solo `StateFlow<List<PetReport>>`. El filtrado por radio se hace localmente si no hay geolocalización en el modelo (ver §7).

### 3.5 Loading States

| Zona | Comportamiento |
|---|---|
| Mapa completo | [ASSUMED] `CircularProgressIndicator` centrado sobre el mapa mientras se cargan los reportes por primera vez |
| `PetPreviewImage` | Coil `placeholder(R.drawable.ic_logo)` + `error(R.drawable.ic_logo)` mientras carga la imagen del pin |
| `ApplyFiltersButton` | [ASSUMED] estado `enabled = false` + ícono de carga mientras se re-fetcha tras aplicar filtros |

### 3.6 Empty States

| Situación | Comportamiento |
|---|---|
| Sin reportes en el mapa | [ASSUMED] Ningún pin visible. Opcionalmente: `Snackbar` o texto overlay "No hay reportes en esta zona" |
| Sin resultados tras filtrar | [ASSUMED] `Snackbar`: "No se encontraron reportes con esos filtros" |

### 3.7 Error States

| Situación | Comportamiento |
|---|---|
| Fallo al cargar reportes de Firestore | [ASSUMED] `Snackbar` con mensaje de error. El mapa queda visible pero sin pines |
| Sin permisos de ubicación | [ASSUMED] El mapa carga centrado en posición por defecto (Colombia). No se muestra "mi ubicación" |

---

## 4. INTERACTIONS & BEHAVIOR

### 4.1 Interacciones

| Trigger | Acción | Resultado |
|---|---|---|
| Tap en `PetMapPin` (cualquier pin) | `onEvent(MapUiEvent.PinClicked(reportId))` | Abre `PinPreviewBottomSheet` con datos del reporte seleccionado. Pin se "selecciona" visualmente (escala mayor [ASSUMED]) |
| Tap en ícono filtro `⛉` | `onEvent(MapUiEvent.OpenFilterSheet)` | Abre `FiltersBottomSheet` |
| Tap en drag handle / fuera del sheet | `onEvent(MapUiEvent.CloseSheet)` | Cierra el BottomSheet activo. `selectedReport = null` |
| Cambio de `Checkbox` Perdidos | `onEvent(MapUiEvent.FilterLostChanged(checked))` | Actualiza `filterLost` en `UiState`. No aplica hasta "Aplicar Filtros" [ASSUMED] |
| Cambio de `Checkbox` Hallazgos | `onEvent(MapUiEvent.FilterFoundChanged(checked))` | Actualiza `filterFound` en `UiState` |
| Drag del `Slider` radio | `onEvent(MapUiEvent.RadiusChanged(km))` | Actualiza `searchRadiusKm` en tiempo real (label numérico debería reflejarlo [ASSUMED]) |
| Tap en "Especie" (chevron) | `onEvent(MapUiEvent.SpeciesChanged(...))` | [ASSUMED] Abre sub-BottomSheet o `DropdownMenu` con opciones: Perro, Gato, Otro |
| Tap en "Aplicar Filtros" | `onEvent(MapUiEvent.ApplyFilters)` | Cierra `FiltersBottomSheet`. Re-filtra los pines en el mapa según los parámetros activos |
| Tap en "Mas Información" | `onEvent(MapUiEvent.MoreInfoClicked(reportId))` | [ASSUMED] Navega a `Routes.ReportDetail` (pantalla pendiente) o expande el mismo sheet |
| Tap en "CONTACTAR" | `onEvent(MapUiEvent.ContactClicked(reportId))` | [ASSUMED] Abre intent externo: llamada al teléfono del owner (si está disponible) o intent de WhatsApp |
| Tap en `+` (añadir reporte) | `onEvent(MapUiEvent.AddReport)` | [ASSUMED] Abre el mismo `Dialog` de creación de reporte que el FAB del `FeedScreen` |
| Tap en lupa `🔍` | `onEvent(MapUiEvent.OpenSearch)` | [ASSUMED] Abre `SearchBar` overlay sobre el mapa o navega a pantalla de búsqueda |
| Pinch/zoom en mapa | Manejado por el SDK del mapa | Zoom in/out nativo. No emite `UiEvent` |
| Pan/drag en mapa | Manejado por el SDK del mapa | Desplaza la cámara. Puede disparar re-carga de reportes si se implementa "cargar al mover" [ASSUMED] |
| Tap en hamburguesa (TopAppBar) | Existente en `AppTopBar` | Abre `DrawerShell` |

### 4.2 Lógica Condicional

- **Filtros activos:** Si `filterLost=true` y `filterFound=false`, solo se muestran pines rojos, y viceversa.
- **Radio de búsqueda:** Filtra `PetReport` según distancia calculada entre la ubicación del report y `userLocation` (o centro del mapa si no hay GPS). [ASSUMED] requiere que `PetReport` tenga coordenadas — ver §7 para flag importante.
- **Owner del reporte:** [ASSUMED] Al igual que en `FeedScreen`, si `report.ownerUid == currentUser.uid`, el `PinPreviewBottomSheet` podría mostrar opción "Eliminar" (no visible en las imágenes — no implementar sin confirmar con diseño).
- **Selección de pin:** Un solo pin puede estar seleccionado a la vez. Tap en el mapa fuera de un pin deselecciona y cierra el sheet.

### 4.3 Animaciones y Transiciones

| Elemento | Animación |
|---|---|
| `FiltersBottomSheet` apertura/cierre | Animación nativa de `ModalBottomSheet` (slide up/down con spring) |
| `PinPreviewBottomSheet` apertura/cierre | Ídem |
| Pin seleccionado | [ASSUMED] `animateFloatAsState` para escalar el pin de 1.0f → 1.3f al seleccionarse |
| `MapBottomBar` | [ASSUMED] aparece con `AnimatedVisibility(slideInVertically)` al cargar el mapa |

---

## 5. PROPS & REUSABILITY

### 5.1 ¿Es reutilizable?
`MapScreen` es una **pantalla one-off** en la navegación. No se instancia como componente en otros contextos.

### 5.2 Componentes Extraíbles como Reutilizables

| Componente | Archivo sugerido | Descripción |
|---|---|---|
| `PetMapPin` | `app/ui/components/PetMapPin.kt` | Composable que renderiza el pin custom (coloreado + foto circular). Recibe `PetReport` y `isSelected: Boolean` |
| `FiltersBottomSheet` | `app/ui/screens/map/MapFiltersSheet.kt` | Sheet de filtros. Recibe estado de filtros y callbacks |
| `PinPreviewBottomSheet` | `app/ui/screens/map/PinPreviewSheet.kt` | Preview card del pin. Recibe `PetReport?` y callbacks de acción |
| `MapBottomBar` | `app/ui/components/MapBottomBar.kt` | Barra inferior del mapa con los 3 íconos. Recibe callbacks |

### 5.3 Props del Screen (wrapper con ViewModel)

```kotlin
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAddReport: () -> Unit
)

// Composable stateless interno:
@Composable
fun MapContent(
    state: MapUiState,
    onEvent: (MapUiEvent) -> Unit,
    onNavigateToDetail: (String) -> Unit
)
```

---

## 6. DEPENDENCIES

### 6.1 Pantallas / Componentes que Deben Existir Antes

| Dependencia | Estado actual |
|---|---|
| `AppTopBar` | ✅ Implementado en `app/ui/components/AppTopBar.kt` |
| `DrawerShell` | ✅ Implementado en `app/ui/components/DrawerShell.kt` |
| `UserAvatar` | ✅ Implementado en `app/ui/components/UserAvatar.kt` |
| `Routes.Map` | ❌ Nuevo — agregar a `Routes.kt` y `AppNavHost.kt` |
| `MapViewModel` | ❌ Nuevo — crear en `app/ui/screens/map/` |
| `MapUiState` / `MapUiEvent` | ❌ Nuevo |
| `Routes.ReportDetail` | ❌ Pendiente MVP (stub de navegación aceptable) |

### 6.2 Puertos In que Consume el ViewModel

| Puerto In | Estado |
|---|---|
| `ObserveReports` | ✅ Implementado |
| `ObserveCurrentUser` | ✅ Implementado |
| `SearchReports` | ✅ Implementado |
| `CreateReport` | ✅ Implementado (si se reutiliza el dialog del Feed) |
| `DeleteReport` | ✅ Implementado (si se expone desde el preview [ASSUMED]) |

### 6.3 Librerías Implicadas por la UI

> ⚠️ **ALERTA CRÍTICA:** Las librerías de mapas NO están en el stack actual y están **fuera de scope MVP** según §14 del `TECHNICAL_CONTEXT.md`. Ver §7 para recomendación.

| Librería | Función | Estado en el proyecto |
|---|---|---|
| **Google Maps SDK for Android** (`maps-compose`) | Renderizado del mapa, pines, cámara | ❌ NO en `libs.versions.toml` — **fuera de scope MVP** |
| **Google Play Services Location** (`play-services-location`) | `fusedLocationClient` para GPS del usuario | ❌ NO en el proyecto |
| `com.google.android.gms:play-services-maps` | Dependencia base de Maps | ❌ NO en el proyecto |
| Coil | Carga de imagen en `PetPreviewImage` y en `PetMapPin` | ✅ Ya incluido (`2.7.0`) |

**Librería recomendada si se decide implementar:**
```toml
# libs.versions.toml
maps-compose = "4.3.3"

[libraries]
maps-compose = { group = "com.google.maps.android", name = "maps-compose", version.ref = "maps-compose" }
```

```kotlin
// app/build.gradle.kts
implementation(libs.maps.compose)
implementation("com.google.android.gms:play-services-maps:19.0.0")
implementation("com.google.android.gms:play-services-location:21.3.0")
```

---

## 7. IMPLEMENTATION NOTES

### 7.1 ⚠️ Flag Crítico — Geolocalización NO está en el Dominio

**Problema:** El modelo `PetReport` en `domain/model/PetReport.kt` solo tiene `location: String` (texto como "Parque Simón Bolívar"). **No tiene coordenadas `lat/lng`**. Esto hace imposible:
- Colocar pines en coordenadas exactas en el mapa.
- Filtrar por radio de búsqueda con distancia real.

**Opciones:**

| Opción | Descripción | Recomendación |
|---|---|---|
| A | Agregar `val latitude: Double?` y `val longitude: Double?` a `PetReport` + migrar DTOs y Firestore | ✅ **Recomendada** — cambio limpio que no viola arquitectura hexagonal |
| B | Geocodificar el `String location` en runtime con la API de Geocoding de Google | ❌ Costoso, lento, requiere API key adicional, viola la regla "sin Retrofit/OkHttp" |
| C | Usar coordenadas ficticias/mock en el mapa para el MVP | ⚠️ Aceptable solo si esta pantalla es demo/preview |

**Si se elige A:** Agregar a `PetReport.kt`:
```kotlin
val latitude: Double? = null,
val longitude: Double? = null,
```
Y actualizar `PetReportDto`, `PetReportMapper`, y la escritura en Firestore.

### 7.2 ⚠️ Flag Crítico — Esta Pantalla Está Fuera de Scope MVP

Según `TECHNICAL_CONTEXT.md §14`:
> **"No implementar mapas, geolocalización ni chat en esta fase."**

**Recomendación:** Documentar esta pantalla como está hecho aquí, pero **no implementarla** hasta que:
1. Se extienda `PetReport` con coordenadas.
2. Se agreguen las dependencias de Maps y Location al `libs.versions.toml`.
3. Se actualice este documento como fuente de verdad.

Si se quiere un placeholder en el Drawer, agregar el item de navegación pero mostrar un `Scaffold` con `Text("Próximamente")`.

### 7.3 Pines Customizados en Maps Compose

Usar `MapPin` bitmaps custom renderizados con Compose Canvas o `BitmapDescriptorFactory`:

```kotlin
// PetMapPin.kt — estrategia recomendada
@Composable
fun rememberPetPinBitmap(report: PetReport): BitmapDescriptor {
    // Renderizar composable a bitmap usando ComposeView + drawToBitmap
    // O usar Canvas para dibujar el shape del pin + imagen circular (Coil async)
}

// En el mapa:
Marker(
    state = MarkerState(position = LatLng(report.latitude!!, report.longitude!!)),
    icon = rememberPetPinBitmap(report),
    onClick = { onEvent(MapUiEvent.PinClicked(report.id)); true }
)
```

> **Alternativa más sencilla:** Usar `MarkerComposable { PetMapPin(report) }` disponible en `maps-compose 4.x`.

### 7.4 BottomSheet Coexistencia

Cuando está abierto un `ModalBottomSheet`, el `MapBottomBar` debe seguir visible. Asegurarse de que el `BottomBar` NO sea parte del `Scaffold.bottomBar` (que quedaría oculto por el sheet) sino un `Box` overlay posicionado con `Alignment.BottomCenter` sobre el mapa.

```kotlin
Box(Modifier.fillMaxSize()) {
    GoogleMap(...)
    MapBottomBar(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 16.dp)
    )
    // Los ModalBottomSheet se manejan fuera del Box, a nivel de Scaffold
}
```

### 7.5 Filtro de Radio sin GPS

Si el usuario no concede permiso de ubicación, el filtro de "Radio de búsqueda" no tiene punto de referencia. Opciones:
- Usar el centro de la cámara del mapa como referencia (recomendado [ASSUMED]).
- Mostrar `Snackbar`: "Activa la ubicación para usar el filtro de radio."
- Deshabilitar el slider si no hay `userLocation`.

### 7.6 Especie — Datos de Dominio

`PetReport` no tiene campo `species` (solo `breed: String`). El filtro "Especie" requiere o bien:
- Inferir la especie del `breed` con un mapper (`"Golden Retriever" → Perro`, `"Siamés" → Gato`).
- Agregar `val species: String` a `PetReport` (recomendado junto con coordenadas).

### 7.7 Accesibilidad (a11y)

| Elemento | Consideración |
|---|---|
| `PetMapPin` | `contentDescription = "${report.petName}, ${if (report.type == LOST) "perdido" else "hallado"}, en ${report.location}"` |
| `MapBottomBar` icons | `contentDescription` en cada `IconButton`: "Añadir reporte", "Abrir filtros", "Buscar" |
| `Slider` de radio | `contentDescription = "Radio de búsqueda: ${searchRadiusKm.toInt()} kilómetros"` |
| Mapa en general | El SDK de Maps tiene soporte TalkBack limitado — documentar como limitación conocida |
| `ModalBottomSheet` | Compose gestiona el foco automáticamente al abrir |

### 7.8 Edge Cases

| Caso | Manejo |
|---|---|
| `PetReport.imageUrl` nulo/roto en pin | Placeholder con inicial del animal (letra "M"/"L" etc.) en `primaryContainer` |
| Múltiples pines solapados en el mismo punto | [ASSUMED] Maps SDK agrupa markers automáticamente — considerar clustering con `maps-compose` utility library |
| `filterLost=false` y `filterFound=false` simultáneamente | Deshabilitar "Aplicar Filtros" o mostrar warning: "Selecciona al menos un tipo" |
| Rotación de pantalla | `rememberSaveable` para la posición de cámara del mapa; los BottomSheets se cierran y re-abren [ASSUMED] |
| Sin conexión a internet | Mapa en modo offline limitado (tiles cacheados); `Snackbar` si no se cargan reportes |

---

## SHORT SUMMARY

- 🗺️ **Librería de mapas no está en el stack:** `maps-compose` debe agregarse como nueva dependencia — requiere aprobación explícita ya que el `TECHNICAL_CONTEXT.md` prohíbe librerías no documentadas. Esta es la **decisión de implementación más crítica** antes de comenzar.

- 📍 **`PetReport` necesita coordenadas:** El modelo de dominio solo tiene `location: String`. Sin `latitude: Double?` / `longitude: Double?` es imposible colocar pines en posiciones reales. Se debe extender el modelo, el DTO, el mapper y los datos seed.

- 🧩 **Tres pantallas = un solo Screen + tres estados de BottomSheet:** Implementar como `MapUiState.bottomSheetState: MapBottomSheetState` (sealed class: `None | Filters | PinPreview`). No crear tres screens separados.

- ⚠️ **Fuera de scope MVP según el propio TECHNICAL_CONTEXT.md:** Coordinar con el equipo antes de implementar. Un placeholder en el Drawer que muestra "Próximamente" es la opción de menor riesgo hasta que las dependencias y el modelo estén listos.

- 🎨 **`PetMapPin` custom es el componente de mayor complejidad visual:** Renderizar un composable Compose a `BitmapDescriptor` para Google Maps requiere una estrategia específica (ComposeView + drawToBitmap o `MarkerComposable` de maps-compose 4.x). Prototipar esto primero antes de construir el resto de la pantalla.

---

*Documento generado con base en Pantallas 6, 7 y 8 del diseño de MascotasPerdidas + TECHNICAL_CONTEXT.md (Junio 2026).*
