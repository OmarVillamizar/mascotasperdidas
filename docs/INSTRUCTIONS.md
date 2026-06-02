# INSTRUCTIONS.md — MascotasPerdidas: Fase 2
## Documento maestro para Claude Code · Leer completo antes de escribir una sola línea

> Este documento fusiona el `TECHNICAL_CONTEXT.md` existente con las especificaciones técnicas de las  
> **Pantallas 6–23** (documentadas en sesiones anteriores). Define exactamente qué construir, en qué orden,  
> cómo se conecta cada pieza y qué tecnología usar. **Es la fuente de verdad para esta fase.**

---

## 0. ESTADO DE PARTIDA

### ✅ Ya implementado (no tocar salvo que se indique)
- Arquitectura hexagonal completa: `domain/` + `data/` + `app/`
- 3 puertos out, 12 puertos in, 12 use cases
- Firebase adapters + Fake adapters + DataStore
- Tema M3 completo (color lila/violeta fijo)
- 6 pantallas: Splash, Profile, OTP, Permissions, Feed, Settings
- DrawerShell + AppTopBar + UserAvatar + StatusChip + PetCard
- DI Hilt completo, navegación Stack con DrawerShell

### ❌ Pendiente en el MVP actual (resolver antes o durante Fase 2)
- `ReportClicked` / `ContactClicked` → stubs vacíos
- Subida de imagen real (solo imágenes preset hoy)
- Permisos runtime reales en `PermissionsScreen`
- FAB del Feed abre Dialog inline → reemplazar por wizard

---

## 1. LIBRERÍA DE MAPAS — OSMDroid (DECISIÓN DEFINITIVA)

**Se usará OSMDroid en lugar de Google Maps SDK.** Costo cero, sin API key, perfecto para proyecto universitario.

### 1.1 Agregar dependencia

```toml
# gradle/libs.versions.toml
[versions]
osmdroid = "6.1.20"

[libraries]
osmdroid = { group = "org.osmdroid", name = "osmdroid-android", version.ref = "osmdroid" }
```

```kotlin
// app/build.gradle.kts
implementation(libs.osmdroid)
```

### 1.2 Inicialización en Application

```kotlin
// MascotasPerdidasApp.kt — AÑADIR al onCreate() existente
Configuration.getInstance().load(
    applicationContext,
    androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
)
Configuration.getInstance().userAgentValue = packageName
```

### 1.3 Wrapper Compose (patrón único para TODA la app)

Crear `app/ui/components/OsmMapView.kt`. Usar este composable en MapScreen, MiniMapView y LocationMapPicker:

```kotlin
@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    center: GeoPoint = GeoPoint(7.89705, -72.50809), // Cúcuta por defecto
    zoom: Double = 14.0,
    markers: List<OsmMarker> = emptyList(),
    onMapClick: ((GeoPoint) -> Unit)? = null,        // null = solo lectura
    onMarkerClick: ((OsmMarker) -> Unit)? = null
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(zoom)
                controller.setCenter(center)
                onMapClick?.let { listener ->
                    setOnMapClickListener { geoPoint -> listener(geoPoint) }
                }
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            markers.forEach { markerData ->
                val marker = Marker(mapView).apply {
                    position = markerData.position
                    title = markerData.title
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    // Color del pin via drawable tintado:
                    // LOST → error (#E57373), FOUND → tertiary (#2E7D32)
                    icon = markerData.icon
                    setOnMarkerClickListener { _, _ ->
                        onMarkerClick?.invoke(markerData)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    )
}

data class OsmMarker(
    val id: String,
    val position: GeoPoint,
    val title: String,
    val icon: Drawable?,
    val reportType: ReportType
)
```

### 1.4 Permisos para OSMDroid

Añadir al `AndroidManifest.xml` (INTERNET ya debería estar):
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

### 1.5 Ciclo de vida (importante)

En cualquier `Activity` o `Fragment` que use `MapView`, llamar `onResume()` / `onPause()`. En Compose con `AndroidView`, usar `DisposableEffect`:

```kotlin
val lifecycleOwner = LocalLifecycleOwner.current
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE  -> mapView.onPause()
            else -> {}
        }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}
```

---

## 2. CAMBIOS DE DOMINIO — HACER PRIMERO (Fase 2A)

Estos cambios afectan múltiples capas y bloquean todo lo demás. Implementar en este orden exacto.

### 2A.1 — Extender `ReportType`

```kotlin
// domain/model/ReportType.kt — REEMPLAZAR
enum class ReportType {
    LOST,            // Mascota perdida (P17)
    FOUND_SIGHTING,  // Vi un animal en la calle (P18)
    FOUND_IN_CARE    // Tengo el animal conmigo (P19)
}
```

**Archivos a actualizar en cascada:**
- `data/dto/PetReportDto.kt` → añadir serialización de los 3 valores
- `data/mapper/PetReportMapper.kt` → `"FOUND_SIGHTING"` y `"FOUND_IN_CARE"` en toDto/toDomain
- `data/firebase/FirestorePetReportRepository.kt` → pasar `type` correcto en queries
- `data/fake/FakePetReportRepository.kt` → actualizar seed data
- `app/ui/components/StatusChip.kt` → nuevo caso `FOUND_SIGHTING` (verde "AVISTADO"), `FOUND_IN_CARE` (verde "RESGUARDO")
- `FeedViewModel` → `observeReports` ahora puede filtrar por los 3 tipos

### 2A.2 — Extender `PetReport`

```kotlin
// domain/model/PetReport.kt — REEMPLAZAR
data class PetReport(
    // ── Campos existentes (NO cambiar nombres) ──
    val id: String,
    val ownerUid: String,
    val ownerInitial: String,
    val ownerName: String = "",              // NUEVO — nombre del reporter para "REPORTADO POR"
    val petName: String,
    val type: ReportType,
    val breed: String,
    val description: String,
    val location: String,                    // texto libre, se mantiene
    val imageUrl: String,
    val recencyLabel: String,
    val createdAtEpochMs: Long,

    // ── Campos nuevos — todos con default para no romper código existente ──
    val species: String = "",               // "Perro", "Gato", "Otro"
    val size: String = "",                  // "Grande", "Mediano", "Pequeño"
    val gender: String = "",               // "Macho", "Hembra", "No sé"
    val color: String = "",               // "Dorado", "Blanco", etc.
    val collarColor: String = "",          // "Azul", "Rojo", "Ninguno"
    val ageApprox: String = "",            // "3 años", "Adulto", "Cachorro", "Joven", "Senior"
    val microchip: String = "",            // "No visible", "Sí", código
    val hasCollarPlate: Boolean = false,
    val hasMicrochip: Boolean = false,
    val physicalStatus: List<String> = emptyList(),    // ["Sano", "Herido", "Desnutrido"...]
    val behaviors: List<String> = emptyList(),          // ["Cariñoso", "Asustado"...]
    val notes: String = "",
    val additionalPhotos: List<String> = emptyList(),   // URLs (máx 5 fotos extra)
    val urgency: String = "",              // CareUrgency enum name para FOUND_IN_CARE
    val stillInArea: Boolean = false,      // Para FOUND_SIGHTING (P18)
    val latitude: Double? = null,          // Para mapas
    val longitude: Double? = null,
    val statuses: List<String> = emptyList()  // multi-select estado (P18: Herido, Asustado...)
)
```

**Archivos a actualizar:**
- `data/dto/PetReportDto.kt` → todos los campos nuevos con `@field:JvmField` o simplemente como propiedades, con defaults en el DTO
- `data/mapper/PetReportMapper.kt` → `toDomain()` y `toDto()` para cada campo nuevo
- `data/firebase/FirestorePetReportRepository.kt` → seed data actualizado con lat/lng de Cúcuta
- `data/fake/FakePetReportRepository.kt` → seed data actualizado

### 2A.3 — Extender `CreateReport` para múltiples fotos

```kotlin
// domain/port/in/CreateReport.kt — REEMPLAZAR
fun interface CreateReport {
    suspend operator fun invoke(report: PetReport, imageBytesList: List<ByteArray>)
}
```

**Archivos a actualizar:**
- `domain/usecase/CreateReportImpl.kt` → iterar `imageBytesList`, subir cada una, guardar URLs en `additionalPhotos`
- `data/firebase/FirestorePetReportRepository.kt` → `suspend createReport(report, imageBytesList: List<ByteArray>)`
- `data/fake/FakePetReportRepository.kt` → adaptar firma
- `FeedViewModel` → ajustar llamada (actualmente usa 1 imagen)

### 2A.4 — Nuevo color token "cuidado/resguardo" (naranja P19)

```kotlin
// app/theme/Color.kt — AÑADIR
val Care = Color(0xFFE65100)          // Naranja oscuro — botón "Publicar resguardo"
val CareContainer = Color(0xFFFFE0B2) // Naranja claro — fondo UrgencyOption seleccionada
val OnCare = Color(0xFFFFFFFF)
val OnCareContainer = Color(0xFF4E2600)

// app/theme/Theme.kt — AÑADIR al ColorScheme (custom extension)
// Usar como MaterialTheme.colorScheme no tiene slot para esto →
// Crear objeto companion o CompositionLocal:
val LocalCareColor = compositionLocalOf { Care }
val LocalCareContainerColor = compositionLocalOf { CareContainer }
```

### 2A.5 — Actualizar `UserAvatar` para colores dinámicos

```kotlin
// app/ui/components/UserAvatar.kt — AÑADIR parámetro
@Composable
fun UserAvatar(
    initial: String,
    photoUrl: String? = null,
    size: Dp = 40.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer  // NUEVO param
)

// Función helper en app/util/AvatarColorUtils.kt
fun colorFromUid(uid: String, colorScheme: ColorScheme): Color {
    val options = listOf(
        colorScheme.primaryContainer,
        colorScheme.errorContainer,
        colorScheme.tertiaryContainer,
        colorScheme.secondaryContainer
    )
    return options[uid.hashCode().absoluteValue % options.size]
}
```

---

## 3. REFACTORIZACIÓN DE NAVEGACIÓN — HACER SEGUNDO (Fase 2B)

La Pantalla 23 introduce una `NavigationBar` de 4 tabs que reemplaza al patrón Drawer como navegación principal.

### 3.1 Nueva estructura de navegación

```
AppNavHost (único NavController raíz)
├── Routes.Splash          → SplashScreen (sin nav secundaria)
├── Routes.Otp             → OtpScreen (sin nav secundaria)
├── Routes.Main            → MainScreen ← NUEVO contenedor con NavigationBar
│   ├── tab: Routes.Feed   → FeedScreen (ya implementado, sin DrawerShell)
│   ├── tab: Routes.Map    → MapScreen (nuevo)
│   ├── tab: Routes.Notifications → NotificationsScreen / SightingsForPetScreen (nuevo)
│   └── tab: Routes.Profile → ProfileScreen (ya implementado, sin DrawerShell)
├── Routes.Settings        → SettingsScreen (stack, sin bottom nav)
├── Routes.Permissions     → PermissionsScreen (stack)
├── Routes.ReportDetail    → ReportDetailScreen (stack, nuevo)
├── Routes.NewReport       → NewReportTypeScreen (stack, nuevo)
├── Routes.FoundSubType    → FoundSubTypeScreen (stack, nuevo)
├── Routes.LostReportForm  → LostReportFormScreen (stack, nuevo)
├── Routes.SightingReportForm → SightingReportFormScreen (stack, nuevo)
├── Routes.InCareReportForm   → InCareReportFormScreen (stack, nuevo)
├── Routes.ReportConfirmed    → ReportConfirmedScreen (stack, nuevo)
└── Routes.MyReports          → MyReportsScreen (stack, nuevo)
```

### 3.2 Nuevo `Routes.kt`

```kotlin
// app/navigation/Routes.kt — REEMPLAZAR COMPLETO
sealed class Routes(val route: String) {
    // Auth flow (sin bottom nav)
    object Splash : Routes("splash")
    object Otp : Routes("otp")
    object Permissions : Routes("permissions")

    // Main contenedor (con bottom nav)
    object Main : Routes("main")
    object Feed : Routes("main/feed")
    object Map : Routes("main/map")
    object Notifications : Routes("main/notifications")
    object Profile : Routes("main/profile")

    // Stack sobre el main (sin bottom nav)
    object Settings : Routes("settings")
    object MyReports : Routes("my_reports")

    // Wizard de creación
    object NewReport : Routes("report/new")
    object FoundSubType : Routes("report/found_subtype")
    object LostReportForm : Routes("report/lost_form")
    object SightingReportForm : Routes("report/sighting_form")
    object InCareReportForm : Routes("report/in_care_form")

    // Con argumentos
    object ReportDetail : Routes("report/detail/{reportId}/{reportType}") {
        fun route(id: String, type: ReportType) = "report/detail/$id/${type.name}"
    }
    object ReportConfirmed : Routes("report/confirmed/{reportId}") {
        fun route(id: String) = "report/confirmed/$id"
    }
    object SightingsForPet : Routes("sightings/{petReportId}") {
        fun route(id: String) = "sightings/$id"
    }
}
```

### 3.3 Nuevo `AppNavHost.kt`

```kotlin
// ESTRUCTURA — no el código completo, Claude Code lo implementa
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController, startDestination = Routes.Splash.route) {

        composable(Routes.Splash.route) { SplashScreen(onNavigateToMain = {...}, onNavigateToOtp = {...}) }
        composable(Routes.Otp.route) { OtpScreen(...) }
        composable(Routes.Permissions.route) { PermissionsScreen(onContinue = { navController.navigate(Routes.Main.route) { popUpTo(Routes.Splash.route) { inclusive = true } } }) }

        // Contenedor principal con NavigationBar
        composable(Routes.Main.route) {
            MainScaffold(
                onNavigateToSettings = { navController.navigate(Routes.Settings.route) },
                onNavigateToNewReport = { navController.navigate(Routes.NewReport.route) },
                onNavigateToReportDetail = { id, type -> navController.navigate(Routes.ReportDetail.route(id, type)) },
                onNavigateToSightingsForPet = { id -> navController.navigate(Routes.SightingsForPet.route(id)) },
                onNavigateToMyReports = { navController.navigate(Routes.MyReports.route) }
            )
        }

        // Pantallas stack fuera del main
        composable(Routes.Settings.route) { SettingsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Routes.MyReports.route) { MyReportsScreen(...) }

        // Wizard
        composable(Routes.NewReport.route) { NewReportTypeScreen(...) }
        composable(Routes.FoundSubType.route) { FoundSubTypeScreen(...) }
        composable(Routes.LostReportForm.route) { LostReportFormScreen(...) }
        composable(Routes.SightingReportForm.route) { SightingReportFormScreen(...) }
        composable(Routes.InCareReportForm.route) { InCareReportFormScreen(...) }

        // Con argumentos
        composable(
            Routes.ReportDetail.route,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType }, navArgument("reportType") { type = NavType.StringType })
        ) { backStackEntry ->
            ReportDetailScreen(
                reportId = backStackEntry.arguments!!.getString("reportId")!!,
                reportType = ReportType.valueOf(backStackEntry.arguments!!.getString("reportType")!!),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ReportConfirmed.route, arguments = listOf(navArgument("reportId") { type = NavType.StringType })) { backStackEntry ->
            ReportConfirmedScreen(
                reportId = backStackEntry.arguments!!.getString("reportId")!!,
                onNavigateToFeed = { navController.navigate(Routes.Main.route) { popUpTo(Routes.NewReport.route) { inclusive = true } } }
            )
        }

        composable(Routes.SightingsForPet.route, arguments = listOf(navArgument("petReportId") { type = NavType.StringType })) { backStackEntry ->
            SightingsForPetScreen(petReportId = backStackEntry.arguments!!.getString("petReportId")!!, onNavigateBack = { navController.popBackStack() })
        }
    }
}
```

### 3.4 Nuevo `MainScaffold.kt`

```kotlin
// app/ui/screens/main/MainScaffold.kt — NUEVO ARCHIVO
@Composable
fun MainScaffold(
    onNavigateToSettings: () -> Unit,
    onNavigateToNewReport: () -> Unit,
    onNavigateToReportDetail: (String, ReportType) -> Unit,
    onNavigateToSightingsForPet: (String) -> Unit,
    onNavigateToMyReports: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                currentRoute = currentRoute,
                onTabSelected = { route ->
                    bottomNavController.navigate(route) {
                        popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.Feed.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.Feed.route) {
                FeedScreen(
                    onNavigateToNewReport = onNavigateToNewReport,
                    onNavigateToReportDetail = onNavigateToReportDetail,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
            composable(Routes.Map.route) { MapScreen(onNavigateToReportDetail = onNavigateToReportDetail) }
            composable(Routes.Notifications.route) { NotificationsPlaceholderScreen() }
            composable(Routes.Profile.route) {
                ProfileScreen(
                    onNavigateToMyReports = onNavigateToMyReports,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
        }
    }
}
```

### 3.5 Nuevo `AppBottomNavigationBar.kt`

```kotlin
// app/ui/components/AppBottomNavigationBar.kt — NUEVO ARCHIVO
@Composable
fun AppBottomNavigationBar(currentRoute: String?, onTabSelected: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.Feed.route,
            onClick = { onTabSelected(Routes.Feed.route) },
            icon = { Icon(Icons.Outlined.Home, contentDescription = stringResource(R.string.tab_inicio)) },
            label = { Text(stringResource(R.string.tab_inicio)) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.Map.route,
            onClick = { onTabSelected(Routes.Map.route) },
            icon = { Icon(Icons.Outlined.LocationOn, contentDescription = stringResource(R.string.tab_mapa)) },
            label = { Text(stringResource(R.string.tab_mapa)) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.Notifications.route,
            onClick = { onTabSelected(Routes.Notifications.route) },
            icon = { Icon(Icons.Outlined.Notifications, contentDescription = stringResource(R.string.tab_avisos)) },
            label = { Text(stringResource(R.string.tab_avisos)) }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.Profile.route,
            onClick = { onTabSelected(Routes.Profile.route) },
            icon = { Icon(Icons.Outlined.Person, contentDescription = stringResource(R.string.tab_perfil)) },
            label = { Text(stringResource(R.string.tab_perfil)) }
        )
    }
}
```

### 3.6 Actualizar DrawerShell

Con la `NavigationBar`, el `DrawerShell` ya no es el contenedor principal. **Mantenerlo** para las pantallas stack (Settings, MyReports) si tienen menú lateral, o simplemente eliminarlo de FeedScreen/ProfileScreen. Los items del Drawer que solapan con los tabs se eliminan.

---

## 4. COMPONENTES COMPARTIDOS NUEVOS — HACER TERCERO (Fase 2C)

Crear estos antes que las pantallas que los usan. Todos van en `app/ui/components/`.

| Archivo | Composable | Usado en |
|---|---|---|
| `OsmMapView.kt` | `OsmMapView(...)` | MapScreen, MiniMapView, LocationMapPicker |
| `AppBottomNavigationBar.kt` | `AppBottomNavigationBar(...)` | MainScaffold |
| `SingleSelectChipGroup.kt` | `SingleSelectChipGroup(options, selected, onSelect)` | P18, P19, ReportDetailScreen |
| `MultiSelectChipGroup.kt` | `MultiSelectChipGroup(options, selected, onToggle)` | P18, P19 |
| `ColorSwatchSelector.kt` | `ColorSwatchSelector(colors, selected, onSelect)` | P18 |
| `PhotoPickerRow.kt` | `PhotoPickerRow(photos, maxPhotos, onAdd, onRemove)` | P17, P18, P19 |
| `FormSectionHeader.kt` | `FormSectionHeader(title)` | P17, P18, P19 |
| `SectionLabel.kt` | `SectionLabel(text)` | P12–P14, P23 |
| `HeroImage.kt` | `HeroImage(url, modifier)` | P12–P14, P21 |
| `AttributesGrid.kt` | `AttributesGrid(items: List<Pair<String,String>>)` | P12–P14 |
| `MiniMapView.kt` | `MiniMapView(lat, lng, type)` | P12 (LOST), P13 (FOUND_SIGHTING) |
| `SelectionWizardStep.kt` | `SelectionWizardStep(title, instruction, options, onSelect)` | P15, P16 |
| `UrgencySelector.kt` | `UrgencySelector(selected, onSelect)` | P19 |
| `StickyContactFooter.kt` | `StickyContactFooter(onContact)` | P12, P13 |
| `MyReportItem.kt` | `MyReportItem(report, onTap, onMenuAction)` | P22 |
| `SightingItem.kt` | `SightingItem(sighting, onTap)` | P23 |

---

## 5. PANTALLAS NUEVAS — ORDEN DE IMPLEMENTACIÓN (Fase 2D)

Implementar en este orden exacto. Cada pantalla depende de las anteriores.

---

### FASE 2D-1: `ReportDetailScreen` (Pantallas 12, 13, 14)
**Ruta:** `Routes.ReportDetail`  
**Prioridad:** Alta — desbloquea el stub `ReportClicked` que está vacío desde el MVP

#### Archivos a crear:
```
app/ui/screens/report/detail/
├── ReportDetailScreen.kt      ← Composable stateless
├── ReportDetailViewModel.kt
├── ReportDetailUiState.kt
└── ReportDetailUiEvent.kt
```

#### Lógica central:
```kotlin
// ReportDetailViewModel.kt
val reportId: String    // desde SavedStateHandle
val reportType: ReportType  // desde SavedStateHandle

// Cargar reporte: filtrar el Flow existente por ID
// Usar combine() de dos flows para cubrir todos los tipos
viewModelScope.launch {
    combine(
        observeReports(ReportType.LOST),
        observeReports(ReportType.FOUND_SIGHTING),
        observeReports(ReportType.FOUND_IN_CARE)
    ) { lost, sightings, inCare ->
        (lost + sightings + inCare).find { it.id == reportId }
    }.collect { report ->
        _uiState.update { it.copy(report = report, isLoading = report == null) }
    }
}
```

#### Variantes del composable (una pantalla, lógica condicional):
```kotlin
// ReportDetailScreen.kt
when (state.report?.type) {
    ReportType.LOST           → LostDetailContent(state, onEvent)     // P12
    ReportType.FOUND_SIGHTING → SightingDetailContent(state, onEvent) // P13
    ReportType.FOUND_IN_CARE  → InCareDetailContent(state, onEvent)   // P14
    null                      → LoadingOrError(state)
}
```

#### Conexión a código existente:
- **`FeedScreen`:** Activar el stub `onReportClicked` → `navController.navigate(Routes.ReportDetail.route(id, type))`
- **`PetCard`:** El botón "Más Información" dispara `onReportClicked`

#### MiniMapView con OSMDroid:
```kotlin
@Composable
fun MiniMapView(latitude: Double, longitude: Double, type: ReportType) {
    OsmMapView(
        modifier = Modifier.height(180.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)),
        center = GeoPoint(latitude, longitude),
        zoom = 15.0,
        markers = listOf(OsmMarker(id = "pin", position = GeoPoint(latitude, longitude), title = "", icon = pinIconForType(type), reportType = type)),
        onMapClick = null  // solo lectura
    )
}
```

---

### FASE 2D-2: Wizard de Creación (Pantallas 15, 16, 17, 18, 19)

#### Refactorizar FAB del FeedScreen PRIMERO:
```kotlin
// FeedScreen.kt — cambiar el FAB
FloatingActionButton(onClick = { onNavigateToNewReport() }) {
    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.nuevo_reporte))
}
// Eliminar el CreateReportDialog existente
```

#### Archivos a crear (wizard):
```
app/ui/screens/report/creation/
├── NewReportTypeScreen.kt          ← P15 (sin ViewModel)
├── FoundSubTypeScreen.kt           ← P16 (sin ViewModel)
├── LostReportFormScreen.kt         ← P17
├── LostReportFormViewModel.kt
├── LostReportFormUiState.kt
├── LostReportFormUiEvent.kt
├── SightingReportFormScreen.kt     ← P18
├── SightingReportFormViewModel.kt
├── SightingReportFormUiState.kt
├── SightingReportFormUiEvent.kt
├── InCareReportFormScreen.kt       ← P19
├── InCareReportFormViewModel.kt
├── InCareReportFormUiState.kt
└── InCareReportFormUiEvent.kt
```

#### Subida de fotos (real, no preset):
```kotlin
// En los ViewModels P17/P18/P19 — convertir Uri a ByteArray
private suspend fun uriToBytes(uri: Uri, context: Context): ByteArray =
    withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: byteArrayOf()
    }
```

#### Publicar reporte (flujo completo):
```kotlin
// LostReportFormViewModel.kt — ejemplo
fun onPublishReport(context: Context) {
    viewModelScope.launch {
        _uiState.update { it.copy(isPublishing = true) }
        try {
            val imageBytesList = state.photos.map { uriToBytes(it, context) }
            val report = PetReport(
                id = "",  // Firestore auto-genera
                ownerUid = currentUser!!.uid,
                ownerInitial = currentUser!!.displayName.firstOrNull()?.toString() ?: "?",
                ownerName = currentUser!!.displayName,
                petName = state.name,
                type = ReportType.LOST,
                species = state.species,
                breed = state.breed,
                // ... resto de campos
                latitude = null,   // Fase 3: geolocalización real
                longitude = null
            )
            createReport(report, imageBytesList)
            _uiState.update { it.copy(isPublishing = false, publishSuccess = true) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isPublishing = false, error = e.message) }
        }
    }
}
```

#### `CareUrgency` enum — en app layer (no domain):
```kotlin
// app/ui/screens/report/creation/CareUrgency.kt
enum class CareUrgency(val titleRes: Int, val subtitleRes: Int, val iconRes: ImageVector) {
    INDEFINITE(R.string.urgency_indefinite_title, R.string.urgency_indefinite_subtitle, Icons.Outlined.Schedule),
    FEW_DAYS(R.string.urgency_few_days_title, R.string.urgency_few_days_subtitle, Icons.Outlined.DateRange),
    TODAY_ONLY(R.string.urgency_today_title, R.string.urgency_today_subtitle, Icons.Outlined.Warning),
    URGENT_NOW(R.string.urgency_now_title, R.string.urgency_now_subtitle, Icons.Outlined.PriorityHigh)
}
```

#### LocationMapPicker en P18/P19 con OSMDroid:
```kotlin
@Composable
fun LocationMapPicker(
    selectedLocation: GeoPoint?,
    onLocationPicked: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    OsmMapView(
        modifier = modifier.height(180.dp),
        center = selectedLocation ?: GeoPoint(7.89705, -72.50809),
        markers = selectedLocation?.let {
            listOf(OsmMarker("picked", it, "", pinIcon, ReportType.FOUND_SIGHTING))
        } ?: emptyList(),
        onMapClick = { geoPoint -> onLocationPicked(geoPoint) }
    )
    Text(stringResource(R.string.toca_para_ajustar_ubicacion), style = MaterialTheme.typography.bodySmall)
}
```

---

### FASE 2D-3: `ReportConfirmedScreen` (Pantalla 21)
**Ruta:** `Routes.ReportConfirmed`

```kotlin
// Back stack al llegar aquí — CRÍTICO
navController.navigate(Routes.ReportConfirmed.route(reportId)) {
    popUpTo(Routes.Main.route) { inclusive = false }
}
// Así, el back button en P21 lleva al Feed, no al formulario
```

```kotlin
// ReportConfirmedScreen — mínimo viable
@Composable
fun ReportConfirmedScreen(reportId: String, onGoToFeed: () -> Unit) {
    // BackHandler para interceptar gesto atrás
    BackHandler { onGoToFeed() }
    // UI: ícono ✅ + título + imagen del reporte + descripción + botón "Ir al feed"
}
```

---

### FASE 2D-4: `MapScreen` (Pantallas 6, 7, 8)
**Ruta:** `Routes.Map` (tab del NavigationBar)

#### Archivos a crear:
```
app/ui/screens/map/
├── MapScreen.kt
├── MapViewModel.kt
├── MapUiState.kt
├── MapUiEvent.kt
├── MapFiltersSheet.kt     ← BottomSheet de filtros (P7)
└── PinPreviewSheet.kt     ← BottomSheet del pin (P8)
```

#### OSMDroid en MapScreen:
```kotlin
// MapScreen.kt — estructura
Box(Modifier.fillMaxSize()) {
    OsmMapView(
        modifier = Modifier.fillMaxSize(),
        center = GeoPoint(7.89705, -72.50809),
        markers = state.filteredReports.mapNotNull { report ->
            if (report.latitude == null || report.longitude == null) null
            else OsmMarker(
                id = report.id,
                position = GeoPoint(report.latitude, report.longitude),
                title = report.petName,
                icon = pinIconForType(report.type, context),
                reportType = report.type
            )
        },
        onMarkerClick = { marker -> onEvent(MapUiEvent.PinClicked(marker.id)) }
    )

    // Bottom bar flotante
    MapBottomBar(
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
        onAdd = { onNavigateToNewReport() },
        onFilter = { onEvent(MapUiEvent.OpenFilterSheet) },
        onSearch = { onEvent(MapUiEvent.OpenSearch) }
    )
}

// Sheets
when (state.bottomSheetState) {
    is MapBottomSheetState.Filters    → MapFiltersSheet(state, onEvent)
    is MapBottomSheetState.PinPreview → PinPreviewSheet(state.selectedReport, onEvent, onNavigateToDetail)
    is MapBottomSheetState.None       → Unit
}
```

#### Filtrado sin GPS (MVP):
```kotlin
// MapViewModel — filtrado local sin geolocalización real
val filteredReports = allReports.filter { report ->
    val typeOk = (state.filterLost && report.type == ReportType.LOST) ||
                 (state.filterFound && report.type != ReportType.LOST)
    val hasCoords = report.latitude != null && report.longitude != null
    typeOk && hasCoords
}
```

#### Pins custom con OSMDroid:
```kotlin
// app/util/MapPinUtils.kt
fun createPinDrawable(context: Context, type: ReportType): Drawable {
    // Usar VectorDrawableCompat + tintList para colorear el pin
    val base = ContextCompat.getDrawable(context, R.drawable.ic_location_pin)!!.mutate()
    val color = when (type) {
        ReportType.LOST         -> ContextCompat.getColor(context, R.color.pin_lost)   // #E57373
        ReportType.FOUND_SIGHTING, ReportType.FOUND_IN_CARE -> ContextCompat.getColor(context, R.color.pin_found) // #2E7D32
    }
    DrawableCompat.setTint(base, color)
    return base
}
```

---

### FASE 2D-5: `MyReportsScreen` (Pantalla 22)
**Ruta:** `Routes.MyReports`

#### Archivos a crear:
```
app/ui/screens/myreports/
├── MyReportsScreen.kt
├── MyReportsViewModel.kt
├── MyReportsUiState.kt
└── MyReportsUiEvent.kt
```

#### Filtrar reportes por ownerUid:
```kotlin
// MyReportsViewModel.kt
combine(
    observeReports(ReportType.LOST),
    observeReports(ReportType.FOUND_SIGHTING),
    observeReports(ReportType.FOUND_IN_CARE),
    observeCurrentUser()
) { lost, sightings, inCare, user ->
    Triple(
        lost.filter { it.ownerUid == user?.uid },
        (sightings + inCare).filter { it.ownerUid == user?.uid },
        user
    )
}.collect { (myLost, myFound, _) ->
    _uiState.update { it.copy(lostReports = myLost, foundReports = myFound, isLoading = false) }
}
```

#### Entry point: desde ProfileScreen (añadir botón):
```kotlin
// ProfileScreen.kt — AÑADIR
TextButton(onClick = onNavigateToMyReports) {
    Text(stringResource(R.string.ver_mis_reportes))
}
```

---

### FASE 2D-6: `SightingsForPetScreen` (Pantalla 23)
**Ruta:** `Routes.SightingsForPet`

**Estrategia de vinculación LOST ↔ FOUND_SIGHTING (MVP):**

Usar heurística por especie + zona geográfica. Si hay coordenadas: mostrar avistamientos dentro de N km. Si no hay coordenadas: mostrar todos los `FOUND_SIGHTING` de la misma especie ordenados por fecha.

```kotlin
// SightingsForPetViewModel.kt
fun loadSightings(petReportId: String) {
    viewModelScope.launch {
        // 1. Cargar la mascota perdida
        val petReport = observeReports(ReportType.LOST).first().find { it.id == petReportId }

        // 2. Cargar avistamientos de la misma especie
        val sightings = observeReports(ReportType.FOUND_SIGHTING).first()
            .filter { it.species == petReport?.species || petReport?.species.isNullOrBlank() }
            .sortedByDescending { it.createdAtEpochMs }

        _uiState.update { it.copy(petReport = petReport, sightings = sightings, isLoading = false) }
    }
}
```

#### Header del mapa con OSMDroid:
```kotlin
// SightingsForPetScreen.kt
Box(modifier = Modifier.height(220.dp).fillMaxWidth()) {
    OsmMapView(
        modifier = Modifier.fillMaxSize(),
        center = petReport?.let { GeoPoint(it.latitude ?: 7.89705, it.longitude ?: -72.50809) }
                         ?: GeoPoint(7.89705, -72.50809),
        zoom = 13.0,
        markers = state.sightings.mapNotNull { s ->
            if (s.latitude == null || s.longitude == null) null
            else OsmMarker(s.id, GeoPoint(s.latitude, s.longitude), s.ownerName, null, ReportType.FOUND_SIGHTING)
        },
        onMapClick = null
    )
    // Overlay: título + subtitle + "Ver mapa completo"
    Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
        Text("Avistamientos de ${state.petReport?.petName}", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Text("${state.petReport?.breed} • ${state.petReport?.recencyLabel}", color = Color.White.copy(alpha = 0.8f))
        OutlinedButton(onClick = onNavigateToMap) { Text("Ver mapa completo") }
    }
    IconButton(onClick = onNavigateBack, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
    }
}
```

---

## 6. SEED DATA ACTUALIZADO

Actualizar `FirestorePetReportRepository.kt` y `FakePetReportRepository.kt` con coordenadas reales de Cúcuta:

```kotlin
// data/fake/FakePetReportRepository.kt — seed data nuevo
private val seedReports = listOf(
    PetReport(
        id = "1", ownerUid = "fake_uid_1", ownerInitial = "J", ownerName = "Juan P.",
        petName = "Max", type = ReportType.LOST, species = "Perro", breed = "Golden Retriever",
        description = "Perro que responde al nombre de MAX...", location = "Parque Simón Bolívar",
        imageUrl = "https://placedog.net/400/300?id=1", recencyLabel = "Hace 2 días",
        createdAtEpochMs = System.currentTimeMillis() - 172_800_000L,
        latitude = 7.8939, longitude = -72.5078,  // Cúcuta
        gender = "Macho", color = "Dorado", ageApprox = "3 años", collarColor = "Verde"
    ),
    PetReport(
        id = "2", ownerUid = "fake_uid_2", ownerInitial = "M", ownerName = "María L.",
        petName = "Luna", type = ReportType.FOUND_SIGHTING, species = "Gato", breed = "Siamés",
        description = "Gata siamesa avistada cerca del centro comercial...", location = "CC Andino",
        imageUrl = "https://placekitten.com/400/300", recencyLabel = "Hoy",
        createdAtEpochMs = System.currentTimeMillis() - 3_600_000L,
        latitude = 7.8890, longitude = -72.5015,
        statuses = listOf("Asustado"), size = "Pequeño"
    ),
    PetReport(
        id = "3", ownerUid = "fake_uid_3", ownerInitial = "C", ownerName = "Carlos R.",
        petName = "Rocky", type = ReportType.FOUND_IN_CARE, species = "Perro", breed = "Bulldog Francés",
        description = "Perro en buen estado bajo mi cuidado...", location = "Zona Rosa",
        imageUrl = "https://placedog.net/400/300?id=3", recencyLabel = "Hace 1 día",
        createdAtEpochMs = System.currentTimeMillis() - 86_400_000L,
        latitude = 7.8950, longitude = -72.5100,
        urgency = "FEW_DAYS", physicalStatus = listOf("Saludable"), behaviors = listOf("Cariñoso")
    )
)
```

---

## 7. STRINGS NUEVOS — `strings.xml`

Añadir a `res/values/strings.xml`:

```xml
<!-- Navigation tabs -->
<string name="tab_inicio">Inicio</string>
<string name="tab_mapa">Mapa</string>
<string name="tab_avisos">Avisos</string>
<string name="tab_perfil">Perfil</string>

<!-- Report types -->
<string name="tipo_perdida">Mascota Perdida</string>
<string name="tipo_hallazgo">Mascota Hallada</string>
<string name="tipo_avistamiento">Avistamiento en calle</string>
<string name="tipo_resguardo">Bajo mi cuidado</string>

<!-- Status chips -->
<string name="chip_perdido">PERDIDO</string>
<string name="chip_avistado">AVISTADO</string>
<string name="chip_resguardo">RESGUARDO</string>

<!-- Forms -->
<string name="especie">Especie</string>
<string name="tamano">Tamaño</string>
<string name="sexo">Sexo</string>
<string name="edad_aprox">Edad aprox.</string>
<string name="color_predominante">Color predominante</string>
<string name="raza">Raza</string>
<string name="descripcion_detallada">Descripción detallada</string>
<string name="ultima_ubicacion">ULTIMA UBICACION</string>
<string name="reportado_por">REPORTADO POR</string>
<string name="estado_fisico">ESTADO FISICO</string>
<string name="notas">NOTAS</string>
<string name="fotos_del_animal">Fotos del animal</string>
<string name="sube_fotos">Sube entre 1 y 6 fotos para ayudar a identificar tu mascota</string>
<string name="toca_para_ajustar_ubicacion">Toca para ajustar ubicación</string>
<string name="publicar_reporte">PUBLICAR REPORTE</string>
<string name="publicar_avistamiento">Publicar avistamiento</string>
<string name="publicar_resguardo">Publicar reporte de resguardo</string>
<string name="reporte_confirmado">Reporte Confirmado</string>
<string name="ver_mis_reportes">Ver mis reportes</string>
<string name="mis_reportes">Mis reportes</string>
<string name="hallazgos">Hallazgos</string>
<string name="desapariciones">Desapariciones</string>
<string name="ver_mapa_completo">Ver mapa completo</string>
<string name="ver_mas_avistamientos">Ver más avistamientos</string>
<string name="mas_reciente_primero">Más reciente primero</string>
<string name="filtrar">Filtrar</string>
<string name="nuevo_reporte">Nuevo Reporte</string>
<string name="reportar_hallazgo">Reportar hallazgo</string>
<string name="generar_reporte">Generar Reporte</string>
<string name="mascota_perdida">Mascota perdida</string>
<string name="avistamiento_en_calle">Avistamineto en calle</string>
<string name="bajo_mi_cuidado">Bajo mi cuidado</string>
<string name="mas_informacion">Más Información</string>
<string name="contactar">CONTACTAR</string>
<string name="aplicar_filtros">Aplicar Filtros</string>
<string name="radio_busqueda">Radio de búsqueda (km)</string>
<string name="perdidos_label">Perdidos</string>
<string name="hallazgos_label">Hallazgos</string>

<!-- Urgency options -->
<string name="urgency_indefinite_title">Indefinido</string>
<string name="urgency_indefinite_subtitle">Puedo cuidarlo el tiempo necesario</string>
<string name="urgency_few_days_title">Pocos días</string>
<string name="urgency_few_days_subtitle">Solo puedo tenerlo unos días</string>
<string name="urgency_today_title">Solo hoy (urgente)</string>
<string name="urgency_today_subtitle">Necesito entregarlo hoy mismo</string>
<string name="urgency_now_title">Necesito entregarlo ya</string>
<string name="urgency_now_subtitle">Situación muy urgente</string>

<!-- Errors -->
<string name="error_campo_requerido">Este campo es requerido</string>
<string name="error_telefono_invalido">Número de teléfono inválido</string>
<string name="error_selecciona_especie">Selecciona al menos la especie</string>
<string name="error_publicar">Error al publicar. Intenta de nuevo.</string>
<string name="descartar_reporte">¿Descartar reporte?</string>
<string name="descartar_confirmacion">Perderás toda la información ingresada.</string>
<string name="descartar">Descartar</string>
<string name="cancelar">Cancelar</string>
```

---

## 8. DI — NUEVOS VIEWMODELS EN HILT

Todos los ViewModels nuevos usan `@HiltViewModel`. Agregar los puertos in necesarios. No es necesario crear nuevos módulos — Hilt ya está configurado.

```kotlin
// Cada ViewModel nuevo sigue este patrón:
@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeReports: ObserveReports,           // ya existe
    private val observeCurrentUser: ObserveCurrentUser,   // ya existe
    private val deleteReport: DeleteReport                 // ya existe
) : ViewModel()

@HiltViewModel
class LostReportFormViewModel @Inject constructor(
    private val createReport: CreateReport,               // firma actualizada
    private val observeCurrentUser: ObserveCurrentUser
) : ViewModel()

// etc. para SightingReportFormViewModel, InCareReportFormViewModel,
// MapViewModel, MyReportsViewModel, SightingsForPetViewModel
```

---

## 9. ORDEN DE IMPLEMENTACIÓN DEFINITIVO

```
FASE 2A — Cambios de dominio (prerequisito de todo)
  ├── 2A.1 Extender ReportType (3 valores)
  ├── 2A.2 Extender PetReport (16 campos nuevos con defaults)
  ├── 2A.3 Extender CreateReport (multi-imagen)
  ├── 2A.4 Nuevo token color "care" naranja
  └── 2A.5 UserAvatar con containerColor dinámico

FASE 2B — Navegación (prerequisito de pantallas)
  ├── Routes.kt (reescribir)
  ├── AppNavHost.kt (reescribir)
  ├── MainScaffold.kt (nuevo)
  └── AppBottomNavigationBar.kt (nuevo)

FASE 2C — Componentes compartidos (prerequisito de pantallas)
  ├── OsmMapView.kt (+ deps OSMDroid en build.gradle.kts + MascotasPerdidasApp.kt init)
  ├── SingleSelectChipGroup.kt + MultiSelectChipGroup.kt
  ├── ColorSwatchSelector.kt
  ├── PhotoPickerRow.kt
  ├── FormSectionHeader.kt + SectionLabel.kt
  ├── HeroImage.kt + AttributesGrid.kt + MiniMapView.kt
  ├── SelectionWizardStep.kt + UrgencySelector.kt
  ├── StickyContactFooter.kt + MapBottomBar.kt
  └── MyReportItem.kt + SightingItem.kt

FASE 2D — Pantallas nuevas (en orden de dependencias)
  ├── 2D-1: ReportDetailScreen (P12/13/14) — desbloquea stubs vacíos
  ├── 2D-2: Wizard creación (P15/16/17/18/19) — refactorizar FAB Feed primero
  ├── 2D-3: ReportConfirmedScreen (P21)
  ├── 2D-4: MapScreen (P6/7/8)
  ├── 2D-5: MyReportsScreen (P22)
  └── 2D-6: SightingsForPetScreen (P23)
```

---

## 10. REGLAS DE CLAUDE CODE PARA ESTA FASE

Las reglas del `TECHNICAL_CONTEXT.md §14` siguen vigentes. Se añaden:

15. **OSMDroid es la librería de mapas.** No usar Google Maps SDK. No usar maps-compose. Todo mapa va a través del composable `OsmMapView` wrapper.
16. **`ReportType` tiene 3 valores:** `LOST`, `FOUND_SIGHTING`, `FOUND_IN_CARE`. No usar `FOUND` genérico.
17. **DrawerShell deja de ser el contenedor principal.** La navegación principal es `MainScaffold` + `AppBottomNavigationBar`. El Drawer queda para Settings/opciones secundarias si se mantiene.
18. **Todos los campos nuevos de `PetReport` tienen default.** No romper código existente.
19. **`CreateReport` acepta `List<ByteArray>`.** Actualizar todos los call sites.
20. **El FAB del Feed navega al wizard, no abre Dialog.** Eliminar `CreateReportDialog` de FeedScreen.
21. **Back stack del wizard:** Al llegar a `ReportConfirmedScreen`, hacer `popUpTo(Routes.Main.route)` para limpiar el wizard del stack.
22. **Coordenadas default para Cúcuta:** `GeoPoint(7.89705, -72.50809)`. Usar cuando `latitude == null`.
23. **Strings nuevos en `strings.xml`** — ver §7 de este documento. Total estimado: ~60 strings nuevos.
24. **`CareUrgency` enum vive en `app/` (no en `domain/`).** Es un detalle de UI, no de negocio.

---

## 11. DIAGRAMA DE CONEXIONES ENTRE PANTALLAS

```
SplashScreen
    │ phoneVerified=true
    ▼
MainScaffold ◄──────────────────────────────────────────────────────────┐
├── Tab: FeedScreen ──────────────────────────────────┐                 │
│   │ FAB (+)           │ tap card "Más Info"          │                 │
│   ▼                   ▼                              │                 │
│   NewReportTypeScreen  ReportDetailScreen ◄──────────┤                 │
│   │ LOST    │ FOUND    (P12/13/14)        │           │                 │
│   ▼         ▼          │ CONTACTAR        │ owner     │                 │
│   LostForm  FoundSub   │ Intent externo   │ MyReports │                 │
│   (P17)    (P16)       │                  ▼           │                 │
│   │         │ calle    │           SightingsForPet    │                 │
│   │         ▼          │           (P23)              │                 │
│   │         SightingForm (P18)     │ "Ver mapa"       │                 │
│   │         │                     ▼                  │                 │
│   │         │ cuidado    Tab: MapScreen (P6/7/8) ─────┘                 │
│   │         ▼           │ pin tap                                       │
│   │         InCareForm  ▼                                               │
│   │         (P19)       ReportDetailScreen                              │
│   │         │                                                           │
│   └────┬────┘                                                           │
│        ▼                                                                │
│        ReportConfirmedScreen (P21) ────────────────────────────────────┘
│        (popUpTo Main)
│
├── Tab: MapScreen (P6/7/8)
│   │ pin tap → ReportDetailScreen
│   │ (+) → NewReportTypeScreen
│
├── Tab: NotificationsScreen / SightingsForPet (P23)
│   │ item tap → ReportDetailScreen
│   │ "Ver mapa" → MapScreen
│
└── Tab: ProfileScreen
    │ "Mis reportes" → MyReportsScreen (P22)
    │                   │ item tap → ReportDetailScreen
    └── SettingsScreen (desde overflow/menú)
```

---

*Documento generado: Junio 2026 — MascotasPerdidas Fase 2 — Integra pantallas 6–23 + OSMDroid + NavigationBar.*  
*Leer TECHNICAL_CONTEXT.md como complemento. En caso de conflicto, este documento tiene precedencia para Fase 2.*
