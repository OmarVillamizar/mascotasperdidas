package com.mascotasperdidas.app.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mascotasperdidas.app.data.dto.UserDto
import com.mascotasperdidas.app.data.mapper.toDomain
import com.mascotasperdidas.app.data.mapper.toDto
import com.mascotasperdidas.app.domain.model.NotificationPrefs
import com.mascotasperdidas.app.domain.model.User
import com.mascotasperdidas.app.domain.port.out.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : UserRepository {

    private val currentUid: String?
        get() = auth.currentUser?.uid

    override fun observeCurrentUser(): Flow<User?> = callbackFlow {
        var firestoreListener: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            // Limpiar listener anterior de Firestore si cambió el usuario
            firestoreListener?.remove()

            val user = firebaseAuth.currentUser
            if (user == null) {
                trySend(null)
                return@AuthStateListener
            }

            val docRef = firestore.collection("users").document(user.uid)

            // Auto-crear documento en Firestore la primera vez que el usuario hace login
            launch {
                val snapshot = docRef.get().await()
                if (!snapshot.exists()) {
                    val dto = UserDto(
                        uid = user.uid,
                        displayName = user.displayName ?: "",
                        email = user.email ?: "",
                        phoneNumber = user.phoneNumber ?: "",
                        phoneVerified = true, // sin OTP en MVP
                        photoUrl = user.photoUrl?.toString(),
                        createdAt = Timestamp.now(),
                    )
                    docRef.set(dto).await()
                }
            }

            // Escuchar cambios en el documento del usuario
            firestoreListener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val dto = snapshot.toObject(UserDto::class.java)
                if (dto != null) {
                    trySend(dto.toDomain())
                } else {
                    trySend(null)
                }
            }
        }

        auth.addAuthStateListener(authListener)
        // Emitir estado inicial
        authListener.onAuthStateChanged(auth)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            firestoreListener?.remove()
        }
    }

    override suspend fun upsertUser(user: User) {
        currentUid?.let { uid ->
            firestore.collection("users").document(uid).set(user.toDto()).await()
        }
    }

    override suspend fun updateProfile(name: String, phone: String) {
        currentUid?.let { uid ->
            val updates = mapOf<String, Any>(
                "displayName" to name,
                "phoneNumber" to phone,
                "phoneVerified" to (phone.isNotBlank()),
            )
            firestore.collection("users").document(uid).update(updates).await()
        }
    }

    override suspend fun updateNotificationPrefs(prefs: NotificationPrefs) {
        currentUid?.let { uid ->
            val updates = mapOf<String, Any>(
                "notificationPrefs.lostPetsNearby" to prefs.lostPetsNearby,
                "notificationPrefs.foundPetsNearby" to prefs.foundPetsNearby,
                "notificationPrefs.sightingsOnMyReports" to prefs.sightingsOnMyReports,
            )
            firestore.collection("users").document(uid).update(updates).await()
        }
    }

    override suspend fun deleteCurrentUserDocument() {
        currentUid?.let { uid ->
            firestore.collection("users").document(uid).delete().await()
        }
    }
}
