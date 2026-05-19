package com.mascotasperdidas.app.domain.port.out

import com.mascotasperdidas.app.domain.model.AuthState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>
    suspend fun signInWithGoogleIdToken(idToken: String)
    suspend fun requestPhoneOtp(phone: String): String
    suspend fun verifyPhoneOtp(verificationId: String, code: String)
    suspend fun signOut()
    suspend fun deleteCurrentUser()
}
