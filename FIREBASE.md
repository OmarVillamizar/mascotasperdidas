# FIREBASE.md — Guía de conexión Firebase para MascotasPerdidas

> **Última fase del roadmap (Fase 14).**  
> Todo el código está preparado con arquitectura hexagonal.  
> **Solo necesitas cambiar 3 líneas en `RepositoryModule.kt`** para pasar de Fakes a Firebase.  
> El dominio, los ViewModels y las pantallas **no se tocan**.

---

## 1. Crear proyecto en Firebase Console

1. Ve a https://console.firebase.google.com/
2. Pulsa **"Agregar proyecto"** (o selecciona uno existente)
3. Nombre sugerido: `MascotasPerdidas`
4. Habilita Google Analytics si quieres (opcional para MVP)
5. Pulsa **"Crear proyecto"**

---

## 2. Registrar la app Android en Firebase

1. En la consola de Firebase, entra al proyecto
2. Pulsa el ícono de **Android** (el robot verde) para añadir una app
3. Rellena:
   - **Nombre del paquete de Android:** `com.mascotasperdidas.app`
     > Lo encuentras en `app/build.gradle.kts` → `applicationId`
   - **Apodo de la app:** `MascotasPerdidas` (opcional)
   - **Certificado de firma SHA-1:** (obligatorio para Google Sign-In y Phone Auth)

### Cómo obtener el SHA-1 de depuración

En Android Studio:
- Abre la pestaña **Gradle** (derecha, ícono de elefante)
- Ve a `MascotasPerdidas > Tasks > android > signingReport`
- Doble clic en `signingReport`
- En la consola de **Run** (abajo), busca:
  ```
  Variant: debug
  SHA-1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
  ```
- Copia ese SHA-1 y pégalo en el campo de Firebase Console

4. Pulsa **"Registrar app"**

---

## 3. Descargar google-services.json

1. Firebase Console te dará un botón **"Descargar google-services.json"**
2. Descárgalo y **muévelo a** la carpeta:
   ```
   app/google-services.json
   ```
3. **NUNCA** comitees este archivo. Ya está en `.gitignore`.
4. Pulsa **"Siguiente"** en los pasos del asistente (no necesitas ejecutar el resto de instrucciones, Gradle ya está configurado)

---

## 4. Activar el plugin de Google Services

En `app/build.gradle.kts`, descomenta la siguiente línea:

```kotlin
// Cambia de:
// alias(libs.plugins.google.services)  // uncomment in Phase 14

// A:
alias(libs.plugins.google.services)
```

> **Nota:** El plugin ya está declarado en `gradle/libs.versions.toml` y en el `build.gradle.kts` raíz. Solo falta descomentarlo en el módulo `app`.

---

## 5. Activar servicios en Firebase Console

### 5.1 Authentication

1. En Firebase Console → menú izquierdo → **Authentication**
2. Pestaña **"Sign-in method"**
3. Habilita **Google**:
   - Pulsa el toggle para habilitarlo
   - Selecciona un correo de soporte del proyecto
   - Guarda
4. Habilita **Teléfono** (Phone):
   - Pulsa el toggle para habilitarlo
   - Guarda

### 5.2 Firestore Database

1. En Firebase Console → menú izquierdo → **Firestore Database**
2. Pulsa **"Crear base de datos"**
3. Selecciona el modo **"Producción"** (no "Prueba")
4. Elige una ubicación (la más cercana: `southamerica-east1` para Colombia)
5. Pulsa **"Habilitar"**

### 5.3 Storage

1. En Firebase Console → menú izquierdo → **Storage**
2. Pulsa **"Comenzar"**
3. Modo producción
4. Ubicación (misma que Firestore)
5. Pulsa **"Listo"**

---

## 6. Configurar las reglas de seguridad

### 6.1 Firestore Rules

En Firebase Console → Firestore → pestaña **"Rules"**, reemplaza con:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Solo el dueño lee/escribe su documento de usuario
    match /users/{uid} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }

    // Reportes: lectura pública, escritura solo autenticada y dueño
    match /pet_reports/{reportId} {
      allow read: if true;
      allow create: if request.auth != null
                     && request.resource.data.ownerUid == request.auth.uid;
      allow update, delete: if request.auth != null
                             && resource.data.ownerUid == request.auth.uid;
    }
  }
}
```

Pulsa **"Publicar"**.

### 6.2 Storage Rules

En Firebase Console → Storage → pestaña **"Rules"**, reemplaza con:

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

Pulsa **"Publicar"**.

---

## 7. Verificar dependencias en build.gradle.kts

Asegúrate de que estas líneas **no estén comentadas** en `app/build.gradle.kts`:

```kotlin
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.auth.ktx)
implementation(libs.firebase.firestore.ktx)
implementation(libs.firebase.storage.ktx)
implementation(libs.play.services.auth)
```

> Estas líneas ya están en el archivo pero comentadas. Descoméntalas todas.

---

## 8. Cambiar el binding de Hilt a Firebase (LA ÚNICA LÍNEA DE CÓDIGO QUE CAMBIA)

En `app/src/main/kotlin/com/mascotasperdidas/app/app/di/RepositoryModule.kt`:

```kotlin
// ANTES (Fases 3-13: Fakes)
@Binds @Singleton abstract fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository
@Binds @Singleton abstract fun bindUserRepository(impl: FakeUserRepository): UserRepository
@Binds @Singleton abstract fun bindPetReportRepository(impl: FakePetReportRepository): PetReportRepository

// DESPUÉS (Fase 14: Firebase)
@Binds @Singleton abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository
@Binds @Singleton abstract fun bindUserRepository(impl: FirestoreUserRepository): UserRepository
@Binds @Singleton abstract fun bindPetReportRepository(impl: FirestorePetReportRepository): PetReportRepository
```

**Eso es todo.** Nada más cambia en el código.

---

## 9. Implementar los adapters Firebase

Los siguientes archivos deben crearse en `data/firebase/` (están planificados en el roadmap Fase 14, aún no existen):

| Archivo | Implementa | Responsabilidad |
|---|---|---|
| `FirebaseAuthRepository.kt` | `AuthRepository` | Google Sign-In, Phone OTP, observar estado auth |
| `FirestoreUserRepository.kt` | `UserRepository` | CRUD de `users/{uid}` en Firestore |
| `FirestorePetReportRepository.kt` | `PetReportRepository` | CRUD de `pet_reports/{id}`, subida de imágenes a Storage |

También se necesitan:
- `data/dto/UserDto.kt` — DTO con anotaciones de Firestore
- `data/dto/PetReportDto.kt` — DTO con anotaciones de Firestore
- `data/mapper/UserMapper.kt` — `toDomain()` / `toDto()`
- `data/mapper/PetReportMapper.kt` — `toDomain()` / `toDto()`

> **Claude Code implementará estos archivos automáticamente cuando ejecutes la Fase 14.**  
> Solo necesitas tener `google-services.json` en su lugar y los servicios activados.

---

## 10. Verificación final

1. **Sync Gradle** en Android Studio (File → Sync Project with Gradle Files)
2. **Clean + Rebuild** (Build → Clean Project, luego Build → Rebuild Project)
3. Ejecuta la app en tu dispositivo
4. Flujo esperado:
   - Splash → Login con Google (usando tu cuenta real)
   - Si es primera vez → Profile (nombre + teléfono)
   - OTP real por SMS al número ingresado
   - Permisos → Feed con datos reales de Firestore
   - Cerrar sesión / Eliminar cuenta funcionales

---

## Resumen rápido

| Tarea | Dónde | Tiempo estimado |
|---|---|---|
| Crear proyecto Firebase | console.firebase.google.com | 3 min |
| Registrar app Android + SHA-1 | Firebase Console | 3 min |
| Bajar google-services.json | Firebase Console → carpeta `app/` | 1 min |
| Activar Auth (Google + Phone) | Firebase Console | 2 min |
| Crear Firestore + Storage | Firebase Console | 2 min |
| Copiar reglas de seguridad | Firebase Console | 2 min |
| Descomentar plugin y dependencias | `app/build.gradle.kts` | 1 min |
| Cambiar 3 líneas en RepositoryModule | `app/di/RepositoryModule.kt` | 1 min |
| **Total** | | **~15 min** |
