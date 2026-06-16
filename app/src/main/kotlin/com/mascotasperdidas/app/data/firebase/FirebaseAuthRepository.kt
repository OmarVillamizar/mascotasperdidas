package com.mascotasperdidas.app.data.firebase

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mascotasperdidas.app.domain.model.AuthState
import com.mascotasperdidas.app.domain.port.out.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context,
    private val activityProvider: ActivityProvider,
) : AuthRepository {

    override fun observeAuthState(): Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                trySend(AuthState.SignedIn(user.uid, user.phoneNumber != null))
            } else {
                trySend(AuthState.SignedOut)
            }
        }
        auth.addAuthStateListener(listener)
        listener.onAuthStateChanged(auth)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    override suspend fun requestPhoneOtp(phone: String): String {
        return suspendCancellableCoroutine { continuation ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume("auto-verified-${System.currentTimeMillis()}")
                        } else {
                            continuation.resumeWithException(
                                task.exception ?: Exception("Auto-verification failed")
                            )
                        }
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    continuation.resumeWithException(e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken,
                ) {
                    continuation.resume(verificationId)
                }
            }

            val activity = activityProvider.current
            if (activity == null) {
                continuation.resumeWithException(
                    IllegalStateException("No hay una actividad activa para la verificación telefónica"),
                )
                return@suspendCancellableCoroutine
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    override suspend fun verifyPhoneOtp(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("No hay usuario autenticado para vincular teléfono")
        currentUser.linkWithCredential(credential).await()
    }

    override suspend fun signOut() {
        // Cerrar sesión de Google para evitar auto-login al siguiente inicio
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(context, gso).signOut().await()
        } catch (_: Exception) {
            // ignora si falla — Firebase signOut es lo crítico
        }
        auth.signOut()
    }

    override suspend fun deleteCurrentUser() {
        val user = auth.currentUser ?: return
        user.delete().await()
    }
}
