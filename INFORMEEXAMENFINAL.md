# INFORME FINAL DE ARQUITECTURA — MascotasPerdidas

> **Aplicación Android para reportar mascotas perdidas y encontradas en Cúcuta, Colombia.**
> Proyecto académico/examen — Junio 2026

---

## 1. Identidad del Proyecto

**MascotasPerdidas** es una aplicación móvil Android desarrollada en **Kotlin** con **Jetpack Compose** que permite a los ciudadanos de Cúcuta:

- Reportar mascotas **perdidas** (LOST)
- Reportar mascotas **encontradas** — ya sea como avistamiento en calle (FOUND_SIGHTING) o bajo cuidado temporal (FOUND_IN_CARE)
- Ver todas las mascotas reportadas en un **mapa interactivo**
- Buscar y filtrar reportes
- Contactar a los dueños/rescatistas vía **WhatsApp** o **llamada telefónica**
- Gestionar sus propios reportes desde un perfil personal

---

## 2. Arquitectura: Hexagonal (Ports & Adapters)

El proyecto implementa **Arquitectura Hexagonal** (también llamada *Ports & Adapters*), propuesta por Alistair Cockburn. Esta arquitectura separa la lógica de negocio pura de los detalles técnicos externos (Android, Firebase, mapas, etc.).

### Diagrama conceptual

```
┌──────────────────────────────────────────────────────────────────┐
│                        APP LAYER                                  │
│  (Android / Jetpack Compose / Hilt / ViewModels / Navegación)     │
│                                                                    │
│  Pantallas ➜ ViewModels ➜ Casos de Uso (puertos IN)              │
├──────────────────────────────────────────────────────────────────┤
│                        DOMAIN LAYER                                │
│  (Kotlin PURO — sin Android, sin Firebase, sin frameworks)         │
│                                                                    │
│  domain/model/      ➜ Entidades de negocio                        │
│  domain/port/in/    ➜ Driving Ports (12 casos de uso)              │
│  domain/port/out/   ➜ Driven Ports (3 interfaces de repositorio)  │
│  domain/usecase/    ➜ Implementaciones de los puertos IN           │
├──────────────────────────────────────────────────────────────────┤
│                        DATA LAYER                                  │
│  (Adapters de salida — Firebase, Fake, DataStore, Imágenes)        │
│                                                                    │
│  data/firebase/     ➜ Implementaciones reales (Firebase)          │
│  data/fake/         ➜ Implementaciones falsas (demo/testing)      │
│  data/dto/          ➜ Objetos de transferencia                    │
│  data/mapper/       ➜ Conversión DTO ↔ Dominio                    │
│  data/image/        ➜ Compresión y codificación de imágenes       │
│  data/datastore/    ➜ Preferencias locales del usuario            │
└──────────────────────────────────────────────────────────────────┘
```

### Regla de dependencia (LA MÁS IMPORTANTE)

- **DOMAIN no conoce a nadie.** No importa Android, Firebase, Compose ni nada externo.
- **DATA conoce a DOMAIN** (implementa sus interfaces `port/out/`).
- **APP conoce a DOMAIN y DATA** (inyecta las dependencias vía Hilt).

Esto se evidencia en la estructura de carpetas:

```
app/src/main/kotlin/com/mascotasperdidas/app/
├── domain/   ← No importa nada de data/ ni de app/
├── data/     ← Importa domain/, implementa domain.port.out.*
└── app/      ← Importa domain/ y data/, contiene UI, DI, navegación
```

---

## 3. Stack Tecnológico

| Categoría | Tecnología | Justificación |
|-----------|-----------|---------------|
| **Lenguaje** | Kotlin 2.x | Oficial para Android, moderno, conciso |
| **UI Toolkit** | Jetpack Compose + Material 3 | Declarativo, nativo Android, diseño moderno |
| **Inyección de dependencias** | Hilt (Dagger) | Estándar Android, soporte para ViewModels |
| **Base de datos / Backend** | Firebase (BaaS) | Firestore (datos), Auth (autenticación), sin servidor propio |
| **Autenticación** | Firebase Auth | Google Sign-In + OTP por teléfono |
| **Mapa** | **OSMDroid** (OpenStreetMap) | **Gratuito, sin API key de Google**, tiles libres de Mapnik |
| **Imágenes** | Coil Compose | Carga asíncrona desde Base64 o URLs |
| **Reactividad** | Kotlin Coroutines + Flow | Observables reactivos, callbackFlow para Firebase |
| **Almacenamiento local** | Jetpack DataStore (Preferences) | Preferencias de notificaciones del usuario |
| **Calidad de código** | Detekt + Ktlint | Análisis estático automatizado |
| **Manejo de dependencias** | Gradle Version Catalog (`libs.versions.toml`) | Centralizado, actualizable |

### Librerías PROHIBIDAS por diseño

- ❌ **Google Maps SDK** (requiere API key de pago, se reemplazó con OSMDroid)
- ❌ **Room / SQLite** (Firestore es la fuente de verdad, no hay BD local relacional)
- ❌ **Retrofit / Ktor** (no hay API REST propia, Firebase SDK maneja la comunicación)
- ❌ **LiveData** (se usa StateFlow/SharedFlow de Kotlin)

---

## 4. Estructura de Carpetas que Evidencia la Arquitectura

```
MascotasPerdidas/
├── app/                                  # Módulo Android principal
│   ├── build.gradle.kts                  # Dependencias, plugins, config Android
│   └── src/
│       ├── main/
│       │   ├── kotlin/com/mascotasperdidas/app/
│       │   │   ├── domain/               # ═══ CAPA DE DOMINIO ═══
│       │   │   │   ├── model/            # 5 modelos de negocio
│       │   │   │   │   ├── PetReport.kt      (35 campos — el reporte de mascota)
│       │   │   │   │   ├── ReportType.kt     (enum: LOST, FOUND_SIGHTING, FOUND_IN_CARE)
│       │   │   │   │   ├── User.kt           (datos del usuario)
│       │   │   │   │   ├── AuthState.kt      (sealed class: SignedIn / SignedOut)
│       │   │   │   │   └── NotificationPrefs.kt
│       │   │   │   ├── port/
│       │   │   │   │   ├── in/            # 12 interfaces (Driving Ports)
│       │   │   │   │   │   ├── CreateReport.kt
│       │   │   │   │   │   ├── DeleteReport.kt
│       │   │   │   │   │   ├── DeleteAccount.kt
│       │   │   │   │   │   ├── ObserveReports.kt
│       │   │   │   │   │   ├── ObserveCurrentUser.kt
│       │   │   │   │   │   ├── SearchReports.kt
│       │   │   │   │   │   ├── SignInWithGoogle.kt
│       │   │   │   │   │   ├── SignOut.kt
│       │   │   │   │   │   ├── RequestPhoneOtp.kt
│       │   │   │   │   │   ├── VerifyPhoneOtp.kt
│       │   │   │   │   │   ├── UpdateUserProfile.kt
│       │   │   │   │   │   └── UpdateNotificationPrefs.kt
│       │   │   │   │   └── out/           # 3 interfaces (Driven Ports)
│       │   │   │   │       ├── AuthRepository.kt
│       │   │   │   │       ├── PetReportRepository.kt
│       │   │   │   │       └── UserRepository.kt
│       │   │   │   └── usecase/           # 12 implementaciones (una por puerto IN)
│       │   │   │       ├── CreateReportImpl.kt
│       │   │   │       └── ... (11 más)
│       │   │   │
│       │   │   ├── data/                  # ═══ CAPA DE DATOS (ADAPTERS OUT) ═══
│       │   │   │   ├── firebase/          # Implementaciones reales
│       │   │   │   │   ├── FirebaseAuthRepository.kt
│       │   │   │   │   ├── FirestoreUserRepository.kt
│       │   │   │   │   ├── FirestorePetReportRepository.kt   (194 líneas — el más complejo)
│       │   │   │   │   └── ActivityProvider.kt
│       │   │   │   ├── fake/              # Datos falsos para desarrollo
│       │   │   │   │   ├── FakeAuthRepository.kt
│       │   │   │   │   ├── FakeUserRepository.kt
│       │   │   │   │   └── FakePetReportRepository.kt
│       │   │   │   ├── dto/               # Objetos de transferencia (Firestore)
│       │   │   │   │   ├── PetReportDto.kt
│       │   │   │   │   ├── UserDto.kt
│       │   │   │   │   └── NotificationPrefsDto.kt
│       │   │   │   ├── mapper/            # Conversión DTO ↔ Dominio
│       │   │   │   │   ├── PetReportMapper.kt
│       │   │   │   │   └── UserMapper.kt
│       │   │   │   ├── image/             # Pipeline de imágenes
│       │   │   │   │   ├── ImageCompressor.kt        (redimensiona + comprime JPEG)
│       │   │   │   │   ├── Base64ImageCodec.kt       (codifica/decodifica Base64)
│       │   │   │   │   └── ImageTooLargeException.kt
│       │   │   │   └── datastore/         # Persistencia local
│       │   │   │       └── PrefsDataStore.kt
│       │   │   │
│       │   │   └── app/                   # ═══ CAPA DE APLICACIÓN (ADAPTERS IN) ═══
│       │   │       ├── MascotasPerdidasApp.kt    # Application class (@HiltAndroidApp)
│       │   │       ├── MainActivity.kt           # Entry point, setContent { ... }
│       │   │       ├── di/                       # Inyección de dependencias (Hilt)
│       │   │       │   ├── FirebaseModule.kt     # Provee FirebaseAuth, Firestore, Storage
│       │   │       │   ├── RepositoryModule.kt   # Bindeo Fake ↔ Firebase
│       │   │       │   └── UseCaseModule.kt      # Provee implementaciones de casos de uso
│       │   │       ├── navigation/
│       │   │       │   ├── Routes.kt             # 13 rutas selladas
│       │   │       │   └── AppNavHost.kt         # NavHost con todas las pantallas
│       │   │       ├── theme/
│       │   │       │   ├── Color.kt
│       │   │       │   ├── Type.kt
│       │   │       │   └── Theme.kt              # MascotasPerdidasTheme (god node: 42 aristas)
│       │   │       ├── util/                     # Utilidades Android
│       │   │       │   ├── PermissionUtils.kt
│       │   │       │   ├── ContactIntents.kt
│       │   │       │   ├── MapPinUtils.kt
│       │   │       │   ├── AvatarColorUtils.kt
│       │   │       │   ├── PetImageModel.kt
│       │   │       │   └── Recency.kt
│       │   │       └── ui/
│       │   │           ├── components/           # 26 composables reutilizables
│       │   │           │   ├── OsmMapView.kt         ★ Componente crítico de mapa
│       │   │           │   ├── LocationMapPicker.kt   ★ Selector de ubicación
│       │   │           │   ├── MiniMapView.kt
│       │   │           │   ├── PetCard.kt
│       │   │           │   ├── PhotoPickerRow.kt     ★ Selector de fotos
│       │   │           │   ├── AppTopBar.kt, AppDrawer.kt, AppBottomNavigationBar.kt
│       │   │           │   ├── HeroImage.kt, UserAvatar.kt
│       │   │           │   └── ... (20 más)
│       │   │           └── screens/             # 11 módulos de pantalla
│       │   │               ├── splash/          (SplashScreen + ViewModel)
│       │   │               ├── otp/             (login OTP)
│       │   │               ├── permissions/     (permisos de ubicación)
│       │   │               ├── feed/            (lista principal)
│       │   │               ├── map/             ★ Pantalla de mapa
│       │   │               ├── report/
│       │   │               │   ├── creation/    (wizard de 4 pasos)
│       │   │               │   ├── detail/      (detalle de reporte)
│       │   │               │   └── confirmed/   (confirmación)
│       │   │               ├── myreports/       (mis reportes)
│       │   │               ├── sightings/       (avistamientos de una mascota)
│       │   │               ├── profile/         (perfil de usuario)
│       │   │               ├── settings/        (configuración)
│       │   │               └── main/            (scaffold principal)
│       │   │
│       │   └── res/                             # Recursos Android
│       │       ├── drawable/
│       │       │   ├── ic_logo.xml, ic_location_pin.xml
│       │       │   ├── ic_whatsapp.xml, ic_google.xml
│       │       │   └── logo_msp.png
│       │       └── values/
│       │           ├── colors.xml, strings.xml, themes.xml
│       │
│       └── test/                                # Tests unitarios
│           └── java/com/mascotasperdidas/app/
│               ├── data/firebase/
│               │   └── FirestorePetReportRepositoryTest.kt
│               └── data/image/
│                   └── Base64ImageCodecTest.kt
│
├── docs/                                       # Documentación técnica
│   ├── TECHNICAL_CONTEXT.md                    # Contexto técnico completo
│   ├── WORKPLAN.md                             # Plan de trabajo por fases
│   ├── INSTRUCTIONS.md                         # Reglas para Claude Code
│   ├── OSMdroid.md                             # Guía de integración del mapa
│   └── Pantalla_*.md                           # Documentos por pantalla (5 archivos)
│
├── graphify-out/                               # Grafo de dependencias (3442 nodos, 3618 aristas)
├── build.gradle.kts                            # Plugins raíz
├── settings.gradle.kts                         # Nombre: "MascotasPerdidas", módulo :app
├── gradle/libs.versions.toml                   # Catálogo de versiones
├── detekt.yml                                  # Reglas de calidad de código
├── FIREBASE.md                                 # Guía de configuración Firebase (10 pasos)
├── PREVIO2.md                                  # Reporte técnico previo (442 líneas)
├── DOCUMENTACION.md                            # Bitácora de migración del proyecto
├── roadmap.md                                  # 14 fases del proyecto
└── README.md
```

---

## 5. Módulos Más Importantes

### 5.1 Capa de Dominio (el hexágono)

- **5 modelos de negocio** en Kotlin puro (sin anotaciones de frameworks):
  - `PetReport`: 35 campos que describen una mascota (nombre, raza, color, ubicación, fotos, urgencia, etc.)
  - `ReportType`: enum con 3 variantes — `LOST`, `FOUND_SIGHTING`, `FOUND_IN_CARE`
  - `User`: datos del usuario autenticado
  - `AuthState`: sealed class con `SignedIn(uid, phoneVerified)` y `SignedOut`
  - `NotificationPrefs`: preferencias de notificaciones push

- **12 puertos de entrada** (Driving Ports): interfaces que definen lo que la app PUEDE hacer:
  - `ObserveReports` — flujo reactivo de reportes por tipo
  - `SearchReports` — búsqueda por texto
  - `CreateReport` — crear reporte con imágenes
  - `DeleteReport` / `DeleteAccount`
  - `SignInWithGoogle` / `SignOut`
  - `RequestPhoneOtp` / `VerifyPhoneOtp`
  - `ObserveCurrentUser` / `UpdateUserProfile` / `UpdateNotificationPrefs`

- **3 puertos de salida** (Driven Ports): interfaces que definen lo que el dominio NECESITA del exterior:
  - `AuthRepository` — autenticación
  - `PetReportRepository` — CRUD de reportes
  - `UserRepository` — perfil de usuario

- **12 implementaciones de casos de uso** (una por puerto IN), todas delegando a los repositorios (puertos OUT).

### 5.2 Capa de Datos (adapters de salida)

- **3 repositorios Firebase** (producción):
  - `FirebaseAuthRepository`: Google Sign-In, OTP telefónico, callbackFlow para estado de auth
  - `FirestoreUserRepository`: perfil en Firestore, guardado de token FCM
  - `FirestorePetReportRepository`: el más complejo — observer con snapshot listener, creación con compresión de imágenes, seeding automático de datos demo, búsqueda textual

- **3 repositorios Fake** (desarrollo/testing): datos en memoria para trabajar sin Firebase

- **DTOs + Mappers**: `PetReportDto`, `UserDto`, `NotificationPrefsDto` con funciones `toDomain()` y `toDto()` para convertir entre capas

- **DataStore**: `PrefsDataStore` para persistir preferencias de notificación localmente

### 5.3 Capa de Aplicación (adapters de entrada)

- **11+ módulos de pantalla** con patrón **MVVM unidireccional**:
  - Cada pantalla = `Screen.kt` + `ViewModel.kt` + `UiState.kt` + `UiEvent.kt`
  - UI emite eventos → ViewModel los procesa → actualiza UiState → UI se recompone
  - Sin estado compartido entre pantallas

- **26 componentes UI reutilizables**: PetCard, PhotoPickerRow, OsmMapView, LocationMapPicker, ColorSwatchSelector, SingleSelectChipGroup, etc.

- **Navegación**: 13 rutas selladas en `Routes.kt`, `AppNavHost` con navegación Compose type-safe

---

## 6. La Parte Frontend y la Parte Backend

### Frontend (APP LAYER)

Todo el código en `app/src/main/kotlin/.../app/app/`:

- **Jetpack Compose** para la UI declarativa
- **Material 3** como sistema de diseño (tema púrpura #6750A4)
- **ViewModels** con `StateFlow` para reactividad
- **Hilt** para inyección de dependencias en ViewModels
- **Coil** para carga de imágenes
- **OSMDroid** renderizado vía `AndroidView` (interop con View tradicional dentro de Compose)

### Backend (NO hay servidor propio)

**Firebase actúa como Backend-as-a-Service (BaaS):**

| Servicio Firebase | Uso en la app |
|---|---|
| **Firebase Auth** | Registro/login con Google y OTP telefónico |
| **Cloud Firestore** | Base de datos NoSQL. Colección `pet_reports`, documentos con los reportes. Consultas por tipo, búsqueda textual. Snapshot listeners para actualización en tiempo real. |
| **Firebase Storage** | Incluido en el módulo DI pero **no se usa actualmente** (las imágenes van inline en Firestore como Base64) |

### Cómo se comunica con la base de datos

```
Usuario (UI) → ViewModel → Caso de Uso (interfaz port/in)
                              ↓
                     Implementación (usecase/)
                              ↓
                     Repositorio (interfaz port/out)
                              ↓
        ┌────────────────────┴───────────────────┐
        ↓                                         ↓
  FakePetReportRepository              FirestorePetReportRepository
  (datos en memoria)                   (Firebase SDK → Firestore)
```

- La comunicación es **siempre asíncrona** (corrutinas + Flow)
- Para lecturas: `callbackFlow` que envuelve `addSnapshotListener` de Firestore → actualización en tiempo real
- Para escrituras: `suspend fun` que usan `.await()` de las Tasks de Firebase
- El cambio entre Fake y Firebase se hace **en un solo archivo** (`RepositoryModule.kt`), cambiando qué implementación se inyecta con `@Binds`

---

## 7. Estrategia de Guardado de Imágenes

### Pipeline de imágenes (MUY IMPORTANTE)

```
1. USUARIO selecciona foto
         ↓
2. PhotoPickerRow (Compose) obtiene bytes raw (JPEG/PNG del dispositivo)
         ↓
3. ImageCompressor.compressToJpeg()
   - Decodifica los bytes a Bitmap
   - Redimensiona: lado más largo ≤ 1024px (nunca agranda)
   - Comprime a JPEG con calidad 70%
   - Recicla los bitmaps intermedios
         ↓
4. Base64ImageCodec.encode()
   - Convierte los bytes JPEG a string Base64 (sin padding)
   - Verifica: MAX_DOC_BASE64_BYTES = 921,600 (~900 KB)
   - Si excede → ImageTooLargeException (se muestra error al usuario)
         ↓
5. El string Base64 se guarda como campo `imageUrl` en el documento de Firestore
         ↓
6. Para mostrar: Coil decodifica el Base64 y lo renderiza en un AsyncImage
```

### Decisiones clave de diseño

- **NO se usa Firebase Storage.** Esto evita:
  - Manejo de buckets, reglas de seguridad adicionales
  - Una llamada de red extra por cada foto
  - URLs públicas que complican la privacidad
- Las imágenes van **inline en el documento Firestore** como Base64
- Límite estricto de **~900 KB por imagen** (Firestore tiene límite de 1 MiB por documento)
- `Base64ImageCodec.isBase64()` puede distinguir entre URLs legacy (http) y Base64 nuevo — compatible hacia atrás con datos de prueba
- Solo se guarda **1 foto por reporte** en el MVP actual (campo `imageUrl`). El array `additionalPhotos` está declarado pero no implementado aún (marcado con TODO).

---

## 8. El Mapa — Componente Crítico

### OSMDroid (OpenStreetMap)

- **Librería**: `org.osmdroid:osmdroid-android`
- **Proveedor de tiles**: Mapnik (OpenStreetMap, gratuito, sin API key)
- **Integración**: `AndroidView` dentro de Compose (`OsmMapView.kt`)
- **Marcadores**: `OsmMarker` con posición (GeoPoint), ícono, título, tipo de reporte
- **Interactividad**: 
  - Multi-touch (zoom, desplazamiento)
  - Click en el mapa para seleccionar ubicación
  - Click en marcador para ver detalle
- **Coordenadas por defecto**: Cúcuta, Colombia (7.89705, -72.50809)

### Componentes de mapa

| Componente | Propósito |
|---|---|
| `OsmMapView` | Mapa principal con marcadores, zoom, interacción |
| `LocationMapPicker` | Selector de ubicación (para formularios de reporte) |
| `MiniMapView` | Mapa pequeño de solo lectura (vista previa) |
| `MapBottomBar` | Barra inferior con filtros (perdidos/encontrados) |
| `MapScreen` | Pantalla completa del mapa con ViewModel |
| `MapPinUtils` | Utilidades para crear iconos de pines por tipo de reporte |

### Por qué OSMDroid y no Google Maps

1. **Gratuito** — no requiere API key ni facturación de Google Cloud
2. **Open source** — tiles de OpenStreetMap, comunidad activa
3. **Sin límites de uso** — ideal para una app de alcance local como Cúcuta
4. **Funcionalidad suficiente** — marcadores, gestos, zoom, personalización de íconos

---

## 9. Autenticación

### Flujo de login

```
1. Usuario abre la app → SplashScreen verifica estado de auth
2. Si no autenticado → OTP Screen:
   a. Opción A: "Iniciar con Google" → Google Sign-In → Firebase Auth
   b. Opción B: Ingresar teléfono → Recibir OTP SMS → Verificar código
3. Firebase Auth emite AuthState → AppNavHost navega a la pantalla principal
```

### Implementación

- **Google Sign-In**: Activity Result API + `GoogleAuthProvider.getCredential(idToken)`
- **Phone OTP**: `PhoneAuthProvider.OnVerificationStateChangedCallbacks` con auto-verificación
- **Estado de auth**: `callbackFlow` que envuelve `AuthStateListener` de Firebase
- **AuthState**: sealed class → `SignedIn(uid, phoneVerified)` o `SignedOut`

---

## 10. Navegación

13 rutas definidas como `sealed class Routes`:

| Ruta | Pantalla |
|---|---|
| `Splash` | Splash inicial con verificación de auth |
| `Otp` | Login con Google/teléfono |
| `Permissions` | Solicitud de permisos de ubicación |
| `Main` | Scaffold principal con BottomNavBar |
| `Feed` | Lista de reportes (principal) |
| `Map` | Mapa interactivo con filtros |
| `MyReports` | Mis reportes publicados |
| `Profile` | Perfil de usuario |
| `Settings` | Configuración y notificaciones |
| `NewReportType` | Wizard paso 1 — tipo de reporte |
| `FoundSubType` | Wizard paso 2 — subtipo (avistamiento/en cuidado) |
| `LostReportForm` / `SightingReportForm` / `InCareReportForm` | Wizard paso 3 — formulario |
| `ReportConfirmed` | Wizard paso 4 — confirmación |
| `ReportDetail` | Detalle de un reporte |
| `SightingsForPet` | Avistamientos de una mascota específica |

Navegación implementada con `NavHost` de Jetpack Compose Navigation.

---

## 11. Inyección de Dependencias (Hilt)

```
FirebaseModule (@Module)
  ├── provee FirebaseAuth
  ├── provee FirebaseFirestore
  └── provee FirebaseStorage

RepositoryModule (@Binds)  ← CAMBIAR AQUÍ PARA SWITCHEAR FAKE ↔ FIREBASE
  ├── AuthRepository → FirebaseAuthRepository
  ├── UserRepository → FirestoreUserRepository
  └── PetReportRepository → FirestorePetReportRepository

UseCaseModule (@Binds)
  ├── ObserveReports → ObserveReportsImpl
  ├── SearchReports → SearchReportsImpl
  └── ... (10 más)
```

**El switch Fake ↔ Firebase se hace editando solo `RepositoryModule.kt`** — se cambia `FirebaseAuthRepository` por `FakeAuthRepository`, etc. Todo el resto del código permanece igual gracias a que los ViewModels dependen de las interfaces (puertos), no de las implementaciones concretas.

---

## 12. Datos de Prueba (Seeding)

`FirestorePetReportRepository` incluye lógica de **seeding automático**: cuando la colección está vacía y un usuario se autentica, inserta 3 reportes de ejemplo con datos realistas de Cúcuta:

1. **Max** — Golden Retriever perdido, Parque Simón Bolívar
2. **Luna** — Gata siamés avistada, Centro Comercial Ventura Plaza
3. **Rocky** — Bulldog Francés bajo cuidado, Zona Rosa

Esto permite probar la app inmediatamente después de instalar sin necesidad de crear reportes manualmente.

---

## 13. Estado del Proyecto (MVP)

| Indicador | Valor |
|---|---|
| Archivos Kotlin | ~74 |
| Modelos de dominio | 5 |
| Puertos de entrada (IN) | 12 |
| Puertos de salida (OUT) | 3 |
| Implementaciones de casos de uso | 12 |
| Pantallas completas | 11+ |
| ViewModels | 12 |
| Componentes UI reutilizables | 26 |
| Nodos en el grafo | 3,442 |
| Aristas en el grafo | 3,618 |
| Comunidades detectadas | 380 |
| Cobertura de tests | Mínima (2 tests unitarios) |

**MVP completado: ~80%** — Pantallas principales funcionales, autenticación, CRUD de reportes, mapa, búsqueda. Pendiente: notificaciones push, múltiples fotos, tests de integración/E2E.

---

## 14. Principios de Diseño Demostrados

1. **Hexagonal puro**: dominio no importa nada externo — ni Android, ni Firebase, ni Compose
2. **SOLID**: cada clase/interfaz tiene una sola responsabilidad
3. **Inversión de dependencias**: el dominio define interfaces, la infraestructura las implementa
4. **MVVM unidireccional**: UI → Event → ViewModel → State → UI
5. **Reactividad**: StateFlow + Flow en toda la cadena
6. **Testeabilidad**: repositorios Fake permiten testear sin Firebase
7. **DataStore sobre SharedPreferences**: tipo-safe, asíncrono, moderno
8. **Gradle Version Catalog**: dependencias centralizadas en `libs.versions.toml`
9. **Cucuta-first**: coordenadas por defecto, datos de prueba locales

---

## 15. Resumen para Exposición Oral

> MascotasPerdidas es una **app Android nativa** construida con **Arquitectura Hexagonal**, **Kotlin + Jetpack Compose**, usando **Firebase como backend serverless** y **OpenStreetMap (OSMDroid) como mapa gratuito**. 
>
> La lógica de negocio está completamente aislada en la carpeta `domain/` — Kotlin puro sin frameworks. Las pantallas (11+) se comunican con Firebase a través de **12 casos de uso** que implementan **3 interfaces de repositorio**, inyectadas con **Hilt**. El cambio entre datos reales (Firebase) y falsos (pruebas) se hace cambiando **un solo archivo**.
>
> Las imágenes de mascotas se comprimen (JPEG 70%, máx 1024px) y se codifican en **Base64** para guardarse directamente en Firestore (sin Storage externo). El mapa usa **OpenStreetMap gratuito** sin API key de Google.
>
> El proyecto demuestra **separación de concerns, inversión de dependencias, MVVM reactivo, y clean code** con 74 archivos Kotlin organizados en 3 capas estrictas: **domain → data → app**.

---

*Informe generado a partir del grafo de dependencias (Graphify) y análisis del código fuente — Junio 2026.*
