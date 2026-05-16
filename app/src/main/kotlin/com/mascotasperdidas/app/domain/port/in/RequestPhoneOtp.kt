package com.mascotasperdidas.app.domain.port.`in`

fun interface RequestPhoneOtp {
    suspend operator fun invoke(phone: String): String
}
