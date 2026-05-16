package com.mascotasperdidas.app.domain.port.`in`

fun interface VerifyPhoneOtp {
    suspend operator fun invoke(verificationId: String, code: String)
}
