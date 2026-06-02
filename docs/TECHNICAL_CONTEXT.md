# TECHNICAL CONTEXT — MascotasPerdidas

> Documento técnico consolidado para Claude Code. Arquitectura hexagonal, reglas de color M3, endpoints Firebase, estado actual y pendientes. **Leer antes de cualquier modificación.**

---

## 1. Stack Tecnológico (INVIOLABLE)

| Componente | Valor |
|---|---|
| Lenguaje | Kotlin (exclusivo, sin Java) |
| UI | Jetpack Compose + Material Design 3 |
| Arquitectura | Hexagonal (Ports & Adapters) |
| Auth | Firebase Authentication (Google + Phone OTP) |
| DB | Cloud Firestore |
| Storage | Firebase Storage |
| DI | Hilt |
| Imágenes | Coil |
| Persistencia local | DataStore Preferences |
| Async | Coroutines + Flow |
| Min SDK | 26 |
| Target SDK | 35 |
| Compile SDK | 35 |
| JDK | 17 |

### Librerías PROHIBIDAS
- XML Layouts (solo Compose)
- RxJava (solo Coroutines/Flow)
- Retrofit/OkHttp directo (solo Firebase SDK)
- Glide/Picasso (solo Coil)
- Componentes UI no-M3

---

## 2. Arquitectura Hexagonal — Reglas de Dependencia

```
   ┌────────────────────────────────────────────────┐
   │                    domain/                      │
   │   (modelos puros, puertos in/out, use cases)    │
   │            ⚠ NO importa de nadie ⚠              │
   └────────────────────────────────────────────────┘
           ▲                                  ▲
           │ implementa                       │ usa
           │ (puertos out)                    │ (puertos in)
   ┌───────┴────────┐                  ┌──────┴────────┐
   │    data/       │                  │    app/        │
   │ (adapters out: │                  │ (adapters in:  │
   │  Firebase,     │                  │  Compose UI,   │
   │  Fake, Cache)  │                  │  ViewModels,   │
   └────────────────┘                  │  Navigation)   │
                                       └───────────────┘
```

### Reglas duras
1. `domain/` NO puede importar de `data/`, `app/`, `androidx.*`, `com.google.firebase.*`, ni `dagger.*`. Solo Kotlin stdlib + `kotlinx-coroutines-core` + `javax.inject`.
2. `data/` puede importar de `domain/`. NO puede importar de `app/`.
3. `app/` puede importar de `domain/`. NO debe importar de `data/` (excepto `app/di/`).
4. **ViewModels solo dependen de puertos in (use cases).** Nunca de repositorios.
5. **Use cases solo dependen de puertos out.** Nunca de Firebase/DataStore.
6. **Modelos de dominio son inmutables, sin anotaciones de frameworks.**

---

## 3. Esquema de Color — M3 Tokens (Hex Exactos)

| Token M3 | Hex | Uso |
|---|---|---|
| `primary` | `#6750A4` | Botones principales, tabs activas, títulos |
| `onPrimary` | `#FFFFFF` | Texto sobre primary |
| `primaryContainer` | `#EADDFF` | Fondo suave, avatar inicial |
| `onPrimaryContainer` | `#21005D` | Texto sobre primaryContainer |
| `secondary` | `#F6E27A` | Chip RECIENTE |
| `onSecondary` | `#3A3200` | Texto sobre secondary |
| `tertiary` | `#2E7D32` | Chip HALLAZGO, botón CONTACTAR |
| `onTertiary` | `#FFFFFF` | Texto sobre tertiary |
| `error` | `#E57373` | Chip PERDIDO, eliminar cuenta, errores |
| `onError` | `#FFFFFF` | Texto sobre error |
| `background` | `#F3E8F7` | Fondo general app (lila muy claro) |
| `onBackground` | `#1C1B1F` | Texto sobre background |
| `surface` | `#FFFFFBFE` | Cards blancas, TopAppBar |
| `onSurface` | `#1C1B1F` | Texto principal |
| `surfaceVariant` | `#E7E0EC` | Botones tonales, chips de raza |
| `onSurfaceVariant` | `#49454F` | Texto secundario |

**Reglas:**
- `dynamicColor = false` (tema fijo)
- Tema oscuro: placeholder (mismos colores que claro por ahora)
- **NUNCA hardcodear colores.** Siempre usar `MaterialTheme.colorScheme.*`
- Definidos en `app/theme/Color.kt`

---

## 4. Modelos de Dominio (`domain/model/`)

```kotlin
// domain/model/User.kt
data class User(
    val uid: String,
    val displayName: String,
    val email: String,
    val phoneNumber: String,          // E.164 format
    val phoneVerified: Boolean,
    val photoUrl: String?,
    val notificationPrefs: NotificationPrefs,
    val createdAtEpochMs: Long
)

// domain/model/NotificationPrefs.kt
data class NotificationPrefs(
    val lostPetsNearby: Boolean = false,
    val foundPetsNearby: Boolean = true,
    val sightingsOnMyReports: Boolean = true
)

// domain/model/ReportType.kt
enum class ReportType { LOST, FOUND }

// domain/model/PetReport.kt
data class PetReport(
    val id: String,
    val ownerUid: String,
    val ownerInitial: String,
    val petName: String,
    val type: ReportType,
    val breed: String,
    val description: String,
    val location: String,
    val imageUrl: String,
    val recencyLabel: String,
    val createdAtEpochMs: Long
)

// domain/model/AuthState.kt
sealed class AuthState {
    object SignedOut : AuthState()
    data class SignedIn(val uid: String, val phoneVerified: Boolean) : AuthState()
}
```

---

## 5. Puertos (Interfaces)

### 5.1 Puertos de Salida — Driven (`domain/port/out/`)

| Interfaz | Métodos |
|---|---|
| `AuthRepository` | `observeAuthState(): Flow<AuthState>`, `suspend signInWithGoogleIdToken(idToken: String)`, `suspend requestPhoneOtp(phone: String): String`, `suspend verifyPhoneOtp(verificationId: String, code: String)`, `suspend signOut()`, `suspend deleteCurrentUser()` |
| `UserRepository` | `observeCurrentUser(): Flow<User?>`, `suspend upsertUser(user: User)`, `suspend updateProfile(name: String, phone: String)`, `suspend updateNotificationPrefs(prefs: NotificationPrefs)`, `suspend deleteCurrentUserDocument()` |
| `PetReportRepository` | `observeReports(type: ReportType): Flow<List<PetReport>>`, `suspend createReport(report: PetReport, imageBytes: ByteArray?)`, `suspend deleteReport(id: String)`, `suspend searchReports(query: String, type: ReportType): List<PetReport>` |

### 5.2 Puertos de Entrada — Driving (`domain/port/in/`) — Todos `fun interface`

| Interfaz | Firma |
|---|---|
| `SignInWithGoogle` | `suspend operator fun invoke(idToken: String)` |
| `RequestPhoneOtp` | `suspend operator fun invoke(phone: String): String` |
| `VerifyPhoneOtp` | `suspend operator fun invoke(verificationId: String, code: String)` |
| `ObserveCurrentUser` | `operator fun invoke(): Flow<User?>` |
| `UpdateUserProfile` | `suspend operator fun invoke(name: String, phone: String)` |
| `UpdateNotificationPrefs` | `suspend operator fun invoke(prefs: NotificationPrefs)` |
| `SignOut` | `suspend operator fun invoke()` |
| `DeleteAccount` | `suspend operator fun invoke()` — orquesta UserRepo + AuthRepo |
| `ObserveReports` | `operator fun invoke(type: ReportType): Flow<List<PetReport>>` |
| `CreateReport` | `suspend operator fun invoke(report: PetReport, imageBytes: ByteArray?)` |
| `SearchReports` | `suspend operator fun invoke(query: String, type: ReportType): List<PetReport>` |
| `DeleteReport` | `suspend operator fun invoke(id: String)` |

---

## 6. Firebase — Estructura y Endpoints

### Proyecto
- **Project ID:** `mascotasperdidas-dda1c`
- **Package:** `com.mascotasperdidas.app`
- **google-services.json:** Existe en `app/` (NO commitear — está en .gitignore)

### Firestore Collections

```
users/{uid}
├── displayName: String
├── email: String
├── phoneNumber: String
├── phoneVerified: Boolean
├── photoUrl: String?
├── notificationPrefs: Map { lostPetsNearby, foundPetsNearby, sightingsOnMyReports }
└── createdAt: Timestamp

pet_reports/{autoId}
├── ownerUid: String
├── ownerInitial: String
├── petName: String
├── type: String ("LOST" | "FOUND")
├── breed: String
├── description: String
├── location: String
├── imageUrl: String
├── recencyLabel: String
└── createdAt: Timestamp
```

### Firebase Auth
- **Google Sign-In:** `GoogleAuthProvider.getCredential(idToken, null)` → `signInWithCredential()`
- **Phone OTP:** `PhoneAuthProvider.verifyPhoneNumber()` → `PhoneAuthCredential` → `linkWithCredential()` (sobre usuario actual de Google)
- **AuthStateListener:** `callbackFlow` emite `AuthState.SignedIn` / `AuthState.SignedOut`
- **Sign Out:** `firebaseAuth.signOut()` + `googleSignInClient.signOut()`
- **Delete Account:** `deleteCurrentUserDocument()` (Firestore) → `currentUser.delete()`

### Firebase Storage
- **Path:** `pet_reports/{uid}/{UUID}.jpg`
- **Upload:** Al crear reporte con `imageBytes`, se sube y se obtiene `downloadUrl`

### Seed Data (FirestorePetReportRepository — se crea automático si colección vacía)
1. **Max** — LOST, Golden Retriever, Parque Simón Bolívar, `placedog.net/400/300?id=1`
2. **Luna** — FOUND, Siamés, CC Andino, `placekitten.com/400/300`
3. **Rocky** — LOST, Bulldog Francés, Zona Rosa, `placedog.net/400/300?id=3`

---

## 7. DI — Hilt Wiring

### `RepositoryModule` (`@InstallIn(SingletonComponent)`)
```kotlin
@Binds @Singleton AuthRepository -> FirebaseAuthRepository
@Binds @Singleton UserRepository -> FirestoreUserRepository
@Binds @Singleton PetReportRepository -> FirestorePetReportRepository
```
> **Para usar Fakes en vez de Firebase:** cambiar estos bindings a `FakeAuthRepository`, `FakeUserRepository`, `FakePetReportRepository`.

### `UseCaseModule` (`@InstallIn(SingletonComponent)`)
- 12 binds: cada puerto in → su implementación (`*Impl`)

### `FirebaseModule` (`@InstallIn(SingletonComponent)`)
- `@Provides @Singleton`: `FirebaseAuth`, `FirebaseFirestore`, `FirebaseStorage`

---

## 8. Navegación — Flujo Completo

| Ruta | Pantalla | Drawer | Lógica |
|---|---|---|---|
| `Routes.Splash` | SplashScreen | ❌ | Observa auth. Si `phoneVerified` → Feed. Si no → botón Google. Login → Profile o Feed |
| `Routes.Profile` | ProfileScreen | ✅ | Editar nombre. "Cambiar número" → OTP. Avatar (sin acción) |
| `Routes.Otp` | OtpScreen | ❌ | 2 pasos: ingresar teléfono → enviar código → verificar → Permissions |
| `Routes.Permissions` | PermissionsScreen | ✅ | Lista 4 permisos (UI estática, sin lógica real). "Continuar" → Feed |
| `Routes.Feed` | FeedScreen | ✅ | SearchBar + TabRow(Perdidas/Avistadas) + PetCards + FAB crear. Dialog creación. Eliminar (owner) |
| `Routes.Settings` | SettingsScreen | ✅ | 3 checkboxes notificaciones. Gestionar permisos. Cerrar sesión. Eliminar cuenta (con confirmación) |

### Drawer Items
- Feed, Perfil, Configuración, Permisos, Cerrar sesión (rojo)

---

## 9. Componentes Composable Reutilizables (`app/ui/components/`)

| Componente | Archivo | Descripción |
|---|---|---|
| `DrawerShell` | `DrawerShell.kt` | `ModalNavigationDrawer` + `CompositionLocalProvider` para `LocalDrawerOpener`/`LocalDrawerCloser` |
| `AppDrawerContent` | `AppDrawer.kt` | Items de navegación del drawer |
| `AppTopBar` | `AppTopBar.kt` | `CenterAlignedTopAppBar` con hamburguesa izq, título centro, avatar der |
| `UserAvatar` | `UserAvatar.kt` | Círculo con inicial o foto (Coil). Color `primaryContainer`. Tamaño configurable |
| `StatusChip` | `StatusChip.kt` | `AssistChip` — rojo `error` para LOST, verde `tertiary` para FOUND |
| `PetCard` | `PetCard.kt` | `ElevatedCard` completo: avatar+nombre+ubicación, imagen 180dp (Coil), chips, descripción (max 3 líneas), "Más Información" (tonal), "CONTACTAR" (filled, tertiary). Si es owner: dropdown "Eliminar" |

---

## 10. ViewModels — Consumo de Puertos

| ViewModel | Puertos In que consume |
|---|---|
| `SplashViewModel` | `ObserveCurrentUser`, `SignInWithGoogle` |
| `ProfileViewModel` | `ObserveCurrentUser`, `UpdateUserProfile` |
| `OtpViewModel` | `ObserveCurrentUser`, `RequestPhoneOtp`, `VerifyPhoneOtp`, `UpdateUserProfile` |
| `PermissionsViewModel` | Ninguno (placeholder vacío) |
| `SettingsViewModel` | `ObserveCurrentUser`, `UpdateNotificationPrefs`, `SignOut`, `DeleteAccount` |
| `FeedViewModel` | `ObserveReports`, `SearchReports`, `CreateReport`, `DeleteReport`, `ObserveCurrentUser` |

---

## 11. Implementaciones — Estado Actual

### ✅ IMPLEMENTADO (MVP completo)
- Arquitectura hexagonal completa (domain + data + app)
- Modelos de dominio puros
- 3 puertos out + 12 puertos in + 12 use cases
- DTOs + Mappers (Firestore ↔ Domain)
- Firebase adapters: Auth, Firestore, Storage (con seed data automático)
- Fake adapters: Auth, User, PetReport (User persiste en DataStore)
- Tema M3 completo con esquema de color lila/violeta
- Navegación completa con transiciones condicionales
- Drawer shell con ModalNavigationDrawer
- 6 pantallas completas con UiState + UiEvent
- CRUD de reportes (crear con imagen preset, eliminar como owner, search, tabs)
- Settings con persistencia de notificaciones, sign out, delete account
- DI Hilt completo
- Strings en español (79 keys)
- Logo vectorial (huella), icono Google, logo PNG para splash
- AndroidManifest con todos los permisos declarados
- ProGuard keep rules para domain
- Detekt configurado

### ❌ PENDIENTE / FALTA
1. **Tests unitarios** — Dependencias declaradas (JUnit, MockK, Turbine) pero sin archivos de test
2. **Permisos runtime reales** — `PermissionsScreen` es UI estática. `PermissionsViewModel` vacío. No se invoca `ActivityResultContracts.RequestMultiplePermissions`
3. **Avatar en Profile** — Botón no implementado ("Fase 13")
4. **ReportClicked / ContactClicked** — Stubs vacíos, no navegan a detalle ni abren contacto
5. **Dark theme real** — Placeholder (mismos colores que claro)
6. **ktlint/detekt CI** — Configurados pero sin ejecución automática
7. **Mapas/Geolocalización** — Fuera de scope MVP
8. **Chat** — Fuera de scope MVP
9. **Subida de imagen real (cámara/galería)** — Solo imágenes preset por ahora

---

## 12. Recursos (`res/`)

| Archivo | Contenido |
|---|---|
| `values/strings.xml` | 79 strings en español |
| `values/themes.xml` | `Theme.MascotasPerdidas` → `android:Theme.Material.Light.NoActionBar` |
| `drawable/ic_google.xml` | Vector G de Google, 24dp |
| `drawable/ic_logo.xml` | Vector huella de pata, `#6750A4`, 120dp |
| `mipmap-*/ic_launcher*` | Launcher icons (generados) |
| `drawable/logo_msp.png` | Logo PNG para SplashScreen |

---

## 13. Build Configuration

| Archivo | Detalle |
|---|---|
| `build.gradle.kts` (root) | Plugins: AGP, Kotlin, Compose, Hilt, KSP, Google Services, Detekt |
| `app/build.gradle.kts` | compileSdk=35, minSdk=26, targetSdk=35, JDK 17, Compose enabled. Firebase BOM 33.5.1, Hilt 2.51.1, Coil 2.7.0 |
| `settings.gradle.kts` | `rootProject.name = "MascotasPerdidas"`, `include(":app")` |
| `gradle.properties` | `configuration-cache=true`, `useAndroidX=true` |
| `gradle/libs.versions.toml` | Version catalog completo |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 8.9 |
| `detekt.yml` | Configuración detekt, maxIssues=0, excluye build/ |

---

## 14. Reglas para Claude Code (Sesiones Futuras)

1. **Leer este documento antes de cualquier cambio.**
2. **No introducir librerías nuevas** sin documentar aquí.
3. **No violar la regla de dependencias hexagonal (§2).** Si una clase en `domain/` necesita importar de Android o Firebase, detenerse y reconsiderar.
4. **Los ViewModels NO conocen repositorios.** Solo casos de uso (puertos in).
5. **Los casos de uso NO conocen Firebase.** Solo repositorios (puertos out).
6. **Nunca hardcodear colores ni strings.** Usar `MaterialTheme.colorScheme.*` y `stringResource()`.
7. **Identificadores en inglés, strings de UI en español.**
8. **Composables stateless:** `Screen(state: UiState, onEvent: (UiEvent) -> Unit)` + wrapper con ViewModel.
9. **`UiState` + `UiEvent` por pantalla** (data class / sealed class).
10. **Manejo de errores:** capturar excepciones, emitir estado de error visible al usuario.
11. **Logs:** nunca imprimir tokens, UIDs ni datos personales.
12. **Para cambiar Firebase ↔ Fake:** solo modificar `RepositoryModule.kt`. No tocar ViewModels ni use cases.
13. **Eliminación de cuenta:** borrar documento Firestore PRIMERO, luego cuenta Auth.
14. **No implementar mapas, geolocalización ni chat en esta fase.**

---

## 15. Archivos Clave por Capa

```
app/src/main/kotlin/com/mascotasperdidas/app/
├── domain/
│   ├── model/        → User, PetReport, ReportType, NotificationPrefs, AuthState
│   ├── port/in/      → 12 interfaces de casos de uso
│   ├── port/out/     → 3 interfaces de repositorio
│   └── usecase/      → 12 implementaciones de casos de uso
├── data/
│   ├── dto/          → UserDto, PetReportDto, NotificationPrefsDto
│   ├── mapper/       → UserMapper, PetReportMapper (toDomain/toDto)
│   ├── firebase/     → FirebaseAuthRepository, FirestoreUserRepository, FirestorePetReportRepository
│   ├── fake/         → FakeAuthRepository, FakeUserRepository, FakePetReportRepository
│   └── datastore/    → PrefsDataStore
└── app/
    ├── di/           → RepositoryModule, UseCaseModule, FirebaseModule
    ├── theme/        → Color.kt, Type.kt, Theme.kt
    ├── navigation/   → Routes.kt, AppNavHost.kt
    ├── ui/
    │   ├── components/  → AppTopBar, AppDrawer, DrawerShell, UserAvatar, StatusChip, PetCard
    │   └── screens/
    │       ├── splash/    → Screen, ViewModel, UiState, UiEvent
    │       ├── profile/   → Screen, ViewModel, UiState, UiEvent
    │       ├── otp/       → Screen, ViewModel, UiState, UiEvent
    │       ├── permissions/ → Screen, ViewModel
    │       ├── feed/      → Screen, ViewModel, UiState, UiEvent
    │       └── settings/  → Screen, ViewModel, UiState, UiEvent
    ├── util/          → PermissionUtils
    ├── MascotasPerdidasApp.kt
    └── MainActivity.kt
```

---

*Última actualización: Junio 2026 — MVP completo, cableado a Firebase real, pendiente tests y permisos runtime.*
