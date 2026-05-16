package com.mascotasperdidas.app.domain.port.`in`

fun interface DeleteReport {
    suspend operator fun invoke(id: String)
}
