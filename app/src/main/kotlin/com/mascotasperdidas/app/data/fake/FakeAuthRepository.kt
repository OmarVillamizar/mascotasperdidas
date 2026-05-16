package com.mascotasperdidas.app.data.fake

import com.mascotasperdidas.app.domain.model.AuthState
import com.mascotasperdidas.app.domain.port.out.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.SignedOut)
    private var pendingVerificationId: String? = null

    override fun observeAuthState(): Flow<AuthState> = _authState.asStateFlow()

    override suspend fun signInWithGoogleIdToken(idToken: String) {
        // Simular latencia de red Firebase
        delay(600L)
        _authState.value = AuthState.SignedIn(
            uid = "fake-uid-001",
            phoneVerified = false,
        )
    }

    override suspend fun requestPhoneOtp(phone: String): String {
        delay(300L)
        pendingVerificationId = "fake-verification-id-${System.currentTimeMillis()}"
        return pendingVerificationId!!
    }

    override suspend fun verifyPhoneOtp(verificationId: String, code: String) {
        delay(300L)
        val current = _authState.value
        if (current is AuthState.SignedIn) {
            // Acepta cualquier código de 6 dígitos
            if (code.length == 6 && code.all { it.isDigit() }) {
                _authState.value = current.copy(phoneVerified = true)
            } else {
                throw IllegalStateException("Código OTP inválido (usa 6 dígitos)")
            }
        }
    }

    override suspend fun signOut() {
        delay(200L)
        _authState.value = AuthState.SignedOut
        pendingVerificationId = null
    }

    override suspend fun deleteCurrentUser() {
        delay(300L)
        _authState.value = AuthState.SignedOut
        pendingVerificationId = null
    }
}
