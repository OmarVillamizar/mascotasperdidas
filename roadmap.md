# Roadmap — MascotasPerdidas

> Plan de ejecución secuencial para Claude Code. **Seguir las fases en orden.** No avanzar a la siguiente fase hasta que la anterior compile, corra y haya sido validada visualmente contra los mockups.
>
> **Arquitectura: Hexagonal (Ports & Adapters).** Cada fase respeta la regla de dependencias definida en `CLAUDE.md` §4.2: `domain/` no importa de `data/` ni `app/`; los ViewModels solo dependen de puertos in (use cases); los use cases solo dependen de puertos out (interfaces de repositorio).
>
> **El login real con Firebase es la ÚLTIMA fase.** Hasta entonces, los puertos out están bindeados a implementaciones Fake en `data/fake/`. Cambiar a Firebase = cambiar el binding en `RepositoryModule`, **sin tocar dominio ni ViewModels**.

---

## Fase 0 — Bootstrap del proyecto

**Objetivo:** proyecto Android vacío compilable con el stack obligatorio y los **3 paquetes raíz** (`domain/`, `data/`, `app/`).

### Tareas

1. Abrir Android Studio Panda 2 \| 2025.3.2 → New Project → **Empty Activity (Compose)**.
2. Configurar:
   - Package raíz: `com.mascotasperdidas.app`.
   - Language: **Kotlin**.
   - Minimum SDK: **API 26**.
   - Build configuration: **Kotlin DSL (`.kts`)**.
3. Actualizar `build.gradle.kts` (Module: app) con las dependencias listadas en `CLAUDE.md` §2.1.
4. Aplicar plugins: `kotlin-kapt` o `ksp`, `dagger.hilt.android.plugin`, `kotlin-parcelize`.
5. Crear `app/MascotasPerdidasApp.kt : Application` con `@HiltAndroidApp`. Registrar en `AndroidManifest.xml`.
6. **Crear los 3 paquetes raíz vacíos** con `.gitkeep`:
   - `com.mascotasperdidas.app.domain`
   - `com.mascotasperdidas.app.data`
   - `com.mascotasperdidas.app.app`
7. Dentro de cada uno, crear los subpaquetes según `CLAUDE.md` §4.4.
8. Configurar **ktlint** y **detekt**.
9. Verificar que **compila** y muestra el "Hello Android" por defecto.

**Definition of done:** el proyecto compila, se instala en emulador, abre una pantalla vacía. Los 3 paquetes raíz están creados.

---

## Fase 1 — Dominio: modelos y puertos

**Objetivo:** definir el hexágono completo en `domain/` **sin ninguna implementación todavía**. Este es el corazón del proyecto.

> ⚠ **Regla:** todo el código de esta fase debe estar en `domain/`. **No** se importa de `androidx.*`, `com.google.firebase.*`, ni `dagger.*`. Solo `kotlin.*`, `kotlinx.coroutines.flow.*` y `javax.inject.*`.

### Tareas

1. **Modelos** en `domain/model/`:
   - `User.kt`
   - `NotificationPrefs.kt`
   - `ReportType.kt` (enum)
   - `PetReport.kt`

   (Ver definiciones en `CLAUDE.md` §5.1.)

2. **Puertos out** en `domain/port/out/` (interfaces que el dominio necesita):
   - `AuthRepository.kt`:
     ```kotlin
     interface AuthRepository {
         fun observeAuthState(): Flow<AuthState>            // AuthState: sealed class
         suspend fun signInWithGoogleIdToken(idToken: String)
         suspend fun requestPhoneOtp(phone: String): String // devuelve verificationId
         suspend fun verifyPhoneOtp(verificationId: String, code: String)
         suspend fun signOut()
         suspend fun deleteCurrentUser()
     }
     ```
     Definir `sealed class AuthState { object SignedOut; data class SignedIn(val uid: String, val phoneVerified: Boolean) }` en el mismo paquete o en `domain/model/`.

   - `UserRepository.kt`:
     ```kotlin
     interface UserRepository {
         fun observeCurrentUser(): Flow<User?>
         suspend fun upsertUser(user: User)
         suspend fun updateProfile(name: String, phone: String)
         suspend fun updateNotificationPrefs(prefs: NotificationPrefs)
         suspend fun deleteCurrentUserDocument()
     }
     ```

   - `PetReportRepository.kt`:
     ```kotlin
     interface PetReportRepository {
         fun observeReports(type: ReportType): Flow<List<PetReport>>
         suspend fun createReport(report: PetReport, imageBytes: ByteArray?)
         suspend fun searchReports(query: String, type: ReportType): List<PetReport>
     }
     ```

3. **Puertos in** en `domain/port/in/` (interfaces de casos de uso) — uno por archivo, usando `fun interface` cuando sea posible:
   - `SignInWithGoogle.kt` → `suspend operator fun invoke(idToken: String)`
   - `RequestPhoneOtp.kt` → `suspend operator fun invoke(phone: String): String`
   - `VerifyPhoneOtp.kt` → `suspend operator fun invoke(verificationId: String, code: String)`
   - `ObserveCurrentUser.kt` → `operator fun invoke(): Flow<User?>`
   - `UpdateUserProfile.kt` → `suspend operator fun invoke(name: String, phone: String)`
   - `UpdateNotificationPrefs.kt` → `suspend operator fun invoke(prefs: NotificationPrefs)`
   - `SignOut.kt` → `suspend operator fun invoke()`
   - `DeleteAccount.kt` → `suspend operator fun invoke()`
   - `ObserveReports.kt` → `operator fun invoke(type: ReportType): Flow<List<PetReport>>`
   - `CreateReport.kt` → `suspend operator fun invoke(report: PetReport, imageBytes: ByteArray?)`
   - `SearchReports.kt` → `suspend operator fun invoke(query: String, type: ReportType): List<PetReport>`

4. **Implementaciones de casos de uso** en `domain/usecase/`. Cada una recibe los puertos out que necesita por constructor con `@Inject`. Ejemplo:
   ```kotlin
   class ObserveReportsImpl @Inject constructor(
       private val petReportRepository: PetReportRepository
   ) : ObserveReports {
       override fun invoke(type: ReportType) = petReportRepository.observeReports(type)
   }
   ```

5. Verificar con un test de compilación que **ningún archivo en `domain/`** importa de `data/`, `app/` o paquetes de Android.

**Definition of done:** el dominio compila por sí solo (haciendo un módulo Gradle aparte sería ideal, pero por simplicidad inicial vive en el mismo módulo). Todas las interfaces y use cases están definidos.

---

## Fase 2 — Tema Material 3 y configuración UI

**Objetivo:** definir el sistema visual.

### Tareas

1. Crear `app/theme/Color.kt` con los tokens del mockup (`CLAUDE.md` §3.3).
2. Crear `app/theme/Type.kt` con la `Typography` por defecto.
3. Crear `app/theme/Theme.kt`:
   - `MascotasPerdidasTheme` composable.
   - `lightColorScheme(...)` con los colores del mockup.
   - `darkColorScheme(...)` (placeholder).
   - `dynamicColor = false`.
4. Envolver `MainActivity.setContent { }` con `MascotasPerdidasTheme`.
5. Crear `strings.xml` con todos los textos visibles de los mockups (en español).
6. Crear assets:
   - Logo placeholder vectorial (`res/drawable/ic_logo.xml`).
   - Ícono Google (`ic_google.xml` o desde Play Services).

**Definition of done:** se puede previsualizar un Composable simple (texto + botón) con los colores correctos.

---

## Fase 3 — Adapters Fake (data/fake/)

**Objetivo:** implementaciones in-memory de los puertos out para poder construir UI sin depender de Firebase.

### Tareas

1. `data/fake/FakeAuthRepository.kt` → implementa `AuthRepository`:
   - Estado interno con un `MutableStateFlow<AuthState>`.
   - `signInWithGoogleIdToken`: simula con un delay y emite `AuthState.SignedIn(uid = "fake-uid", phoneVerified = false)`.
   - `requestPhoneOtp`: devuelve un `verificationId` mock.
   - `verifyPhoneOtp`: acepta cualquier código de 6 dígitos, marca `phoneVerified = true`.
   - `signOut` / `deleteCurrentUser` resetean el estado.

2. `data/fake/FakeUserRepository.kt` → implementa `UserRepository`:
   - `MutableStateFlow<User?>` interno.
   - Persistencia opcional en memoria por ahora; en Fase 11 se conecta a DataStore.

3. `data/fake/FakePetReportRepository.kt` → implementa `PetReportRepository`:
   - Lista hardcodeada con los 2 reportes de los mockups (MAX el golden, gato siamés).
   - `observeReports(type)` devuelve un `Flow` filtrado por `type`.
   - `searchReports` filtra en memoria por `petName`, `breed`, `description`.

4. **Wiring Hilt** en `app/di/RepositoryModule.kt`:
   ```kotlin
   @Module
   @InstallIn(SingletonComponent::class)
   abstract class RepositoryModule {
       @Binds @Singleton
       abstract fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository
       @Binds @Singleton
       abstract fun bindUserRepository(impl: FakeUserRepository): UserRepository
       @Binds @Singleton
       abstract fun bindPetReportRepository(impl: FakePetReportRepository): PetReportRepository
   }
   ```

5. **Wiring Hilt** en `app/di/UseCaseModule.kt`:
   ```kotlin
   @Module
   @InstallIn(SingletonComponent::class)
   abstract class UseCaseModule {
       @Binds abstract fun bindObserveReports(impl: ObserveReportsImpl): ObserveReports
       // ... uno por cada puerto in
   }
   ```

**Definition of done:** Hilt resuelve la inyección completa. Un test simple instancia un use case y verifica que devuelve datos del fake.

---

## Fase 4 — Navegación esqueleto

**Objetivo:** estructura de navegación con todas las rutas, cada una mostrando un placeholder.

### Tareas

1. Crear `app/navigation/Routes.kt` con las rutas:
   - `Splash`, `Profile`, `Otp`, `Permissions`, `Feed`, `Settings`.
2. Crear `app/navigation/AppNavHost.kt`:
   - `NavHost` con `startDestination = Routes.Splash`.
   - Cada destino muestra un `Text("Pantalla X")` por ahora.
3. Conectar `AppNavHost` desde `MainActivity`.
4. Probar navegación manual con botones temporales.

**Definition of done:** se puede navegar por las 6 rutas desde la app.

---

## Fase 5 — Componentes reutilizables

**Objetivo:** building blocks visuales de M3.

### Tareas

1. **`AppTopBar.kt`** (`CenterAlignedTopAppBar`):
   - Slot izq: hamburguesa → abre drawer.
   - Título centrado.
   - Slot der: avatar circular del usuario.
2. **`AppDrawer.kt`** (`ModalNavigationDrawer`):
   - Items: Feed, Perfil, Configuración, Permisos, Cerrar sesión.
3. **`UserAvatar.kt`**: círculo con inicial o `AsyncImage` (Coil).
4. **`StatusChip.kt`**: recibe un `ReportType` y devuelve el chip con color correcto.
5. **`PetCard.kt`** (`ElevatedCard`): recibe un `PetReport` y dibuja:
   - Header: avatar + nombre/título + ubicación + `IconButton` con `MoreVert`.
   - `AsyncImage` (Coil, esquinas redondeadas).
   - Fila de chips: `StatusChip` + `AssistChip(breed)` + `AssistChip(recency)`.
   - Texto descripción.
   - Fila de botones: `FilledTonalButton("Mas Información")` + `Button("CONTACTAR")` verde.
6. **Previews** con datos mock para cada componente.

**Definition of done:** las previews muestran los componentes idénticos al mockup.

---

## Fase 6 — Pantalla Splash (mockup #1)

**Objetivo:** primera pantalla.

### Tareas

1. `app/ui/screens/splash/SplashUiState.kt` — `data class SplashUiState(val isCheckingAuth: Boolean = true)`.
2. `app/ui/screens/splash/SplashUiEvent.kt` — `sealed class SplashUiEvent { object ContinueWithGoogle }`.
3. `SplashScreen.kt`:
   - Fondo `surface` lila.
   - Logo grande centrado.
   - Título "Mascotas Perdidas" en `headlineLarge` color `primary`.
   - Subtítulo "Reporta desapariciones o hallazgos de mascotas perdidas".
   - Botón "Continuar con Google" → emite `ContinueWithGoogle`.
   - Texto fino "Al continuar, aceptas los términos y condiciones".
4. `SplashViewModel.kt`:
   - Inyecta `ObserveCurrentUser` (puerto in).
   - **NO** inyecta `AuthRepository` directamente.
   - Por ahora, al recibir `ContinueWithGoogle`, navega a Profile (en Fase 12 se conectará con `SignInWithGoogle`).

**Definition of done:** pantalla idéntica al mockup #1. Al pulsar Google navega a Perfil.

---

## Fase 7 — Pantalla Perfil (mockup #2)

**Objetivo:** edición de datos del usuario.

### Tareas

1. `ProfileUiState.kt` — `data class ProfileUiState(val name: String, val phone: String, val isSavingName: Boolean, val canChangePhone: Boolean, val error: String?)`.
2. `ProfileUiEvent.kt` — eventos: `NameChanged(String)`, `SaveName`, `ChangePhoneClicked`.
3. `ProfileScreen.kt`:
   - `Scaffold` con `AppTopBar(title = "Perfil")`.
   - `ElevatedCard` con título "Mis datos".
   - `OutlinedTextField("Nombre")` + `Button("Guardar")` con ícono check.
   - `OutlinedTextField("Teléfono")` solo lectura + botón "Cambiar número telefónico".
4. `ProfileViewModel.kt`:
   - Inyecta `ObserveCurrentUser` y `UpdateUserProfile` (puertos in).
   - `viewModelScope.launch { observeCurrentUser().collect { ... } }` para llenar el state.
   - `onEvent(SaveName)` → llama `updateUserProfile(name, phoneActual)`.
   - `onEvent(ChangePhoneClicked)` → navega a Otp.

**Definition of done:** se puede editar nombre, guardar, y el botón de cambiar teléfono navega a Otp. Cambios persisten vía `FakeUserRepository`.

---

## Fase 8 — Pantalla OTP (mockup #5)

**Objetivo:** UI del código SMS de 6 dígitos.

### Tareas

1. `OtpUiState.kt` — `data class OtpUiState(val digits: List<String> = List(6) { "" }, val verificationId: String? = null, val isVerifying: Boolean, val error: String?)`.
2. `OtpUiEvent.kt` — `DigitChanged(index, value)`, `Confirm`.
3. `OtpScreen.kt`:
   - Título "Verificación de Teléfono Celular" en `displaySmall`.
   - Subtítulo "Enviamos un código de 6 dígitos por SMS al teléfono que digitaste".
   - 6 `OutlinedTextField` cuadrados con auto-focus.
   - Botón "Confirmar" habilitado solo con 6 dígitos.
4. `OtpViewModel.kt`:
   - Inyecta `RequestPhoneOtp` y `VerifyPhoneOtp` (puertos in).
   - En `init`, llama `requestPhoneOtp(phoneActual)` y guarda el `verificationId`.
   - `onEvent(Confirm)` → llama `verifyPhoneOtp(verificationId, code)`. En caso de éxito navega a Permissions.

**Definition of done:** se ingresan 6 dígitos, el botón se habilita, al confirmar el `FakeAuthRepository` acepta y se navega a Permisos.

---

## Fase 9 — Pantalla Permisos (mockup #4)

**Objetivo:** gestión de permisos del sistema.

### Tareas

1. `PermissionsScreen.kt`:
   - `Scaffold` con `AppTopBar(title = "Permisos")`.
   - `ElevatedCard` con título "Gestionar mis permisos" + descripción.
   - Lista de 4 items con ícono + label + `Icons.ChevronRight`:
     - Cámara, Notificaciones, Almacenamiento, Ubicación.
2. `app/util/PermissionUtils.kt`: helpers para chequear/solicitar permisos en Android 13+.
3. Permisos: `CAMERA`, `POST_NOTIFICATIONS` (API 33+), `READ_MEDIA_IMAGES` (API 33+) / `READ_EXTERNAL_STORAGE`, `ACCESS_FINE_LOCATION`.
4. `PermissionsViewModel.kt`: no necesita puertos in (los permisos son tema Android, vivien en `app/`).
5. Tras un primer pase, ofrecer botón "Continuar" → Feed.

**Definition of done:** se solicitan los 4 permisos y al terminar se ve el Feed.

---

## Fase 10 — Pantalla Configuración (mockup #3)

**Objetivo:** preferencias del usuario.

### Tareas

1. `SettingsUiState.kt` — espejo de `NotificationPrefs` + `isLoading`, `error`.
2. `SettingsUiEvent.kt` — `ToggleLostNearby`, `ToggleFoundNearby`, `ToggleSightings`, `ManagePermissions`, `SignOut`, `DeleteAccountConfirmed`.
3. `SettingsScreen.kt`:
   - Sección **Notificaciones** con 3 `Checkbox`:
     - Mascotas perdidas cerca de mi ubicación.
     - Mascotas halladas cerca de mi ubicación.
     - Avistamientos recibidos de mis reportes de mascota perdida.
   - Sección **Permisos**: botón "Gestionar Permisos" → navega a Permissions.
   - Sección **Mi cuenta**: "Cerrar Sesión" + "Eliminar cuenta" (color `error`).
   - `AlertDialog` de confirmación para eliminar.
4. `SettingsViewModel.kt`:
   - Inyecta `ObserveCurrentUser`, `UpdateNotificationPrefs`, `SignOut`, `DeleteAccount` (puertos in).
   - Cada toggle llama `updateNotificationPrefs(...)`.
   - `SignOut` → `signOut()` y navega a Splash.
   - `DeleteAccountConfirmed` → `deleteAccount()` y navega a Splash.

**Definition of done:** toggles persisten (vía `FakeUserRepository`), dialog de eliminar aparece y al confirmar simula el borrado.

---

## Fase 11 — Pantalla Feed (mockups #6, #7, #8)

**Objetivo:** pantalla principal con buscador, tabs y lista.

### Tareas

1. `FeedUiState.kt` — `data class FeedUiState(val selectedTab: ReportType, val query: String, val reports: List<PetReport>, val isLoading: Boolean)`.
2. `FeedUiEvent.kt` — `TabSelected(ReportType)`, `QueryChanged(String)`, `ReportClicked(id)`, `ContactClicked(id)`.
3. `FeedScreen.kt`:
   - `Scaffold` con `AppTopBar(title = "MascotasPerdidas")`.
   - `SearchBar` M3 con placeholder "Nombre, raza, descripción, etc...".
   - Banner amarillo opcional "Filtrando por: Hallazgos" cuando `selectedTab == FOUND` y se viene de un filtro explícito.
   - `TabRow` con 2 tabs: **Perdidas** / **Avistadas**.
   - `LazyColumn` con `PetCard`s.
4. `FeedViewModel.kt`:
   - Inyecta `ObserveReports` y `SearchReports` (puertos in).
   - Mantiene `combine(selectedTab, query) { ... }` para reaccionar.
   - Cuando `query` está vacío → `observeReports(selectedTab)`.
   - Cuando `query` tiene texto → `searchReports(query, selectedTab)`.

**Definition of done:** ambos tabs muestran las cards del fake correctamente, el buscador filtra, la UI replica los mockups #6, #7, #8.

---

## Fase 12 — Persistencia local con DataStore

**Objetivo:** que `FakeUserRepository` use DataStore en lugar de solo memoria, para que la sesión sobreviva reinicios.

### Tareas

1. `data/datastore/PrefsDataStore.kt`:
   - Wrap de `DataStore<Preferences>` para guardar `displayName`, `phone`, `notificationPrefs`.
2. Refactorizar `FakeUserRepository` para leer/escribir a `PrefsDataStore`.
3. Verificar que cerrar y abrir la app preserva el estado del usuario.

**Definition of done:** se mantiene la sesión y los datos tras reiniciar la app, usando el `FakeUserRepository` con backend DataStore.

---

## Fase 13 — Drawer y navegación final

**Objetivo:** drawer cableado y flujo end-to-end.

### Tareas

1. Envolver pantallas autenticadas con `ModalNavigationDrawer` + `AppDrawer`.
2. Transiciones: Feed ↔ Perfil ↔ Configuración ↔ Permisos.
3. Cerrar sesión en Configuración → Splash limpiando back stack (`popUpTo(Splash) { inclusive = true }`).
4. Splash decide la pantalla destino según `ObserveCurrentUser`:
   - `null` → Splash visible con botón Google.
   - `User` con `phoneVerified = false` → Profile.
   - `User` con `phoneVerified = true` → Feed.

**Definition of done:** flujo completo end-to-end con fakes. Cerrar app y reabrir lleva al estado correcto.

---

## Fase 14 — Firebase: configuración (ÚLTIMA fase)

**Objetivo:** integrar Firebase Auth, Firestore y Storage. Reemplazar Fakes por implementaciones Firebase **sin tocar dominio ni ViewModels**.

> **Esta es la única fase donde se toca código nuevo en `data/firebase/` y se cambia el binding en `app/di/RepositoryModule.kt`.**

### 14.1 Setup

1. Crear proyecto en [Firebase Console](https://console.firebase.google.com/).
2. Agregar app Android con el `applicationId` del proyecto.
3. Descargar `google-services.json` → `app/google-services.json`. Agregar a `.gitignore`.
4. Aplicar plugin `com.google.gms.google-services`.
5. Habilitar en Console:
   - **Authentication** → providers: **Google** + **Phone**.
   - **Firestore Database** → modo producción.
   - **Storage**.
6. Configurar SHA-1 y SHA-256 del keystore de debug en Console.
7. Verificar dependencias del BOM de Firebase (ya listadas en `CLAUDE.md` §2.1).

### 14.2 DTOs y Mappers

1. `data/dto/UserDto.kt`:
   ```kotlin
   data class UserDto(
       val uid: String = "",
       val displayName: String = "",
       val email: String = "",
       val phoneNumber: String = "",
       val phoneVerified: Boolean = false,
       val photoUrl: String? = null,
       val notificationPrefs: NotificationPrefsDto = NotificationPrefsDto(),
       val createdAt: Timestamp? = null
   )
   ```
2. `data/dto/NotificationPrefsDto.kt`, `data/dto/PetReportDto.kt` análogos.
3. `data/mapper/UserMapper.kt`:
   ```kotlin
   fun UserDto.toDomain(): User = User(
       uid = uid,
       displayName = displayName,
       // ...
       createdAtEpochMs = createdAt?.toDate()?.time ?: 0L
   )
   fun User.toDto(): UserDto = ...
   ```
4. `data/mapper/PetReportMapper.kt` análogo, con conversión `ReportType` ↔ `String` ("LOST"/"FOUND").

### 14.3 `FirebaseAuthRepository`

Implementa `AuthRepository` (puerto out definido en `domain/`).

1. Google Sign-In:
   - Usar **Credential Manager API** (recomendado en 2025+).
   - `signInWithGoogleIdToken(idToken)`:
     ```kotlin
     val credential = GoogleAuthProvider.getCredential(idToken, null)
     auth.signInWithCredential(credential).await()
     ```
2. Phone OTP:
   - `requestPhoneOtp(phone)`: `PhoneAuthProvider.verifyPhoneNumber(...)` y devolver `verificationId` desde `onCodeSent`.
   - `verifyPhoneOtp(verificationId, code)`:
     ```kotlin
     val credential = PhoneAuthProvider.getCredential(verificationId, code)
     auth.currentUser!!.linkWithCredential(credential).await()
     ```
3. `observeAuthState()`: callbackFlow sobre `auth.addAuthStateListener`.
4. `signOut()`, `deleteCurrentUser()`.

### 14.4 `FirestoreUserRepository`

Implementa `UserRepository`.

1. `observeCurrentUser()`: snapshot listener sobre `users/{uid}` → `Flow<User?>` (usando mapper `toDomain()`).
2. `upsertUser`, `updateProfile`, `updateNotificationPrefs`, `deleteCurrentUserDocument`.

### 14.5 `FirestorePetReportRepository`

Implementa `PetReportRepository`.

1. `observeReports(type)`: query `pet_reports.whereEqualTo("type", type.name).orderBy("createdAt", DESCENDING)`.
2. `createReport(report, imageBytes)`:
   - Si `imageBytes != null`, subir a `Firebase Storage` en `pet_reports/{uid}/{uuid}.jpg`.
   - Obtener `downloadUrl`.
   - Guardar documento con el URL.
3. `searchReports(query, type)`: filtrar client-side por ahora (Firestore no soporta full-text nativamente).

### 14.6 Security Rules

`firestore.rules`:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read: if request.auth != null && request.auth.uid == uid;
      allow create, update, delete: if request.auth != null && request.auth.uid == uid;
    }
    match /pet_reports/{reportId} {
      allow read: if true;
      allow create: if request.auth != null && request.resource.data.ownerUid == request.auth.uid;
      allow update, delete: if request.auth != null && resource.data.ownerUid == request.auth.uid;
    }
  }
}
```

`storage.rules`:
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /pet_reports/{userId}/{file=**} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### 14.7 Cambio de binding Hilt

En `app/di/RepositoryModule.kt`, **solo cambian las líneas de `@Binds`**:

```kotlin
// ANTES (fases 3-13):
@Binds @Singleton abstract fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository
@Binds @Singleton abstract fun bindUserRepository(impl: FakeUserRepository): UserRepository
@Binds @Singleton abstract fun bindPetReportRepository(impl: FakePetReportRepository): PetReportRepository

// DESPUÉS (fase 14):
@Binds @Singleton abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository
@Binds @Singleton abstract fun bindUserRepository(impl: FirestoreUserRepository): UserRepository
@Binds @Singleton abstract fun bindPetReportRepository(impl: FirestorePetReportRepository): PetReportRepository
```

> **Nada más cambia.** Ni dominio, ni ViewModels, ni Compose. Esta es la prueba de que la arquitectura hexagonal funcionó.

### 14.8 Validación final

- Splash → Login Google → Profile → OTP real → Permissions → Feed con datos de Firestore.
- Cerrar sesión funciona.
- Eliminar cuenta borra doc + Firebase Auth user.
- Sembrar Firestore con 2-3 reportes de prueba que reproduzcan los mockups.

**Definition of done:** la app funciona con Firebase en dispositivo físico, replicando el flujo de los 8 mockups con datos reales.

---

## Resumen del orden

| Fase | Entregable | Capa principal | Firebase? |
|---|---|---|---|
| 0 | Proyecto compilable + 3 paquetes raíz | — | No |
| 1 | **Dominio: modelos + puertos in/out + use cases** | `domain/` | No |
| 2 | Tema M3 + strings + assets | `app/theme` | No |
| 3 | **Adapters Fake + wiring Hilt** | `data/fake` + `app/di` | No |
| 4 | Navegación esqueleto | `app/navigation` | No |
| 5 | Componentes reutilizables (PetCard, TopBar, Drawer) | `app/ui/components` | No |
| 6 | Splash (mockup #1) | `app/ui/screens/splash` | No |
| 7 | Profile (mockup #2) | `app/ui/screens/profile` | No |
| 8 | OTP (mockup #5) | `app/ui/screens/otp` | No |
| 9 | Permissions (mockup #4) | `app/ui/screens/permissions` | No |
| 10 | Settings (mockup #3) | `app/ui/screens/settings` | No |
| 11 | Feed (mockups #6, #7, #8) | `app/ui/screens/feed` | No |
| 12 | DataStore para persistir sesión local | `data/datastore` | No |
| 13 | Drawer + navegación final end-to-end | `app/navigation` | No |
| 14 | **Firebase real (Auth + Firestore + Storage)** | `data/firebase` + 1 cambio en `app/di` | **Sí — última fase** |

---

## Notas operativas para Claude Code

- **Leer `CLAUDE.md` antes de cada fase.**
- Trabajar en una **rama por fase** (`feat/fase-1-domain`, `feat/fase-3-fakes`, etc.).
- Cada fase termina con un commit limpio y la app compilando.
- Si una fase requiere romper una regla de `CLAUDE.md` (especialmente §4.2 dependencias hexagonales), **detenerse y consultar** antes de continuar.
- **Verificación clave después de cada fase:** ningún archivo en `domain/` importa de `androidx.*`, `com.google.firebase.*`, ni `com.mascotasperdidas.app.data.*` ni `com.mascotasperdidas.app.app.*`. Si esto se rompe, la arquitectura está comprometida.
