# Reporte Técnico — MascotasPerdidas
> Fecha: 19 de mayo de 2026 · Exposición de arquitectura y estado de la aplicación

---

## 1. Identidad del Proyecto

**MascotasPerdidas** es una aplicación móvil Android que permite a usuarios reportar mascotas perdidas y avistamientos, visualizarlos en un feed de tarjetas y gestionar su perfil de usuario.

| Dato | Valor |
|------|-------|
| Plataforma | Android (Min SDK 26 · Target SDK 34) |
| Lenguaje | Kotlin exclusivamente |
| UI Toolkit | Jetpack Compose + Material Design 3 |
| Autenticación | Firebase Authentication |
| Base de datos | Cloud Firestore |
| Almacenamiento | Firebase Storage |
| Inyección de dependencias | Hilt (Dagger) |
| Arquitectura | **Hexagonal (Ports & Adapters)** |

---

## 2. Arquitectura Hexagonal — Principios

La arquitectura hexagonal (también llamada Ports & Adapters) posiciona el **dominio** como el núcleo del sistema. El dominio no conoce nada del exterior: ni Android, ni Compose, ni Firebase, ni Hilt.

```
        ┌──────────────────────────────────────────────────────┐
        │                       DOMINIO                        │
        │   (modelos puros · puertos in/out · casos de uso)    │
        │              ⚠ NO importa de nadie ⚠                 │
        └──────────────────────────────────────────────────────┘
                 ▲                                  ▲
                 │ implementa                       │ usa
                 │ (puertos out)                    │ (puertos in)
        ┌────────┴───────┐                  ┌───────┴────────┐
        │      DATA      │                  │      APP       │
        │  Firebase      │                  │  Compose UI    │
        │  Fake/Cache    │                  │  ViewModels    │
        └────────────────┘                  │  Navigation    │
                                            └────────────────┘
```

**Regla de dependencias (inviolable):**
- `domain/` → solo Kotlin stdlib + coroutines-core + javax.inject
- `data/` → puede importar de `domain/`, **nunca** de `app/`
- `app/` → puede importar de `domain/`, wiring solo en `app/di/`

---

## 3. Estructura de Paquetes

El proyecto tiene **74 archivos Kotlin** organizados en tres capas:

```
com.mascotasperdidas.app/
│
├── domain/               ← HEXÁGONO (Kotlin puro)
│   ├── model/            5 modelos de dominio
│   ├── port/in/          12 interfaces de casos de uso (driving ports)
│   ├── port/out/         3 interfaces de repositorios (driven ports)
│   └── usecase/          12 implementaciones de casos de uso
│
├── data/                 ← ADAPTERS DE SALIDA
│   ├── dto/              3 DTOs de Firestore
│   ├── mapper/           2 mappers (DTO ↔ dominio)
│   ├── firebase/         3 repositorios Firebase (producción)
│   ├── fake/             3 repositorios Fake (pruebas/demo)
│   └── datastore/        1 caché local cifrada (DataStore)
│
└── app/                  ← ADAPTER DE ENTRADA (Android)
    ├── di/               3 módulos Hilt
    ├── navigation/       AppNavHost + Routes
    ├── ui/components/    6 composables reutilizables
    ├── ui/screens/       6 pantallas (Screen + ViewModel + UiState + UiEvent)
    ├── theme/            Color.kt · Type.kt · Theme.kt
    └── util/             PermissionUtils
```

---

## 4. Capa Dominio — El Hexágono

### 4.1 Modelos de Dominio (Kotlin puro, sin anotaciones de frameworks)

| Modelo | Campos clave | Propósito |
|--------|-------------|-----------|
| `User` | uid, displayName, email, phoneNumber, phoneVerified, photoUrl, notificationPrefs, createdAtEpochMs | Perfil del usuario autenticado |
| `PetReport` | id, ownerUid, ownerInitial, petName, type, breed, description, location, imageUrl, recencyLabel, createdAtEpochMs | Reporte de mascota perdida/hallada |
| `ReportType` | LOST / FOUND | Enum de clasificación |
| `AuthState` | SignedOut / SignedIn(uid, phoneVerified) | Máquina de estados de autenticación |
| `NotificationPrefs` | lostPetsNearby, foundPetsNearby, sightingsOnMyReports | Preferencias de notificación |

### 4.2 Puertos de Salida (Driven Ports — lo que el dominio necesita del exterior)

```kotlin
// domain/port/out/AuthRepository.kt
interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>
    suspend fun signInWithGoogleIdToken(idToken: String)
    suspend fun requestPhoneOtp(phone: String): String
    suspend fun verifyPhoneOtp(verificationId: String, code: String)
    suspend fun signOut()
    suspend fun deleteCurrentUser()
}

// domain/port/out/UserRepository.kt
interface UserRepository {
    fun observeCurrentUser(): Flow<User?>
    suspend fun upsertUser(user: User)
    suspend fun updateProfile(name: String, phone: String)
    suspend fun updateNotificationPrefs(prefs: NotificationPrefs)
    suspend fun deleteCurrentUserDocument()
}

// domain/port/out/PetReportRepository.kt
interface PetReportRepository {
    fun observeReports(type: ReportType): Flow<List<PetReport>>
    suspend fun createReport(report: PetReport, imageBytes: ByteArray?)
    suspend fun deleteReport(id: String)
    suspend fun searchReports(query: String, type: ReportType): List<PetReport>
}
```

### 4.3 Puertos de Entrada (Driving Ports — lo que el dominio ofrece al exterior)

12 fun interfaces, todas de un solo método:

| Grupo | Casos de uso |
|-------|-------------|
| **Autenticación** | SignInWithGoogle · RequestPhoneOtp · VerifyPhoneOtp · SignOut · DeleteAccount |
| **Usuario** | ObserveCurrentUser · UpdateUserProfile · UpdateNotificationPrefs |
| **Reportes** | ObserveReports · CreateReport · SearchReports · DeleteReport |

### 4.4 Implementación de Caso de Uso (Ejemplo)

```kotlin
// domain/usecase/ObserveCurrentUserImpl.kt
class ObserveCurrentUserImpl @Inject constructor(
    private val userRepository: UserRepository   // solo conoce el puerto out
) : ObserveCurrentUser {
    override fun invoke(): Flow<User?> = userRepository.observeCurrentUser()
}
```

El caso de uso **no sabe** si el repositorio es Firebase, Fake o Cache. Depende únicamente de la interfaz.

---

## 5. Capa Data — Adapters de Salida

### 5.1 DTOs y Mappers

Los modelos de dominio son puros. Los DTOs tienen las anotaciones de Firestore:

```kotlin
// data/dto/PetReportDto.kt
data class PetReportDto(
    val ownerUid: String = "",
    val petName: String = "",
    val type: String = "",           // "LOST" / "FOUND"
    val createdAt: Timestamp? = null
    // ...
)

// data/mapper/PetReportMapper.kt
fun PetReportDto.toDomain(docId: String): PetReport = PetReport(
    id = docId,
    type = if (type == "LOST") ReportType.LOST else ReportType.FOUND,
    createdAtEpochMs = createdAt?.toDate()?.time ?: 0L
    // ...
)
```

El `Timestamp` de Firestore se convierte a `Long` epoch en el mapper. El dominio nunca ve `Timestamp`.

### 5.2 Implementaciones Firebase (Producción)

**FirebaseAuthRepository:**
- Google Sign-In via `GoogleAuthProvider.getCredential(idToken)`
- OTP Phone via `PhoneAuthProvider.verifyPhoneNumber()` con callbacks → coroutines
- Vincula cuenta de teléfono a cuenta Google existente
- `signOut()` limpia tanto Firebase Auth como Google Sign-In

**FirestoreUserRepository:**
- Escucha `firestore.collection("users").document(uid)` con `addSnapshotListener`
- Auto-crea documento en primer login
- Actualiza campos anidados: `"notificationPrefs.lostPetsNearby"`

**FirestorePetReportRepository:**
- Escucha `pet_reports` con `.whereEqualTo("type", typeName)` en tiempo real
- Upload de imágenes: `storage.reference.child("pet_reports/$uid/$uuid.jpg").putBytes(bytes)`
- Recupera `downloadUrl` y lo guarda como `imageUrl` en Firestore
- Auto-siembra 3 mascotas demo si la colección está vacía

### 5.3 Implementaciones Fake (Testing/Demo)

| Operación | Firebase | Fake |
|-----------|----------|------|
| Estado auth | Firebase AuthStateListener | `MutableStateFlow` |
| OTP | SMS real, timeout 60s | Acepta cualquier 6 dígitos |
| Usuario | Firestore real | In-memory + DataStore |
| Reportes | Firestore real | Lista hardcodeada (Max/Luna/Rocky) |
| Imágenes | Firebase Storage upload | URLs preset (placedog.net) |

### 5.4 Cambiar Firebase ↔ Fake

Un solo archivo controla todo el binding:

```kotlin
// app/di/RepositoryModule.kt
@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(
        impl: FirebaseAuthRepository   // ← cambiar a FakeAuthRepository para demo
    ): AuthRepository

    @Binds @Singleton
    abstract fun bindUserRepository(
        impl: FirestoreUserRepository  // ← cambiar a FakeUserRepository
    ): UserRepository

    @Binds @Singleton
    abstract fun bindPetReportRepository(
        impl: FirestorePetReportRepository // ← cambiar a FakePetReportRepository
    ): PetReportRepository
}
```

**Si cambiar la fuente de datos requiere tocar un ViewModel → la arquitectura está rota.** En este proyecto, solo se toca `RepositoryModule`.

---

## 6. Capa App — Adapters de Entrada

### 6.1 Pantallas y ViewModels

| Pantalla | UiState (resumen) | UiEvent (resumen) |
|----------|-------------------|-------------------|
| **Splash** | isCheckingAuth, navigateTo | ContinueWithGoogle |
| **Profile** | name, phone, email, photoUrl, isSaving | NameChanged, SaveName, ChangePhoneClicked |
| **Otp** | phoneInput, digits[6], verificationId, isVerified | PhoneChanged, SendCode, DigitChanged, Confirm |
| **Permissions** | — (stateless) | — |
| **Feed** | selectedTab, query, reports, showCreateDialog, canCreateReport | TabSelected, QueryChanged, CreateReport, DeleteReport |
| **Settings** | lostPetsNearby, foundPetsNearby, sightingsOnMyReports, showDeleteDialog | Toggle*, SignOut, DeleteAccountConfirmed |

**Regla clave:** Los ViewModels solo conocen **puertos in** (casos de uso). Nunca repositorios.

```kotlin
// app/ui/screens/feed/FeedViewModel.kt
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val observeReports: ObserveReports,   // puerto in
    private val searchReports: SearchReports,      // puerto in
    private val createReport: CreateReport,        // puerto in
    private val deleteReport: DeleteReport         // puerto in
) : ViewModel()
```

### 6.2 Flujo de Navegación

```
LAUNCH
  ↓
Splash [observeCurrentUser()]
  ├─ null        → Mostrar botón "Continuar con Google"
  │               ↓ [GoogleSignInClient launcher]
  │               ↓ [IdToken → signInWithGoogle()]
  │               ↓ Profile
  │
  ├─ phoneVerified=false → Profile → Otp → Permissions → Feed
  └─ phoneVerified=true  → Feed (onboarding saltado)

HUB PRINCIPAL (Feed)
  ├─ Tab PERDIDAS / AVISTADAS (tiempo real)
  ├─ Búsqueda por nombre, raza, descripción
  ├─ Diálogo: Crear reporte (nombre, raza, descripción, ubicación, imagen)
  └─ Drawer ─── Perfil / Configuración / Permisos

LOGOUT
  Settings → [Cerrar sesión] → auth.signOut() → observeCurrentUser emits null → Splash

ELIMINAR CUENTA
  Settings → [Eliminar cuenta] → confirm dialog
           → deleteCurrentUser() + deleteCurrentUserDocument()
           → observeCurrentUser emits null → Splash
```

### 6.3 Componentes Reutilizables

| Componente | Descripción |
|------------|-------------|
| `AppTopBar` | CenterAlignedTopAppBar con hamburgesa izq y avatar circular der |
| `AppDrawer` | ModalNavigationDrawer con items de menú |
| `DrawerShell` | Wrapper del drawer con CompositionLocal (evita prop-drilling) |
| `PetCard` | ElevatedCard con imagen, nombre, raza, chips de estado |
| `StatusChip` | Chip PERDIDO (rojo) / HALLAZGO (verde) / RECIENTE (amarillo) |
| `UserAvatar` | Avatar circular con foto o inicial del nombre |

### 6.4 Theming Material Design 3

```kotlin
// app/theme/Color.kt
val primary = Color(0xFF6750A4)          // violeta — botones principales
val primaryContainer = Color(0xFFEADDFF) // fondo suave
val surface = Color(0xFFF3E8F7)          // lila muy claro — fondo app
val error = Color(0xFFE57373)            // rojo coral — chip PERDIDO
val tertiary = Color(0xFF2E7D32)         // verde — chip HALLAZGO / CONTACTAR
val secondary = Color(0xFFF6E27A)        // amarillo — chip RECIENTE
```

- `dynamicColor = false` (colores fijos del brand)
- Ningún color hardcodeado en composables → siempre `MaterialTheme.colorScheme.*`

---

## 7. Integración Firebase — Estado Actual

### 7.1 Servicios Conectados

| Servicio | Uso en la app |
|----------|--------------|
| **Firebase Auth** | Google Sign-In + Phone OTP (SMS real) |
| **Cloud Firestore** | Usuarios (`users/`) y reportes (`pet_reports/`) |
| **Firebase Storage** | Imágenes de mascotas (`pet_reports/{uid}/{uuid}.jpg`) |

### 7.2 Colecciones Firestore

```
users/{uid}
  ├── displayName: String
  ├── email: String
  ├── phoneNumber: String       (E.164)
  ├── phoneVerified: Boolean
  ├── photoUrl: String?
  ├── createdAt: Timestamp
  └── notificationPrefs/
       ├── lostPetsNearby: Boolean
       ├── foundPetsNearby: Boolean
       └── sightingsOnMyReports: Boolean

pet_reports/{docId}
  ├── ownerUid: String
  ├── ownerInitial: String
  ├── petName: String
  ├── type: String              ("LOST" | "FOUND")
  ├── breed: String
  ├── description: String
  ├── location: String
  ├── imageUrl: String
  ├── recencyLabel: String
  └── createdAt: Timestamp
```

### 7.3 Dependencias Firebase en Gradle

```kotlin
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.auth.ktx)          // Auth + Phone
implementation(libs.firebase.firestore.ktx)      // Firestore
implementation(libs.firebase.storage.ktx)        // Cloud Storage
implementation(libs.play.services.auth)          // Google Sign-In
```

---

## 8. Estado de Implementación

| Feature | Estado | Backend |
|---------|--------|---------|
| Google Sign-In | ✅ Completo | Firebase Auth |
| Phone OTP (SMS real) | ✅ Completo | Firebase Auth |
| Perfil de usuario | ✅ Completo | Firestore |
| Feed de reportes (tiempo real) | ✅ Completo | Firestore |
| Búsqueda de reportes | ✅ Completo | Firestore (client-side) |
| Crear reporte con imagen | ✅ Completo | Firestore + Storage |
| Eliminar reporte | ✅ Completo | Firestore |
| Preferencias de notificación | ✅ Completo | Firestore |
| Cerrar sesión | ✅ Completo | Firebase Auth + Google |
| Eliminar cuenta | ✅ Completo | Firebase Auth + Firestore |
| Pantalla de permisos | ⚠️ Skeleton | — (solicitud existe, sin lógica) |
| Vista detalle de reporte | ⚠️ Stub | — (onClick existe, sin pantalla) |
| Contactar dueño | ⚠️ Stub | — (fuera del scope MVP) |
| Geolocalización / Mapas | ⚠️ Fuera de scope | — (fase futura) |

---

## 9. Calidad y Tooling

| Herramienta | Propósito |
|-------------|-----------|
| **ktlint** | Formateo de código Kotlin |
| **detekt** | Análisis estático + auto-correct |
| **Hilt** | Inyección de dependencias (testable) |
| **Turbine** | Testing de Flows de coroutines |
| **MockK** | Mocking de interfaces de repositorios |
| **Konsist** (recomendado) | Verificar que `domain/` no importa de `data/` o `app/` |

Los casos de uso son **trivialmente testeables** porque solo dependen de interfaces (puertos out), que se mockean fácilmente:

```kotlin
// Test de ObserveCurrentUserImpl
val fakeRepo = mockk<UserRepository>()
every { fakeRepo.observeCurrentUser() } returns flowOf(testUser)
val useCase = ObserveCurrentUserImpl(fakeRepo)
useCase().test { assertEquals(testUser, awaitItem()) }
```

---

## 10. Métricas del Proyecto

| Métrica | Valor |
|---------|-------|
| Archivos Kotlin | 74 |
| Modelos de dominio | 5 |
| Puertos de repositorio | 3 |
| Casos de uso | 12 |
| Pantallas | 6 |
| ViewModels | 6 |
| Servicios Firebase | 3 (Auth · Firestore · Storage) |
| Colecciones Firestore | 2 (users · pet_reports) |
| Módulos Hilt | 3 (Firebase · Repository · UseCase) |
| Componentes UI | 6 |
| Mappers | 2 |
| Features completos | ~80% del MVP |

---

## 11. Principios Demostrados

1. **Hexagonal puro:** Cambiar Firebase por Fake = 1 archivo (`RepositoryModule`). Ningún ViewModel cambia.
2. **Dominio sin frameworks:** `domain/` no contiene ni un `import com.google.*` ni `import androidx.*`.
3. **Testeabilidad:** Los 12 casos de uso son mockeable por construcción (dependen de interfaces).
4. **Reactividad:** Toda la UI reacciona a `StateFlow` — sin polling, sin callbacks en la capa de UI.
5. **Unidirección:** `UiEvent` → ViewModel → `UiState` → Compose. Sin estado mutado directamente desde la UI.

---

*Generado automáticamente por análisis estático del repositorio · MascotasPerdidas MVP · 2026*
