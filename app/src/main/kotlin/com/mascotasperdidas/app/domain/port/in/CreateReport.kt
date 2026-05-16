package com.mascotasperdidas.app.domain.port.`in`

import com.mascotasperdidas.app.domain.model.PetReport

fun interface CreateReport {
    suspend operator fun invoke(report: PetReport, imageBytes: ByteArray?)
}
