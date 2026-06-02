# Technical Documentation — Pantallas 19 · 21 · 22 · 23
## Mascota Bajo Mi Cuidado · Reporte Confirmado · Mis Reportes · Avistamientos Recibidos

> **Nota de arquitectura crítica:** La **Pantalla 23** introduce una `NavigationBar` (bottom nav) con 4 tabs  
> (Inicio, Mapa, Avisos, Perfil) que **no existe en el sistema de navegación actual**. Esto implica  
> una refactorización profunda de `AppNavHost.kt` y potencialmente de `DrawerShell`. Ver §7.5.

---

## PANTALLA 19 — `InCareReportFormScreen`
### "Mascota bajo mi cuidado"

---

### 1. SCREEN IDENTITY

| Campo | Valor |
|---|---|
| **Screen name** | `InCareReportFormScreen` |
| **Route** | `Routes.InCareReportForm` |
| **Screen type** | Stack (sin Drawer, sin bottom nav) |
| **Entry points** | `FoundSubTypeScreen` (P16) → tap "Está bajo mi cuidado" |
| **Exit points** | "Publicar reporte de resguardo" (success) → `Routes.ReportConfirmed` (P21) o `popBackStack()`; ← → `AlertDialog` descarte → `popBackStack()` |

---

### 2. VISUAL COMPONENTS

| # | Nombre Composable | MD3 (Compose) | Variante / Notas | Estados |
|---|---|---|---|---|
| 1 | `SimpleTopAppBar` | `TopAppBar` (Small) | Flecha ← izquierda, título "Mascota bajo mi cuidado", fondo `surface` | default |
| 2 | `FormSectionHeader` (×5) | `Text` | `titleMedium` bold. Secciones: "Fotos", "¿Cómo es?", "Identificación", "Estado y comportamiento", "Urgencia / disponibilidad", "Ubicación" | default |
| 3 | `PhotoPickerTile` | `OutlinedCard` o `DashedBorderBox` | Área cuadrada ~90dp con `Icon(Add)` centrado, borde punteado. Solo 1 tile visible (puede expandir a `LazyRow`) | default, has_photo |
| 4 | `PhotoSubtitle` | `Text` | `bodySmall`, color `onSurfaceVariant`. "Agrega fotos de frente, perfil y detalles como collar o marcas" | default |
| 5 | `SpeciesChipGroup` | `Row` de `FilterChip` | Single-select: "Perro", "Gato", "Otro" | default, selected |
| 6 | `SizeChipGroup` | `Row` de `FilterChip` | Single-select: "Pequeño", "Mediano", "Grande" | default, selected |
| 7 | `BreedTextField` | `OutlinedTextField` | Label "Raza (si la conoces)", placeholder "Ej: Labrador, Siamés, Mestizo..." | default, focused |
| 8 | `GenderChipGroup` | `Row` de `FilterChip` | Single-select: "Macho", "Hembra", "No sé" | default, selected |
| 9 | `AgeChipGroup` | `FlowRow` de `FilterChip` | Single-select: "Cachorro", "Joven", "Adulto", "Senior" (4 chips, 2da fila por ancho) | default, selected |
| 10 | `CollarPlateSwitch` | `Row { Text + Switch }` | Label "¿Tiene collar con placa?", `Switch` a la derecha | default, on, off |
| 11 | `MicrochipSwitch` | `Row { Text + Switch }` | Label "¿Tiene microchip?", `Switch` a la derecha | default, on, off |
| 12 | `PhysicalStatusChipGroup` | `FlowRow` de `FilterChip` | Multi-select: "Herido", "Desnutrido", "Saludable", "Sucio", "Con pulgas". Label "Estado físico" | default, selected |
| 13 | `BehaviorChipGroup` | `FlowRow` de `FilterChip` | Multi-select: "Cariñoso", "Asustado", "Tranquilo", "Nervioso", "Agresivo". Label "Comportamiento" | default, selected |
| 14 | `NotesTextField` | `OutlinedTextField` | Label "Notas adicionales", placeholder "Cualquier detalle importante sobre su estado", multiline | default, focused |
| 15 | `UrgencyOptionGroup` (×4) | `Row` de cards tipo `RadioButton` | Opciones de disponibilidad. **Ver detalle abajo.** | default, selected |
| 16 | `UrgencyOption` | `OutlinedCard` + `Row { Icon + Column { title + subtitle } }` | Card seleccionable. Cuando selected: borde naranja/terracota + fondo naranja claro. Ícono de color naranja cuando selected. Opciones: "Indefinido" (reloj), "Pocos días" (reloj), "Solo hoy (urgente)" (ícono urgente), "Necesito entregarlo ya" (rayo) | default, selected |
| 17 | `FindDateTimeField` | `OutlinedTextField` | Label "Fecha y hora del hallazgo", tap abre DatePicker + TimePicker | default, focused |
| 18 | `LocationMapPicker` | `AndroidView` / `Box` con pin | Área rectangular ~180dp con pin centrado + texto "Toca para ajustar ubicación". Igual que P18 | default, selecting |
| 19 | `PublishInCareButton` | `Button` (FilledButton) | **Color naranja/terracota** (NO `tertiary` verde — atención), texto "Publicar reporte de resguardo", ancho completo sticky | default, loading, disabled |

> ⚠️ **[ASSUMED] Color del botón "Publicar reporte de resguardo":** El botón es naranja, diferente al verde `tertiary` usado en P17/P18. El esquema M3 actual no tiene un token naranja. Opciones: (A) Usar el color naranja del ícono "Está bajo mi cuidado" (P16) como nuevo token `careColor`, (B) usar `error` (`#E57373`) que es rojizo, (C) agregar token nuevo. **Confirmar con diseño.**

---

### 3. DATA & STATE

#### UiState
```kotlin
data class InCareReportFormUiState(
    // Fotos
    val photos: List<Uri> = emptyList(),

    // ¿Cómo es?
    val species: String = "",
    val size: String = "",
    val breed: String = "",
    val gender: String = "",
    val ageRange: String = "",             // "Cachorro" | "Joven" | "Adulto" | "Senior"

    // Identificación
    val hasCollarPlate: Boolean = false,
    val hasMicrochip: Boolean = false,

    // Estado y comportamiento
    val physicalStatus: List<String> = emptyList(),  // multi-select
    val behaviors: List<String> = emptyList(),       // multi-select
    val notes: String = "",

    // Urgencia
    val urgency: CareUrgency = CareUrgency.INDEFINITE,

    // Ubicación
    val foundDateTime: LocalDateTime? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationRef: String = "",

    // UI
    val isPublishing: Boolean = false,
    val publishSuccess: Boolean = false,
    val error: String? = null
)

enum class CareUrgency {
    INDEFINITE,   // "Indefinido — Puedo cuidarlo el tiempo necesario"
    FEW_DAYS,     // "Pocos días — Solo puedo tenerlo unos días"
    TODAY_ONLY,   // "Solo hoy (urgente) — Necesito entregarlo hoy mismo"
    URGENT_NOW    // "Necesito entregarlo ya — Situación muy urgente"
}
```

#### UiEvent
```kotlin
sealed class InCareReportFormUiEvent {
    data class PhotoAdded(val uri: Uri) : InCareReportFormUiEvent()
    data class PhotoRemoved(val uri: Uri) : InCareReportFormUiEvent()
    data class SpeciesSelected(val value: String) : InCareReportFormUiEvent()
    data class SizeSelected(val value: String) : InCareReportFormUiEvent()
    data class BreedChanged(val value: String) : InCareReportFormUiEvent()
    data class GenderSelected(val value: String) : InCareReportFormUiEvent()
    data class AgeRangeSelected(val value: String) : InCareReportFormUiEvent()
    data class CollarPlateChanged(val value: Boolean) : InCareReportFormUiEvent()
    data class MicrochipChanged(val value: Boolean) : InCareReportFormUiEvent()
    data class PhysicalStatusToggled(val status: String) : InCareReportFormUiEvent()
    data class BehaviorToggled(val behavior: String) : InCareReportFormUiEvent()
    data class NotesChanged(val value: String) : InCareReportFormUiEvent()
    data class UrgencySelected(val urgency: CareUrgency) : InCareReportFormUiEvent()
    data class DateTimeSelected(val dt: LocalDateTime) : InCareReportFormUiEvent()
    data class LocationPicked(val lat: Double, val lng: Double) : InCareReportFormUiEvent()
    object PublishReport : InCareReportFormUiEvent()
    object DismissError : InCareReportFormUiEvent()
}
```

#### API Calls

| Operación | Puerto In | Cuándo | Qué hace |
|---|---|---|---|
| Publicar reporte resguardo | `createReport(report, imageBytes)` | Tap "Publicar reporte de resguardo" | Crea `PetReport` con `type = FOUND_IN_CARE`, sube fotos, guarda en Firestore |

---

### 4. INTERACTIONS & BEHAVIOR

| Trigger | Acción | Resultado |
|---|---|---|
| Tap en `PhotoPickerTile` `+` | Lanza `PickVisualMedia` | Foto agregada. Tile `+` persiste hasta llegar al límite [ASSUMED: mismo límite que P17 = 6] |
| Tap en chip (Especie/Tamaño/Género/Edad) | Single-select | Solo uno activo por grupo |
| Toggle Switch collar/microchip | `onEvent(CollarPlateChanged / MicrochipChanged)` | Actualiza booleano — [ASSUMED] sin campos extra visibles en este estado |
| Tap en chip Estado físico / Comportamiento | Multi-select | Togglea el chip activo |
| Tap en `UrgencyOption` | `onEvent(UrgencySelected(urgency))` | Card seleccionada: borde + fondo naranja. Las demás: outline neutro |
| Tap en "Fecha y hora del hallazgo" | Abre `DatePickerDialog` + `TimePickerDialog` | Secuencial fecha → hora |
| Tap en `LocationMapPicker` | [ASSUMED] `LocationPickerScreen` o placeholder texto | Actualiza `lat/lng` |
| Tap "Publicar reporte de resguardo" | Valida + `onEvent(PublishReport)` | Loading → success → navega a `Routes.ReportConfirmed` (P21) |
| Tap ← con datos | `AlertDialog` descarte | Confirmar: `popBackStack()` |

#### Lógica Condicional
```kotlin
// Botón habilitado si:
val isFormValid = state.species.isNotBlank() && state.urgency != null

// [ASSUMED] Switch collar con placa → podría expandir TextField para número de placa
// [ASSUMED] Switch microchip → podría expandir TextField para código de chip
```

---

### 5. PROPS & REUSABILITY

- Pantalla one-off pero comparte la mayoría de componentes con P18: `SingleSelectChipGroup`, `MultiSelectChipGroup`, `LocationMapPicker`, `PhotoPickerRow`, `FormSectionHeader`.
- `UrgencyOptionGroup` es un componente **nuevo y único** de esta pantalla — extraer como `UrgencySelector`.
- `AgeChipGroup` con 4 opciones en FlowRow es reutilizable en filtros.

```kotlin
@Composable
fun UrgencySelector(
    selected: CareUrgency,
    onSelected: (CareUrgency) -> Unit,
    options: List<UrgencyOption>
)

data class UrgencyOption(
    val urgency: CareUrgency,
    val icon: ImageVector,
    val iconTint: Color,
    val title: String,
    val subtitle: String
)
```

---

### 6. DEPENDENCIES

| Dependencia | Estado |
|---|---|
| `Routes.InCareReportForm` | ❌ Nuevo |
| `Routes.ReportConfirmed` | ❌ Nuevo (navega a P21) |
| `InCareReportFormViewModel` | ❌ Nuevo |
| `CareUrgency` enum | ❌ Nuevo — en `domain/model/` o `app/ui/screens/` [ASSUMED en app layer] |
| `CreateReport` puerto | ✅ Existente — requiere extensión multi-imagen (flagueado en P17/18) |
| Componentes compartidos con P18 | ❌ Aún por crear (documentados en P18) |

---

### 7. IMPLEMENTATION NOTES (P19)

- **`FlowRow`** para chips de edad y estado físico: Usar `androidx.compose.foundation.layout.FlowRow` (stable desde Compose 1.6). No usar `LazyHorizontalGrid` para grupos pequeños de chips.
- **`UrgencyOption` seleccionado:** El color naranja del borde y fondo no tiene token en el esquema actual. Propuesta: agregar `val careContainer = Color(0xFFFFE0B2)` y `val care = Color(0xFFE65100)` a `Color.kt` como tokens semánticos de "resguardo".
- **`Switch` con `CollarPlate/Microchip`:** Considerar expandir con `AnimatedVisibility` un `TextField` para número de placa o código de microchip cuando el switch está ON — visible en muchos formularios veterinarios. No visible en la imagen pero altamente probable.
- **Scroll + sticky button:** Usar `Scaffold(bottomBar = { PublishInCareButton })` + `LazyColumn` con `contentPadding = PaddingValues(bottom = buttonHeight)` para que el último campo no quede tapado.

---
---

## PANTALLA 21 — `ReportConfirmedScreen`
### "Reporte Confirmado"

---

### 1. SCREEN IDENTITY

| Campo | Valor |
|---|---|
| **Screen name** | `ReportConfirmedScreen` |
| **Route** | `Routes.ReportConfirmed` (recibe `reportId: String` como argumento) |
| **Screen type** | Stack — **sin botón ← atrás** (pantalla de destino final del wizard) |
| **Entry points** | Cualquier formulario del wizard (P17, P18, P19) tras publicación exitosa |
| **Exit points** | Botón gris [ASSUMED] → `Routes.Feed` o `Routes.MyReports` (P22); [ASSUMED] tap en imagen → `Routes.ReportDetail` |

---

### 2. VISUAL COMPONENTS

| # | Nombre Composable | MD3 (Compose) | Variante / Notas | Estados |
|---|---|---|---|---|
| 1 | `ConfirmedTitle` | `Text` | `headlineLarge` bold, centrado. Texto "Reporte Confirmado". Prefijo con ícono checkbox verde `✅` (o `Icon(Icons.Filled.CheckCircle, tint = tertiary)`) | default |
| 2 | `ConfirmedPetImage` | `AsyncImage` (Coil) | Imagen del reporte recién creado, ~180×160dp, esquinas redondeadas (~16dp), alineada a la izquierda | default, loading |
| 3 | `ConfirmedDescription` | `Text` | `bodyMedium`, color `onBackground`, a la derecha de la imagen. Muestra la descripción del reporte | default |
| 4 | `ActionButton` | `Button` o `OutlinedButton` | Botón gris/neutral [ASSUMED `OutlinedButton` o `FilledTonalButton`] centrado debajo del par imagen+texto. El label no es legible en la imagen — [ASSUMED] "Ver mi reporte" o "Ir al inicio" | default, pressed |

> ⚠️ **[ASSUMED]** La pantalla parece incompleta en el diseño (mucho espacio vacío abajo). Probable que falten elementos: botón de compartir, CTA secundario "Volver al feed", o animación de confetti. Confirmar con diseño si hay más elementos.

> ⚠️ **[ASSUMED]** No hay `TopAppBar`. La pantalla es un destino final — al navegar aquí se hace `popUpTo(Routes.Feed) { inclusive = false }` para que el botón atrás del sistema no regrese al formulario.

---

### 3. DATA & STATE

```kotlin
data class ReportConfirmedUiState(
    val report: PetReport? = null,
    val isLoading: Boolean = true
)
```

#### API Calls

| Operación | Puerto | Cuándo | Qué hace |
|---|---|---|---|
| Obtener reporte confirmado | [ASSUMED] `reportId` pasado como argumento de navegación → `observeReports().find { it.id == reportId }` | `LaunchedEffect(reportId)` | Muestra imagen y descripción del reporte recién creado |

> **Alternativa más simple:** Pasar el `PetReport` serializado como argumento de navegación o usar un `SharedViewModel` del wizard. Evita el round-trip a Firestore.

---

### 4. INTERACTIONS & BEHAVIOR

| Trigger | Acción | Resultado |
|---|---|---|
| Tap en botón gris [ASSUMED "Ir al inicio" / "Ver mi reporte"] | Navega | `navController.navigate(Routes.Feed) { popUpTo(Routes.NewReport) { inclusive = true } }` |
| Botón atrás del sistema (gesto ←) | [ASSUMED] Deshabilitado o navega a Feed | `BackHandler { navController.navigate(Routes.Feed) { popUpTo(...) } }` |
| Tap en imagen [ASSUMED] | Navega a detalle | `navController.navigate(Routes.ReportDetail(reportId))` |

---

### 5. PROPS & REUSABILITY

Pantalla one-off. Sin componentes únicos extraíbles — usa `HeroImage` (documentado en P12/13/14) y `Text` estándar.

```kotlin
@Composable
fun ReportConfirmedScreen(
    reportId: String,
    onNavigateToFeed: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: ReportConfirmedViewModel = hiltViewModel()
)
```

---

### 6. DEPENDENCIES

| Dependencia | Estado |
|---|---|
| `Routes.ReportConfirmed` | ❌ Nuevo |
| `ReportConfirmedViewModel` | ❌ Nuevo (mínimo — solo carga el reporte) |
| `HeroImage` composable | ❌ Documentado en P12/13/14, aún por crear |

---

### 7. IMPLEMENTATION NOTES (P21)

- **Navegación back stack:** Al llegar a esta pantalla desde el wizard, el back stack contiene: `Feed → NewReport → FoundSubType/LostReportForm → InCareForm/SightingForm → ReportConfirmed`. El botón de acción debe limpiar todo el wizard del stack: `popUpTo(Routes.NewReport) { inclusive = true }`.
- **Animación de entrada [ASSUMED]:** Una pantalla de éxito sin animación pierde impacto. Considerar `AnimatedVisibility` con `scaleIn` + `fadeIn` para el ícono ✅ y el título, y `slideInVertically` para el contenido. O un lottie de confetti (requeriría `lottie-compose` — nueva dependencia, fuera de scope MVP).
- **El diseño luce incompleto:** El espacio vacío inferior es inusual. Confirmar con diseño si faltan CTAs como "Compartir reporte", "Ver en el mapa", o "Crear otro reporte".

---
---

## PANTALLA 22 — `MyReportsScreen`
### "Mis Reportes"

---

### 1. SCREEN IDENTITY

| Campo | Valor |
|---|---|
| **Screen name** | `MyReportsScreen` |
| **Route** | `Routes.MyReports` |
| **Screen type** | Stack (sin Drawer, sin bottom nav visible en la imagen) |
| **Entry points** | [ASSUMED] Drawer → item "Mis Reportes" (no está en el drawer actual); o desde `ProfileScreen`; o desde P23 |
| **Exit points** | ← → `popBackStack()`; Tap en reporte → `Routes.ReportDetail`; ⋮ menú → opciones (editar/eliminar) |

---

### 2. VISUAL COMPONENTS

| # | Nombre Composable | MD3 (Compose) | Variante / Notas | Estados |
|---|---|---|---|---|
| 1 | `SimpleTopAppBar` | `TopAppBar` (Small) | Flecha ← izquierda, título "Mis reportes" | default |
| 2 | `MyReportsSectionHeader` (×2) | `Text` | `titleLarge` bold, color `onBackground`. Textos: "Hallazgos", "Desaparaciones" [sic] | default |
| 3 | `MyReportItem` | `ListItem` de MD3 | Layout horizontal: thumbnail cuadrado izquierda, título + descripción texto centro, ícono `⋮` derecha. Sin separadores entre items | default, pressed |
| 4 | `ReportThumbnail` | `AsyncImage` (Coil) dentro de `Box(clip = RoundedCornerShape(8.dp))` | ~60×60dp, cuadrado, esquinas redondeadas | default, loading, error |
| 5 | `ReportItemTitle` | `Text` en `ListItem.headlineContent` | `bodyLarge` bold — muestra especie + raza: "Perro, Golden Retriever", "Gato, Lu" | default |
| 6 | `ReportItemSubtitle` | `Text` en `ListItem.supportingContent` | `bodyMedium`, color `onSurfaceVariant`, máx 2 líneas, `TextOverflow.Ellipsis` | default |
| 7 | `ReportItemMenu` | `DropdownMenu` + `IconButton(Icons.Default.MoreVert)` | Ícono `⋮`. Al tocar: `DropdownMenu` con opciones [ASSUMED]: "Ver detalle", "Editar", "Eliminar" | default, expanded |
| 8 | `EmptyReportsMessage` | `Column { Icon + Text }` [ASSUMED] | Cuando no hay reportes en alguna sección | empty |

> **Diferencia vs `FeedScreen`:** `MyReportsScreen` muestra solo los reportes del usuario actual (`ownerUid == currentUser.uid`), agrupados por tipo. `FeedScreen` muestra todos los reportes de todos los usuarios.

---

### 3. DATA & STATE

```kotlin
data class MyReportsUiState(
    val foundReports: List<PetReport> = emptyList(),    // FOUND_SIGHTING + FOUND_IN_CARE del user
    val lostReports: List<PetReport> = emptyList(),     // LOST del user
    val isLoading: Boolean = true,
    val error: String? = null,
    val reportToDelete: PetReport? = null,              // Para el AlertDialog de confirmación
)
```

#### UiEvent
```kotlin
sealed class MyReportsUiEvent {
    data class ReportClicked(val reportId: String, val type: ReportType) : MyReportsUiEvent()
    data class DeleteReportRequested(val report: PetReport) : MyReportsUiEvent()
    object ConfirmDelete : MyReportsUiEvent()
    object DismissDelete : MyReportsUiEvent()
    data class EditReportClicked(val reportId: String) : MyReportsUiEvent()  // [ASSUMED]
}
```

#### API Calls

| Operación | Puerto In | Cuándo | Filtro |
|---|---|---|---|
| Cargar hallazgos del usuario | `observeReports(FOUND_SIGHTING)` + `observeReports(FOUND_IN_CARE)` combinados con `combine()` | `LaunchedEffect(Unit)` | Filtrar por `ownerUid == currentUser.uid` en el ViewModel |
| Cargar desapariciones del usuario | `observeReports(LOST)` | `LaunchedEffect(Unit)` | Filtrar por `ownerUid == currentUser.uid` |
| Eliminar reporte | `deleteReport(id)` | `onEvent(ConfirmDelete)` | Borra documento Firestore |

> **Nota:** No existe un puerto `GetMyReports(uid)` — el filtrado por `ownerUid` se hace en el ViewModel tras obtener todos los reportes. Para escala, considerar un índice Firestore: `pet_reports where ownerUid == uid`.

---

### 4. INTERACTIONS & BEHAVIOR

| Trigger | Acción | Resultado |
|---|---|---|
| Tap en `MyReportItem` | `onEvent(ReportClicked(id, type))` | Navega a `Routes.ReportDetail(id, type)` |
| Tap en `⋮` | Abre `DropdownMenu` | Opciones: "Ver detalle", [ASSUMED] "Editar", "Eliminar" |
| Tap "Eliminar" en dropdown | `onEvent(DeleteReportRequested(report))` | Muestra `AlertDialog` "¿Eliminar este reporte?" |
| Confirmar eliminar | `onEvent(ConfirmDelete)` | `deleteReport(id)` → el Flow actualiza la lista automáticamente |
| Cancelar eliminar | `onEvent(DismissDelete)` | Cierra diálogo |
| Tap ← | `popBackStack()` | Sin confirmación |

#### Lógica Condicional
```kotlin
// Sección "Hallazgos" visible solo si foundReports.isNotEmpty()
// Sección "Desaparaciones" visible solo si lostReports.isNotEmpty()
// Si ambas vacías: mostrar estado vacío general "Aún no has creado reportes"
```

---

### 5. PROPS & REUSABILITY

- `MyReportItem` es un nuevo componente de lista más compacto que `PetCard`. Extraer como reutilizable — podría usarse en notificaciones, historial, etc.
- La agrupación por sección (header + items) puede generalizarse con un composable `ReportGroup(title, items, onItemClick, onItemMenuAction)`.

---

### 6. DEPENDENCIES

| Dependencia | Estado |
|---|---|
| `Routes.MyReports` | ❌ Nuevo |
| `MyReportsViewModel` | ❌ Nuevo |
| `ObserveReports` (×2 tipos) | ✅ Implementado |
| `DeleteReport` | ✅ Implementado |
| `ObserveCurrentUser` | ✅ Implementado |
| Entry point (¿desde dónde se llega?) | ⚠️ No hay entry point claro — ver §7.3 |

---

### 7. IMPLEMENTATION NOTES (P22)

- **Entry point sin definir:** P22 no tiene entrada clara en la navegación actual. El Drawer no la incluye. Ver §7 global para recomendación sobre la `NavigationBar` de P23.
- **Typo en diseño:** "Desaparaciones" debe ser "Desapariciones" en el string resource `strings.xml`.
- **Agrupación eficiente:** En lugar de dos llamadas `observeReports` separadas + `combine`, considerar `observeReports(null)` con filtro de todos los tipos — requiere ajustar el puerto para aceptar `type: ReportType?` nullable.
- **Consulta Firestore eficiente:** Para usuarios con muchos reportes, agregar índice compuesto en Firestore: `ownerUid ASC + createdAt DESC`. Sin esto, Firestore puede rechazar la query en producción.

---
---

## PANTALLA 23 — `SightingsForPetScreen`
### "Avistamientos Recibidos" (para una mascota perdida específica)

---

### 1. SCREEN IDENTITY

| Campo | Valor |
|---|---|
| **Screen name** | `SightingsForPetScreen` |
| **Route** | `Routes.SightingsForPet` (recibe `petReportId: String`) |
| **Screen type** | Stack con `BottomNavigationBar` visible |
| **Entry points** | [ASSUMED] `MyReportsScreen` → tap en reporte LOST → sección "Avistamientos"; o desde notificación push; o desde `ReportDetailScreen` para el owner |
| **Exit points** | ← → `popBackStack()`; Tap en item de avistamiento → `Routes.ReportDetail` del avistamiento; "Ver mapa completo" → `Routes.Map` centrado; bottom nav tabs → sus respectivas rutas |

---

### 2. VISUAL COMPONENTS

| # | Nombre Composable | MD3 (Compose) | Variante / Notas | Estados |
|---|---|---|---|---|
| 1 | `SightingsMapHeader` | `AndroidView` (Google Maps) sobre `Box` | Mapa de ~200dp de alto, ocupa todo el ancho, con pins de colores (verde, morado, rojo/naranja) representando distintos avistamientos. Sin `TopAppBar` superpuesto — la flecha ← está sobre el mapa como `FloatingActionButton` circular o `IconButton` con fondo semitransparente | default |
| 2 | `BackButtonOverMap` | `FilledIconButton` o `IconButton` dentro de `Box` | Flecha ← posicionada en overlay sobre el mapa, esquina superior izquierda. Fondo blanco circular semitransparente | default, pressed |
| 3 | `PetNameTitle` | `Text` sobre el mapa, parte inferior del header | `headlineMedium` bold, color blanco con sombra. Texto: "Avistamientos de Luna" | default |
| 4 | `PetBreedSubtitle` | `Text` sobre el mapa, bajo el título | `bodyMedium`, color blanco semitransparente. "Golden Retriever • Perdida hace 3 días" | default |
| 5 | `ViewFullMapButton` | `OutlinedButton` o `FilledTonalButton` | Ícono 🗺️ + texto "Ver mapa completo". Fondo blanco/surface, esquinas redondeadas, posicionado sobre el mapa en la parte inferior izquierda | default, pressed |
| 6 | `SortFilterRow` | `Row { Text + Text }` | "Más reciente primero" (izquierda, `bodyMedium`) + "Filtrar" (derecha, `bodyMedium` `primary` o link style) | default |
| 7 | `SightingItem` (×N) | `ListItem` de MD3 | Avatar circular con iniciales (colores distintos por usuario: CM morado, PA naranja, SG verde), título (nombre abreviado), metadata (tiempo + distancia + lugar), cita textual del avistamiento, chevron `>` derecha | default, pressed |
| 8 | `SighterAvatar` | `UserAvatar` existente | Círculo con iniciales del reporter, colores variados por usuario (ver §7 — problema con color fijo actual) | default |
| 9 | `SightingMeta` | `Text` | `bodySmall`, color `onSurfaceVariant`. "Hace 2 horas • 0.4 km • Parque Central" | default |
| 10 | `SightingQuote` | `Text` | `bodySmall`, color `onSurfaceVariant`, estilo cursiva [ASSUMED] o normal. `"La vi cerca del parque principal..."` | default |
| 11 | `LoadMoreButton` | `OutlinedButton` | Texto "Ver más avistamientos", ancho completo, centrado, esquinas redondeadas. Sin ícono | default, loading, hidden_when_no_more |
| 12 | `AppBottomNavigationBar` | `NavigationBar` (MD3) | **4 tabs:** Inicio (`Icons.Outlined.Home`), Mapa (`Icons.Outlined.LocationOn`), Avisos (`Icons.Outlined.Notifications` — con badge), Perfil (`Icons.Outlined.Person`). Tab "Avisos" activa | default, tab_selected |

> **[ASSUMED]** Los pins de colores en el mapa header (verde, morado, rojo/naranja) representan avistamientos de distintos usuarios o distintos momentos. No son los colores `tertiary`/`error` del sistema — podrían ser colores generados dinámicamente por hash del `ownerUid` del avistador.

---

### 3. DATA & STATE

```kotlin
data class SightingsForPetUiState(
    val petReport: PetReport? = null,          // La mascota perdida cuya pantalla es esta
    val sightings: List<SightingWithUser> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val sortOrder: SortOrder = SortOrder.NEWEST_FIRST,
    val error: String? = null
)

// Modelo compuesto [ASSUMED — nuevo] — combina avistamiento + datos del reporter
data class SightingWithUser(
    val sighting: PetReport,           // type = FOUND_SIGHTING
    val reporterName: String,          // "Carlos M."
    val reporterInitials: String,      // "CM"
    val reporterAvatarColor: Color,    // Color generado por hash de uid
    val distanceKm: Float?,            // Distancia desde la ubicación del LOST
    val timeAgo: String                // "Hace 2 horas", "Ayer"
)

enum class SortOrder { NEWEST_FIRST, NEAREST_FIRST }
```

#### API Calls

| Operación | Puerto | Cuándo | Nota |
|---|---|---|---|
| Cargar mascota perdida | `observeReports(LOST).find { id }` | `LaunchedEffect(petReportId)` | Obtiene datos de "Luna" para el header |
| Cargar avistamientos | [ASSUMED] `observeReports(FOUND_SIGHTING)` filtrado por zona/fecha | `LaunchedEffect(petReportId)` | **No existe relación explícita** entre un LOST y sus FOUND_SIGHTING — ver §7.1 |
| Paginación "Ver más" | [ASSUMED] `searchReports` con cursor | Tap "Ver más avistamientos" | Carga siguiente página |
| Datos de reporters | [ASSUMED] `GetUserById` para cada avistamiento | Tras cargar avistamientos | Para nombres e iniciales |

---

### 4. INTERACTIONS & BEHAVIOR

| Trigger | Acción | Resultado |
|---|---|---|
| Tap ← (sobre el mapa) | `popBackStack()` | Regresa a pantalla anterior |
| Tap "Ver mapa completo" | Navega a `Routes.Map` | [ASSUMED] centrado en zona de avistamientos de esta mascota |
| Tap en `SightingItem` | Navega a `Routes.ReportDetail(sighting.id, FOUND_SIGHTING)` | Detalle del avistamiento |
| Tap en `> (chevron)` del item | Igual que tap en item | Mismo resultado |
| Tap "Ver más avistamientos" | `onEvent(LoadMore)` | `isLoadingMore = true`, carga siguiente página, appends a lista |
| Tap en tab "Inicio" del bottom nav | Navega a `Routes.Feed` | Tab "Avisos" se desactiva |
| Tap en tab "Mapa" del bottom nav | Navega a `Routes.Map` | — |
| Tap en tab "Perfil" del bottom nav | Navega a `Routes.Profile` | — |
| Tap "Filtrar" (texto derecha) | [ASSUMED] Abre `ModalBottomSheet` de filtros | Filtrar por distancia, fecha, estado |

---

### 5. PROPS & REUSABILITY

```kotlin
@Composable
fun SightingsForPetScreen(
    petReportId: String,
    viewModel: SightingsForPetViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToMap: (LatLng) -> Unit,
    onNavigateToSightingDetail: (String) -> Unit
)
```

`SightingItem` es reutilizable — similar a una notificación de avistamiento. `SightingsMapHeader` es único a esta pantalla.

---

### 6. DEPENDENCIES

| Dependencia | Estado |
|---|---|
| `Routes.SightingsForPet` | ❌ Nuevo |
| `SightingsForPetViewModel` | ❌ Nuevo |
| `SightingWithUser` model | ❌ Nuevo — modelo compuesto (app layer, no domain) |
| `AppBottomNavigationBar` | ❌ **Nuevo componente crítico** — ver §7.5 |
| `maps-compose` | ❌ Fuera de scope MVP para el header del mapa |
| `GetUserById` puerto | ❌ No existe — para datos de reporters |
| Relación LOST ↔ FOUND_SIGHTING | ❌ No modelada — ver §7.1 |

---

## 7. IMPLEMENTATION NOTES GLOBALES (Aplican a Todas las Pantallas de Este Batch)

### 7.1 ⚠️ Flag Crítico — No Existe Relación LOST ↔ FOUND_SIGHTING en el Modelo de Dominio

P23 muestra "Avistamientos de **Luna**" — los avistamientos están vinculados a una mascota perdida específica. Sin embargo, en el modelo actual `PetReport` no tiene ningún campo que vincule un `FOUND_SIGHTING` con un `LOST`.

**Opciones de modelado:**

| Opción | Campo nuevo | Ventaja | Desventaja |
|---|---|---|---|
| A | `relatedLostReportId: String?` en `FOUND_SIGHTING` | Simple, query directa en Firestore | El reporter debe conocer el ID del LOST (flujo complejo de UI) |
| B | Zona geográfica + especie como proxy | Sin campo nuevo | Falsos positivos — cualquier avistamiento de Golden Retriever aparece para cualquier Golden perdido |
| C | El owner del LOST "vincula" avistamientos manualmente | Control del owner | Requiere pantalla de vinculación nueva |
| D | Subcollección: `pet_reports/{lostId}/sightings/{sightingId}` | Estructura más limpia para queries | Migración de schema Firestore |

**Recomendación para MVP:** Opción B (proxy por zona + especie) es aceptable como heurística; Opción D es la más escalable para producción.

### 7.2 ⚠️ Flag — `SighterAvatar` Necesita Colores Dinámicos

El `UserAvatar` actual usa `primaryContainer` fijo para todos los avatares. En P23, cada reporter tiene un color distinto (CM=morado, PA=naranja, SG=verde). Esto requiere que `UserAvatar` acepte un `containerColor: Color` calculado dinámicamente:

```kotlin
// Función para color determinista por uid
fun colorFromUid(uid: String): Color {
    val palette = listOf(
        Color(0xFFD0BCFF), // primaryContainer
        Color(0xFFFFB4AB), // errorContainer
        Color(0xFFB8F5B0), // tertiaryContainer-like
        Color(0xFFFFE0B2), // careContainer
        Color(0xFFBBDEFB)  // blueContainer
    )
    return palette[uid.hashCode().absoluteValue % palette.size]
}
```

Actualizar `UserAvatar.kt` para aceptar `containerColor: Color = MaterialTheme.colorScheme.primaryContainer`.

### 7.3 ⚠️ Entry Points de P22 y P23 No Están Definidos

`MyReportsScreen` (P22) no tiene un punto de entrada claro en la navegación. El Drawer actual no la incluye. Opciones:

| Opción | Implementación |
|---|---|
| A. Agregar al Drawer | Item nuevo "Mis reportes" entre Feed y Perfil |
| B. Accesible desde `ProfileScreen` | Botón/link "Ver mis reportes" en el perfil |
| C. Tab del `BottomNavigationBar` | Si se implementa la `NavigationBar` de P23 |

### 7.4 ⚠️ `ReportConfirmedScreen` — Back Stack del Wizard

Al completar el wizard (P15→P16→P17/P18/P19→P21), el back stack crece. Si el usuario presiona ← en P21 no debe volver al formulario. Implementar con `popUpTo`:

```kotlin
// En el ViewModel, al publicar exitosamente:
// Emitir evento de navegación con instrucción de limpiar el stack
navController.navigate(Routes.ReportConfirmed(reportId)) {
    popUpTo(Routes.Feed) { inclusive = false }
}
```

### 7.5 🚨 Flag Arquitectural Crítico — `NavigationBar` en P23 Rompe el Paradigma Actual

**Este es el hallazgo más importante de este batch.**

La Pantalla 23 introduce una `NavigationBar` (bottom tabs) con 4 tabs: Inicio, Mapa, Avisos, Perfil. Esto implica:

1. **El `DrawerShell` y la `NavigationBar` son incompatibles** si se aplican a las mismas pantallas. No es buena práctica tener ambos simultáneamente en la misma pantalla.

2. **La navegación actual es un Stack puro** (`AppNavHost` con `composable()`). Una `NavigationBar` requiere una estructura anidada:

```
AppNavHost
└── MainScreen (con NavigationBar)
    ├── HomeTab (Feed)
    ├── MapTab (MapScreen)
    ├── NotificationsTab (SightingsForPet / Avisos)
    └── ProfileTab (Profile / MyReports)
```

3. **Los items del Drawer coinciden con los tabs del bottom nav** (Feed=Inicio, Mapa, Perfil). Esto sugiere que el diseño está evolucionando de Drawer a NavigationBar como patrón de navegación principal.

**Decisión arquitectural — Opciones:**

| Opción | Descripción | Recomendación |
|---|---|---|
| A. **Eliminar Drawer → NavigationBar** | Reemplazar `DrawerShell` con `NavigationBar` en un `MainScaffold`. Drawer queda para opciones secundarias (Settings, etc.) | ✅ **Recomendada** — más estándar en apps móviles modernas, MD3 favorece NavigationBar para 3-5 destinos |
| B. **Mantener ambos** | DrawerShell para algunas pantallas, NavigationBar para otras | ❌ Confuso para el usuario, complejo de implementar |
| C. **NavigationBar solo en pantallas específicas** | `NavigationBar` aparece solo en las pantallas principales (Feed, Map, Avisos, Profile). Desaparece en flujos de detalle/creación | ✅ Viable como compromiso — implementar `MainScaffold` con `NavigationBar` solo para las 4 rutas top-level |

**Implementación recomendada (Opción A/C):**

```kotlin
// AppNavHost.kt — nueva estructura
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Routes.Main) {
        
        // Ruta contenedora con NavigationBar
        composable(Routes.Main) {
            MainScaffold(navController)
        }
        
        // Rutas fuera del NavigationBar (sin bottom nav)
        composable(Routes.NewReport) { NewReportTypeScreen(...) }
        composable(Routes.LostReportForm) { LostReportFormScreen(...) }
        // ... resto del wizard
        composable(Routes.ReportDetail) { ReportDetailScreen(...) }
        composable(Routes.ReportConfirmed) { ReportConfirmedScreen(...) }
    }
}

// MainScaffold.kt — nuevo componente
@Composable
fun MainScaffold(appNavController: NavHostController) {
    val bottomNavController = rememberNavController()
    Scaffold(
        bottomBar = { AppBottomNavigationBar(bottomNavController) }
    ) { padding ->
        NavHost(bottomNavController, startDestination = Routes.Feed) {
            composable(Routes.Feed) { FeedScreen(...) }
            composable(Routes.Map) { MapScreen(...) }
            composable(Routes.Notifications) { NotificationsScreen(...) }
            composable(Routes.Profile) { ProfileScreen(...) }
        }
    }
}
```

> **Impacto en DrawerShell:** Si se adopta `NavigationBar`, el `DrawerShell` queda para Settings, Permissions y pantallas secundarias — o se elimina completamente si esos destinos se mueven al `NavigationBar` o a menús de overflow.

### 7.6 Badge en Tab "Avisos"

El tab "Avisos" en P23 debe mostrar un badge cuando hay nuevos avistamientos no leídos. MD3 lo soporta nativo:

```kotlin
NavigationBarItem(
    icon = {
        BadgedBox(badge = {
            if (unreadCount > 0) Badge { Text("$unreadCount") }
        }) {
            Icon(Icons.Outlined.Notifications, ...)
        }
    },
    ...
)
```

Esto requiere un nuevo `ObserveUnreadNotificationsCount` puerto o campo en `User` — nueva deuda técnica.

### 7.7 Accesibilidad Global

| Elemento | Consideración |
|---|---|
| `SightingItem` | `semantics { contentDescription = "${reporterName}, avistamiento hace ${timeAgo} a ${distanceKm}km, ${quote}" }` |
| `AppBottomNavigationBar` | MD3 `NavigationBar` maneja a11y automáticamente para tabs. Verificar que los labels sean descriptivos |
| `UrgencyOption` (P19) | `Modifier.semantics { role = Role.RadioButton; selected = isSelected }` |
| `SightingsMapHeader` (mapa) | `contentDescription = "Mapa con ${sightings.size} avistamientos de ${petName}"` |
| `BackButtonOverMap` | `contentDescription = "Volver atrás"` |

---

## SHORT SUMMARY GLOBAL (Pantallas 19, 21, 22, 23)

- 🚨 **`NavigationBar` de P23 requiere refactorizar toda la navegación:** Pasar de Stack + Drawer puro a una estructura `MainScaffold` con `NavigationBar` para los 4 destinos top-level (Feed, Mapa, Avisos, Perfil) y Stack independiente para flujos secundarios. Esta es la **decisión arquitectural de mayor impacto** de todo el proyecto.

- 🔗 **No existe relación entre LOST y FOUND_SIGHTING en el modelo:** P23 asume que los avistamientos están vinculados a una mascota perdida específica. El modelo de dominio no tiene este vínculo. Requiere definir estrategia: subcollección Firestore, campo `relatedLostReportId`, o proxy heurístico por zona+especie.

- 🟠 **P19 introduce un nuevo token de color "cuidado/resguardo" naranja** que no está en el esquema M3 actual. El botón "Publicar reporte de resguardo" y las `UrgencyOption` seleccionadas usan naranja — necesita un nuevo token (`care` / `careContainer`) en `Color.kt`.

- 📋 **P22 (`MyReportsScreen`) necesita un entry point:** La pantalla no tiene una ruta de entrada en la navegación actual. Con la `NavigationBar` de P23, "Perfil" → "Mis reportes" es el candidato natural. Alternativamente, agregar al Drawer como item secundario.

- ✅ **P21 (`ReportConfirmedScreen`) es la más simple pero tiene el riesgo más alto en back stack:** Si el `popUpTo` no se configura correctamente, el usuario puede volver al formulario después de publicar. Implementar `popUpTo(Routes.Feed) { inclusive = false }` en la navegación al confirmar.

---

*Documento generado con base en Pantallas 19, 21, 22 y 23 del diseño de MascotasPerdidas + TECHNICAL_CONTEXT.md (Junio 2026).*
