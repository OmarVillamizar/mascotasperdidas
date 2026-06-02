package com.mascotasperdidas.app.domain.port.out

import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import kotlinx.coroutines.flow.Flow

interface PetReportRepository {
    fun observeReports(type: ReportType): Flow<List<PetReport>>
    suspend fun createReport(report: PetReport, imageBytesList: List<ByteArray>)
    suspend fun deleteReport(id: String)
    suspend fun searchReports(query: String, type: ReportType): List<PetReport>
}
