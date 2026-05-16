package com.mascotasperdidas.app.domain.usecase

import com.mascotasperdidas.app.domain.port.`in`.DeleteReport
import com.mascotasperdidas.app.domain.port.out.PetReportRepository
import javax.inject.Inject

class DeleteReportImpl @Inject constructor(
    private val petReportRepository: PetReportRepository,
) : DeleteReport {
    override suspend fun invoke(id: String) = petReportRepository.deleteReport(id)
}
