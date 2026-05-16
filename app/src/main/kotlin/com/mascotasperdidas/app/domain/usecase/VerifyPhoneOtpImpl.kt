package com.mascotasperdidas.app.domain.usecase

import com.mascotasperdidas.app.domain.port.`in`.VerifyPhoneOtp
import com.mascotasperdidas.app.domain.port.out.AuthRepository
import javax.inject.Inject

class VerifyPhoneOtpImpl @Inject constructor(
    private val authRepository: AuthRepository,
) : VerifyPhoneOtp {
    override suspend fun invoke(verificationId: String, code: String) =
        authRepository.verifyPhoneOtp(verificationId, code)
}
