package com.mascotasperdidas.app.domain.usecase

import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.port.`in`.CreateReport
import com.mascotasperdidas.app.domain.port.out.PetReportRepository
import javax.inject.Inject

class CreateReportImpl @Inject constructor(
    private val petReportRepository: PetReportRepository,
) : CreateReport {
    override suspend fun invoke(report: PetReport, imageBytesList: List<ByteArray>) =
        petReportRepository.createReport(report, imageBytesList)
}
