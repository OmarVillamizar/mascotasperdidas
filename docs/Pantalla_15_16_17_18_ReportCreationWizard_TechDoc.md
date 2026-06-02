# Technical Documentation — Report Creation Wizard
## Pantallas 15 · 16 · 17 · 18: Nuevo Reporte · Selección Sub-tipo · Reporte Mascota Perdida · Avistamiento en Calle

> **Nota de arquitectura:** Las cuatro pantallas forman un **wizard de creación de reporte** (flujo lineal de 2–4 pasos).  
> Son **pantallas independientes en la pila de navegación**, no estados de una sola pantalla.  
> - **P15** → `NewReportTypeScreen` — selector de tipo de reporte (LOST vs FOUND)  
> - **P16** → `FoundSubTypeScreen` — selector de sub-tipo de hallazgo (solo si eligió FOUND)  
> - **P17** → `LostReportFormScreen` — formulario completo para mascota perdida  
> - **P18** → `SightingReportFormScreen` — formulario para avistamiento en calle  
> Un cuarto formulario ("Bajo mi cuidado") se infiere de P16 pero no aparece en imágenes — ver §7.4.

---

## 1. SCREEN IDENTITY

### Pantalla 15 — `NewReportTypeScreen`

| Campo | Valor |
|---|---|
| **Route** | `Routes.NewReport` |
| **Screen type** | Stack (sin Drawer) |
| **Entry points** | `FeedScreen` → FAB `+`; `MapScreen` → ícono `+` en `MapBottomBar` |
| **Exit points** | Tap "Mascota Perdida" → `Routes.LostReportForm`; Tap "Mascota Hallada" → `Routes.FoundSubType`; ← → `popBackStack()` |

### Pantalla 16 — `FoundSubTypeScreen`

| Campo | Valor |
|---|---|
| **Route** | `Routes.FoundSubType` |
| **Screen type** | Stack (sin Drawer) |
| **Entry points** | `NewReportTypeScreen` → tap "Mascota Hallada" |
| **Exit points** | Tap "La vi en la calle" → `Routes.SightingReportForm`; Tap "Está bajo mi cuidado" → `Routes.InCareReportForm` [ASSUMED]; ← → `popBackStack()` |

### Pantalla 17 — `LostReportFormScreen`

| Campo | Valor |
|---|---|
| **Route** | `Routes.LostReportForm` |
| **Screen type** | Stack (sin Drawer) |
| **Entry points** | `NewReportTypeScreen` → tap "Mascota Perdida" |
| **Exit points** | "PUBLICAR REPORTE" (success) → `popBackStack()` a `FeedScreen` o `Routes.Feed`; ← → `popBackStack()` con confirmación de descarte [ASSUMED] |

### Pantalla 18 — `SightingReportFormScreen`

| Campo | Valor |
|---|---|
| **Route** | `Routes.SightingReportForm` |
| **Screen type** | Stack (sin Drawer) |
| **Entry points** | `FoundSubTypeScreen` → tap "La vi en la calle" |
| **Exit points** | "Publicar avistamiento" (success) → `popBackStack()` a `FeedScreen`; ← → `popBackStack()` con confirmación de descarte [ASSUMED] |

---

## 2. VISUAL COMPONENTS

### 2.1 Pantalla 15 — `NewReportTypeScreen`

| # | Nombre Composable | MD3 (Compose) | Variante / Notas | Estados |
|---|---|---|---|---|
| 1 | `SimpleTopAppBar` | `TopAppBar` (Small) | Flecha ← izquierda, título "Nuevo Reporte", sin avatar ni hamburguesa, fondo `surface` | default |
| 2 | `InstructionText` | `Text` | `bodyLarge`, color `onBackground`, margen horizontal | default |
| 3 | `ReportTypeCard` (×2) | `OutlinedCard` o `ElevatedCard` | Cards seleccionables con ícono + título + subtítulo + descripción en recuadro gris interno. Una para LOST, otra para FOUND | default, pressed, [ASSUMED] selected (borde `primary`) |
| 4 | `ReportTypeIcon` (LOST) | `Icon(Icons.Outlined.Warning)` | Ícono triángulo de alerta, color `onSurface` | default |
| 5 | `ReportTypeIcon` (FOUND) | `Icon(Icons.Outlined.RemoveRedEye)` | Ícono ojo, color `onSurface` | default |
| 6 | `ReportTypeTitle` | `Text` | `titleMedium` bold, color `onSurface` | default |
| 7 | `ReportTypeSubtitle` | `Text` | `bodyMedium`, color `onSurfaceVariant` | default |
| 8 | `ReportTypeDescription` | `Text` dentro de `Surface(color = surfaceVariant)` | `bodySmall`, fondo gris `surfaceVariant`, esquinas redondeadas, padding interno | default |

### 2.2 Pantalla 16 — `FoundSubTypeScreen`

| # | Nombre Composable | MD3 (Compose) | Variante / Notas | Estados |
|---|---|---|---|---|
| 9 | `SimpleTopAppBar` | `TopAppBar` (Small) | Título "Reportar hallazgo" | default |
| 10 | `InstructionText` | `Text` | `bodyLarge` — "¿Cómo encontraste esta mascota?..." | default |
| 11 | `SubTypeCard` "La vi en la calle" | `OutlinedCard` / `ElevatedCard` | Ícono ojo oscuro (fondo gris circular), título, subtítulo, descripción | default, pressed |
| 12 | `SubTypeCard` "Está bajo mi cuidado" | `OutlinedCard` / `ElevatedCard` | Ícono casa en fondo naranja/terracota circular (`tertiaryContainer` [ASSUMED]), título, subtítulo, descripción | default, pressed |
| 13 | `SubTypeIconContainer` | `Box(clip = CircleShape)` con color de fondo | Ícono dentro de círculo: gris para avistamiento, naranja/terracota para "bajo mi cuidado" | default |

> **[ASSUMED]** El color naranja del ícono "casa" no está en el esquema M3 definido. Es probable que use `tertiary` o un color `orange` temporal. Confirmar con diseño — opciones: usar `secondaryContainer` o añadir token de color `careContainer`.

### 2.3 Pantalla 17 — `LostReportFormScreen`

| # | Nombre Composable | MD3 (Compose) | Variante / Notas | Estados |
|---|---|---|---|---|
| 14 | `DrawerTopBar` | `CenterAlignedTopAppBar` | Con hamburguesa ≡ izquierda, título "Generar Reporte", avatar der (igual que `AppTopBar`). **Nota:** esta pantalla SÍ tiene el drawer bar a diferencia de P15/P16/P18 | default |
| 15 | `ReportTypeBanner` | `Surface(color = secondary)` + `Text` | Banner amarillo (`secondary = #F6E27A`) con texto "Reporté mi animal perdido", ancho completo, sin esquinas redondeadas | default |
| 16 | `FormSectionHeader` (×4) | `Text` | `titleMedium` bold, color `onBackground`. Textos: "Fotos del animal", "Información básica", "Descripción y señas particulares", "Última ubicación vista", "Información de contacto" | default |
| 17 | `PhotoPickerArea` | `OutlinedCard` o `DashedBorderBox` [ASSUMED] | Área cuadrada ~100dp con ícono de galería y texto "Galería". Borde punteado o línea. Contador "0 de 6 fotos" debajo | default, has_photos, max_reached |
| 18 | `FormTextField` (×múltiples) | `OutlinedTextField` | Variante estándar MD3. Campos: Nombre o apodo*, Color predominante, Raza, Fecha*, Hora*, Recompensa (opcional) | default, focused, error, disabled |
| 19 | `SpeciesDropdown` | `ExposedDropdownMenuBox` + `ExposedDropdownMenuDefaults` | Label "Especie*", opciones: Perro, Gato, Otro. Trailing ícono chevron ↓ | default, expanded, error |
| 20 | `GenderDropdown` | `ExposedDropdownMenuBox` | Label "Sexo", opciones: Macho, Hembra, No sé | default, expanded |
| 21 | `ContactPrefDropdown` | `ExposedDropdownMenuBox` | Label "Preferencia de contacto", opciones [ASSUMED]: Llamada, WhatsApp, Ambas | default, expanded |
| 22 | `AgeTextField` | `OutlinedTextField` | Label "Edad aprox.", placeholder "Ej: 2 años", teclado texto | default, focused |
| 23 | `LocationTextField` | `OutlinedTextField` | Label "Referencia de ubicación", leading ícono `Icons.Outlined.LocationOn`, placeholder "Ej: Cerca del parque central..." | default, focused |
| 24 | `PhoneTextField` | `OutlinedTextField` | Label "Teléfono*", leading ícono teléfono, texto pre-llenado "+57 3209241000" (del perfil del usuario) | default, focused, error |
| 25 | `DescriptionTextField` | `OutlinedTextField` | Label "Descripción detallada", multiline (`minLines = 4`), sin contador visible | default, focused |
| 26 | `DescriptionHintList` | `Column { Text(bullet) }` | Lista de pistas: "Cicatrices o marcas", "Collar o accesorios", "Microchip", "Comportamientos". Color `onSurfaceVariant` | default |
| 27 | `PublishButton` | `Button` (FilledButton) | Color `tertiary` (`#2E7D32`), texto "PUBLICAR REPORTE" en mayúsculas, ancho completo, sticky en la parte inferior | default, loading, disabled |

### 2.4 Pantalla 18 — `SightingReportFormScreen`

| # | Nombre Composable | MD3 (Compose) | Variante / Notas | Estados |
|---|---|---|---|---|
| 28 | `SimpleTopAppBar` | `TopAppBar` (Small) | Flecha ← izquierda, título "Avistamiento en la calle" | default |
| 29 | `QuestionSectionHeader` | `Text` | `titleMedium` bold. Textos de sección: "¿Qué viste?", "¿Cómo estaba?", "¿Cuándo y dónde?", "Descripción", "Fotos" | default |
| 30 | `SpeciesChipGroup` | `Row` de `FilterChip` | Chips single-select: "Perro", "Gato", "Otro". Solo uno puede estar activo | default, selected, unselected |
| 31 | `SizeChipGroup` | `Row` de `FilterChip` | Chips single-select: "Pequeño", "Mediano", "Grande" | default, selected, unselected |
| 32 | `ColorSwatchRow` | `Row` de `Box(clip = CircleShape)` con colores | Círculos de color seleccionables: negro, marrón/naranja, blanco, gris, amarillo/dorado, blanco-crema. Borde al seleccionar [ASSUMED] | default, selected |
| 33 | `StatusChipGroup` | `FlowRow` de `FilterChip` | Chips multi-select: "Herido", "Asustado", "Tranquilo", "Agresivo", "Hambriento". Múltiples pueden estar activos. Label: "Estado (puedes seleccionar varios)" | default, selected, unselected |
| 34 | `CollarChipGroup` | `Row` de `FilterChip` | Chips single-select: "Sí", "No", "No recuerdo". Label: "¿Tenía collar?" | default, selected, unselected |
| 35 | `StillThereSwitch` | `Row { Text + Switch }` | Label "¿Sigue en esa zona?" alineado a la izquierda, `Switch` a la derecha. Estado inicial: off | default, on, off |
| 36 | `DateTimeTextField` | `OutlinedTextField` | Label "Fecha y hora", toca para abrir `DatePickerDialog` + `TimePickerDialog` [ASSUMED] | default, focused |
| 37 | `LocationMapPicker` | `AndroidView` (Google Maps) o `Box` con `Icon(LocationPin)` estático | Área rectangular ~180dp con pin centrado y texto "Toca para ajustar ubicación". Al tocar: abre selector de ubicación en mapa [ASSUMED] | default, selecting |
| 38 | `SightingDescriptionField` | `OutlinedTextField` | Multiline, placeholder "Añade detalles que puedan ayudar a identific..." | default, focused |
| 39 | `AddPhotoButton` | `OutlinedIconButton` o `Box` | Ícono `+` centrado en área cuadrada ~80dp. Label encima "Fotos" + "Opcional" en gris | default, pressed |
| 40 | `PublishSightingButton` | `Button` (FilledButton) | Color `tertiary` (`#2E7D32`), texto "Publicar avistamiento", ancho completo, sticky footer | default, loading, disabled |

---

## 3. DATA & STATE

### 3.1 Estado del Wizard — Compartido entre pantallas

El wizard tiene dos opciones de gestión de estado:

**Opción A (Recomendada):** `shared ViewModel` con `hiltViewModel()` scoped a la ruta padre o usando `SavedStateHandle` en cada ViewModel con navegación de argumentos.

**Opción B:** Pasar datos como argumentos de navegación entre pantallas (viable solo si son pocos campos).

Dado el volumen de campos de P17/P18, se recomienda **Opción A** — un `ReportCreationViewModel` compartido entre las pantallas del wizard:

```kotlin
// app/ui/screens/report/creation/ReportCreationViewModel.kt
@HiltViewModel
class ReportCreationViewModel @Inject constructor(
    private val createReport: CreateReport,
    private val observeCurrentUser: ObserveCurrentUser
) : ViewModel() {
    // Tipo seleccionado en P15
    var selectedType: ReportType? = null
    
    // Sub-tipo seleccionado en P16
    var selectedSubType: FoundSubType? = null  // SIGHTING | IN_CARE [ASSUMED nuevo enum]
}
```

### 3.2 UiState por Pantalla

#### P15 — `NewReportTypeUiState`
```kotlin
data class NewReportTypeUiState(
    val isLoading: Boolean = false  // Prácticamente sin estado
)
```

#### P16 — `FoundSubTypeUiState`
```kotlin
data class FoundSubTypeUiState(
    val isLoading: Boolean = false  // Prácticamente sin estado
)
```

#### P17 — `LostReportFormUiState`
```kotlin
data class LostReportFormUiState(
    // Fotos
    val photos: List<Uri> = emptyList(),          // Máx 6
    
    // Información básica
    val name: String = "",                         // Obligatorio*
    val species: String = "",                      // Obligatorio* — "Perro", "Gato", "Otro"
    val breed: String = "",
    val gender: String = "",                       // "Macho", "Hembra", "No sé"
    val ageApprox: String = "",
    val color: String = "",
    
    // Descripción
    val description: String = "",
    
    // Última ubicación
    val locationRef: String = "",                  // Texto libre
    val lostDate: LocalDate? = null,               // Obligatorio*
    val lostTime: LocalTime? = null,               // Obligatorio*
    val latitude: Double? = null,                  // [ASSUMED] si se añade picker de mapa
    val longitude: Double? = null,
    
    // Contacto
    val phone: String = "",                        // Pre-llenado desde User.phoneNumber
    val contactPreference: String = "",            // "Llamada", "WhatsApp", "Ambas"
    val reward: String = "",                       // Opcional
    
    // UI
    val isPublishing: Boolean = false,
    val publishSuccess: Boolean = false,
    val error: String? = null,
    
    // Validación
    val nameError: String? = null,
    val speciesError: String? = null,
    val dateError: String? = null,
    val phoneError: String? = null
)
```

#### P18 — `SightingReportFormUiState`
```kotlin
data class SightingReportFormUiState(
    // ¿Qué viste?
    val species: String = "",                      // Single-select chip
    val size: String = "",                         // Single-select chip
    val colors: List<String> = emptyList(),        // [ASSUMED] single-select swatch
    
    // ¿Cómo estaba?
    val statuses: List<String> = emptyList(),      // Multi-select chips
    val hasCollar: String = "",                    // "Sí", "No", "No recuerdo"
    val stillInArea: Boolean = false,              // Switch
    
    // ¿Cuándo y dónde?
    val sightingDateTime: LocalDateTime? = null,
    val locationRef: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    // Descripción
    val description: String = "",
    
    // Fotos
    val photos: List<Uri> = emptyList(),           // Opcional
    
    // UI
    val isPublishing: Boolean = false,
    val publishSuccess: Boolean = false,
    val error: String? = null
)
```

### 3.3 UiEvents

#### P17
```kotlin
sealed class LostReportFormUiEvent {
    data class NameChanged(val value: String) : LostReportFormUiEvent()
    data class SpeciesSelected(val value: String) : LostReportFormUiEvent()
    data class BreedChanged(val value: String) : LostReportFormUiEvent()
    data class GenderSelected(val value: String) : LostReportFormUiEvent()
    data class AgeChanged(val value: String) : LostReportFormUiEvent()
    data class ColorChanged(val value: String) : LostReportFormUiEvent()
    data class DescriptionChanged(val value: String) : LostReportFormUiEvent()
    data class LocationRefChanged(val value: String) : LostReportFormUiEvent()
    data class DateSelected(val date: LocalDate) : LostReportFormUiEvent()
    data class TimeSelected(val time: LocalTime) : LostReportFormUiEvent()
    data class PhoneChanged(val value: String) : LostReportFormUiEvent()
    data class ContactPrefSelected(val value: String) : LostReportFormUiEvent()
    data class RewardChanged(val value: String) : LostReportFormUiEvent()
    data class PhotosAdded(val uris: List<Uri>) : LostReportFormUiEvent()
    data class PhotoRemoved(val uri: Uri) : LostReportFormUiEvent()
    object PublishReport : LostReportFormUiEvent()
    object DismissError : LostReportFormUiEvent()
}
```

#### P18
```kotlin
sealed class SightingReportFormUiEvent {
    data class SpeciesSelected(val value: String) : SightingReportFormUiEvent()
    data class SizeSelected(val value: String) : SightingReportFormUiEvent()
    data class ColorSelected(val value: String) : SightingReportFormUiEvent()
    data class StatusToggled(val status: String) : SightingReportFormUiEvent()
    data class CollarSelected(val value: String) : SightingReportFormUiEvent()
    data class StillInAreaChanged(val value: Boolean) : SightingReportFormUiEvent()
    data class DateTimeSelected(val dateTime: LocalDateTime) : SightingReportFormUiEvent()
    data class LocationPicked(val lat: Double, val lng: Double) : SightingReportFormUiEvent()
    data class DescriptionChanged(val value: String) : SightingReportFormUiEvent()
    data class PhotoAdded(val uri: Uri) : SightingReportFormUiEvent()
    object PublishSighting : SightingReportFormUiEvent()
    object DismissError : SightingReportFormUiEvent()
}
```

### 3.4 Global State

| Puerto In | Pantalla | Lectura | Escritura |
|---|---|---|---|
| `ObserveCurrentUser` | P17 | Pre-llenar `phone` desde `User.phoneNumber` | No |
| `CreateReport` | P17, P18 | No | Envía el nuevo `PetReport` a Firestore + imagen a Storage |

### 3.5 API / Firebase Calls

| Operación | Puerto In | Cuándo se dispara | Qué hace |
|---|---|---|---|
| Pre-llenar teléfono | `observeCurrentUser()` | `LaunchedEffect(Unit)` en P17 | Toma `User.phoneNumber` y pone en `phone` del form |
| Publicar reporte perdido | `createReport(report, imageBytes)` | Tap "PUBLICAR REPORTE" tras validación | Sube hasta 6 fotos a Storage, crea documento en `pet_reports/` con `type = LOST` |
| Publicar avistamiento | `createReport(report, imageBytes?)` | Tap "Publicar avistamiento" | Sube foto opcional, crea documento con `type = FOUND_SIGHTING` [ASSUMED nuevo enum valor] |

> **Nota sobre múltiples fotos:** El puerto `createReport(report, imageBytes: ByteArray?)` solo acepta **una imagen**. Para soportar hasta 6 fotos (P17) se necesita extender el puerto — ver §7.2.

### 3.6 Loading / Empty / Error States

| Estado | Zona | Comportamiento |
|---|---|---|
| **Loading** | Botón "PUBLICAR REPORTE" | `CircularProgressIndicator` dentro del botón + `enabled = false` durante la subida |
| **Success** | Pantalla completa | `LaunchedEffect(publishSuccess)` → `navController.popBackStack()` + [ASSUMED] Snackbar "Reporte publicado" en FeedScreen |
| **Error** | Snackbar / `AlertDialog` | Mensaje de error con opción "Reintentar". El formulario queda intacto |
| **Validación** | Campos individuales | `OutlinedTextField(isError = true, supportingText = { Text(fieldError) })` |
| **Foto área vacía** | `PhotoPickerArea` | Estado inicial "0 de 6 fotos" visible |
| **Foto área con fotos** | `LazyRow` de thumbnails + tile `+` | Fotos en fila horizontal, botón × en cada una para eliminar [ASSUMED] |
| **Descarte sin guardar** | `AlertDialog` | Al presionar ← con campos no vacíos: "¿Descartar reporte?" Confirmar/Cancelar [ASSUMED] |

---

## 4. INTERACTIONS & BEHAVIOR

### 4.1 Flujo de Navegación Completo

```
FAB (+) en FeedScreen
    → NewReportTypeScreen (P15)
        → [Tap "Mascota Perdida"] → LostReportFormScreen (P17)
            → [Tap "PUBLICAR REPORTE"] → success → popBackStack() a FeedScreen
            → [← atrás] → AlertDialog descarte → NewReportTypeScreen

        → [Tap "Mascota Hallada"] → FoundSubTypeScreen (P16)
            → [Tap "La vi en la calle"] → SightingReportFormScreen (P18)
                → [Tap "Publicar avistamiento"] → success → popBackStack() a FeedScreen
            → [Tap "Está bajo mi cuidado"] → InCareReportFormScreen (P19) [ASSUMED — no en imágenes]
            → [← atrás] → NewReportTypeScreen
```

### 4.2 Interacciones — P15 y P16

| Trigger | Acción | Resultado |
|---|---|---|
| Tap en card "Mascota Perdida" | Navegación inmediata | `navController.navigate(Routes.LostReportForm)` |
| Tap en card "Mascota Hallada" | Navegación inmediata | `navController.navigate(Routes.FoundSubType)` |
| Tap en card "La vi en la calle" | Navegación inmediata | `navController.navigate(Routes.SightingReportForm)` |
| Tap en card "Está bajo mi cuidado" | Navegación inmediata | `navController.navigate(Routes.InCareReportForm)` [ASSUMED] |
| Tap en ← | `popBackStack()` | Sin confirmación (ningún dato ingresado aún) |

### 4.3 Interacciones — P17 (LostReportForm)

| Trigger | Acción | Resultado |
|---|---|---|
| Tap en área de fotos "Galería" | Lanza `PickMultipleVisualMedia(6)` | Picker nativo de fotos. Selecciona hasta 6 |
| Selección de fotos en picker | `onEvent(PhotosAdded(uris))` | Miniaturas aparecen en `LazyRow` reemplazando el área vacía. Contador "N de 6 fotos" |
| Tap × en miniatura de foto | `onEvent(PhotoRemoved(uri))` | Foto removida. Contador decrementado |
| Cambio en `OutlinedTextField` | `onEvent(*Changed(value))` | Actualiza `UiState` en tiempo real. Limpia el error del campo |
| Tap en dropdown "Especie" | Abre `ExposedDropdownMenu` | Lista: Perro, Gato, Otro |
| Tap en opción del dropdown | `onEvent(SpeciesSelected(value))` | Cierra dropdown, actualiza campo |
| Tap en campo "Fecha" | Abre `DatePickerDialog` [ASSUMED] | `MaterialDatePicker` de Compose MD3 |
| Tap en campo "Hora" | Abre `TimePickerDialog` [ASSUMED] | `TimePicker` de Compose MD3 |
| Tap "PUBLICAR REPORTE" sin validar | Ejecuta validación | Marca campos inválidos con `isError = true` + `supportingText`. Hace scroll al primer error [ASSUMED] |
| Tap "PUBLICAR REPORTE" válido | `onEvent(PublishReport)` | `isPublishing = true`, llama `CreateReport`, navega atrás en success |
| Tap ← con campos no vacíos | Muestra `AlertDialog` | "¿Descartar reporte?" → Confirmar: `popBackStack()` sin guardar [ASSUMED] |

### 4.4 Interacciones — P18 (SightingReportForm)

| Trigger | Acción | Resultado |
|---|---|---|
| Tap en chip Especie/Tamaño/Collar | `onEvent(SpeciesSelected / SizeSelected / CollarSelected)` | Single-select: deselecciona el anterior, selecciona el nuevo. Estilo `FilterChip` con `selected = true` |
| Tap en swatch de color | `onEvent(ColorSelected(colorName))` | [ASSUMED] Single-select — borde resaltado en el círculo seleccionado |
| Tap en chip de Estado | `onEvent(StatusToggled(status))` | Multi-select: togglea el chip (puede haber 0 a N seleccionados) |
| Toggle de `Switch` "¿Sigue en esa zona?" | `onEvent(StillInAreaChanged(value))` | Actualiza `stillInArea: Boolean` |
| Tap en campo "Fecha y hora" | Abre `DatePickerDialog` + `TimePickerDialog` [ASSUMED] | Secuencial: primero fecha, luego hora |
| Tap en `LocationMapPicker` | [ASSUMED] Abre `LocationPickerScreen` o `BottomSheet` con mapa interactivo | El usuario coloca pin en el mapa. Al confirmar: `onEvent(LocationPicked(lat, lng))` |
| Tap en `+` de fotos | Lanza `PickVisualMedia` (una foto) [ASSUMED] | Picker nativo, foto opcional |
| Tap "Publicar avistamiento" sin Especie | Muestra error inline [ASSUMED] | Al menos Especie requerida |
| Tap "Publicar avistamiento" válido | `onEvent(PublishSighting)` | `isPublishing = true`, `CreateReport`, navega atrás |

### 4.5 Lógica Condicional

```kotlin
// P17 — botón habilitado solo si campos obligatorios completos
val isFormValid = state.name.isNotBlank() &&
                  state.species.isNotBlank() &&
                  state.lostDate != null &&
                  state.lostTime != null &&
                  state.phone.isNotBlank() &&
                  isValidPhone(state.phone)

// P17 — máximo 6 fotos
val canAddMorePhotos = state.photos.size < 6

// P18 — Switch deshabilitado si no se seleccionó ubicación [ASSUMED]
val switchEnabled = state.latitude != null

// P18 — botón de publicar: mínimo Especie seleccionada [ASSUMED]
val isSightingValid = state.species.isNotBlank()
```

### 4.6 Animaciones / Transiciones

| Elemento | Animación |
|---|---|
| Entrada a P15/P16/P17/P18 | `slideInHorizontally` nativo del Stack navigation |
| Chips seleccionados | `FilterChip` de MD3 ya anima el cambio de estado (checked icon + color) |
| Botón "PUBLICAR" → loading | [ASSUMED] `AnimatedContent` entre texto e `CircularProgressIndicator` |
| `AlertDialog` descarte | Animación nativa MD3 |
| Galería de fotos tras añadir | [ASSUMED] `AnimatedVisibility` al reemplazar área vacía con `LazyRow` |

---

## 5. PROPS & REUSABILITY

### 5.1 P15 y P16 — Reutilizables Como `SelectionWizardStep`

P15 y P16 son estructuralmente idénticas: título de instrucción + 2 cards de selección. Pueden generalizarse:

```kotlin
@Composable
fun SelectionWizardStep(
    title: String,
    instruction: String,
    options: List<WizardOption>,
    onOptionSelected: (WizardOption) -> Unit
)

data class WizardOption(
    val id: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val title: String,
    val subtitle: String,
    val description: String
)
```

### 5.2 Componentes Extraíbles como Reutilizables

| Composable | Archivo sugerido | Dónde se reutiliza |
|---|---|---|
| `SelectionWizardStep` | `app/ui/components/SelectionWizardStep.kt` | P15, P16, [ASSUMED] P19 "Bajo mi cuidado" |
| `SingleSelectChipGroup` | `app/ui/components/SingleSelectChipGroup.kt` | P18 Especie, Tamaño, Collar — también `ReportDetailScreen` |
| `MultiSelectChipGroup` | `app/ui/components/MultiSelectChipGroup.kt` | P18 Estado, [ASSUMED] P19 Estado físico |
| `ColorSwatchSelector` | `app/ui/components/ColorSwatchSelector.kt` | P18, potencialmente en búsqueda/filtros |
| `PhotoPickerRow` | `app/ui/components/PhotoPickerRow.kt` | P17 (hasta 6 fotos), P18 (opcional), [ASSUMED] P19 |
| `FormSectionHeader` | `app/ui/components/FormSectionHeader.kt` | P17, P18, [ASSUMED] P19 |
| `LocationMapPicker` | `app/ui/components/LocationMapPicker.kt` | P18 (y P19), `MapScreen` filters. Encapsula mapa interactivo o static |
| `PublishButton` | Extraer de P17/P18 | Botón verde ancho completo sticky — idéntico en ambas pantallas |
| `SectionLabel` | `app/ui/components/SectionLabel.kt` | Ya documentado en P12/13/14 — mismo patrón aquí |

---

## 6. DEPENDENCIES

### 6.1 Dependencias de Navegación

| Dependencia | Estado |
|---|---|
| `Routes.NewReport` | ❌ Nuevo — agregar a `Routes.kt` |
| `Routes.FoundSubType` | ❌ Nuevo |
| `Routes.LostReportForm` | ❌ Nuevo |
| `Routes.SightingReportForm` | ❌ Nuevo |
| `Routes.InCareReportForm` | ❌ Nuevo [ASSUMED] |
| `FeedScreen` → FAB con navegación al wizard | ⚠️ El FAB actual abre un `Dialog` inline — necesita refactorizarse para navegar |
| `AppNavHost.kt` — nuevas rutas del wizard | ❌ Nuevo |

### 6.2 ViewModels Nuevos

| ViewModel | Puertos In |
|---|---|
| `LostReportFormViewModel` | `ObserveCurrentUser`, `CreateReport` |
| `SightingReportFormViewModel` | `CreateReport` |
| [ASSUMED] `InCareReportFormViewModel` | `CreateReport`, `ObserveCurrentUser` |

> **P15 y P16 no necesitan ViewModel** — son pantallas de navegación pura sin estado ni llamadas de red.

### 6.3 Puertos y Use Cases

| Puerto In | Estado actual | Nota |
|---|---|---|
| `ObserveCurrentUser` | ✅ Implementado | Para pre-llenar teléfono en P17 |
| `CreateReport(report, imageBytes: ByteArray?)` | ✅ Implementado | Solo acepta 1 imagen — ver §7.2 |

### 6.4 Librerías Implicadas

| Librería | Función | Estado |
|---|---|---|
| `ActivityResultContracts.PickMultipleVisualMedia` | Picker de hasta 6 fotos en P17 | ✅ AndroidX nativo — sin lib extra |
| `ActivityResultContracts.PickVisualMedia` | Picker de 1 foto opcional en P18 | ✅ AndroidX nativo |
| Compose MD3 `DatePicker` / `TimePicker` | Selector de fecha y hora en P17/P18 | ✅ Ya en Compose MD3 — usar `rememberDatePickerState` |
| Google Maps SDK / `maps-compose` | `LocationMapPicker` en P18 | ❌ Fuera de scope MVP — ver §7.3 |
| Coil | Thumbnails de fotos seleccionadas | ✅ Ya incluido |

---

## 7. IMPLEMENTATION NOTES

### 7.1 ⚠️ Colisión con Implementación Actual del FAB en FeedScreen

Según `TECHNICAL_CONTEXT.md §11`, el `FeedScreen` ya tiene implementado un **Dialog inline de creación** de reportes accesible desde el FAB. Las nuevas pantallas 15–18 proponen un **wizard de navegación independiente** que reemplaza ese dialog.

**Decisión requerida:**

| Opción | Descripción | Recomendación |
|---|---|---|
| A. Reemplazar Dialog por wizard | Eliminar el Dialog de FeedScreen. FAB navega a `Routes.NewReport` (P15) | ✅ Recomendada — el wizard es más rico y escalable |
| B. Mantener Dialog para MVP + wizard como "v2" | El wizard existe como pantallas pero no se conecta al FAB todavía | ⚠️ Genera deuda técnica y duplicación |
| C. Hacer el Dialog un paso del wizard | El Dialog se convierte en el P15 (modal) y navega a formularios completos | ❌ Mezcla paradigmas de navegación |

**Si se elige A:** Actualizar `FeedScreen` para que el FAB llame a:
```kotlin
onNavigateToNewReport = { navController.navigate(Routes.NewReport) }
```
Y eliminar el `CreateReportDialog` composable de `FeedScreen`.

### 7.2 ⚠️ Puerto `CreateReport` Solo Acepta Una Imagen

P17 permite subir hasta **6 fotos**. El puerto actual:
```kotlin
suspend fun invoke(report: PetReport, imageBytes: ByteArray?)
```
Solo acepta un `ByteArray?`. Opciones:

| Opción | Implementación | Recomendación |
|---|---|---|
| A. Cambiar firma del puerto | `suspend fun invoke(report: PetReport, imageBytesList: List<ByteArray>)` | ✅ Limpio — actualizar use case, adapter Firebase y Fake |
| B. Múltiples llamadas consecutivas | `createReport` una vez con la imagen principal, luego `updateReport` para las demás | ❌ Inconsistente, riesgo de reporte sin fotos adicionales |
| C. Pre-subir fotos a Storage antes de llamar `createReport` | `uploadImages(uris): List<String>` → pasar URLs al report | ⚠️ Requiere nuevo método en el repositorio o use case separado |

**Recomendación A** para mantener la operación atómica. Cambios en cascada:
- `domain/port/in/CreateReport.kt`
- `domain/usecase/CreateReportImpl.kt`
- `data/firebase/FirestorePetReportRepository.kt`
- `data/fake/FakePetReportRepository.kt`
- `PetReport.additionalPhotos: List<String>` (ya flagueado en P12/13/14)

### 7.3 `LocationMapPicker` en P18 — Estrategia sin maps-compose

El área de mapa en P18 (con pin fijo y texto "Toca para ajustar ubicación") requiere una de estas implementaciones:

| Estrategia | Descripción | Complejidad |
|---|---|---|
| A. Placeholder estático | `Box` con fondo gris + `Icon(LocationPin)` centrado. Al tocar: `TextField` de texto libre | ✅ Mínima — 0 dependencias nuevas. Aceptable para MVP |
| B. Static Maps API + tap | Imagen estática del mapa (Coil + URL de Static Maps). Al tocar: abre `LocationPickerScreen` con maps-compose | ⚠️ Requiere API key |
| C. `maps-compose` completo | Mapa interactivo embebido. `Marker` movible con drag | ❌ Fuera de scope MVP |

**Recomendación para MVP:** Estrategia A — campo de texto "Referencia de ubicación" igual que en P17. Anotar como `// TODO: reemplazar con MapPicker en Fase N`.

### 7.4 Pantalla "Bajo mi Cuidado" No Está en las Imágenes

P16 menciona el sub-tipo "Está bajo mi cuidado". No se entregó imagen del formulario correspondiente (sería P19). 

**Recomendación:** Crear `Routes.InCareReportForm` que por ahora navega a una pantalla placeholder `Text("Próximamente")`. El formulario tendrá campos similares a P17 (fotos obligatorias, descripción detallada, disponibilidad de tiempo según el texto de P16).

### 7.5 Validación de Teléfono

El campo de teléfono en P17 muestra "+57 3209241000" (pre-llenado desde `User.phoneNumber` en formato E.164). Validar con:

```kotlin
fun isValidPhone(phone: String): Boolean {
    val cleaned = phone.replace(Regex("[\\s\\-()]"), "")
    return Regex("^\\+?[1-9]\\d{7,14}$").matches(cleaned)
}
```

> Si `User.phoneNumber` ya está verificado (E.164 desde OTP), el campo puede ser read-only por defecto con opción "Cambiar" [ASSUMED].

### 7.6 `ExposedDropdownMenuBox` — Bug conocido en Compose

`ExposedDropdownMenuBox` en Compose tiene un comportamiento de teclado conocido: el dropdown no cierra el teclado al abrirse. Aplicar:

```kotlin
val focusManager = LocalFocusManager.current
ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = {
        focusManager.clearFocus()
        expanded = !expanded
    }
)
```

### 7.7 `DatePicker` y `TimePicker` MD3

Usar los componentes nativos de Compose MD3 — ya disponibles sin dependencias adicionales:

```kotlin
// Fecha (P17, P18)
val datePickerState = rememberDatePickerState()
if (showDatePicker) {
    DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = { TextButton(onClick = {
            onEvent(DateSelected(datePickerState.selectedDateMillis.toLocalDate()))
            showDatePicker = false
        }) { Text("OK") } }
    ) { DatePicker(state = datePickerState) }
}

// Hora (P17, P18)
val timePickerState = rememberTimePickerState()
if (showTimePicker) {
    AlertDialog(...) { TimePicker(state = timePickerState) }
}
```

### 7.8 ReportType — Extensión del Enum (Sinergia con P12/13/14)

Este wizard confirma la necesidad de extender `ReportType`:

```kotlin
// domain/model/ReportType.kt
enum class ReportType {
    LOST,           // P15 → P17 — mascota perdida
    FOUND_SIGHTING, // P16 → P18 — vi un animal en la calle  
    FOUND_IN_CARE   // P16 → P19 — tengo el animal en mi casa
}
```

Impacto en el repositorio:
```kotlin
// FirestorePetReportRepository — ajustar serialización
"LOST" → ReportType.LOST
"FOUND_SIGHTING" → ReportType.FOUND_SIGHTING
"FOUND_IN_CARE" → ReportType.FOUND_IN_CARE
```

También actualizar `StatusChip` que actualmente solo distingue `LOST` vs `FOUND`:
```kotlin
// StatusChip.kt — actualizar lógica
when (report.type) {
    LOST -> AssistChip(colors = errorColors, label = "PERDIDO")
    FOUND_SIGHTING -> AssistChip(colors = tertiaryColors, label = "AVISTADO")
    FOUND_IN_CARE -> AssistChip(colors = tertiaryColors, label = "HALLAZGO")
}
```

### 7.9 Accesibilidad (a11y)

| Elemento | Consideración |
|---|---|
| Cards P15/P16 | `Modifier.semantics { role = Role.Button }` + `contentDescription` con título + descripción |
| `FilterChip` grupos | `contentDescription = "Especie: ${chip}. ${if selected "Seleccionado" else "No seleccionado"}"` |
| `ColorSwatchSelector` | `contentDescription = "Color: ${colorName}"` — nunca usar solo el color como información |
| `PhotoPickerRow` | `contentDescription = "Foto ${index+1} de ${total}. Tocar para eliminar"` para cada thumbnail |
| Botones "PUBLICAR" | `contentDescription` descriptivo; `enabled` con semántica correcta cuando `isPublishing = true` |
| `OutlinedTextField` con error | `isError = true` + `supportingText` ya maneja la semántica de error correctamente en MD3 |
| `Switch` "¿Sigue en esa zona?" | Label a la izquierda asociado con `Modifier.toggleable(role = Role.Switch)` |

### 7.10 Edge Cases

| Caso | Manejo |
|---|---|
| Usuario sin teléfono verificado intenta publicar | [ASSUMED] Snackbar "Verifica tu número para publicar reportes" + redirect a OTP |
| 6 fotos seleccionadas → tap en tile `+` | Tile `+` se oculta o deshabilita cuando `photos.size == 6` |
| Foto seleccionada > 10MB | [ASSUMED] Comprimir antes de subir con `BitmapFactory` + `compress()`. Máximo recomendado: 1MB por foto |
| Publicación falla a mitad (fotos subidas, Firestore falla) | [ASSUMED] Rollback: borrar fotos ya subidas en Storage. Mostrar error "Publicación fallida" |
| Campo "Hora" sin "Fecha" | Deshabilitar campo hora hasta que fecha esté seleccionada, o validar juntos |
| Back press del sistema (gesto ← Android) en P17/P18 con datos | `BackHandler` para interceptar y mostrar `AlertDialog` de descarte |

---

## SHORT SUMMARY

- 🔀 **El FAB del FeedScreen debe refactorizarse:** Actualmente abre un `Dialog` inline. El wizard requiere que el FAB navegue a `Routes.NewReport` (P15). Eliminar `CreateReportDialog` de `FeedScreen` es el primer cambio a hacer.

- 📸 **Puerto `CreateReport` necesita soporte multi-imagen:** La firma actual acepta solo `ByteArray?`. P17 permite hasta 6 fotos — cambiar a `List<ByteArray>` actualizando use case, adapter Firebase y Fake en cascada.

- 🗂️ **`ReportType` debe extenderse a 3 valores:** `LOST / FOUND_SIGHTING / FOUND_IN_CARE`. Este es el cambio de dominio que conecta P15/16 (selección de tipo) → P17/18/19 (formularios) → P12/13/14 (detalle). Todos los adaptadores de Firestore y el `StatusChip` deben actualizarse.

- 🗺️ **`LocationMapPicker` de P18 → placeholder para MVP:** Reemplazar el widget de mapa interactivo con un `TextField` de texto libre (igual que P17). La integración de maps-compose para el picker de ubicación queda para una fase posterior.

- ♻️ **P15 y P16 son el mismo componente:** Ambas pantallas tienen estructura idéntica (instrucción + 2 cards). Extraer `SelectionWizardStep` como composable genérico — también servirá para P19 ("Bajo mi cuidado") cuyo formulario aún no se ha diseñado.

---

*Documento generado con base en Pantallas 15, 16, 17 y 18 del diseño de MascotasPerdidas + TECHNICAL_CONTEXT.md (Junio 2026).*
