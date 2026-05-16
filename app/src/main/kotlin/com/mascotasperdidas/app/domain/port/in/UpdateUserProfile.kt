package com.mascotasperdidas.app.domain.port.`in`

fun interface UpdateUserProfile {
    suspend operator fun invoke(name: String, phone: String)
}
