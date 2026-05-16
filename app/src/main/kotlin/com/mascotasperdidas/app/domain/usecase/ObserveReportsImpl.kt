package com.mascotasperdidas.app.domain.usecase

import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.domain.port.`in`.ObserveReports
import com.mascotasperdidas.app.domain.port.out.PetReportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveReportsImpl @Inject constructor(
    private val petReportRepository: PetReportRepository,
) : ObserveReports {
    override fun invoke(type: ReportType): Flow<List<PetReport>> =
        petReportRepository.observeReports(type)
}
