package com.mascotasperdidas.app.domain.usecase

import com.mascotasperdidas.app.domain.port.`in`.RequestPhoneOtp
import com.mascotasperdidas.app.domain.port.out.AuthRepository
import javax.inject.Inject

class RequestPhoneOtpImpl @Inject constructor(
    private val authRepository: AuthRepository,
) : RequestPhoneOtp {
    override suspend fun invoke(phone: String): String = authRepository.requestPhoneOtp(phone)
}
