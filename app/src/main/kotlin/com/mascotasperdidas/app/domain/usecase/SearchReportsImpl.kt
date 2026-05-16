package com.mascotasperdidas.app.domain.usecase

import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.domain.port.`in`.SearchReports
import com.mascotasperdidas.app.domain.port.out.PetReportRepository
import javax.inject.Inject

class SearchReportsImpl @Inject constructor(
    private val petReportRepository: PetReportRepository,
) : SearchReports {
    override suspend fun invoke(query: String, type: ReportType): List<PetReport> =
        petReportRepository.searchReports(query, type)
}
