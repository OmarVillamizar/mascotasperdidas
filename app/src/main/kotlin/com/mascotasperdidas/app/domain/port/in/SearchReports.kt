package com.mascotasperdidas.app.domain.port.`in`

import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType

fun interface SearchReports {
    suspend operator fun invoke(query: String, type: ReportType): List<PetReport>
}
