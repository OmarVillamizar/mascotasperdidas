# CLAUDE.md — MascotasPerdidas

> Este archivo contiene los **requerimientos explícitos e inviolables** del proyecto. Cualquier instancia de Claude Code que trabaje en este repositorio **DEBE** leer y respetar este documento antes de generar o modificar código.

---

## 1. Identidad del proyecto

- **Nombre de la app:** MascotasPerdidas
- **Propósito:** Aplicación móvil Android que permite a los usuarios reportar **mascotas perdidas** y **avistamientos** de mascotas, visualizándolos en un feed de cards.
- **Scope actual (MVP):** Login, gestión de perfil, configuración, permisos, verificación OTP de teléfono, y feed de publicaciones (perdidas / avistadas).
- **Scope futuro (NO incluido ahora):** Integración con mapas, geolocalización en tiempo real, chat directo entre usuarios. **No implementar nada de esto en esta fase.**

---

## 2. Stack tecnológico OBLIGATORIO

Estos requerimientos son **exclusivos y no negociables**.

| Componente | Requerimiento |
|---|---|
| **IDE** | Android Studio Panda 2 \| 2025.3.2 |
| **Lenguaje** | **Kotlin** exclusivamente (sin Java) |
| **UI Toolkit** | **Jetpack Compose** con **Material Design 3** EXCLUSIVAMENTE (https://m3.material.io/) |
| **Arquitectura** | **Hexagonal (Ports & Adapters)** — ver §4 |
| **Autenticación** | **Firebase Authentication** (Google Sign-In + Phone Auth para OTP) |
| **Base de datos** | **Cloud Firestore** (Firebase) |
| **Almacenamiento de imágenes** | **Firebase Storage** |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 34 o superior |
| **Compile SDK** | 34 o superior |
| **JDK** | 17 |

### 2.1 Librerías permitidas

- `androidx.compose.material3:material3` — **única librería de componentes UI permitida**
- `androidx.compose.material:material-icons-extended`
- `androidx.navigation:navigation-compose`
- `androidx.lifecycle:lifecycle-viewmodel-compose`
- `com.google.firebase:firebase-bom` (BOM, versión estable)
- `com.google.firebase:firebase-auth-ktx`
- `com.google.firebase:firebase-firestore-ktx`
- `com.google.firebase:firebase-storage-ktx`
- `com.google.android.gms:play-services-auth`
- `io.coil-kt:coil-compose`
- `com.google.dagger:hilt-android` + `androidx.hilt:hilt-navigation-compose`
- `androidx.datastore:datastore-preferences`
- `org.jetbrains.kotlinx:kotlinx-coroutines-android`
- `org.jetbrains.kotlinx:kotlinx-coroutines-play-services`

### 2.2 Librerías PROHIBIDAS

- ❌ Librerías de UI distintas a Material 3.
- ❌ XML Layouts para nuevas pantallas.
- ❌ RxJava (usar Coroutines + Flow).
- ❌ Retrofit / OkHttp directo (todo a través de Firebase SDK).
- ❌ Glide / Picasso (usar Coil).

---

## 3. Reglas de Material Design 3

**Toda pantalla debe construirse con componentes oficiales de M3.**

### 3.1 Componentes M3 que se usarán

- `Scaffold` con `CenterAlignedTopAppBar`.
- `ModalNavigationDrawer`.
- `ElevatedCard`.
- `FilterChip` y `AssistChip`.
- `Button`, `FilledTonalButton`, `OutlinedButton`, `TextButton`.
- `OutlinedTextField`.
- `SearchBar` de M3.
- `TabRow` con `Tab`.
- `Checkbox`.
- `Icon` con `Icons.Outlined` / `Icons.Filled`.
- `AlertDialog`.
- `Snackbar`.

### 3.2 Theming

- `MaterialTheme` en `app/theme/Theme.kt`.
- Color seed: violeta acorde a mockups.
- `dynamicColor = false`.
- Tema claro prioritario, oscuro placeholder.
- Tipografía: `Typography` por defecto de M3.

### 3.3 Tokens de color

| Token | Uso | Hex aprox. |
|---|---|---|
| `primary` | Botones principales, tabs activas | `#6750A4` |
| `primaryContainer` | Fondos suaves | `#EADDFF` |
| `surface` / fondo app | Lila muy claro | `#F3E8F7` |
| `error` / chip PERDIDO | Rojo coral | `#E57373` |
| `tertiary` / chip HALLAZGO / botón CONTACTAR | Verde | `#2E7D32` |
| `secondary` / chip RECIENTE | Amarillo suave | `#F6E27A` |

> Definir en `app/theme/Color.kt`. **Nunca hardcodear colores**; siempre `MaterialTheme.colorScheme.*`.

---

## 4. Arquitectura — HEXAGONAL (Ports & Adapters)

**Esta es la decisión arquitectónica más importante del proyecto y es inviolable.**

### 4.1 Principio fundamental

El **dominio** es el centro. **No conoce nada del exterior** — ni Android, ni Compose, ni Firebase, ni Hilt. Es Kotlin puro.

El dominio define **puertos**:
- **Puertos de entrada (driving / inbound):** lo que el dominio **ofrece** al exterior → casos de uso (`use cases`).
- **Puertos de salida (driven / outbound):** lo que el dominio **necesita** del exterior → interfaces de repositorios.

Los **adapters** implementan los puertos:
- **Adapters de entrada (driving adapters):** UI (Compose) + ViewModels que invocan los casos de uso.
- **Adapters de salida (driven adapters):** Firebase, DataStore, Fakes — implementan las interfaces de repositorio definidas en `domain/`.

### 4.2 Regla de dependencias (CRÍTICA)

```
   ┌────────────────────────────────────────────────┐
   │                    domain                      │
   │   (modelos puros, puertos in/out, use cases)   │
   │            ⚠ NO importa de nadie ⚠             │
   └────────────────────────────────────────────────┘
           ▲                                  ▲
           │ implementa                       │ usa
           │ (puertos out)                    │ (puertos in)
   ┌───────┴────────┐                  ┌──────┴────────┐
   │      data      │                  │      app      │
   │ (adapters out: │                  │ (adapters in: │
   │  Firebase,     │                  │  Compose UI,  │
   │  Fake, Cache)  │                  │  ViewModels,  │
   └────────────────┘                  │  Navigation)  │
                                       └───────────────┘
```

**Reglas duras:**

1. `domain/` **NO** puede importar de `data/`, ni de `app/`, ni de `androidx.*`, ni de `com.google.firebase.*`, ni de `dagger.*`. **Solo Kotlin stdlib + kotlinx-coroutines-core + javax.inject (JSR-330).**
2. `data/` puede importar de `domain/`. **No** puede importar de `app/`.
3. `app/` puede importar de `domain/`. **No** debe importar directamente de `data/`, excepto en `app/di/` que es donde se hace el wiring.
4. Los modelos del dominio **no son** DTOs con `@PropertyName` ni anotaciones de Firestore. Son tipos puros. Hay **mappers** en `data/mapper/` que convierten DTOs ↔ modelos de dominio.

### 4.3 Capas

#### `domain/` — el hexágono

- **`domain/model/`** — entidades puras: `User`, `PetReport`, `ReportType`, `NotificationPrefs`.
- **`domain/port/in/`** — interfaces de **casos de uso** (driving ports).
- **`domain/port/out/`** — interfaces de **repositorios** (driven ports).
- **`domain/usecase/`** — implementaciones de los casos de uso. Reciben puertos out por constructor.

#### `data/` — adapters de salida

- **`data/firebase/`** — implementaciones Firebase.
- **`data/fake/`** — implementaciones Fake/in-memory.
- **`data/datastore/`** — implementaciones DataStore.
- **`data/dto/`** — DTOs específicos de Firestore.
- **`data/mapper/`** — funciones `toDomain()` / `toDto()`.

#### `app/` — adapters de entrada (Android)

- **`app/MainActivity.kt`** + **`app/MascotasPerdidasApp.kt`**.
- **`app/theme/`** — `Color.kt`, `Type.kt`, `Theme.kt`.
- **`app/navigation/`** — `AppNavHost.kt`, `Routes.kt`.
- **`app/ui/components/`** — composables reutilizables.
- **`app/ui/screens/<feature>/`** — `<Feature>Screen.kt` + `<Feature>ViewModel.kt` + `<Feature>UiState.kt` + `<Feature>UiEvent.kt`.
- **`app/di/`** — módulos Hilt.

### 4.4 Estructura concreta de paquetes

```
com.mascotasperdidas.app/
│
├── domain/                                     ← HEXÁGONO (Kotlin puro)
│   ├── model/
│   │   ├── User.kt
│   │   ├── PetReport.kt
│   │   ├── ReportType.kt
│   │   └── NotificationPrefs.kt
│   ├── port/
│   │   ├── in/                                 ← driving ports
│   │   │   ├── SignInWithGoogle.kt
│   │   │   ├── VerifyPhoneOtp.kt
│   │   │   ├── RequestPhoneOtp.kt
│   │   │   ├── ObserveCurrentUser.kt
│   │   │   ├── UpdateUserProfile.kt
│   │   │   ├── UpdateNotificationPrefs.kt
│   │   │   ├── SignOut.kt
│   │   │   ├── DeleteAccount.kt
│   │   │   ├── ObserveReports.kt
│   │   │   ├── CreateReport.kt
│   │   │   └── SearchReports.kt
│   │   └── out/                                ← driven ports
│   │       ├── AuthRepository.kt
│   │       ├── UserRepository.kt
│   │       └── PetReportRepository.kt
│   └── usecase/                                ← impls de puertos in
│       ├── SignInWithGoogleImpl.kt
│       ├── VerifyPhoneOtpImpl.kt
│       ├── RequestPhoneOtpImpl.kt
│       ├── ObserveCurrentUserImpl.kt
│       ├── UpdateUserProfileImpl.kt
│       ├── UpdateNotificationPrefsImpl.kt
│       ├── SignOutImpl.kt
│       ├── DeleteAccountImpl.kt
│       ├── ObserveReportsImpl.kt
│       ├── CreateReportImpl.kt
│       └── SearchReportsImpl.kt
│
├── data/                                       ← adapters out
│   ├── dto/
│   │   ├── UserDto.kt
│   │   └── PetReportDto.kt
│   ├── mapper/
│   │   ├── UserMapper.kt
│   │   └── PetReportMapper.kt
│   ├── firebase/
│   │   ├── FirebaseAuthRepository.kt           ← implementa AuthRepository
│   │   ├── FirestoreUserRepository.kt          ← implementa UserRepository
│   │   └── FirestorePetReportRepository.kt     ← implementa PetReportRepository
│   ├── fake/
│   │   ├── FakeAuthRepository.kt
│   │   ├── FakeUserRepository.kt
│   │   └── FakePetReportRepository.kt
│   └── datastore/
│       └── PrefsDataStore.kt
│
└── app/                                        ← adapter in (Android)
    ├── MascotasPerdidasApp.kt                  ← @HiltAndroidApp
    ├── MainActivity.kt
    ├── theme/
    │   ├── Color.kt
    │   ├── Type.kt
    │   └── Theme.kt
    ├── navigation/
    │   ├── AppNavHost.kt
    │   └── Routes.kt
    ├── ui/
    │   ├── components/
    │   │   ├── PetCard.kt
    │   │   ├── AppTopBar.kt
    │   │   ├── AppDrawer.kt
    │   │   ├── UserAvatar.kt
    │   │   └── StatusChip.kt
    │   └── screens/
    │       ├── splash/
    │       │   ├── SplashScreen.kt
    │       │   ├── SplashViewModel.kt
    │       │   ├── SplashUiState.kt
    │       │   └── SplashUiEvent.kt
    │       ├── profile/
    │       │   ├── ProfileScreen.kt
    │       │   ├── ProfileViewModel.kt
    │       │   ├── ProfileUiState.kt
    │       │   └── ProfileUiEvent.kt
    │       ├── otp/
    │       │   ├── OtpScreen.kt
    │       │   ├── OtpViewModel.kt
    │       │   ├── OtpUiState.kt
    │       │   └── OtpUiEvent.kt
    │       ├── permissions/
    │       │   ├── PermissionsScreen.kt
    │       │   └── PermissionsViewModel.kt
    │       ├── settings/
    │       │   ├── SettingsScreen.kt
    │       │   ├── SettingsViewModel.kt
    │       │   ├── SettingsUiState.kt
    │       │   └── SettingsUiEvent.kt
    │       └── feed/
    │           ├── FeedScreen.kt
    │           ├── FeedViewModel.kt
    │           ├── FeedUiState.kt
    │           └── FeedUiEvent.kt
    ├── util/
    │   └── PermissionUtils.kt
    └── di/                                     ← wiring Hilt
        ├── RepositoryModule.kt                 ← bindea puertos out → adapters
        └── UseCaseModule.kt                    ← bindea puertos in → impls
```

### 4.5 Ejemplos de código

**Puerto out (`domain/port/out/UserRepository.kt`):**
```kotlin
package com.mascotasperdidas.app.domain.port.out

import com.mascotasperdidas.app.domain.model.NotificationPrefs
import com.mascotasperdidas.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeCurrentUser(): Flow<User?>
    suspend fun updateProfile(name: String, phone: String)
    suspend fun updateNotificationPrefs(prefs: NotificationPrefs)
    suspend fun deleteAccount()
}
```

**Puerto in (`domain/port/in/ObserveCurrentUser.kt`):**
```kotlin
package com.mascotasperdidas.app.domain.port.`in`

import com.mascotasperdidas.app.domain.model.User
import kotlinx.coroutines.flow.Flow

fun interface ObserveCurrentUser {
    operator fun invoke(): Flow<User?>
}
```

**Use case (`domain/usecase/ObserveCurrentUserImpl.kt`):**
```kotlin
package com.mascotasperdidas.app.domain.usecase

import com.mascotasperdidas.app.domain.model.User
import com.mascotasperdidas.app.domain.port.`in`.ObserveCurrentUser
import com.mascotasperdidas.app.domain.port.out.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentUserImpl @Inject constructor(
    private val userRepository: UserRepository
) : ObserveCurrentUser {
    override fun invoke(): Flow<User?> = userRepository.observeCurrentUser()
}
```

> `javax.inject.Inject` se usa porque es estándar JSR-330 y Kotlin/Hilt lo entienden sin acoplar al dominio con Android.

**Adapter out (`data/firebase/FirestoreUserRepository.kt`):**
```kotlin
package com.mascotasperdidas.app.data.firebase

import com.mascotasperdidas.app.data.mapper.toDomain
import com.mascotasperdidas.app.domain.model.User
import com.mascotasperdidas.app.domain.port.out.UserRepository
// implementación con Firestore
```

**Adapter in (`app/ui/screens/feed/FeedViewModel.kt`):**
```kotlin
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val observeReports: ObserveReports,        // puerto in
    private val searchReports: SearchReports           // puerto in
) : ViewModel() {
    // El ViewModel SOLO conoce puertos in, nunca repositorios.
}
```

**Wiring Hilt (`app/di/RepositoryModule.kt`):**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindUserRepository(
        impl: FirestoreUserRepository    // o FakeUserRepository en fases tempranas
    ): UserRepository
}
```

### 4.6 Reglas prácticas para Claude Code

- **Los ViewModels solo dependen de puertos in (use cases).** Nunca de repositorios directamente.
- **Los use cases solo dependen de puertos out.** Nunca de Firebase ni DataStore directamente.
- **Los modelos en `domain/model/` son inmutables, sin anotaciones de frameworks.** `Timestamp` de Firestore se convierte a `Long` epoch en el mapper.
- **Cambiar de Fake a Firebase = cambiar el binding en `RepositoryModule`.** Si para cambiar la fuente de datos hay que tocar un ViewModel, la arquitectura está mal.

---

## 5. Modelo de datos

### 5.1 Modelos de dominio (`domain/model/`)

```kotlin
data class User(
    val uid: String,
    val displayName: String,
    val email: String,
    val phoneNumber: String,            // E.164
    val phoneVerified: Boolean,
    val photoUrl: String?,
    val notificationPrefs: NotificationPrefs,
    val createdAtEpochMs: Long
)

data class NotificationPrefs(
    val lostPetsNearby: Boolean = false,
    val foundPetsNearby: Boolean = true,
    val sightingsOnMyReports: Boolean = true
)

enum class ReportType { LOST, FOUND }

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
```

### 5.2 DTOs Firestore (`data/dto/`)

DTOs con campos `Timestamp` y constructor sin argumentos. Se mapean a dominio en `data/mapper/`.

---

## 6. Flujo de autenticación

El login con Firebase se implementa **AL FINAL**. Hasta entonces, `RepositoryModule` bindea los puertos out a las implementaciones **Fake** en `data/fake/`.

Orden de pantallas:

1. **Splash** → si no hay sesión → Login con Google.
2. **Login Google** → si primera vez → **Perfil**.
3. **Perfil** ingresa teléfono → **OTP**.
4. **OTP verificado** → **Permisos**.
5. **Permisos concedidos** → **Feed**.
6. Desde drawer: Configuración, Perfil, Permisos, Cerrar sesión.

---

## 7. Reglas de codificación

- **Identificadores en inglés**, **strings de UI en español** (`strings.xml`).
- **Sin strings hardcodeados** en composables: `stringResource(R.string.xxx)`.
- **Sin colores hardcodeados**: siempre `MaterialTheme.colorScheme.*`.
- **Composables stateless**: cada screen = un Composable raíz que recibe `state: UiState` + `onEvent: (UiEvent) -> Unit`. Un wrapper inyecta el ViewModel.
- **`UiState` + `UiEvent` por pantalla** (data class / sealed class).
- **Manejo de errores:** todo flujo asíncrono captura excepciones y emite estado de error visible al usuario.
- **Logs:** nunca imprimir tokens, UIDs ni datos personales.

---

## 8. Calidad

- **ktlint** + **detekt** configurados desde el inicio.
- **Tests unitarios** mínimos para use cases (JUnit + Turbine + MockK). Los use cases son trivialmente testeables porque solo dependen de interfaces.
- **Compose Preview** para cada screen.
- **Test de arquitectura** recomendado: usar **Konsist** para verificar que `domain/` no importa de `data/` ni de `app/`.

---

## 9. Seguridad y privacidad

- **Nunca** comitear `google-services.json`. Agregar a `.gitignore`.
- Firestore Rules: feed con lectura pública; escritura solo por dueño autenticado.
- Storage Rules: subida solo autenticada, lectura pública para imágenes.
- "Eliminar cuenta" borra doc `users/{uid}` y la cuenta en Firebase Auth.

---

## 10. Fidelidad a los mockups

- TopAppBar: hamburguesa izq, título centrado, avatar circular der.
- Fondo: lila claro (`surface`).
- Cards blancos, esquinas ~20 dp, elevación suave.
- Chips pill: rojo PERDIDO, verde HALLAZGO, amarillo RECIENTE, gris claro raza.
- "CONTACTAR" verde, "Mas Información" gris claro.
- "Eliminar cuenta" en `error`.
- Botón Google blanco con borde, ícono G.
- Avatar con inicial cuando no hay foto.
- Tabs Perdidas/Avistadas con indicador inferior morado.
- Banner amarillo "Filtrando por: Hallazgos" cuando hay filtro activo.

---

## 11. Reglas para Claude Code (futuras sesiones)

1. **Leer este archivo antes de cualquier cambio.**
2. **Seguir `roadmap.md`** paso a paso. No saltar fases.
3. **No introducir librerías nuevas** sin actualizar este documento.
4. **No usar componentes que no sean Material 3.**
5. **No violar la regla de dependencias hexagonal (§4.2).** Si una clase en `domain/` necesita importar de Android o Firebase, está mal modelada — detenerse y reconsiderar.
6. **Los ViewModels NO conocen repositorios. Solo casos de uso (puertos in).**
7. **Los casos de uso NO conocen Firebase. Solo repositorios (puertos out).**
8. **El login real con Firebase es el ÚLTIMO paso del roadmap.** Antes, usar Fakes en `data/fake/`.
9. **Si un mockup es ambiguo, preguntar.** No inventar funcionalidad fuera del scope.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
