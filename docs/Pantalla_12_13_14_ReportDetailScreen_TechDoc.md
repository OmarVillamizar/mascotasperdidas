# Technical Documentation — ReportDetailScreen
## Pantallas 12 · 13 · 14: Mascota Perdida · Avistamiento en Calle · Bajo mi Cuidado

> **Nota de interpretación:** Las tres pantallas son **variantes de una misma pantalla** (`ReportDetailScreen`),  
> cuyo contenido y secciones cambian según el `ReportType` y la relación del usuario con el reporte.  
> - **Pantalla 12** → `ReportType.LOST` (mascota perdida) — vista pública  
> - **Pantalla 13** → `ReportType.FOUND` / avistamiento — vista pública  
> - **Pantalla 14** → `ReportType.FOUND` — animal bajo cuidado del reporter (vista extendida / "bajo mi cuidado")  
> El título del `TopAppBar` cambia según el tipo: "Mascota perdida" / "Avistamineto en calle" / "Bajo mi cuidado".

---

## 1. SCREEN IDENTITY

| Campo | Valor |
|---|---|
| **Screen name** | `ReportDetailScreen` |
| **Route** | `Routes.ReportDetail` (recibe `reportId: String` como argumento) |
| **Screen type** | Stack (sin Drawer — solo flecha ← atrás en TopAppBar) |
| **Entry points** | `FeedScreen` → tap en card "Más Información"; `MapScreen` → tap en "Mas Información" desde `PinPreviewBottomSheet`; [ASSUMED] notificaciones push → deep link a este detalle |
| **Exit points** | ← Atrás (popBackStack); "CONTACTAR" → intent externo o `Routes.Contact` [ASSUMED]; "+" en galería de fotos → picker de imagen [ASSUMED, solo para el owner] |

---

## 2. VISUAL COMPONENTS

### 2.1 Componentes Comunes a las Tres Variantes

| # | Nombre Composable | Equivalente MD3 (Compose) | Variante / Notas | Estados visibles |
|---|---|---|---|---|
| 1 | `DetailTopAppBar` | `TopAppBar` (Small) | Flecha ← izquierda, título variable ("Mascota perdida" / "Avistamineto en calle" / "Bajo mi cuidado"), sin avatar ni hamburguesa | default |
| 2 | `HeroImage` | `AsyncImage` (Coil) dentro de `Card` o `Box` con `clip(RoundedCornerShape(12.dp))` | Imagen principal del reporte, ancho completo, ~220–240dp de altura, esquinas redondeadas | default, loading (shimmer [ASSUMED]), error (placeholder huella) |
| 3 | `AttributesGrid` | Composable custom con `LazyVerticalGrid` o dos columnas con `Row` | Grid de atributos label+valor en 2 columnas. Separador horizontal `HorizontalDivider` arriba y abajo | default |
| 4 | `AttributeItem` | `Column { Text(label) + Text(value) }` | Label en `labelMedium`/`labelSmall` bold (`onSurface`), valor en `bodyMedium` (`onSurfaceVariant`) | default |
| 5 | `DescriptionText` | `Text` | `bodyMedium`, color `onBackground`, sin límite de líneas visible, dentro de `ElevatedCard` o sobre fondo `background` | default |

### 2.2 Componentes Específicos — Pantalla 12 (LOST — Mascota Perdida)

| # | Nombre Composable | Equivalente MD3 (Compose) | Notas | Estados |
|---|---|---|---|---|
| 6 | `PhotoGalleryRow` | `LazyRow` de `AsyncImage` + botón `+` | 3 fotos existentes + 1 tile `+` (agregar foto). Tiles cuadrados ~90dp, esquinas redondeadas. **Solo visible en variante LOST [ASSUMED: solo para el owner]** | default, loading por tile |
| 7 | `AddPhotoTile` | `OutlinedCard` o `Box(border)` con `Icon(Icons.Default.Add)` | Último tile de la `LazyRow`, fondo `primaryContainer`, ícono `+` en `onPrimaryContainer` | default, pressed |
| 8 | `DescriptionCard` | `ElevatedCard` o `Surface(color = surface)` | Contiene `DescriptionText` con la descripción de la mascota perdida. Fondo blanco/surface | default |
| 9 | `LastLocationLabel` | `Text` | `labelLarge` o `titleSmall`, caps, color `onSurfaceVariant`. Texto: "ULTIMA UBICACION" | default |
| 10 | `MiniMapView` | `AndroidView` (Google Maps SDK) o imagen estática | Mapa pequeño embebido, ~180dp altura, con `PetMapPin` rojo encima. Solo visible en P12 y P13 | default, loading [ASSUMED] |
| 11 | `LostPinOnMap` | `Marker` con custom `BitmapDescriptor` | Pin rojo con foto del animal, igual que en `MapScreen` | default |

> **Atributos visibles en P12:** Especie, Sexo, Edad, Color

### 2.3 Componentes Específicos — Pantalla 13 (FOUND — Avistamiento en Calle)

| # | Nombre Composable | Equivalente MD3 (Compose) | Notas | Estados |
|---|---|---|---|---|
| 12 | `DescriptionText` (inline) | `Text` | La descripción va directamente en el scroll, sin card contenedora separada (a diferencia de P12) | default |
| 13 | `LastLocationLabel` | Mismo que P12 | "ULTIMA UBICACION" | default |
| 14 | `MiniMapView` | Mismo que P12 | Pin verde con foto del animal avistado | default |
| 15 | `FoundPinOnMap` | `Marker` verde | Pin verde con foto circular | default |
| 16 | `ReportedByLabel` | `Text` | `labelLarge` caps, color `onSurfaceVariant`. Texto: "REPORTADO POR" — sección visible pero cortada por el scroll | default |
| 17 | `ReporterInfo` | [ASSUMED] `Row { UserAvatar + Column { Text(nombre) + Text(fecha) } }` | Info del usuario que reportó el avistamiento. Fuera de pantalla (requiere scroll) | default |

> **Atributos visibles en P13:** Especie, Tamaño, Estado, Collar (4 atributos — no Sexo/Edad/Color)

### 2.4 Componentes Específicos — Pantalla 14 (FOUND — Bajo mi Cuidado)

| # | Nombre Composable | Equivalente MD3 (Compose) | Notas | Estados |
|---|---|---|---|---|
| 18 | `AttributesGrid` (extendida) | Igual al común | 6 atributos: Especie, Tamaño, Estado, Collar, Edad aprox, Microchip | default |
| 19 | `PhysicalStatusLabel` | `Text` | `labelLarge` caps, color `onSurfaceVariant`. Texto: "ESTADO FISICO" | default |
| 20 | `PhysicalStatusChips` | `Row` de `FilterChip` o `AssistChip` | Chips: "Sano" (seleccionado/activo — fondo `tertiary` verde), "Asustado" (outline/inactivo), "Docil" (outline/inactivo). **Solo variante P14** | selected, unselected |
| 21 | `NotesLabel` | `Text` | `labelLarge` caps, color `onSurfaceVariant`. Texto: "NOTAS" | default |
| 22 | `NotesText` | `Text` | `bodyMedium`, color `onBackground`, sin límite de líneas | default |

> **Atributos visibles en P14:** Especie, Tamaño, Estado, Collar, Edad aprox, Microchip  
> **NO tiene mapa** — "Bajo mi cuidado" implica que la ubicación es la del cuidador (privada)  
> **NO tiene galería de fotos** en la parte superior como P12

### 2.5 Botones de Acción (Footer) — Visibilidad Condicional

| Botón | Composable | Cuándo es visible |
|---|---|---|
| "Más Información" | `OutlinedButton` | [ASSUMED] Solo en vistas donde hay más datos que mostrar — no visible en las imágenes del detalle completo |
| "CONTACTAR" | `Button` (FilledButton) con color `tertiary` (`#2E7D32`) | [ASSUMED] Visible en P12 (LOST) y P13 (FOUND) cuando el viewer NO es el owner. Fijo en la parte inferior de la pantalla (sticky footer) |
| Acciones del owner | [ASSUMED] `DropdownMenu` o botones "Editar" / "Eliminar" | Solo si `currentUser.uid == report.ownerUid` |

> ⚠️ **[ASSUMED]** El botón "CONTACTAR" no aparece en ninguna de las 3 imágenes del detalle (las pantallas muestran scroll hacia arriba). Se infiere que existe como footer sticky basado en la Pantalla 8 (PinPreviewBottomSheet) que sí lo muestra.

---

## 3. DATA & STATE

### 3.1 Local State (UiState)

```kotlin
data class ReportDetailUiState(
    val report: PetReport? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isOwner: Boolean = false,           // currentUser.uid == report.ownerUid
    val reporter: User? = null,             // [ASSUMED] datos del usuario que creó el reporte
    
    // Solo relevante para P14 / FOUND con cuidado
    val physicalStatus: List<String> = emptyList(),   // ["Sano", "Asustado", "Dócil"] — [ASSUMED] campo nuevo
    val notes: String = "",                            // [ASSUMED] campo nuevo en PetReport
    
    // Solo P12 — galería adicional
    val additionalPhotos: List<String> = emptyList(), // URLs adicionales [ASSUMED] — campo nuevo
    
    // Mapa
    val reportLocation: LatLng? = null      // [ASSUMED] requiere coordenadas en PetReport (ver §7)
)
```

### 3.2 UiEvent

```kotlin
sealed class ReportDetailUiEvent {
    object NavigateBack : ReportDetailUiEvent()
    object ContactOwner : ReportDetailUiEvent()
    object AddPhoto : ReportDetailUiEvent()             // Abre picker — solo owner P12
    data class PhotoClicked(val url: String) : ReportDetailUiEvent()  // [ASSUMED] preview foto
    object DeleteReport : ReportDetailUiEvent()         // Solo owner
    object EditReport : ReportDetailUiEvent()           // [ASSUMED] solo owner
}
```

### 3.3 Global State (Puertos)

| Puerto In | Lectura | Escritura |
|---|---|---|
| `ObserveCurrentUser` | Determina `isOwner`, muestra avatar en acciones | No |
| `ObserveReports` (filtrado por id) [ASSUMED] | Carga el `PetReport` específico por ID | No |
| `DeleteReport` | — | Elimina el reporte si es owner |
| `CreateReport` / [ASSUMED] `UpdateReport` | — | [ASSUMED] Si se implementa edición o adición de fotos |

> **Nota:** No existe un puerto `GetReportById` en el contexto actual. Ver §7 para el flag correspondiente.

### 3.4 API / Firebase Calls

| Operación | Puerto / Mecanismo | Cuándo se dispara | Qué hace |
|---|---|---|---|
| Cargar reporte por ID | [ASSUMED] `observeReports(type).map { it.find { r -> r.id == reportId } }` o nuevo `GetReportById` | `LaunchedEffect(reportId)` al montar | Obtiene el `PetReport` completo para mostrar en detalle |
| Cargar datos del reporter | [ASSUMED] `GetUserById(report.ownerUid)` (puerto nuevo) | Tras cargar el reporte | Obtiene nombre e inicial del creador para "REPORTADO POR" |
| Eliminar reporte | `DeleteReport(reportId)` | Tap confirm en diálogo de confirmación | Borra el documento de Firestore y navega atrás |
| Subir foto adicional | [ASSUMED] `UpdateReport` con nueva imagen | Tap en tile `+` → picker → confirm | Sube a Storage y actualiza `additionalPhotos` en Firestore |

### 3.5 Loading States

| Zona | Comportamiento |
|---|---|
| Pantalla completa al entrar | `CircularProgressIndicator` centrado o shimmer de la estructura completa [ASSUMED] mientras se carga el reporte |
| `HeroImage` | Coil `placeholder` + `error` con `ic_logo` |
| Tiles de galería (P12) | Shimmer individual por tile |
| `MiniMapView` (P12, P13) | Mapa con loading nativo de Google Maps SDK |

### 3.6 Empty States

| Situación | Comportamiento |
|---|---|
| Reporte no encontrado (eliminado) | [ASSUMED] `Snackbar("Este reporte ya no está disponible")` + `popBackStack()` automático |
| Sin fotos adicionales (P12) | Solo se muestra el tile `+`, no hay thumbnails previos |
| Sin notas (P14) | [ASSUMED] Sección "NOTAS" oculta o muestra texto placeholder en gris |

### 3.7 Error States

| Situación | Comportamiento |
|---|---|
| Fallo al cargar reporte | `Text("No se pudo cargar el reporte")` + botón "Reintentar" [ASSUMED] |
| Fallo al eliminar | `Snackbar("Error al eliminar. Intenta de nuevo")` |
| Sin conexión al cargar mapa mini | Tile del mapa con fondo gris y ícono de ubicación desactivado [ASSUMED] |

---

## 4. INTERACTIONS & BEHAVIOR

### 4.1 Interacciones

| Trigger | Acción | Resultado |
|---|---|---|
| Tap en ← (TopAppBar) | `onEvent(NavigateBack)` | `navController.popBackStack()` |
| Tap en "CONTACTAR" (footer sticky) [ASSUMED] | `onEvent(ContactOwner)` | Abre `Intent.ACTION_DIAL` con teléfono del owner, o intent de WhatsApp si hay número E.164 |
| Tap en tile `+` de galería (P12, owner) | `onEvent(AddPhoto)` | Lanza `ActivityResultContracts.GetContent("image/*")` o `TakePicture` |
| Tap en foto de galería (P12) | `onEvent(PhotoClicked(url))` | [ASSUMED] Abre `Dialog` fullscreen con zoom (PhotoViewer) |
| Tap en `MiniMapView` (P12, P13) | [ASSUMED] Navega a `Routes.Map` centrado en esa ubicación | Abre el mapa completo con el pin del reporte ya visible |
| Tap en "Eliminar" (owner) | Muestra `AlertDialog` de confirmación | Confirmar → `DeleteReport` → `popBackStack()`; Cancelar → cierra diálogo |
| Scroll vertical | `LazyColumn` nativo | Revela secciones inferiores (mapa, "REPORTADO POR", etc.) |
| Tap en chip de estado físico (P14) | [ASSUMED] Solo lectura — no editable desde aquí | Sin acción (chips decorativos en esta vista) |

### 4.2 Lógica Condicional

```
isOwner = (currentUser?.uid == report.ownerUid)

cuando report.type == LOST:
  → Mostrar galería de fotos (P12)
  → Mostrar: Especie, Sexo, Edad, Color
  → Mostrar MiniMapView con pin rojo
  → Título: "Mascota perdida"
  → Si isOwner: mostrar tile "+" en galería, menú de acciones (Editar/Eliminar)
  → Si !isOwner: mostrar footer "CONTACTAR"

cuando report.type == FOUND && !isCaredFor [ASSUMED campo nuevo]:
  → NO mostrar galería
  → Mostrar: Especie, Tamaño, Estado, Collar
  → Mostrar descripción + MiniMapView con pin verde
  → Mostrar sección "REPORTADO POR"
  → Título: "Avistamineto en calle"

cuando report.type == FOUND && isCaredFor [ASSUMED campo nuevo]:
  → NO mostrar galería
  → Mostrar: Especie, Tamaño, Estado, Collar, Edad aprox, Microchip
  → Mostrar chips de ESTADO FISICO
  → Mostrar sección NOTAS
  → NO mostrar mapa (ubicación privada del cuidador)
  → Título: "Bajo mi cuidado"
```

> **[ASSUMED]** La distinción entre "Avistamiento en calle" y "Bajo mi cuidado" implica un campo adicional en `PetReport` — quizás `isCaredFor: Boolean` o una extensión del enum `ReportType` (ver §7).

### 4.3 Animaciones y Transiciones

| Elemento | Animación |
|---|---|
| Entrada a la pantalla | `slideInHorizontally` desde derecha (Stack navigation por defecto en Compose) |
| `HeroImage` | Crossfade Coil al cargar (ya soportado por `AsyncImage(model = ..., contentScale = FillWidth)`) |
| `AlertDialog` eliminar | Animación MD3 nativa del `AlertDialog` |
| [ASSUMED] Foto fullscreen | `AnimatedVisibility` con `scaleIn`/`scaleOut` al abrir/cerrar PhotoViewer |

---

## 5. PROPS & REUSABILITY

### 5.1 ¿Es reutilizable?
`ReportDetailScreen` es una **pantalla parametrizada** — una sola pantalla con lógica condicional según el tipo y el ownership. No se instancia en múltiples rutas diferentes.

### 5.2 Composable Stateless Interno

```kotlin
// Wrapper con ViewModel:
@Composable
fun ReportDetailScreen(
    reportId: String,
    viewModel: ReportDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToMap: (LatLng) -> Unit    // [ASSUMED] para tap en MiniMap
)

// Composable stateless:
@Composable
fun ReportDetailContent(
    state: ReportDetailUiState,
    onEvent: (ReportDetailUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToMap: (LatLng) -> Unit
)
```

### 5.3 Componentes Extraíbles como Reutilizables

| Composable | Archivo sugerido | Descripción |
|---|---|---|
| `AttributesGrid` | `app/ui/components/AttributesGrid.kt` | Grid genérico de label+valor en 2 columnas. Recibe `List<Pair<String,String>>`. Reutilizable en formularios, detalles, etc. |
| `HeroImage` | `app/ui/components/HeroImage.kt` | `AsyncImage` de Coil con shimmer, esquinas redondeadas, ancho completo, altura configurable |
| `MiniMapView` | `app/ui/components/MiniMapView.kt` | Mapa estático pequeño, no interactivo (o interactivo con tap para navegar al mapa completo). Recibe `LatLng` y `ReportType` para el color del pin |
| `PhotoGalleryRow` | `app/ui/components/PhotoGalleryRow.kt` | `LazyRow` de fotos + tile `+`. Recibe `List<String>` y `onAddPhoto: (() -> Unit)?` (null = solo lectura) |
| `SectionLabel` | `app/ui/components/SectionLabel.kt` | `Text` en caps para cabeceras de sección ("ESTADO FISICO", "NOTAS", "ULTIMA UBICACION", "REPORTADO POR"). Reutilizable en múltiples pantallas |
| `StickyContactFooter` | `app/ui/components/StickyContactFooter.kt` | Footer fijo con botón "CONTACTAR" color `tertiary` |

---

## 6. DEPENDENCIES

### 6.1 Pantallas / Componentes que Deben Existir Antes

| Dependencia | Estado actual |
|---|---|
| `FeedScreen` (entry point) | ✅ Implementado |
| `MapScreen` (entry point) | ❌ Nueva — Pantalla 6/7/8 (documentada, no implementada) |
| `Routes.ReportDetail` | ❌ Nuevo — agregar a `Routes.kt` con argumento `reportId` |
| `AppNavHost.kt` — composable para la ruta | ❌ Nuevo |
| `ReportDetailViewModel` | ❌ Nuevo |
| `ReportDetailUiState` / `ReportDetailUiEvent` | ❌ Nuevo |
| `MiniMapView` (si se implementa mapa) | ❌ Nuevo — depende de maps-compose (ver §7) |

### 6.2 Puertos In que Consume el ViewModel

| Puerto In | Estado actual | Nota |
|---|---|---|
| `ObserveCurrentUser` | ✅ Implementado | Para determinar `isOwner` |
| `ObserveReports` | ✅ Implementado | Para obtener el reporte por ID (filtrar por id en el Flow) |
| `DeleteReport` | ✅ Implementado | Para el owner |
| `GetReportById` | ❌ **No existe** — ver §7 | Puerto nuevo necesario para eficiencia |
| `GetUserById` [ASSUMED] | ❌ **No existe** | Para mostrar "REPORTADO POR" |
| `UpdateReport` [ASSUMED] | ❌ **No existe** | Para agregar fotos adicionales |

### 6.3 Librerías Implicadas

| Librería | Función | Estado |
|---|---|---|
| Coil | `HeroImage`, tiles de galería, foto circular del reporter | ✅ Ya incluido |
| Google Maps SDK / `maps-compose` | `MiniMapView` con pin | ❌ Fuera de scope MVP (mismo blocker que MapScreen) |
| [ASSUMED] `accompanist-permissions` o `ActivityResultContracts` | Picker de imagen para tile `+` | ⚠️ Solo `ActivityResultContracts` (ya en AndroidX, sin lib extra) |

---

## 7. IMPLEMENTATION NOTES

### 7.1 ⚠️ Flag Crítico — El Modelo `PetReport` es Insuficiente para Esta Pantalla

La pantalla de detalle revela **campos nuevos** que no existen en el modelo de dominio actual:

| Campo nuevo | Pantalla | Tipo sugerido | Decisión requerida |
|---|---|---|---|
| `species: String` | P12, P13, P14 | `String` o `enum class Species { DOG, CAT, OTHER }` | El atributo "Especie" se muestra separado de `breed`. Actualmente solo existe `breed: String` |
| `size: String` | P13, P14 | `String` ("Grande", "Mediano", "Pequeño") | No existe en el modelo |
| `gender: String?` | P12 | `String?` ("Masculino", "Femenino", "No sé") | No existe |
| `color: String?` | P12 | `String?` | No existe |
| `collarColor: String?` | P13, P14 | `String?` ("Azul", "Rojo", etc.) | No existe |
| `age: String?` | P12, P14 | `String?` ("3 años", "Adulto") | No existe |
| `microchip: String?` | P14 | `String?` ("No visible", código, etc.) | No existe |
| `physicalStatus: List<String>` | P14 | `List<String>` (["Sano", "Asustado", "Dócil"]) | No existe |
| `notes: String?` | P14 | `String?` | No existe — `description` existe pero parece usarse diferente |
| `additionalPhotos: List<String>` | P12 | `List<String>` | Solo existe `imageUrl: String` (una foto) |
| `isCaredFor: Boolean` | P14 | `Boolean` | Distingue P13 de P14 — no existe |
| `latitude: Double?` | P12, P13 | `Double?` | No existe (flagueado también en MapScreen) |
| `longitude: Double?` | P12, P13 | `Double?` | No existe |
| `reporterName: String?` | P13 | `String?` | Solo existe `ownerInitial: String` |

**Modelo extendido sugerido:**
```kotlin
// domain/model/PetReport.kt — PROPUESTA DE EXTENSIÓN
data class PetReport(
    // Campos existentes
    val id: String,
    val ownerUid: String,
    val ownerInitial: String,
    val ownerName: String = "",             // NUEVO — para "REPORTADO POR"
    val petName: String,
    val type: ReportType,
    val breed: String,
    val description: String,
    val location: String,
    val imageUrl: String,
    val recencyLabel: String,
    val createdAtEpochMs: Long,
    
    // Campos nuevos — todos opcionales para no romper seed data
    val species: String = "",               // "Perro", "Gato", "Otro"
    val size: String = "",                  // "Grande", "Mediano", "Pequeño"
    val gender: String? = null,             // "Masculino", "Femenino"
    val color: String? = null,              // "Dorado", "Blanco", etc.
    val collarColor: String? = null,        // "Azul", "Rojo", "Ninguno"
    val ageApprox: String? = null,          // "3 años", "Adulto", "Cachorro"
    val microchip: String? = null,          // "No visible", "Sí", código
    val physicalStatus: List<String> = emptyList(),  // ["Sano", "Asustado"]
    val notes: String? = null,
    val additionalPhotos: List<String> = emptyList(),
    val isCaredFor: Boolean = false,        // P13 vs P14
    val latitude: Double? = null,
    val longitude: Double? = null
)
```

> ⚠️ Este cambio requiere actualizar `PetReportDto`, `PetReportMapper`, el schema de Firestore, y los datos seed. Es la decisión de mayor impacto de esta pantalla.

### 7.2 ⚠️ Flag — `GetReportById` No Existe Como Puerto

Actualmente `ObserveReports(type)` devuelve **toda la lista** de un tipo. Para `ReportDetailScreen` necesitamos un único reporte por ID. Opciones:

| Opción | Pros | Contras | Recomendación |
|---|---|---|---|
| A. Filtrar el Flow existente: `observeReports(type).map { it.find { r -> r.id == id } }` | No requiere puerto nuevo | Descarga toda la colección solo para un documento; el tipo debe pasarse como argumento junto al ID | ⚠️ Aceptable para MVP |
| B. Nuevo puerto `GetReportById(id): Flow<PetReport?>` + Firestore `.document(id).snapshots()` | Eficiente, limpio, escalable | Requiere nuevo puerto in + use case + adapter | ✅ Recomendada para producción |

**Para MVP:** pasar `reportType` junto a `reportId` en la navegación y usar la opción A.
```kotlin
// Routes.kt
object ReportDetail : Routes("report_detail/{reportId}/{reportType}") {
    fun route(id: String, type: ReportType) = "report_detail/$id/${type.name}"
}
```

### 7.3 Distinción P13 vs P14 — SubType o Campo Boolean

El diseño muestra dos sabores de `ReportType.FOUND`:
- **P13:** Animal visto en la calle, no está bajo cuidado del reporter.
- **P14:** El reporter tiene el animal en su casa / bajo su cuidado directo.

Opciones de modelado:

| Opción | Implementación |
|---|---|
| A. `isCaredFor: Boolean` en `PetReport` | Simple, pero `ReportType` sigue siendo binario |
| B. Extender el enum: `enum class ReportType { LOST, FOUND_SIGHTING, FOUND_IN_CARE }` | Más expresivo, permite lógica condicional sin booleanos flotantes |

**Recomendación: Opción B** — el enum extendido es más limpio y encaja mejor con la arquitectura hexagonal (la lógica vive en el tipo, no en un flag booleano).

```kotlin
// domain/model/ReportType.kt
enum class ReportType { LOST, FOUND_SIGHTING, FOUND_IN_CARE }
```

### 7.4 Navegación con Argumento Tipado

```kotlin
// AppNavHost.kt
composable(
    route = Routes.ReportDetail.route,
    arguments = listOf(
        navArgument("reportId") { type = NavType.StringType },
        navArgument("reportType") { type = NavType.StringType }
    )
) { backStackEntry ->
    val reportId = backStackEntry.arguments?.getString("reportId") ?: return@composable
    val reportType = ReportType.valueOf(
        backStackEntry.arguments?.getString("reportType") ?: "LOST"
    )
    ReportDetailScreen(
        reportId = reportId,
        reportType = reportType,
        onNavigateBack = { navController.popBackStack() }
    )
}
```

### 7.5 `MiniMapView` — Estrategia sin Maps SDK

Si se quiere evitar la dependencia de Google Maps en esta pantalla (que sí está en scope, a diferencia de la `MapScreen` que es una feature completa), hay una alternativa válida:

**Usar Static Maps API de Google** — devuelve una imagen PNG de un mapa centrado en coordenadas, sin SDK nativo:

```kotlin
// URL de imagen estática — no requiere maps-compose
val staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap" +
    "?center=${lat},${lng}&zoom=15&size=400x200&maptype=roadmap" +
    "&markers=color:red|${lat},${lng}" +
    "&key=${BuildConfig.MAPS_API_KEY}"

AsyncImage(model = staticMapUrl, ...)
```

> **Pros:** Solo Coil (ya en el proyecto), sin dependencias nuevas, respeta la regla de "no maps SDK para esta fase".  
> **Contras:** Requiere API key de Maps Platform, consume créditos por request, no es interactivo.  
> **Recomendación:** Usar esta estrategia para MVP del detalle. La `MapScreen` completa es una feature separada.

### 7.6 `PhotoGalleryRow` y Picker de Imagen

El tile `+` en P12 debe lanzar un picker. Usar `ActivityResultContracts` (ya en AndroidX, sin librerías nuevas):

```kotlin
// En ReportDetailScreen (wrapper):
val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri ->
    uri?.let { viewModel.onEvent(ReportDetailUiEvent.AddPhoto(it)) }
}
```

> Usar `PickVisualMedia` (Photo Picker de Android 13+) como primera opción; fallback a `GetContent("image/*")` para API < 33.

### 7.7 Scroll y Footer Sticky

La estructura de la pantalla debe ser:

```kotlin
Scaffold(
    topBar = { DetailTopAppBar(...) },
    bottomBar = {
        if (!state.isOwner && state.report != null) {
            StickyContactFooter(onContact = { onEvent(ContactOwner) })
        }
    }
) { padding ->
    LazyColumn(modifier = Modifier.padding(padding)) {
        item { HeroImage(...) }
        item { if (report.type == LOST) PhotoGalleryRow(...) }
        item { AttributesGrid(...) }
        item { DescriptionText(...) }
        item { if (report.type != FOUND_IN_CARE) LastLocationSection(...) }
        item { if (report.type == FOUND_IN_CARE) PhysicalStatusSection(...) }
        item { if (report.type == FOUND_IN_CARE) NotesSection(...) }
        item { if (report.type != LOST) ReportedBySection(...) }
    }
}
```

### 7.8 Accesibilidad (a11y)

| Elemento | Consideración |
|---|---|
| `HeroImage` | `contentDescription = "Foto de ${report.petName}"` |
| Tiles de galería | `contentDescription = "Foto adicional ${index + 1}"` |
| Tile `+` | `contentDescription = "Añadir foto"` |
| `MiniMapView` / Static Map | `contentDescription = "Última ubicación conocida: ${report.location}"` |
| Chips de estado físico (P14) | `contentDescription = "Estado físico: ${chip}"`, `selected` para accesibilidad |
| Botón "CONTACTAR" | `contentDescription = "Contactar al reporter de ${report.petName}"` |
| `AttributeItem` | Semántica: `clearAndSetSemantics { contentDescription = "${label}: ${value}" }` |

### 7.9 Edge Cases

| Caso | Manejo |
|---|---|
| `report.imageUrl` nulo o roto | Placeholder de huella `ic_logo` centrado en `HeroImage` |
| `additionalPhotos` vacío (P12) | Solo se muestra tile `+` si es owner; nada si no es owner |
| `latitude`/`longitude` nulos (P12, P13) | Ocultar sección "ULTIMA UBICACION" completamente (no mostrar mapa vacío) |
| Nombre de mascota muy largo en TopAppBar | `maxLines = 1`, `overflow = TextOverflow.Ellipsis` |
| Owner intenta contactarse a sí mismo | Ocultar "CONTACTAR" cuando `isOwner = true` |
| Reporte eliminado mientras el usuario está en detalle | El Flow emitirá `null`; navegar atrás automáticamente con Snackbar |
| Rotación de pantalla | `rememberSaveable` para scroll position; estado del ViewModel sobrevive |

---

## SHORT SUMMARY

- 🧩 **Una sola pantalla, tres variantes:** `ReportDetailScreen` maneja los tres estados mediante lógica condicional sobre `ReportType` (propuesto: extender a `LOST / FOUND_SIGHTING / FOUND_IN_CARE`) e `isOwner`. No crear tres rutas separadas.

- 🏗️ **`PetReport` necesita ~12 campos nuevos:** Especie, Tamaño, Sexo, Color, Collar, Edad, Microchip, Estado físico, Notas, fotos adicionales, `isCaredFor`, coordenadas. Esta es la refactorización de mayor impacto — debe hacerse antes de implementar la pantalla. Actualizar DTO, mapper y seed data en cascada.

- 🗺️ **`MiniMapView` sin maps-compose:** Usar Google Static Maps API como imagen cargada con Coil. Evita la dependencia de `maps-compose` para esta pantalla, manteniendo la feature completa de mapas para una fase posterior.

- 🔌 **Dos puertos nuevos necesarios para producción:** `GetReportById(id)` (eficiencia en Firestore) y `GetUserById(uid)` (sección "REPORTADO POR"). Para MVP es aceptable filtrar el Flow existente y omitir el reporter; documentar como deuda técnica.

- 📸 **Picker de imagen para galería (P12/owner):** Usar `ActivityResultContracts.PickVisualMedia` (Photo Picker de Android 13+) — ya disponible en AndroidX, sin librerías adicionales. Subir a Firebase Storage en el path existente `pet_reports/{uid}/{UUID}.jpg`.

---

*Documento generado con base en Pantallas 12, 13 y 14 del diseño de MascotasPerdidas + TECHNICAL_CONTEXT.md (Junio 2026).*
